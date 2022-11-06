package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioGroup;

import com.tpp.private_doh.R;
import com.tpp.private_doh.controller.ProtocolId;

import java.util.ArrayList;
import java.util.Arrays;

public class ProtocolSelector extends RadioGroup {

    private final String TAG = this.getClass().getSimpleName();
    ArrayList<ProtocolSelectorButton> RbIDtoProtocolID;

    public ProtocolSelector(Context context, AttributeSet attrs) {
        super(context, attrs);

        RbIDtoProtocolID = new ArrayList<>(Arrays.asList(
                new ProtocolSelectorButton(context, ProtocolId.DOH, R.string.doh),
                new ProtocolSelectorButton(context, ProtocolId.DNS, R.string.dns),
                new ProtocolSelectorButton(context, ProtocolId.HYBRID, R.string.both)
        ));

        for (ProtocolSelectorButton button : RbIDtoProtocolID) {
            addView(button);
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
