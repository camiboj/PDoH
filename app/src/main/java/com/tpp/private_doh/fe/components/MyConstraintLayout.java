package com.tpp.private_doh.fe.components;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MyConstraintLayout extends ConstraintLayout {
    public MyConstraintLayout(@NonNull Context context, LayoutParams lp) {
        super(context);
        setLayoutParams(lp);
    }
}
