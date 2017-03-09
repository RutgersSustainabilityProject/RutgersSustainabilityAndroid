package com.rutgerssustainability.android.rutgerssustainability.api;

import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by shreyashirday on 3/9/17.
 */
public interface NoiseService {
    @Multipart
    @POST(Constants.API.POST_NOISE_ENDPOINT)
    Call<TrashWrapper> postNoise(@Part MultipartBody.Part audioUrl, @Part MultipartBody.Part userId, @Part MultipartBody.Part latitude, @Part MultipartBody.Part longitude, @Part MultipartBody.Part decibels, @Part MultipartBody.Part epoch, @Part MultipartBody.Part tags);

    @GET(Constants.API.GET_NOISE_ENDPOINT)
    Call<TrashWrapper> getNoise(@Query(Constants.API.USER_ID_KEY) String userId);
}
