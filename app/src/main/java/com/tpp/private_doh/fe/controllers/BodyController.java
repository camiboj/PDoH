package com.tpp.private_doh.fe.controllers;

import android.content.Context;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tpp.private_doh.fe.components.MyConstraintLayout;
import com.tpp.private_doh.fe.components.metrics_screen.MetricsLayout;
import com.tpp.private_doh.fe.components.metrics_screen.MetricsScrollView;

public class BodyController extends FEController{

    private final int w;
    private final int h;
    private final MetricsScrollView sv;
    private final ConstraintLayout layout;

    public BodyController(Context context, int w, int h, int topAndBottomPadding) {
        super(context);
        this.w = w;
        this.h = h;

        layout = new MyConstraintLayout(context, new ConstraintLayout.LayoutParams(w, h));
        layout.setPadding(0, topAndBottomPadding, 0, topAndBottomPadding);

        sv = new MetricsScrollView(context, new ScrollView.LayoutParams(w, h));

        layout.addView(sv);
    }

    private MetricsLayout getBody() {
        return sv.getMetricLayout();
    }
}
