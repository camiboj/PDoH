package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.content.ContextCompat;

import com.tpp.private_doh.R;
import com.tpp.private_doh.controller.ProtocolId;

import java.util.HashMap;
import java.util.Map;

public class ProtocolSelector extends RadioGroup {

    private final String TAG = this.getClass().getSimpleName();
    Map<Integer, ProtocolId> RbIDtoProtocolID = new HashMap<Integer, ProtocolId>() {
        {
            put(R.id.rbDoH, ProtocolId.DOH);
            put(R.id.rbDNS, ProtocolId.DNS);
            put(R.id.rbBoth, ProtocolId.HYBRID);
        }
    };

    public ProtocolSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        Log.i(TAG, String.format("this.getChildCount(): %s", this.getChildCount()));
        super.setEnabled(enabled);
        int color = enabled? R.color.colorPrimary : R.color.colorDisabled;
        for (int i = 0; i < getChildCount(); i++) {
            RadioButton rb = (RadioButton) getChildAt(i);
            rb.setEnabled(enabled);
            rb.setButtonTintList(ContextCompat.getColorStateList(getContext(), color));
        }
    }

    public ProtocolId getProtocol() throws UnselectedProtocol {
        int selectedId = getCheckedRadioButtonId();
        if (selectedId == -1) {
            throw new UnselectedProtocol();
        }
        return RbIDtoProtocolID.get(selectedId);
    }
}
