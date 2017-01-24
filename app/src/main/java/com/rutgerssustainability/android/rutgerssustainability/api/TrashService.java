package com.rutgerssustainability.android.rutgerssustainability.api;




import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.squareup.okhttp.ResponseBody;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by shreyashirday on 1/13/17.
 */
public interface TrashService {
    @Multipart
    @POST("/trash/postTrash")
    Call<Void> postTrash(@Part MultipartBody.Part trashPhoto, @Part MultipartBody.Part userId, @Part MultipartBody.Part latitude, @Part MultipartBody.Part longitude, @Part MultipartBody.Part epoch, @Part MultipartBody.Part tags);

    @GET("/trash/getTrashByUserId")
    Call<TrashWrapper> getTrash(@Query("userId") String userId);
}
