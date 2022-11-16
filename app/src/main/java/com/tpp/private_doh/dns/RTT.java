package com.tpp.private_doh.dns;

import static java.lang.Math.abs;

public class RTT {

    private static final float ALPHA = 0.125F;
    private static final float BETA = 0.25F;
    private float avgRTT = 0;
    private float devRTT = 0;

    public void update(float new_rtt) {
        if (avgRTT == 0) {
            avgRTT = new_rtt;
            return;
        }
        float newAvgRTT = (1 - ALPHA) * avgRTT + ALPHA * new_rtt;
        float newDevRTT = (1 - BETA) * devRTT + BETA * abs(new_rtt - newAvgRTT);
        avgRTT = newAvgRTT;
        devRTT = newDevRTT;
    }

    public float getAvgMilliSecond() {
        return avgRTT/1000000;
    }

    public float getDevMilliSecond() {
        return devRTT/1000000;
    }

}
