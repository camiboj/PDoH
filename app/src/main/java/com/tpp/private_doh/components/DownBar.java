package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class DownBar extends RelativeLayout {
    private CustomButton vpnScreenButton;
    private CustomButton metricsScreenButton;
    private View vpnScreen;
    private View metricScreen;

    public DownBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int buttonSize = getWidth() / 4;
                int spacesSize = getWidth() / 6;
                createButtons(buttonSize, spacesSize);
            }
        });
    }

    private MarginLayoutParams getMarginLayoutParams(int buttonWidth, int spacesLeft, int spacesRight ) {
        MarginLayoutParams mlp = new MarginLayoutParams(buttonWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        mlp.setMargins(spacesLeft, 0, spacesRight, 0);
        return mlp;
    }

    private void createButtons(int buttonWidth, int spacesToClosestSize) {
        int spacesToFarthestSize = getWidth() - buttonWidth - spacesToClosestSize;

        MarginLayoutParams vpnScreenButtonMlp = getMarginLayoutParams(buttonWidth, spacesToClosestSize, spacesToFarthestSize);
        MarginLayoutParams metricsScreenButtonMlp = getMarginLayoutParams(buttonWidth, spacesToFarthestSize, spacesToClosestSize);

        vpnScreenButton = new CustomButton(getContext(), "VPN", vpnScreenButtonMlp);
        metricsScreenButton = new CustomButton(getContext(), "Metrics", metricsScreenButtonMlp);

        this.addView(metricsScreenButton);
        this.addView(vpnScreenButton);

        vpnScreenButton.setEnabled(true);
        vpnScreenButton.setOnClickListener(v -> {
            setVpnClicked(true);
        });
        metricsScreenButton.setOnClickListener(v -> {
            setMetricsClicked(true);
        });
    }

    private void setMetricsClicked(boolean isMetricsClicked) {
        setVpnClicked(!isMetricsClicked);
    }


    private void setVpnClicked(Boolean isVpnClicked) {
        int vpnVisibility = isVpnClicked ? VISIBLE : INVISIBLE;
        int metricsVisibility = isVpnClicked ? INVISIBLE : VISIBLE;
        vpnScreenButton.setEnabled(isVpnClicked);
        vpnScreen.setVisibility(vpnVisibility);
        metricsScreenButton.setEnabled(!isVpnClicked);
        metricScreen.setVisibility(metricsVisibility);
    }

    public void setVpnScreen(View vpnScreen) {
        this.vpnScreen = vpnScreen;
    }
    public void setMetricsScreen(View metricScreen) {
        this.metricScreen = metricScreen;
        metricScreen.setVisibility(INVISIBLE);
    }
}
