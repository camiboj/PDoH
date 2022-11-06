package com.tpp.private_doh.components.protocol_selector;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.tpp.private_doh.R;
import com.tpp.private_doh.components.CustomLinearLayout;
import com.tpp.private_doh.components.Subtitle;
import com.tpp.private_doh.components.Title;

public class ProtocolSelectorLayout extends CustomLinearLayout {

    private final Title title;
    private final Subtitle subtitle;
    private final ProtocolSelectorRadioGroup rg;

    public ProtocolSelectorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        title = new Title(context, R.string.racing_amount_title);
        subtitle = new Subtitle(context, R.string.racing_amount_subtitle);
        rg = new ProtocolSelectorRadioGroup(context);
        addView(title);
        addView(subtitle);
        addView(rg);
    }


    public ProtocolSelectorRadioGroup getProtocolSelectorRadioGroup() {
        return rg;
    }
}
