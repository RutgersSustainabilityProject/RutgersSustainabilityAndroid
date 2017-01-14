package com.rutgerssustainability.android.rutgerssustainability.api;


import android.database.Observable;

import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by shreyashirday on 1/13/17.
 */
public interface TrashService {
    @Multipart
    @POST("/trash/postTrash")
    Observable<Response> postTrash(@Part MultipartBody.Part trashPhoto, @Part MultipartBody.Part userId, @Part MultipartBody.Part latitude, @Part MultipartBody.Part longitude, @Part MultipartBody.Part epoch, @Part MultipartBody.Part tags);
}
