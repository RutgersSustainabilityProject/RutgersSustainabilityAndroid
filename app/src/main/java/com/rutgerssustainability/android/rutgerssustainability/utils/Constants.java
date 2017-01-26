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

    public static class API {
        //url bases
        public static final String LOCALHOST_BASE = "http://192.168.1.17:3000";
        public static final String HEROKU_BASE = "https://powerful-ocean-97485.herokuapp.com";

        //endpoints
        public static final String POST_TRASH_ENDPOINT = "/trash/postTrash";
        public static final String GET_TRASH_ENDPOINT = "/trash/getTrashByUserId";

        //keys
        public static final String PICTURE_KEY = "trashPhoto";
        public static final String USER_ID_KEY = "userId";
        public static final String LAT_KEY = "latitude";
        public static final String LON_KEY = "longitude";
        public static final String EPOCH_KEY = "epoch";
        public static final String TAGS_KEY = "tags";
    }

}
