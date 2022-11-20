package com.tpp.private_doh;


import static com.tpp.private_doh.config.Config.QUEUE_CAPACITY;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tpp.private_doh.app.MainActivity;
import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.factory.ShardingControllerFactory;
import com.tpp.private_doh.handler.DnsDownWorker;
import com.tpp.private_doh.handler.TcpPacketHandler;
import com.tpp.private_doh.handler.UdpPacketHandler;
import com.tpp.private_doh.network.NetworkManager;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.util.ResourceUtils;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDoHVpnService extends VpnService {
    private static final String TAG = PDoHVpnService.class.getSimpleName();
    private static ExecutorService executorService;
    private ParcelFileDescriptor vpnInterface = null;
    private BroadcastReceiver stopVpn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Config.STOP_SIGNAL.equals(intent.getAction())) {
                stopVpn();
            }
        }
    };
    private PendingIntent pendingIntent;
    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;
    private BlockingQueue<DnsPacket> dnsResponsesQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService dnsWorkers;

    public static void setShardingControllerFactory(ShardingControllerFactory scd) {
        NetworkManager.setShardingControllerFactory(scd);
    }

    public static boolean isRunning() {
        return executorService != null && !executorService.isShutdown();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupVPN();
        deviceToNetworkUDPQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        deviceToNetworkTCPQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        dnsResponsesQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        networkToDeviceQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        executorService = Executors.newFixedThreadPool(Config.EXECUTOR_SERVICE_N);
        dnsWorkers = Executors.newFixedThreadPool(Config.N_DNS_WORKERS);

        FileDescriptor vpnFileDescriptor = vpnInterface.getFileDescriptor();
        executorService.submit(new UdpPacketHandler(deviceToNetworkUDPQueue, networkToDeviceQueue, this));
        executorService.submit(new TcpPacketHandler(deviceToNetworkTCPQueue, networkToDeviceQueue, this));
        executorService.submit(new DnsDownWorker(networkToDeviceQueue, dnsResponsesQueue));
        executorService.submit(new NetworkManager(vpnFileDescriptor, deviceToNetworkUDPQueue,
                deviceToNetworkTCPQueue, dnsResponsesQueue, networkToDeviceQueue, dnsWorkers));
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(stopVpn, new IntentFilter(Config.STOP_SIGNAL));

        showServiceNotification();
    }

    private void setupVPN() {
        try {
            if (vpnInterface == null) {
                Builder builder = new Builder();
                builder.addAddress(Config.VPN_ADDRESS, 32);
                builder.addRoute(Config.VPN_ROUTE, 0);
                builder.addDnsServer(Config.DNS_PROVIDER);
                vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(pendingIntent).establish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Fail to setup VPN", e);
            System.exit(0);
        }
    }

    public void stopVpn() {
        try {
            if (vpnInterface != null) {
                //vpnInterface.close();
                vpnInterface = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to stopVpn", e);
        }
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVpn();
        cleanup();
        Log.i(TAG, "Stopped");
    }

    private void cleanup() {
        dnsWorkers.shutdownNow();
        executorService.shutdownNow();
        ResourceUtils.closeResources(vpnInterface);
        vpnInterface = null;
    }

    private void showServiceNotification() {
        Intent activityNotificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingActivityIntent = PendingIntent.getActivity(this, 0, activityNotificationIntent, 0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(
                Config.NOTIFICATION_CHANNEL_ID,
                "VPN Status",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new NotificationCompat.Builder(this, Config.NOTIFICATION_CHANNEL_ID)
                .setContentIntent(pendingActivityIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(Config.NOTIFICATION_ID, notification);
    }

}

