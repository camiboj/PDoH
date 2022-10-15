package com.tpp.private_doh.app;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tpp.private_doh.PDoHVpnService;
import com.tpp.private_doh.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {

    private static final int VPN_REQUEST_CODE = 0x0F;
    private int racing_amount = 2;
    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    private SeekBar seekBar;
    private RadioGroup rgProtocol;
    Map<Integer, Integer> RbIDtoProtocolID = new HashMap<Integer, Integer>()
    {
        {
            put(R.id.rbDoH, 1);
            put(R.id.rbDNS, 2);
            put(R.id.rbBoth, 3);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        rgProtocol = findViewById(R.id.rgProtocol);
        seekBar = findViewById(R.id.RacingSeekBar);
        setSeekBar(findViewById(R.id.progress));
    }

    private void setSeekBar(TextView t) {
        seekBar.setProgress(racing_amount);
        // TODO: when we get the dns/doh servers list
        // seekBar.setMin();
        // seekBar.setMax();

        t.setText(String.valueOf(racing_amount));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                t.setText(String.valueOf(i));
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

    // @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data, int protocol, int racingAmount) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            PDoHVpnService.setProtocolId(protocol);
            PDoHVpnService.setRacingAmount(racingAmount);
            startService(new Intent(this, PDoHVpnService.class));
        }
    }

    private int getProtocol() {
        int selectedId = rgProtocol.getCheckedRadioButtonId();
        RadioButton selectedRb = (RadioButton) findViewById(selectedId);
        if (selectedId==-1) {
            Toast.makeText(MainActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
            // TODO: maybe throw error
            return -1;
        }
        return RbIDtoProtocolID.get(selectedId);
    }
    private void startVpn() {
        int protocol = getProtocol();
        if (protocol == -1) {
            return;
        }

        Intent vpnIntent = VpnService.prepare(this);

        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null, protocol, seekBar.getProgress());
            rgProtocol.setEnabled(false);
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
