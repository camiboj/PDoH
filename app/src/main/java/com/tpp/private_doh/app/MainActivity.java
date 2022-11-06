package com.tpp.private_doh.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tpp.private_doh.PDoHVpnService;
import com.tpp.private_doh.R;
import com.tpp.private_doh.components.DownBar;
import com.tpp.private_doh.components.MetricsScreen;
import com.tpp.private_doh.components.protocol_selector.ProtocolSelectorLayout;
import com.tpp.private_doh.components.protocol_selector.ProtocolSelectorRadioGroup;
import com.tpp.private_doh.components.RacingAmountSelector;
import com.tpp.private_doh.components.StartVPNButton;
import com.tpp.private_doh.components.UnselectedProtocol;
import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.factory.ShardingControllerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {
    private static final int VPN_REQUEST_CODE = 0x0F;
    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    private final String TAG = this.getClass().getSimpleName();

    private ProtocolSelectorRadioGroup protocolSelector;
    private RacingAmountSelector racingAmountSelector;
    // private TextView countOutput;
    private ShardingControllerFactory shardingControllerFactory;

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
        racingAmountSelector = findViewById(R.id.racingAmountSelector);

        protocolSelector.setOnCheckedChangeListener((group, checkedId) -> setSeekBarMax());
        racingAmountSelector.setCustomMin(Config.MIN_RACING_AMOUNT);
        setSeekBarMax();
        setButtonHandlers();

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
            setMetricsShardingController();
            startService(new Intent(this, PDoHVpnService.class));
        }
    }

    private void setMetricsShardingController() {
        MetricsScreen ms = findViewById(R.id.metrics_layout);
        ms.setShardingControllerFactory(shardingControllerFactory);
    }

    private void enableVpnComponents(boolean enabled) {
        protocolSelector.setEnabled(enabled);
        racingAmountSelector.setEnabled(enabled);
    }

    private void setButtonHandlers() {
        StartVPNButton startVpnButton = findViewById(R.id.startVpn);
        startVpnButton.setOnclick(this::startVpn, this::stopVpn);

    }

    private void startVpn() {
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
    }

    private void stopVpn() {
        Intent intent = new Intent(Config.STOP_SIGNAL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        enableVpnComponents(true);
        shardingControllerFactory = null;
        setMetricsShardingController();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void bugClicked(View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(Config.BUG_LINK));

        startActivity(httpIntent);
    }
}