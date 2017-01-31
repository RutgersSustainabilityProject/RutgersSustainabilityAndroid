package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;


/**
 * Created by shreyashirday on 1/31/17.
 */
public class AlertDialogHelper {

    private final static String OKAY_BTN_TITLE = "Okay!";

    public static AlertDialog createAlertDialog(final Activity activity, final String title, final String message, final boolean leave) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(title).setPositiveButton(OKAY_BTN_TITLE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (leave) {
                    ActivityHelper.leave(activity);
                }
            }
        }).create();
        if (message != null) {
            builder.setMessage(message);
        }
        return builder.create();
    }


}


