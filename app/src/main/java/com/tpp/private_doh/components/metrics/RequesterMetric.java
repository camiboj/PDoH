package com.tpp.private_doh.components.metrics;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tpp.private_doh.dns.RTT;

public class RequesterMetric extends LinearLayout {

    public RequesterMetric(Context context, String requesterName, int countMetric, RTT timeMetric, int totalMetrics) {
        super(context);
        int h = ViewGroup.LayoutParams.WRAP_CONTENT;
        // SUPER HARDCODED. TO FULFIL THE METRICS SCREEN SPACE
        if (totalMetrics < 4) {
            h = 2000 / totalMetrics;
        }

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        View requester = new RequesterName(
                context,
                requesterName
        );
        View count = new RequesterCount(
                context,
                countMetric
        );

        View time = new RequesterTime(
                context,
                timeMetric
        );

        this.addView(requester);
        this.addView(count);
        this.addView(time);
    }

}
