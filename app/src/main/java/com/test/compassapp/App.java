package com.test.compassapp;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by dbro on 10/24/16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
