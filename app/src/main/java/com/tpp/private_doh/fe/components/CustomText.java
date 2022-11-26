package com.tpp.private_doh.fe.components;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class CustomText extends androidx.appcompat.widget.AppCompatTextView {
    public CustomText(@NonNull Context context, int textId, int style) {
        super(new ContextThemeWrapper(context, style));
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setText(textId);
    }
}
