package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tpp.private_doh.R;

public class RequesterOutput extends androidx.appcompat.widget.LinearLayoutCompat {

    public RequesterOutput(Context context, String text, String descriptiveMessage, LinearLayout.LayoutParams layoutParams, int style) {
        super(context);
        setLayoutParams(layoutParams);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        TextView tv = new TextView(new ContextThemeWrapper(context, style));
        tv.setText(text);
        addView(tv);

        if (descriptiveMessage == null || descriptiveMessage.isEmpty()) {
            setDescription(context, descriptiveMessage);
        }
    }

    private void setDescription(Context context, String descriptiveMessage) {
        FloatingActionButton fab = new FloatingActionButton(context);
        fab.setTooltipText(descriptiveMessage);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.question, context.getTheme()));
        //fab.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.noColor));
        addView(fab);
    }
}
