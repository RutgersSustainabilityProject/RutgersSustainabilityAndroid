package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.app.Activity;
import android.content.Intent;

import com.rutgerssustainability.android.rutgerssustainability.MainActivity;

/**
 * Created by shreyashirday on 1/31/17.
 */
public class ActivityHelper {

    public static void leave(final Activity activity) {
        final Intent back = new Intent(activity, MainActivity.class);
        activity.finish();
        activity.startActivity(back);
    }

}
