package com.mocyx.basic_client;


import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.mocyx.basic_client.config.Config;
import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.handler.DnsDownWorker;
import com.mocyx.basic_client.handler.TcpPacketHandler;
import com.mocyx.basic_client.handler.UdpPacketHandler;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.util.ResourceUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDoHVpnService extends VpnService {
    private static final String TAG = PDoHVpnService.class.getSimpleName();
    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;
    private BlockingQueue<DnsPacket> dnsResponsesQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        setupVPN();
        deviceToNetworkUDPQueue = new ArrayBlockingQueue<>(1000);
        deviceToNetworkTCPQueue = new ArrayBlockingQueue<>(1000);
        dnsResponsesQueue = new ArrayBlockingQueue<>(1000);
        networkToDeviceQueue = new ArrayBlockingQueue<>(1000);

        executorService = Executors.newFixedThreadPool(4);
        executorService.submit(new UdpPacketHandler(deviceToNetworkUDPQueue, networkToDeviceQueue, this));
        executorService.submit(new TcpPacketHandler(deviceToNetworkTCPQueue, networkToDeviceQueue, this));
        executorService.submit(new DnsDownWorker(networkToDeviceQueue, dnsResponsesQueue));
        executorService.submit(new NetworkManager(vpnInterface.getFileDescriptor(),
                deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, dnsResponsesQueue, networkToDeviceQueue));
    }

    private void setupVPN() {
        try {
            if (vpnInterface == null) {
                Builder builder = new Builder();
                builder.addAddress(VPN_ADDRESS, 32);
                builder.addRoute(VPN_ROUTE, 0);
                builder.addDnsServer(Config.dns);
                if (Config.testLocal) {
                    builder.addAllowedApplication("com.mocyx.basic_client");
                }
                vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(pendingIntent).establish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Fail to setup VPN", e);
            System.exit(0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
        cleanup();
        Log.i(TAG, "Stopped");
    }

    private void cleanup() {
        deviceToNetworkTCPQueue = null;
        deviceToNetworkUDPQueue = null;
        networkToDeviceQueue = null;
        ResourceUtils.closeResources(vpnInterface);
    }
}
