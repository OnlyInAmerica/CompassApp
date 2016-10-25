package com.test.compassapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dbro on 10/24/16.
 */
public class Util {

    /**
     * Email {@param logFiles} to the Pearl Auto email destinations.
     * Note {@param logFiles} must be accessible to external processes
     */
    public static void emailLogs(@NonNull final Activity hostActivity,
                                 @NonNull String title,
                                 @NonNull String description,
                                 @NonNull final ArrayList<File> logFiles) {

        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{"david@kamama.com"});  // TODO : Add your emails here :)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Magnet Mount Detection Bug Report");
        emailIntent.putExtra(Intent.EXTRA_TEXT, title + '\n' + description);

        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : logFiles) {
            uris.add(Uri.fromFile(file));
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        hostActivity.startActivity(Intent.createChooser(emailIntent, "Send Magnet Mount Detection Bug Report..."));
    }
}
