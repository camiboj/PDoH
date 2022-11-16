package com.tpp.private_doh.components;

import android.content.Context;
import android.widget.LinearLayout;

import com.tpp.private_doh.R;
import com.tpp.private_doh.dns.RTT;

import java.text.DecimalFormat;

public class RequesterTime extends RequesterOutput {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public RequesterTime(Context context, RTT rtt, LinearLayout.LayoutParams layoutParams) {
        super(
                context,
                "Winning count: " + df.format(rtt.getAvgMilliSecond()) + " +/- " + df.format(rtt.getDevMilliSecond()),
                layoutParams,
                R.style.AppTheme_MetricsHeadLine2
        );
    }
}
