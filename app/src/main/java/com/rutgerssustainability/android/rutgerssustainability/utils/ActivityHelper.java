package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

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

    public static boolean getDeviceId(final Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.READ_PHONE_STATE};
            ActivityCompat.requestPermissions(activity,permissions,Constants.PERMISSIONS.RPS_REQUEST_CODE);
            return false;
        } else {
            final SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(activity);
            String deviceId = sharedPreferenceUtil.getDeviceId();
            if (deviceId == null) {
                final TelephonyManager tm = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getDeviceId();
                sharedPreferenceUtil.insertDeviceId(deviceId);
            }
            return true;
        }
    }

    public static boolean checkPermissionGranted(final int[] grantResults) {
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

}
