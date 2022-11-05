package com.tpp.private_doh.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tpp.private_doh.PDoHVpnService;
import com.tpp.private_doh.R;
import com.tpp.private_doh.components.ProtocolSelector;
import com.tpp.private_doh.components.RacingAmountSelector;
import com.tpp.private_doh.components.StartVPNButton;
import com.tpp.private_doh.components.UnselectedProtocol;
import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.factory.ShardingControllerFactory;
import com.tpp.private_doh.network.InternetChecker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final int VPN_REQUEST_CODE = 0x0F;
    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    private final String TAG = this.getClass().getSimpleName();
    private final List<Integer> ACCEPTED_NETWORK_CAPABILITIES = Arrays.asList(
            NetworkCapabilities.TRANSPORT_CELLULAR, NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_ETHERNET);

    private ProtocolSelector protocolSelector;
    private RacingAmountSelector racingAmountSelector;
    private TextView countOutput;
    private ShardingControllerFactory shardingControllerFactory;

    private BroadcastReceiver stopVpnInternet = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Config.STOP_SIGNAL_FOR_INTERNET.equals(intent.getAction())) {
                stopVpnInternet();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent vpnIntent = VpnService.prepare(this);

        // Ask for permission - dont start VPN
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        }

        setContentView(R.layout.activity_main);

        countOutput = findViewById(R.id.resolversCountsText);

        protocolSelector = findViewById(R.id.protocolSelector);
        racingAmountSelector = findViewById(R.id.racingAmountSelector);

        protocolSelector.setOnCheckedChangeListener((group, checkedId) -> setSeekBarMax());
        racingAmountSelector.setCustomMin(Config.MIN_RACING_AMOUNT);
        setSeekBarMax();
        setButtonHandlers();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(stopVpnInternet, new IntentFilter(Config.STOP_SIGNAL_FOR_INTERNET));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setSeekBarMax() {
        try {
            int availableRequesterAmount = ShardingControllerFactory.getAvailableRequesterAmount(protocolSelector.getProtocol());
            racingAmountSelector.setCustomMax(availableRequesterAmount);
        } catch (UnselectedProtocol unselectedProtocol) {
            Log.e(TAG, Arrays.toString(unselectedProtocol.getStackTrace()));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data, ProtocolId protocol, int racingAmount) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            shardingControllerFactory = new ShardingControllerFactory(protocol, racingAmount);
            PDoHVpnService.setShardingControllerFactory(shardingControllerFactory);
            startService(new Intent(this, PDoHVpnService.class));
        }
    }

    private void enableVpnComponents(boolean enabled) {
        protocolSelector.setEnabled(enabled);
        racingAmountSelector.setEnabled(enabled);
    }

    private void setButtonHandlers() {
        StartVPNButton startVpnButton = findViewById(R.id.startVpn);
        startVpnButton.setOnClick(this::startVpn, this::stopVpn);
    }

    private boolean startVpn() {
        if (!checkInternet()) {
            // TODO: add toast to let the user know there is no internet available
            // TODO: test this when we are connected to roaming instead of wifi
            Log.e(TAG, "There is no internet");
            Toast toast = Toast.makeText(getApplicationContext(), "There is no internet\nTry again when you're connected to wifi", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        ProtocolId protocol = ProtocolId.DOH;
        try {
            protocol = protocolSelector.getProtocol();
        } catch (UnselectedProtocol unselectedProtocol) {
            Log.e(TAG, Arrays.toString(unselectedProtocol.getStackTrace()));
            Toast.makeText(MainActivity.this, "No protocol selected. Set default protocol: DoH", Toast.LENGTH_SHORT).show();
        }

        Intent vpnIntent = VpnService.prepare(this);

        if (vpnIntent == null) {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null, protocol, racingAmountSelector.getCustomProgress());
        }
        enableVpnComponents(false);

        InternetChecker internetChecker = new InternetChecker(this::checkVpnTraffic);
        Thread t = new Thread(internetChecker);
        t.start();
        return true;
    }

    private void stopVpnInternet() {
        // TODO: show the user we are stopping the vpn because of a connection issue
        Log.e(TAG, "We need to stop the vpn due to a connectivity issue");
        StartVPNButton startVpnButton = findViewById(R.id.startVpn);
        startVpnButton.closeVpn();
        stopVpn();
    }

    private void stopVpn() {
        Intent intent = new Intent(Config.STOP_SIGNAL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        enableVpnComponents(true);
        shardingControllerFactory = null;
    }

    public void fetchCount(View view) {
        String message = "No running VPN";
        if (shardingControllerFactory != null) {
            Map<String, Integer> map = shardingControllerFactory.getRequestersMetrics();
            // TODO: agregar padding lindo para que los contadores esten alineados
            message = map.keySet().stream()
                    .map(key -> map.get(key) + "\n " + key)
                    .collect(Collectors.joining("\n\n"));
        }
        countOutput.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVpn();
    }

    public void bugClicked(View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(Config.BUG_LINK));

        startActivity(httpIntent);
    }

    public boolean checkInternet() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] mWifi = connManager.getAllNetworks();
        return mWifi.length != 0;
    }

    public boolean checkVpnTraffic() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] mWifi = connManager.getAllNetworks();
        NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(connManager.getActiveNetwork());
        boolean stillConnectedToWifi = networkCapabilities != null
                && ACCEPTED_NETWORK_CAPABILITIES.stream().anyMatch(networkCapabilities::hasTransport);
        return stillConnectedToWifi || mWifi.length > 1; // This is because the vpn connection counts as one
    }
}