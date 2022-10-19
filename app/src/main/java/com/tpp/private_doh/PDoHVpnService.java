package com.tpp.private_doh;


import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.tpp.private_doh.config.Config;
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
    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static final Integer CAPACITY = 1000;

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
        deviceToNetworkUDPQueue = new ArrayBlockingQueue<>(CAPACITY);
        deviceToNetworkTCPQueue = new ArrayBlockingQueue<>(CAPACITY);
        dnsResponsesQueue = new ArrayBlockingQueue<>(CAPACITY);
        networkToDeviceQueue = new ArrayBlockingQueue<>(CAPACITY);

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
                    builder.addAllowedApplication("com.tpp.private_doh");
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

