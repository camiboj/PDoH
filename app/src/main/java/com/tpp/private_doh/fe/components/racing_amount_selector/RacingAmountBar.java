package com.tpp.private_doh.fe.components.racing_amount_selector;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;

public class RacingAmountBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private static final int RACING_AMOUNT_MIN = 0;
    private static int offset = 2;
    private ProgressOutput progressOutput;

    public RacingAmountBar(Context context, ProgressOutput progressOutput) {
        super(new ContextThemeWrapper(context, R.style.AppTheme_ProgressBar));
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.progressOutput = progressOutput;

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
        if (progressOutput!=null) {
            progressOutput.setText(String.valueOf(getCustomProgress()));
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
        progressOutput.setEnabled(enabled);
    }
}
