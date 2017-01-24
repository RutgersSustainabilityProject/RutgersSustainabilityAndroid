package com.rutgerssustainability.android.rutgerssustainability.utils;

/**
 * Created by shreyashirday on 1/19/17.
 */
public class Constants {

    public static class AWS {
        public static final String BUCKET_NAME = "rusustainability";
        public static final String BUCKET_URL = "http://" + BUCKET_NAME + ".s3.amazonaws.com";
    }

    public static class PERMISSIONS {
        public static final int LOCATION_REQUEST_CODE = 1;
        public static final int RPS_REQUEST_CODE = 2;
    }

}
