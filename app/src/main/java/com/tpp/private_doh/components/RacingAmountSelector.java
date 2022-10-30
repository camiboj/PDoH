package com.tpp.private_doh.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;

public class RacingAmountSelector extends androidx.appcompat.widget.AppCompatSeekBar {
    private static final int RACING_AMOUNT_MIN = 0;
    private static int offset = 2;
    private int progressOutputId;

    public RacingAmountSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RacingAmountSelector );
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.RacingAmountSelector_text_id) {
                progressOutputId = a.getResourceId(attr, 0);
            }
        }
        a.recycle();

        setMin(RACING_AMOUNT_MIN);

        setOnChange();
    }


    public void setCustomMin(int i) {
        setMax(getMax() + Math.abs(offset-i));
        offset = i;
    }

    public void setCustomMax(int max) {
        int current = getCustomProgress();
        super.setMax(max - offset);
        setCustomProgress(current);
    }

    public int getCustomProgress() {
        return super.getProgress() + offset;
    }

    public void setCustomProgress(int i) {
        super.setProgress(i - offset);
    }

    private void onChange() {
        TextView t = getRootView().findViewById(progressOutputId);
        if (t!=null) {
            t.setText(String.valueOf(getCustomProgress()));
        }
    }

    private void setOnChange() {
        setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                onChange();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int thumbTint = enabled? R.color.colorPrimary : R.color.colorDisabled;
        int progressTint = enabled? R.color.colorPrimaryDark : R.color.colorDisabled;
        setThumbTintList(ContextCompat.getColorStateList(getContext(), thumbTint));
        setProgressTintList(ContextCompat.getColorStateList(getContext(), progressTint));
    }
}
