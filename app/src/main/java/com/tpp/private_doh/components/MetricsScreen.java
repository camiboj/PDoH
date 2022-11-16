package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.dns.RTT;
import com.tpp.private_doh.factory.ShardingControllerFactory;

import java.util.Map;

public class MetricsScreen extends LinearLayout {
    private CustomButton button;
    private ShardingControllerFactory shardingControllerFactory;

    public MetricsScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = getWidth();
                int height = getHeight();
            }
        });
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

    private int getWinnersCount(Map<String, Integer> counts) {
        int metricsAmount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 0) {
                metricsAmount++;
            }
        }
        return metricsAmount;
    }
    private void fetchMetrics() {
        if (shardingControllerFactory == null) {
            Toast.makeText(getContext(), "No running VPN", Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, Integer> counts = shardingControllerFactory.getRequestersWinningMetrics();
        Map<String, RTT> times = shardingControllerFactory.getRequestersTimesMetrics();

        int metricsAmount = getWinnersCount(counts);
        int count = 0;
        for (String requesterName : counts.keySet()) {
            int requesterCount = counts.get(requesterName);
            RTT requesterTime = times.get(requesterName);
            if (requesterCount > 0) {
                count++;
                createMetricLayout(requesterName, requesterCount, requesterTime, metricsAmount, count);
            }
        }
    }

    private void createMetricLayout(String requesterName, int countMetric, RTT timeMetric, int metricsAmount, int count) {
        int color = count%2 == 0 ? R.color.colorMetric1 : R.color.colorMetric2;
        int metricHeight = getHeight() / metricsAmount;
        RequesterMetric rm = new RequesterMetric(getContext(), requesterName, countMetric, timeMetric, metricHeight);
        rm.setBackground(ContextCompat.getDrawable(getContext(), color));
        addView(rm);
    }
}
