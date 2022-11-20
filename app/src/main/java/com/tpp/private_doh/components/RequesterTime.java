package com.tpp.private_doh.components;

import android.content.Context;
import android.widget.LinearLayout;

import com.tpp.private_doh.R;
import com.tpp.private_doh.dns.RTT;

import java.text.DecimalFormat;

public class RequesterTime extends RequesterOutput {
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    protected static String getRTTHTML(String subIndex){
        return String.format("R&#773;T&#773;T&#773;<sub>%s</sub>", subIndex);
    }

    private static final String FORMULA_MESSAGE = String.format("%s = (1 - &alpha;) * %s + &alpha; * RTT;", getRTTHTML("n"), getRTTHTML("n-1"));
    private static final String DESCRIPTIVE_MESSAGE = "<font size=\"1\"> The average RTT is calculate with the formula used by TCP (RFC 2988)\n%s </font>";

    public RequesterTime(Context context, RTT rtt, LinearLayout.LayoutParams layoutParams) {
        super(
                context,
                "Winning count (milli sec):\n" + DF.format(rtt.getAvgMilliSecond()) + " +/- " + DF.format(rtt.getDevMilliSecond()),
                String.format(DESCRIPTIVE_MESSAGE, FORMULA_MESSAGE),
                layoutParams,
                R.style.AppTheme_MetricsHeadLine2
        );
    }
}
