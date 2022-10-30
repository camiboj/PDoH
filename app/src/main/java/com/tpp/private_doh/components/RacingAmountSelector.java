package com.tpp.private_doh.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tpp.private_doh.R;

public class RacingAmountSelector extends androidx.appcompat.widget.AppCompatSeekBar {
    private static final int RACING_AMOUNT_MIN = 0;
    private static int offset = 0;
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


    @Override
    public void setMin(int i) {
        setMax(getMax() + Math.abs(offset-i));
        offset = i;
    }

    @Override
    public void setMax(int max) {
        int current = getProgress();
        super.setMax(max - offset);
        setProgress(current);
    }

    @Override
    public int getProgress() {
        return super.getProgress() + offset;
    }


    @Override
    public void setProgress(int i) {
        super.setProgress(i - offset);
    }

    private void onChange() {
        TextView t = getRootView().findViewById(progressOutputId);
        if (t!=null) {
            t.setText(String.valueOf(getProgress()));
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



}
