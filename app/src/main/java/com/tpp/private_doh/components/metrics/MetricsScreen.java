package com.tpp.private_doh.components.metrics;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.components.CustomButton;
import com.tpp.private_doh.dns.RTT;
import com.tpp.private_doh.factory.ShardingControllerFactory;

import java.util.HashMap;
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

    private void fetchMetrics() {
        if (shardingControllerFactory == null) {
            Toast.makeText(getContext(), "No running VPN", Toast.LENGTH_LONG).show();
            return;
        }
        // Map<String, Integer> counts = shardingControllerFactory.getRequestersWinningMetrics();
        // Map<String, RTT> times = shardingControllerFactory.getRequestersTimesMetrics();

        Map<String, Integer> counts = new HashMap<String, Integer>() {
            {
                put("1.1.1.1", 2);
            }
            {
                put("Google", 5);
            }
            {
                put("CloudFare", 3);
            }
            {
                put("9.9.9.9", 8);
            }
        };

         Map<String, RTT> times = new HashMap<String, RTT>() {
             {
                 put("1.1.1.1", new RTT(20000000, 2000000));
             }
             {
                 put("Google", new RTT(50000000, 5000000));
             }
             {
                 put("CloudFare", new RTT(30000000, 3000000));
             }
             {
                 put("9.9.9.9", new RTT(80000000, 8000000));
             }
         };

        int count = 0;
        for (String requesterName : counts.keySet()) {
            int requesterCount = counts.get(requesterName);
            RTT requesterTime = times.get(requesterName);
            if (requesterCount > 0) {
                count++;
                createMetricLayout(requesterName, requesterCount, requesterTime, count);
            }
        }
    }

    private void createMetricLayout(String requesterName, int countMetric, RTT timeMetric, int count) {
        int color = count%2 == 0 ? R.color.colorMetric1 : R.color.colorMetric2;
        RequesterMetric rm = new RequesterMetric(getContext(), requesterName, countMetric, timeMetric);
        rm.setBackground(ContextCompat.getDrawable(getContext(), color));
        addView(rm);
    }
}
