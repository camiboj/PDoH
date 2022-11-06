package com.tpp.private_doh.components.racing_amount_selector;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.components.CustomText;

public class ProgressOutput extends CustomText {
    public ProgressOutput(@NonNull Context context, int textId) {
        super(context, textId, R.style.AppTheme_ProgressOutput);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int color = enabled ? R.color.colorPrimary : R.color.colorDisabled;
        setTextColor(ContextCompat.getColorStateList(getContext(), color));
    }
}
