package com.tpp.private_doh.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioGroup;

import com.tpp.private_doh.R;
import com.tpp.private_doh.controller.ProtocolId;

import java.util.HashMap;
import java.util.Map;

public class ProtocolSelector extends RadioGroup {

    private final String TAG = this.getClass().getSimpleName();
    Map<Integer, ProtocolId> RbIDtoProtocolID = new HashMap<Integer, ProtocolId>()
    {
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
        for(int i = 0; i < getChildCount(); i++){
            getChildAt(i).setEnabled(enabled);
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
