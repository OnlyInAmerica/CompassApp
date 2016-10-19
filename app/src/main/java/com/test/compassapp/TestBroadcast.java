package com.test.compassapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by chavi on 9/4/16.
 */
public class TestBroadcast extends BroadcastReceiver {
    public static final String ACTION_START = BuildConfig.APPLICATION_ID + "action.START";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_START:
                Intent startActivityIntent = new Intent(context, MainActivity.class);
                startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startActivityIntent);
                break;
        }
    }
}
