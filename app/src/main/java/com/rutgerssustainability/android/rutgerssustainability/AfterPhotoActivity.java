package com.rutgerssustainability.android.rutgerssustainability;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.rutgerssustainability.android.rutgerssustainability.api.RestClient;
import com.rutgerssustainability.android.rutgerssustainability.api.TrashService;
import com.rutgerssustainability.android.rutgerssustainability.aws.AWSHelper;
import com.rutgerssustainability.android.rutgerssustainability.db.DataSource;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.rutgerssustainability.android.rutgerssustainability.utils.ActivityHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.AlertDialogHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;
import com.rutgerssustainability.android.rutgerssustainability.utils.ImageHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.SharedPreferenceUtil;

import java.io.File;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AfterPhotoActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //UI elements
    private ImageView trashImageView;
    private Button sendPicBtn;
    private Button cancelBtn;
    private EditText tagsField;

    //Constants
    private static final String TAG = "AfterPhotoActivity";

    //API objects
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //variables
    private String mDeviceId = null;

    //utils
    private SharedPreferenceUtil sharedPreferenceUtil;
    private DataSource dataSource;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_photo);
        trashImageView = (ImageView) findViewById(R.id.trash_pic_view);
        sendPicBtn = (Button) findViewById(R.id.send_pic_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        tagsField = (EditText) findViewById(R.id.tags_field);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        dataSource = new DataSource(this);
        mDeviceId = sharedPreferenceUtil.getDeviceId();
        final String photoPath = getIntent().getExtras().getString("path");
        final Bitmap bitmap = ImageHelper.rotateImage(photoPath, TAG, sharedPreferenceUtil);
        trashImageView.setImageBitmap(bitmap);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        sendPicBtn.setEnabled(false);
        final ProgressDialog dialog = new ProgressDialog(AfterPhotoActivity.this);
        sendPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = tagsField.getText().toString();
                new AmazonService(getApplicationContext(),photoPath,tags).execute();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.leave(AfterPhotoActivity.this);
            }
        });
    }

    private class AmazonService extends AsyncTask<String, Boolean, Boolean> {
        Context mContext;
        String mPhotoPath;
        String mTags;
        boolean go = false;
        public AmazonService(final Context context, final String photoPath, final String tags) {
            mContext = context;
            mPhotoPath = photoPath;
            mTags = tags;
        }

        @Override
        protected Boolean doInBackground(final String ... params) {
            final String newTags = processTags(mTags);
            final File file = new File(mPhotoPath);
            final TransferUtility transferUtility = AWSHelper.getTransferUtility(getApplicationContext());
            Log.d(TAG, "file size: " + file.length());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ProgressDialog progressDialog = new ProgressDialog(AfterPhotoActivity.this);
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
                                final MultipartBody.Part imagePart = MultipartBody.Part.createFormData(Constants.API.PICTURE_KEY, fileS3Url);
                                final MultipartBody.Part userIdPart = MultipartBody.Part.createFormData(Constants.API.USER_ID_KEY, mDeviceId);
                                final MultipartBody.Part latitudePart = MultipartBody.Part.createFormData(Constants.API.LAT_KEY, String.valueOf(mLastLocation.getLatitude()));
                                final MultipartBody.Part longitudePart = MultipartBody.Part.createFormData(Constants.API.LON_KEY, String.valueOf(mLastLocation.getLongitude()));
                                long epoch = System.currentTimeMillis();
                                final MultipartBody.Part epochPart = MultipartBody.Part.createFormData(Constants.API.EPOCH_KEY, String.valueOf(epoch));
                                final MultipartBody.Part tagsPart = MultipartBody.Part.createFormData(Constants.API.TAGS_KEY, newTags);
                                final RestClient restClient = new RestClient();
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
                                        if (!dataSource.hasTrash(trash)) {
                                            dataSource.addTrash(trash);
                                        }
                                        final AlertDialog dialog = AlertDialogHelper.createAlertDialog(AfterPhotoActivity.this,"Trash posted!",null,true);
                                        dialog.show();
                                    }

                                    @Override
                                    public void onFailure(final Call<TrashWrapper> call, final Throwable t) {
                                        Log.e(TAG, "Call failed: " + t.getMessage());
                                        progressDialog.dismiss();
                                    }
                                });
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

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        final boolean granted = checkPermissionGranted(grantResults);
        switch (requestCode){
            case Constants.PERMISSIONS.LOCATION_REQUEST_CODE:
                if (granted) {
                    getLastLocation();
                }
                break;
            case Constants.PERMISSIONS.RPS_REQUEST_CODE:
                if (granted) {
                    getDeviceId();
                }
                break;
            default: break;
        }
        return;
    }

    private boolean checkPermissionGranted(final int[] grantResults) {
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnected(final Bundle bundle) {
        getLastLocation();
    }

    private void getDeviceId() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.READ_PHONE_STATE};
            ActivityCompat.requestPermissions(this,permissions,Constants.PERMISSIONS.RPS_REQUEST_CODE);
            return;
        } else {
            if (mDeviceId == null) {
                final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                mDeviceId = tm.getDeviceId();
                sharedPreferenceUtil.insertDeviceId(mDeviceId);
            }
            sendPicBtn.setEnabled(true);
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions,Constants.PERMISSIONS.LOCATION_REQUEST_CODE);
            return;
        }
        else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                if (mDeviceId != null) {
                    sendPicBtn.setEnabled(true);
                } else {
                    getDeviceId();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.e(TAG,"Connection Suspended");
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        Log.e(TAG,"Connection Failed: " + connectionResult.getErrorMessage());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, Constants.PERMISSIONS.LOCATION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,e.getMessage());
            }
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this,connectionResult.getErrorCode(),Constants.PERMISSIONS.LOCATION_REQUEST_CODE).show();
        }
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
