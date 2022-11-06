package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


public class CustomLinearLayout extends LinearLayout {
    private int height;

    public CustomLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setChildrenHeight();
    }

    protected void setChildHeight(View view, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = height;
        view.requestLayout();
    }

    private void setChildrenHeight() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (height != getHeight() ) {
                    return;
                }
                height = getHeight();
                onHeightChange(height);
            }
        });
    }

    protected void onHeightChange(int height) {
    }
}
