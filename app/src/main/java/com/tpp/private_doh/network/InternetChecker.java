package com.tpp.private_doh.network;

import android.util.Log;

import com.tpp.private_doh.config.Config;

import java.util.concurrent.Callable;

public class InternetChecker implements Runnable {
    private final String TAG = this.getClass().getSimpleName();
    private final Callable<Boolean> checkInternet;
    private final Runnable stopVpnInternet;

    public InternetChecker(Callable<Boolean> checkInternet, Runnable stopVpnInternet) {
        this.checkInternet = checkInternet;
        this.stopVpnInternet = stopVpnInternet;
    }

    @Override
    public void run() {
        boolean internetOn = checkInternet();
        while (internetOn) {
            Log.i(TAG, "We have internet!");
            internetOn = checkInternet();
            sleepThread();
        }
        Log.e(TAG, "We detected that we ran out of internet");
        stopVpnInternet.run();
    }

    private void sleepThread() {
        try {
            Thread.sleep(Config.INTERNET_PERIOD);
        } catch (InterruptedException e) {
        }
    }

    private boolean checkInternet() {
        try {
            return checkInternet.call();
        } catch (Exception e) {
            Log.w(TAG, "This shouldn't happen");
            return false;
        }
    }
}
