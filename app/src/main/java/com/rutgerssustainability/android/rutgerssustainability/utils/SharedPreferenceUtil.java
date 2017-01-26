package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by shreyashirday on 1/26/17.
 */
public class SharedPreferenceUtil {

    //shared preferences
    private SharedPreferences sharedPreferences;

    //constants
    private static final String DEVICE_ID_KEY = "deviceId";

    public SharedPreferenceUtil(final Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

    public void insertDeviceId(final String deviceID) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(DEVICE_ID_KEY, deviceID);
        editor.commit();
    }

    public String getDeviceId() {
        return sharedPreferences.getString(DEVICE_ID_KEY,null);
    }

    //NOTE: Orientation is actually degrees to rotate
    public void insertOrientationForImage(final String filename, final int orientation) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putInt(filename,orientation);
        editor.commit();
    }

    public int getOrientationForImage(final String filename) {
        return sharedPreferences.getInt(filename, 0);
    }
}


