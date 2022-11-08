package com.tpp.private_doh.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tpp.private_doh.config.Config;

import java.util.concurrent.Callable;

public class InternetChecker extends Service implements Runnable {
    private final String TAG = this.getClass().getSimpleName();
    private final Callable<Boolean> checkInternet;

    public InternetChecker(Callable<Boolean> checkInternet) {
        this.checkInternet = checkInternet;
    }

    @Override
    public void run() {
        boolean internetOn = checkInternet();
        while (internetOn) {
            internetOn = checkInternet();
            sleepThread();
        }
        Log.e(TAG, "We detected that we ran out of internet");

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Config.STOP_SIGNAL_FOR_INTERNET);
        localBroadcastManager.sendBroadcast(intent);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
