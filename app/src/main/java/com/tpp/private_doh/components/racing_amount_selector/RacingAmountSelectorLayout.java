package com.tpp.private_doh.components.racing_amount_selector;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.tpp.private_doh.R;
import com.tpp.private_doh.components.CustomLinearLayout;
import com.tpp.private_doh.components.Subtitle;
import com.tpp.private_doh.components.Title;

public class RacingAmountSelectorLayout extends CustomLinearLayout {

    private final Title title;
    private final Subtitle subtitle;
    private final ProgressOutput progressOutput;
    private final RacingAmountBar bar;

    public RacingAmountSelectorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        title = new Title(context, R.string.racing_amount_title);
        subtitle = new Subtitle(context, R.string.racing_amount_subtitle);
        progressOutput = new ProgressOutput(context);
        bar = new RacingAmountBar(context, progressOutput);
        addView(title);
        addView(subtitle);
        addView(progressOutput);
        addView(bar);
    }

    @Override
    protected void onHeightChange(int height) {
        int sections = 6;
        setChildHeight(title, height/sections);
        setChildHeight(bar, height/sections);
    }

    public RacingAmountBar getBar() {
        return bar;
    }
}
