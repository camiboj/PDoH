package com.tpp.private_doh.components;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;

public class CustomButton extends androidx.appcompat.widget.AppCompatButton {

    public CustomButton(Context context, String text, ViewGroup.MarginLayoutParams marginLayoutParams) {
        super(new ContextThemeWrapper(context, R.style.AppTheme_Button), null, R.style.AppTheme_Button);

        this.setLayoutParams(marginLayoutParams);
        this.setText(text);
        this.setGravity(Gravity.CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        int drawable = enabled ? R.drawable.button : R.drawable.unselected_button;
        this.setBackground(ContextCompat.getDrawable(getContext(), drawable));
    }
}
