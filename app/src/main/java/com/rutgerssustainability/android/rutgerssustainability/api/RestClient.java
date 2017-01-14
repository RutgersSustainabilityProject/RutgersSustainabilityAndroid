package com.rutgerssustainability.android.rutgerssustainability.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shreyashirday on 1/13/17.
 */
public class RestClient {

    private static final String BASE_URL = "http://localhost:3000";

    private TrashService trashService;

    public RestClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        trashService = retrofit.create(TrashService.class);
    }

    public TrashService getTrashService() {
        return trashService;
    }
}
