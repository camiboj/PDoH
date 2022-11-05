package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RequesterMetric extends LinearLayout {
    public RequesterMetric(Context context, String requesterName, int metric, int metricHeight) {
        super(context);
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight));
        this.setOrientation(VERTICAL);

        TextView requester = new TextView(context);
        requester.setText(requesterName);
        TextView count = new TextView(context);
        count.setText(String.valueOf(metric));

        this.addView(requester);
        this.addView(count);
    }

}
