package com.tpp.private_doh.fe.components.metrics_screen;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.fe.components.metrics.RequesterMetric;
import com.tpp.private_doh.dns.RTT;
import com.tpp.private_doh.factory.ShardingControllerFactory;

import java.util.Map;

public class MetricsLayout extends LinearLayout {
    private ShardingControllerFactory shardingControllerFactory;

    public MetricsLayout(Context context,  android.view.ViewGroup.LayoutParams layoutParams) {
        super(context);
        setOrientation(VERTICAL);
        setLayoutParams(layoutParams);
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            fetchMetrics();
        } else {
            removeAllViews();
        }
    }

    public void setShardingControllerFactory(ShardingControllerFactory shardingControllerFactory) {
        this.shardingControllerFactory = shardingControllerFactory;
    }

    private void fetchMetrics() {
        if (shardingControllerFactory == null) {
            Toast.makeText(getContext(), "No running VPN", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Integer> counts = shardingControllerFactory.getRequestersWinningMetrics();
        Map<String, RTT> times = shardingControllerFactory.getRequestersTimesMetrics();

        int winnersAmount = getWinnersAmount(counts);
        int count = 0;
        for (String requesterName : counts.keySet()) {
            int requesterCount = counts.get(requesterName);
            RTT requesterTime = times.get(requesterName);
            if (requesterCount > 0) {
                count++;
                createMetricLayout(requesterName, requesterCount, requesterTime, count, winnersAmount);
            }
        }
    }

    private int getWinnersAmount(Map<String, Integer> counts) {
        int amount = 0;
        for (String requesterName : counts.keySet()) {
            int requesterCount = counts.get(requesterName);
            if (requesterCount > 0) {
                amount++;
            }
        }
        return amount;
    }

    private void createMetricLayout(String requesterName, int countMetric, RTT timeMetric, int count, int metricsAmount) {
        int color = count%2 == 0 ? R.color.colorMetric1 : R.color.colorMetric2;
        RequesterMetric rm = new RequesterMetric(getContext(), requesterName, countMetric, timeMetric, metricsAmount);
        rm.setBackground(ContextCompat.getDrawable(getContext(), color));
        addView(rm);
    }
}
