package com.tpp.private_doh.fe.components.metrics_screen;

import android.content.Context;
import android.widget.ScrollView;

public class MetricsScrollView extends ScrollView {

    private final MetricsLayout metricsLayout;

    public MetricsScrollView(Context context, android.view.ViewGroup.LayoutParams layoutParams) {
        super(context);
        setLayoutParams(layoutParams);
        metricsLayout = new MetricsLayout(context, getLayoutParams());
        addView(metricsLayout);
    }


    public MetricsLayout getMetricLayout() {
        return metricsLayout;
    }
}
