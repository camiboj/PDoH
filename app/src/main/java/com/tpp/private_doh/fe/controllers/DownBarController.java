package com.tpp.private_doh.fe.controllers;

import android.content.Context;

public class DownBarController extends FEController{

    private final int w;
    private final int h;
    private final BodyController bodyController;

    public DownBarController(Context context, int w, int h, BodyController bodyController) {
        super(context);
        this.w = w;
        this.h = h;
        this.bodyController = bodyController;
    }

}
