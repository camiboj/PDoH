package com.tpp.private_doh.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tpp.private_doh.dns.RTT;

public class RequesterMetric extends LinearLayout {

    public RequesterMetric(Context context, String requesterName, int countMetric, RTT timeMetric) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
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
