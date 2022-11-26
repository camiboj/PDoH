package com.tpp.private_doh.fe.components.protocol_selector;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.tpp.private_doh.R;
import com.tpp.private_doh.fe.components.CustomLinearLayout;
import com.tpp.private_doh.fe.components.Subtitle;
import com.tpp.private_doh.fe.components.Title;

public class ProtocolSelectorLayout extends CustomLinearLayout {

    private final Title title;
    private final Subtitle subtitle;
    private final ProtocolSelectorRadioGroup rg;

    public ProtocolSelectorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        title = new Title(context, R.string.select_protocol_title);
        subtitle = new Subtitle(context, R.string.select_protocol_subtitle);
        rg = new ProtocolSelectorRadioGroup(context);
        addView(title);
        addView(subtitle);
        addView(rg);
    }

    @Override
    protected void onHeightChange(int height) {
        int sections = 6;
        setChildHeight(title, height/sections);
        setChildHeight(rg, height/sections);
    }

    public ProtocolSelectorRadioGroup getProtocolSelectorRadioGroup() {
        return rg;
    }
}
