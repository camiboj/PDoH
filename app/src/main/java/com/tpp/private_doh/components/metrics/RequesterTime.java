package com.tpp.private_doh.components.metrics;

import android.content.Context;

import com.tpp.private_doh.dns.RTT;

import java.text.DecimalFormat;

public class RequesterTime extends RequesterOutputWithDescription {
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    protected static String getRTTHTML(String subIndex){
        return String.format("R&#773;T&#773;T&#773;<sub>%s</sub>", subIndex);
    }

    private static final String DESCRIPTIVE_MESSAGE = String.format("The average RTT is calculated with the formula used by TCP (RFC 2988)<br>%s;<sub>n</sub> = (1 - &alpha;) * %s;<sub>n-1</sub> + &alpha; * RTT;", getRTTHTML("n"), getRTTHTML("n-1"));

    public RequesterTime(Context context, RTT rtt) {
        super(
                context,
                "Response Time (milli sec)", "Mean  " + DF.format(rtt.getAvgMilliSecond()) + "\nVariance  " + DF.format(rtt.getDevMilliSecond()),
                DESCRIPTIVE_MESSAGE
        );
    }
}
