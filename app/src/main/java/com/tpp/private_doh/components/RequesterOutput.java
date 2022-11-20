package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RequesterOutput extends androidx.appcompat.widget.LinearLayoutCompat {

    public RequesterOutput(Context context, String text, int style) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        TextView tv = new TextView(new ContextThemeWrapper(context, style));
        tv.setText(text);
        addView(tv);
    }
}
