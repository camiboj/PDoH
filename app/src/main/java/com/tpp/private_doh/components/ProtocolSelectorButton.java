package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.controller.ProtocolId;

public class ProtocolSelectorButton extends androidx.appcompat.widget.AppCompatRadioButton {

    private final String TAG = this.getClass().getSimpleName();
    private final ProtocolId protocolId;


    public ProtocolSelectorButton(Context context, ProtocolId protocolId, int text) {
        super(context);
        this.protocolId = protocolId;
        setText(text);
        setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int color = enabled? R.color.colorPrimary : R.color.colorDisabled;
        setButtonTintList(ContextCompat.getColorStateList(getContext(), color));
    }

    public ProtocolId getProtocolId() {
        return protocolId;
    }
}
