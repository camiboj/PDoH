package com.tpp.private_doh.components;

import android.content.Context;

import androidx.annotation.NonNull;

import com.tpp.private_doh.R;

public class Title extends CustomText {
    public Title(@NonNull Context context, int textId) {
        super(context, textId, R.style.AppTheme_Subtitle);
    }
}
