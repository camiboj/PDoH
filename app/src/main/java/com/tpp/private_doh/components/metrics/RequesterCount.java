package com.tpp.private_doh.components.metrics;

import android.content.Context;

public class RequesterCount extends RequesterOutputWithDescription {

    public RequesterCount(Context context, int count) {
        super(context, "Winning count",  String.valueOf(count), "Count descriptive message");
    }
}
