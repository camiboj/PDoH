package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class MetricsScreen extends RelativeLayout {
    private CustomButton button;

    public MetricsScreen(Context context, AttributeSet attrs) {
        super(context, attrs);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int buttonWidth = getWidth() - getWidth() / 3;
                int leftRightSpace = getWidth() / 3;
                int topSpace = getHeight() / 12;
                int buttonHeight = getHeight() / 12;
                createButton(leftRightSpace, topSpace, buttonWidth, buttonHeight);
            }
        });
    }

    private void createButton(int leftRightSpace, int topSpace, int buttonWidth, int buttonHeight) {
        MarginLayoutParams mlp = new MarginLayoutParams(buttonWidth, buttonHeight);
        mlp.setMargins(leftRightSpace, topSpace, leftRightSpace, 0);
        button = new CustomButton(getContext(), "Fetch", mlp);
        this.addView(button);
        button.setEnabled(true);
    }

}
