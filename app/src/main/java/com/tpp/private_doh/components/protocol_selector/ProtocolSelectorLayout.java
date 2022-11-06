package com.tpp.private_doh.components.protocol_selector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.tpp.private_doh.R;
import com.tpp.private_doh.components.Subtitle;
import com.tpp.private_doh.components.Title;

public class ProtocolSelectorLayout extends LinearLayout {

    private final Title title;
    private final Subtitle subtitle;
    private ProtocolSelectorRadioGroup rg;

    public ProtocolSelectorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        rg = new ProtocolSelectorRadioGroup(context);
        title = new Title(context, R.string.select_protocol_title);
        subtitle = new Subtitle(context, R.string.select_protocol_subtitle);
        addView(title);
        addView(subtitle);
        addView(rg);


        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = getHeight()/4;
                addHeight(title, height);
                addHeight(subtitle, height);
                addHeight(rg, height);
            }
        });
    }

    private void addHeight(View view, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = height;
        view.requestLayout();
    }

    public ProtocolSelectorRadioGroup getProtocolSelectorRadioGroup() {
        return rg;
    }
}
