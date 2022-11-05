package com.tpp.private_doh.components;

import android.content.Context;
import android.widget.LinearLayout;

import com.tpp.private_doh.R;

public class RequesterName extends RequesterOutput {

    public RequesterName(Context context, String text, LinearLayout.LayoutParams layoutParams) {
        super(context, text, layoutParams, R.style.AppTheme_MetricsHeadLine1);
    }
}
