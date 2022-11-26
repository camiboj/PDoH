package com.tpp.private_doh.fe.controllers;

import android.content.Context;

import com.tpp.private_doh.R;

public class ScreenController extends FEController{
    private final int w;
    private final int h;
    private final DownBarController downBarController;
    private final BodyController bodyController;

    public ScreenController(Context context, int w, int h) {
        super(context);
        this.w = w;
        this.h = h;
        bodyController = new BodyController(context, w, h - 2 * R.attr.actionBarSize, R.attr.actionBarSize);
        downBarController = new DownBarController(context, w, R.attr.actionBarSize, bodyController);
    }
}
