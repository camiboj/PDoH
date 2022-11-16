package com.tpp.private_doh.dns;

import static java.lang.Math.abs;

import java.text.DecimalFormat;

public class RTT {

    private static final float ALPHA = 0.125F;
    private static final float BETA = 0.25F;
    private float avgRTT = 0;
    private float dev_rtt = 0;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public void update(float new_rtt) {
        if (avgRTT == 0) {
            avgRTT = new_rtt;
            return;
        }
        float newAvgRTT = (1 - ALPHA) * avgRTT + ALPHA * new_rtt;
        float newDev_rtt = (1 - BETA) * dev_rtt + BETA * abs(new_rtt - newAvgRTT);
        avgRTT = newAvgRTT;
        dev_rtt = newDev_rtt;
    }

    @Override
    public String toString() {
        return df.format(avgRTT) + "+/-" + df.format(dev_rtt);
    }

}
