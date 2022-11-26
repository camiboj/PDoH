package com.tpp.private_doh.fe.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatButton;

import java.util.concurrent.Callable;


public class StartVPNButton extends AppCompatButton {
    private final String TAG = this.getClass().getSimpleName();
    private boolean vpnOn = false;
    private String VPN_ON = "Stop VPN";
    private String VPN_OFF = "Start VPN";

    public StartVPNButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateText();
    }

    public void update(boolean vpnOn) {
        this.vpnOn = vpnOn;
        updateText();
    }

    private void updateText() {
        setText(vpnOn ? VPN_ON : VPN_OFF);
    }

    public void setOnClick(Callable<Boolean> onStartVPN, Runnable onStopVPN) {
        this.setOnClickListener(v -> {
            vpnOn = !vpnOn;
            if (vpnOn) {
                try {
                    Boolean result = onStartVPN.call();
                    Log.i(TAG, "Starting vpn....");
                    if (!result) {
                        vpnOn = !vpnOn;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "This shouldn't happen");
                }
            } else {
                Log.i(TAG, "Stopping vpn....");
                onStopVPN.run();
            }
            updateText();
        });
    }

    public void closeVpn() {
        vpnOn = false;
        setText(VPN_OFF);
    }
}
