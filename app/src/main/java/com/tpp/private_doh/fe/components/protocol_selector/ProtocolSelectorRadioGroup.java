package com.tpp.private_doh.fe.components.protocol_selector;

import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.fe.components.UnselectedProtocol;
import com.tpp.private_doh.controller.ProtocolId;

import java.util.ArrayList;
import java.util.Arrays;

public class ProtocolSelectorRadioGroup extends RadioGroup {

    private final String TAG = this.getClass().getSimpleName();
    ArrayList<ProtocolSelectorButton> RbIDtoProtocolID;

    public ProtocolSelectorRadioGroup(Context context) {
        super(new ContextThemeWrapper(context, R.style.AppTheme_Button));
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        RbIDtoProtocolID = new ArrayList<>(Arrays.asList(
                new ProtocolSelectorButton(context, ProtocolId.DOH, R.string.doh),
                new ProtocolSelectorButton(context, ProtocolId.DNS, R.string.dns),
                new ProtocolSelectorButton(context, ProtocolId.HYBRID, R.string.both)
        ));

        for (ProtocolSelectorButton button : RbIDtoProtocolID) {
            addView(button);
            button.setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));
        }
        RbIDtoProtocolID.get(0).setChecked(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        Log.i(TAG, String.format("this.getChildCount(): %s", this.getChildCount()));
        super.setEnabled(enabled);
        for (ProtocolSelectorButton button : RbIDtoProtocolID){
            button.setEnabled(enabled);
        }
    }

    public ProtocolId getProtocol() throws UnselectedProtocol {
        int selectedId = getCheckedRadioButtonId();
        if (selectedId == -1) {
            throw new UnselectedProtocol();
        }
        return ((ProtocolSelectorButton) findViewById(selectedId)).getProtocolId();
    }
}
