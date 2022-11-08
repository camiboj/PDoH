package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RequesterMetric extends LinearLayout {
    public RequesterMetric(Context context, String requesterName, int metric, int metricHeight) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight));
        setOrientation(VERTICAL);
        TextView requester = new RequesterName(
                context,
                requesterName,
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight/2)
        );
        TextView count = new RequesterCount(
                context,
                String.valueOf(metric),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight/2)
        );

        this.addView(requester);
        this.addView(count);
    }

}
