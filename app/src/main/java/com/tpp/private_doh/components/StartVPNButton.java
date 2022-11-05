package com.tpp.private_doh.components;

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

    private void updateText() {
        setText(vpnOn ? VPN_ON : VPN_OFF);
    }

    public void setOnclick(Callable<Boolean> onStartVPN, Runnable onStopVPN) {
        this.setOnClickListener(v -> {
            vpnOn = !vpnOn;
            if (vpnOn) {
                try {
                    Boolean result = onStartVPN.call();
                    if (!result) {
                        vpnOn = !vpnOn;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "This shouldn't happen");
                }
            } else {
                onStopVPN.run();
            }
            updateText();
        });
    }
}
