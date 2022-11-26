package com.tpp.private_doh.fe.components.metrics;

import android.content.Context;

public class RequesterCount extends RequesterOutputWithDescription {

    public RequesterCount(Context context, int count) {
        super(context, "Winning count",  String.valueOf(count), "The amount of times the provider won the race of response time against other providers");
    }
}
