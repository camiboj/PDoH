package com.tpp.private_doh.fe.components.metrics;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tpp.private_doh.R;

public class RequesterOutput extends androidx.appcompat.widget.LinearLayoutCompat {

    public RequesterOutput(Context context, String text) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        TextView tv = new TextView(new ContextThemeWrapper(context, R.style.AppTheme_MetricsHeadLine1));
        tv.setText(text);
        addView(tv);
    }
}
