package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RequesterOutputWithDescription extends androidx.appcompat.widget.LinearLayoutCompat {

    public RequesterOutputWithDescription(Context context, String title, String metric, String descriptiveMessage, int style) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        RequesterOutputWithDescriptionTitle titleView = new RequesterOutputWithDescriptionTitle(context, title, descriptiveMessage, style);
        addView(titleView);

        TextView metricView = new TextView(new ContextThemeWrapper(context, style));
        metricView.setText(metric);
        addView(metricView);

    }

}