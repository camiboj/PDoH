package com.tpp.private_doh.components;

import android.content.Context;

public class RacingAmountInput extends androidx.appcompat.widget.AppCompatSeekBar {

    private static final int DEFAULT = 2;
    // private final TextView t;

    public RacingAmountInput(Context context) {
        super(context);


    }

    public int getAmount() {
        return super.getProgress();
    }
}

