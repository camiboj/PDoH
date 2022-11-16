package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tpp.private_doh.dns.RTT;

public class RequesterMetric extends LinearLayout {
    private static final int LINES = 3;

    public RequesterMetric(Context context, String requesterName, int countMetric, RTT timeMetric, int metricHeight) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight));
        setOrientation(VERTICAL);
        TextView requester = new RequesterName(
                context,
                requesterName,
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight/LINES)
        );
        TextView count = new RequesterCount(
                context,
                String.valueOf(countMetric),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight/LINES)
        );

        TextView time = new RequesterCount(
                context,
                String.valueOf(timeMetric),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, metricHeight/LINES)
        );

        this.addView(requester);
        this.addView(count);
        this.addView(time);
    }

}
