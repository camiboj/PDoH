package com.tpp.private_doh.components;

import android.content.Context;
import android.widget.LinearLayout;

import com.tpp.private_doh.R;

public class RequesterCount extends RequesterOutput {

    public RequesterCount(Context context, int count, LinearLayout.LayoutParams layoutParams) {
        super(context, "Winning count:\n" + count, "Count descriptive message", layoutParams, R.style.AppTheme_MetricsHeadLine2);
    }
}
