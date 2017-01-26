package com.rutgerssustainability.android.rutgerssustainability.api;



import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shreyashirday on 1/13/17.
 */
public class RestClient {

    private TrashService trashService;

    public RestClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API.HEROKU_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        trashService = retrofit.create(TrashService.class);
    }

    public TrashService getTrashService() {
        return trashService;
    }
}
