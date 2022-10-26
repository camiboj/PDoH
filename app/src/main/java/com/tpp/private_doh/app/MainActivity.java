package com.tpp.private_doh.app;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tpp.private_doh.PDoHVpnService;
import com.tpp.private_doh.R;
import com.tpp.private_doh.components.ProtocolSelector;
import com.tpp.private_doh.components.UnselectedProtocol;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.controller.ShardingControllerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {

    private static final int RACING_AMOUNT_MIN = 0;
    private static final int RACING_AMOUNT_OFFSET = 2;
    private static final int VPN_REQUEST_CODE = 0x0F;
    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    private final String TAG = this.getClass().getSimpleName();
    private ProtocolSelector protocolSelector;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        seekBar = findViewById(R.id.RacingSeekBar);
        protocolSelector = findViewById(R.id.protocolSelector);

        protocolSelector.setOnCheckedChangeListener((group, checkedId) -> setSeekBarMax());
        setSeekBar(findViewById(R.id.progress));
    }

    private void setSeekBarMax() {
        int current = seekBar.getProgress();
        try {
            int availableRequesterAmount = ShardingControllerFactory.getAvailableRequesterAmount(protocolSelector.getProtocol());
            seekBar.setMax(availableRequesterAmount - RACING_AMOUNT_OFFSET);
        } catch (UnselectedProtocol unselectedProtocol) {
            Log.e(TAG, Arrays.toString(unselectedProtocol.getStackTrace()));
        }
        seekBar.setProgress(current);
    }

    private void setSeekBar(TextView t) {
        seekBar.setProgress(RACING_AMOUNT_MIN);
        seekBar.setMin(RACING_AMOUNT_MIN);
        setSeekBarMax();

        t.setText(String.valueOf(RACING_AMOUNT_OFFSET));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                t.setText(String.valueOf(i+RACING_AMOUNT_OFFSET));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data, ProtocolId protocol, int racingAmount) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            ShardingControllerFactory.setProtocolId(protocol);
            ShardingControllerFactory.setRacingAmount(racingAmount);
            startService(new Intent(this, PDoHVpnService.class));
        }
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

        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null, protocol, seekBar.getProgress() + RACING_AMOUNT_OFFSET);
            protocolSelector.setEnabled(false);
            seekBar.setEnabled(false);
            findViewById(R.id.startVpn).setEnabled(false);
        }
    }

    public void clickSwitch(View view) {
        this.startVpn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
