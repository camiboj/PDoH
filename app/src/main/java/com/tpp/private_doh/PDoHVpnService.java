package com.tpp.private_doh;


import static com.tpp.private_doh.config.Config.QUEUE_CAPACITY;
import static com.tpp.private_doh.config.Config.VPN_ADDRESS;
import static com.tpp.private_doh.config.Config.VPN_ROUTE;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.controller.PingController;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.handler.DnsDownWorker;
import com.tpp.private_doh.handler.TcpPacketHandler;
import com.tpp.private_doh.handler.UdpPacketHandler;
import com.tpp.private_doh.network.NetworkManager;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.util.ResourceUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDoHVpnService extends VpnService {
    private static final String TAG = PDoHVpnService.class.getSimpleName();
    private static PingController PING_CONTROLLER;
    private static Integer RACING_AMOUNT;
    private static ProtocolId PROTOCOL_ID;

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;
    private BlockingQueue<DnsPacket> dnsResponsesQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    static public void setRacingAmount(int n) {
        // must be call only once and before creating any instance of PDoHVpnService
        RACING_AMOUNT = n;
    }

    static public void setPingController(PingController pingController) {
        // must be call only once and before creating any instance of PDoHVpnService
        PING_CONTROLLER = pingController;
    }

    public static void setProtocolId(ProtocolId n) {
        // must be call only once and before creating any instance of PDoHVpnService
        PROTOCOL_ID = n;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupVPN();
        deviceToNetworkUDPQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        deviceToNetworkTCPQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        dnsResponsesQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        networkToDeviceQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        executorService = Executors.newFixedThreadPool(4);
        executorService.submit(new UdpPacketHandler(deviceToNetworkUDPQueue, networkToDeviceQueue, this));
        executorService.submit(new TcpPacketHandler(deviceToNetworkTCPQueue, networkToDeviceQueue, this));
        executorService.submit(new DnsDownWorker(networkToDeviceQueue, dnsResponsesQueue));
        executorService.submit(new NetworkManager(vpnInterface.getFileDescriptor(),
                deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, dnsResponsesQueue, networkToDeviceQueue, RACING_AMOUNT, PING_CONTROLLER, PROTOCOL_ID));
    }

    private void setupVPN() {
        try {
            if (vpnInterface == null) {
                Builder builder = new Builder();
                builder.addAddress(VPN_ADDRESS, 32);
                builder.addRoute(VPN_ROUTE, 0);
                builder.addDnsServer(Config.DNS_PROVIDER);
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

