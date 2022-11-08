package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.LinearLayout;

public class RequesterOutput extends androidx.appcompat.widget.AppCompatTextView {

    public RequesterOutput(Context context, String text, LinearLayout.LayoutParams layoutParams, int style) {
        super(new ContextThemeWrapper(context, style));
        setText(text);
        setLayoutParams(layoutParams);
    }
}
