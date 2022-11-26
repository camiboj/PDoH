package com.tpp.private_doh.fe.components.metrics;

import android.content.Context;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tpp.private_doh.R;

public class RequesterOutputWithDescriptionTitle extends androidx.appcompat.widget.LinearLayoutCompat {

    public RequesterOutputWithDescriptionTitle(Context context, String title, String descriptiveMessage, int style) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        TextView titleView = new TextView(new ContextThemeWrapper(context, style));
        titleView.setText(title);
        addView(titleView);

        FloatingActionButton fab = new FloatingActionButton(new ContextThemeWrapper(context, R.style.AppTheme_QuestionMark));
        fab.setTooltipText(Html.fromHtml(descriptiveMessage, Html.FROM_HTML_MODE_COMPACT));
        fab.setSize(FloatingActionButton.SIZE_MINI);
        fab.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.noColor));
        addView(fab);
    }

}
