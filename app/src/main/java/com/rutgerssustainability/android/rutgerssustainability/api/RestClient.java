package com.rutgerssustainability.android.rutgerssustainability.api;



import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shreyashirday on 1/13/17.
 */
public class RestClient {

    private static final String LOCALHOST_URL = "http://192.168.1.17:3000";
    private static final String HEROKU_URL = "https://powerful-ocean-97485.herokuapp.com";

    private TrashService trashService;

    public RestClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HEROKU_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        trashService = retrofit.create(TrashService.class);
    }

    public TrashService getTrashService() {
        return trashService;
    }
}
