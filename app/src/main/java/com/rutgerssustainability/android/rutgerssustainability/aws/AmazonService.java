package com.rutgerssustainability.android.rutgerssustainability.aws;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.rutgerssustainability.android.rutgerssustainability.AfterPhotoActivity;
import com.rutgerssustainability.android.rutgerssustainability.api.NoiseService;
import com.rutgerssustainability.android.rutgerssustainability.api.RestClient;
import com.rutgerssustainability.android.rutgerssustainability.api.TrashService;
import com.rutgerssustainability.android.rutgerssustainability.db.DataSource;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Noise;
import com.rutgerssustainability.android.rutgerssustainability.pojos.NoiseWrapper;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.rutgerssustainability.android.rutgerssustainability.utils.AlertDialogHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

import java.io.File;
import java.sql.SQLException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by shreyashirday on 3/18/17.
 */

public class AmazonService extends AsyncTask<String,Boolean,Boolean> {
    private Activity mContext;
    private String mFilePath;
    private String mTags;
    private DataSource mDataSource;
    private Location mLastLocation;
    private static final String TAG = "AmazonService";
    private String mDeviceId;
    private double mDecibels;
    boolean go = false;
    public AmazonService(final Activity context, final String filePath, final String tags, final DataSource dataSource, final String deviceId, final Location location, final double decibels) {
        mContext = context;
        mFilePath = filePath;
        mTags = tags;
        mDataSource = dataSource;
        mDeviceId = deviceId;
        mLastLocation = location;
        mDecibels = decibels;
    }

    @Override
    protected Boolean doInBackground(final String ... params) {
        final String newTags = processTags(mTags);
        final File file = new File(mFilePath);
        final TransferUtility transferUtility = AWSHelper.getTransferUtility(mContext.getApplicationContext());
        Log.d(TAG, "file size: " + file.length());
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle("Uploading...");
                progressDialog.setMax(100);

                final TransferObserver transferObserver = transferUtility.upload(Constants.AWS.BUCKET_NAME,
                        file.getName(),
                        file,
                        CannedAccessControlList.PublicRead);
                transferObserver.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(final int id, final TransferState state) {
                        Log.d(TAG, "State: " + state.name());
                        if (state == TransferState.COMPLETED) {
                            progressDialog.setIndeterminate(true);
                            progressDialog.setTitle("Saving...");
                            final String fileS3Url = AWSHelper.createS3FileUrl(file.getName());
                            Log.d(TAG, "s3 Url: " + fileS3Url);
                            final MultipartBody.Part userIdPart = MultipartBody.Part.createFormData(Constants.API.USER_ID_KEY, mDeviceId);
                            final MultipartBody.Part latitudePart = MultipartBody.Part.createFormData(Constants.API.LAT_KEY, String.valueOf(mLastLocation.getLatitude()));
                            final MultipartBody.Part longitudePart = MultipartBody.Part.createFormData(Constants.API.LON_KEY, String.valueOf(mLastLocation.getLongitude()));
                            long epoch = System.currentTimeMillis();
                            final MultipartBody.Part epochPart = MultipartBody.Part.createFormData(Constants.API.EPOCH_KEY, String.valueOf(epoch));
                            final MultipartBody.Part tagsPart = MultipartBody.Part.createFormData(Constants.API.TAGS_KEY, newTags);
                            final RestClient restClient = new RestClient();
                            if (mDecibels < 0) {
                                final MultipartBody.Part imagePart = MultipartBody.Part.createFormData(Constants.API.PICTURE_KEY, fileS3Url);
                                final TrashService trashService = restClient.getTrashService();
                                final Call<TrashWrapper> call = trashService.postTrash(imagePart, userIdPart, latitudePart, longitudePart, epochPart, tagsPart);
                                call.enqueue(new Callback<TrashWrapper>() {
                                    @Override
                                    public void onResponse(final Call<TrashWrapper> call, final Response<TrashWrapper> response) {
                                        Log.d(TAG, "Call Success!");
                                        Log.d(TAG, "Response message: " + response.message());
                                        progressDialog.dismiss();
                                        final TrashWrapper trashWrapper = response.body();
                                        final Trash[] trashArray = trashWrapper.getTrash();
                                        final Trash trash = trashArray[0];
                                        try {
                                            mDataSource.open();
                                            if (!mDataSource.hasTrash(trash)) {
                                                mDataSource.addTrash(trash);
                                            }
                                            mDataSource.close();
                                        } catch (final SQLException e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                        final AlertDialog dialog = AlertDialogHelper.createAlertDialog(mContext, "Posted!", null, true);
                                        dialog.show();
                                    }

                                    @Override
                                    public void onFailure(final Call<TrashWrapper> call, final Throwable t) {
                                        Log.e(TAG, "Call failed: " + t.getMessage());
                                        progressDialog.dismiss();
                                    }
                                });
                            } else {
                                final MultipartBody.Part audioPart = MultipartBody.Part.createFormData(Constants.API.AUDIO_KEY, fileS3Url);
                                final MultipartBody.Part decibelPart = MultipartBody.Part.createFormData(Constants.API.DECIBEL_KEY, String.valueOf(mDecibels));
                                final NoiseService noiseService = restClient.getNoiseService();
                                final Call<NoiseWrapper> call = noiseService.postNoise(audioPart, userIdPart, latitudePart, longitudePart, decibelPart, epochPart, tagsPart);
                                call.enqueue(new Callback<NoiseWrapper>() {
                                    @Override
                                    public void onResponse(Call<NoiseWrapper> call, Response<NoiseWrapper> response) {
                                        progressDialog.dismiss();
                                        final AlertDialog dialog = AlertDialogHelper.createAlertDialog(mContext, "Posted!", null, true);
                                        dialog.show();
                                    }
                                    @Override
                                    public void onFailure(Call<NoiseWrapper> call, Throwable t) {
                                        Log.e(TAG, "Call failed: " + t.getMessage());
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onProgressChanged(final int id, final long bytesCurrent, final long bytesTotal) {
                        final double percentage = (double) bytesCurrent / (double) bytesTotal;
                        final int points = (int) (percentage * 100);
                        progressDialog.setProgress(points);
                        if (!go) {
                            progressDialog.show();
                            go = true;
                        }
                        Log.d(TAG, "bytesCurrent: " + bytesCurrent);
                    }

                    @Override
                    public void onError(final int id, final Exception ex) {
                        Log.e(TAG, "AWS ERROR: " + ex.getMessage());
                    }
                });

            }
        });

        return true;
    }


    private String processTags(final String tags) {
        final String[] tagArray = tags.split(",");
        final int len = tagArray.length;
        for (int i = 0; i < len; i++) {
            String tag = tagArray[i];
            tag = tag.replaceAll("\\s+","");
            tagArray[i] = tag;
        }
        final StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final String tag = tagArray[i];
            tagBuilder.append(tag);
            if (i != tagArray.length - 1) {
                tagBuilder.append(",");
            }
        }
        final String processedTags = tagBuilder.toString();
        return processedTags;
    }

}
