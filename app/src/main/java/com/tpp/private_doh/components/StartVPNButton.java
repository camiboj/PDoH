package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;


public class StartVPNButton extends AppCompatButton {
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

    public void setOnclick(Runnable onStartVPN, Runnable onStopVPN) {
        this.setOnClickListener(v -> {
            vpnOn = !vpnOn;
            updateText();
            if (vpnOn) {
                onStartVPN.run();
            } else {
                onStopVPN.run();
            }
        });
    }
}
