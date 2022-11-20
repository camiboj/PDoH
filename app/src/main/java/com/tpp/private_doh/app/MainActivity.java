package com.tpp.private_doh.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tpp.private_doh.PDoHVpnService;
import com.tpp.private_doh.R;
import com.tpp.private_doh.components.DownBar;
import com.tpp.private_doh.components.MetricsScreen;
import com.tpp.private_doh.components.StartVPNButton;
import com.tpp.private_doh.components.UnselectedProtocol;
import com.tpp.private_doh.components.protocol_selector.ProtocolSelectorLayout;
import com.tpp.private_doh.components.protocol_selector.ProtocolSelectorRadioGroup;
import com.tpp.private_doh.components.racing_amount_selector.RacingAmountBar;
import com.tpp.private_doh.components.racing_amount_selector.RacingAmountSelectorLayout;
import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.factory.ShardingControllerFactory;
import com.tpp.private_doh.network.InternetChecker;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {
    private static final int VPN_REQUEST_CODE = 0x0F;
    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    private final String TAG = this.getClass().getSimpleName();
    private int actualTransport = -1;

    private ProtocolSelectorRadioGroup protocolSelector;
    private RacingAmountBar racingAmountBar;
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
        // countOutput = findViewById(R.id.resolversCountsText);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        ProtocolSelectorLayout protocolSelectorLayout = findViewById(R.id.protocolSelectorLayout);
        protocolSelector = protocolSelectorLayout.getProtocolSelectorRadioGroup();
        RacingAmountSelectorLayout racingAmountLayout = findViewById(R.id.racingAmountLayout);
        racingAmountBar = racingAmountLayout.getBar();
        protocolSelector.setOnCheckedChangeListener((group, checkedId) -> setSeekBarMax());
        racingAmountBar.setCustomMin(Config.MIN_RACING_AMOUNT);
        setSeekBarMax();
        setButtonHandlers();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(stopVpnInternet, new IntentFilter(Config.STOP_SIGNAL_FOR_INTERNET));
        DownBar db = findViewById(R.id.down_bar);
        db.setVpnScreen(findViewById(R.id.vpn_layout));
        db.setMetricsScreen(findViewById(R.id.metrics_layout));
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
            racingAmountBar.setCustomMax(availableRequesterAmount);
        } catch (UnselectedProtocol unselectedProtocol) {
            Log.e(TAG, Arrays.toString(unselectedProtocol.getStackTrace()));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Context context = getApplicationContext();
            Intent intent = new Intent(this, PDoHVpnService.class);
            //intent.setAction(Config.START_FOREGROUND_ACTION);
            context.startForegroundService(intent);
        }
    }

    private void setMetricsShardingController() {
        MetricsScreen ms = findViewById(R.id.metrics_layout);
        ms.setShardingControllerFactory(shardingControllerFactory);
    }

    private void enableVpnComponents(boolean enabled) {
        protocolSelector.setEnabled(enabled);
        racingAmountBar.setEnabled(enabled);
    }

    private void setButtonHandlers() {
        StartVPNButton startVpnButton = findViewById(R.id.startVpn);
        boolean vpnOn = PDoHVpnService.isRunning();
        startVpnButton.update(vpnOn);
        enableVpnComponents(!vpnOn);
        startVpnButton.setOnClick(this::startVpn, this::stopVpn);
    }

    private boolean startVpn() {
        if (!checkInternet()) {
            Log.e(TAG, "There is no internet");
            Toast toast = Toast.makeText(getApplicationContext(), "There is no internet\nTry again when you're connected to wifi", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        if (PDoHVpnService.isRunning()) {
            Log.e(TAG, "VPN already on");
            Toast toast = Toast.makeText(getApplicationContext(), "We are trying to close the vpn\nTry again when the logo is off", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        setInternetErrorMessage(false);

        ProtocolId protocol = ProtocolId.DOH;
        try {
            protocol = protocolSelector.getProtocol();
        } catch (UnselectedProtocol unselectedProtocol) {
            Log.e(TAG, Arrays.toString(unselectedProtocol.getStackTrace()));
            Toast.makeText(MainActivity.this, "No protocol selected. Set default protocol: DoH", Toast.LENGTH_SHORT).show();
        }

        Intent vpnIntent = VpnService.prepare(this);

        if (vpnIntent == null) {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
        shardingControllerFactory = new ShardingControllerFactory(protocol, racingAmountBar.getCustomProgress());
        PDoHVpnService.setShardingControllerFactory(shardingControllerFactory);
        setMetricsShardingController();
        enableVpnComponents(false);

        InternetChecker internetChecker = new InternetChecker(this::checkInternet);
        Thread t = new Thread(internetChecker);
        t.start();
        return true;
    }

    private void setInternetErrorMessage(boolean isError) {
        TextView output = findViewById(R.id.internetErrorOutput);
        output.setText(isError ? R.string.internet_error : R.string.empty);
    }

    private void stopVpnInternet() {
        Log.e(TAG, "We need to stop the vpn due to a connectivity issue");
        setInternetErrorMessage(true);
        StartVPNButton startVpnButton = findViewById(R.id.startVpn);
        startVpnButton.closeVpn();

        stopVpn();
    }

    private void stopVpn() {
        Intent intent = new Intent(Config.STOP_SIGNAL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        enableVpnComponents(true);
        shardingControllerFactory.destroy();
        shardingControllerFactory = null;
        setMetricsShardingController();
        Intent serviceIntent = new Intent(this, PDoHVpnService.class);
        serviceIntent.setAction(Config.STOP_FOREGROUND_ACTION);
        startService(serviceIntent);
        actualTransport = -1;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "On destroy was called");
        super.onDestroy();
        if (isFinishing()) {
            Log.i(TAG, "The user is closing the app");
            stopVpn();
        } else {
            Log.i(TAG, "The OS is closing the app");
        }
    }

    public void bugClicked(View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(Config.BUG_LINK));

        startActivity(httpIntent);
    }

    public boolean checkInternet() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] mWifi = connManager.getAllNetworks();
        NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(connManager.getActiveNetwork());

        if (this.actualTransport == -1) {
            if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                this.actualTransport = NetworkCapabilities.TRANSPORT_WIFI;
            } else if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                this.actualTransport = NetworkCapabilities.TRANSPORT_CELLULAR;
            }
        }

        boolean stillConnectedToWifi = networkCapabilities != null && networkCapabilities.hasTransport(this.actualTransport);
        boolean onlyVpn = networkCapabilities != null && !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                && !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);

        return stillConnectedToWifi || (mWifi.length > 1 && onlyVpn); // This is because the vpn connection counts as one
    }
}