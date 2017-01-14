package com.rutgerssustainability.android.rutgerssustainability;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.rutgerssustainability.android.rutgerssustainability.api.RestClient;
import com.rutgerssustainability.android.rutgerssustainability.api.TrashService;
import com.squareup.okhttp.ResponseBody;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AfterPhotoActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //UI elements
    private ImageView trashImageView;
    private Button sendPicBtn;
    private Button cancelBtn;
    private EditText tagsField;

    //Constants
    private static final String TAG = "AfterPhotoActivity";
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int RPS_REQUEST_CODE = 2;

    //API objects
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //variables
    private String mDeviceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_photo);
        trashImageView = (ImageView) findViewById(R.id.trash_pic_view);
        sendPicBtn = (Button) findViewById(R.id.send_pic_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        tagsField = (EditText) findViewById(R.id.tags_field);
        final String photoPath = getIntent().getExtras().getString("path");
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        trashImageView.setImageBitmap(bitmap);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        sendPicBtn.setEnabled(false);
        sendPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = tagsField.getText().toString();
                tags = processTags(tags);
                File file = new File(photoPath);
                RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("trashPhoto", file.getName(), requestBody);
                MultipartBody.Part userIdPart = MultipartBody.Part.createFormData("userId", mDeviceId);
                MultipartBody.Part latitudePart = MultipartBody.Part.createFormData("latitude",String.valueOf(mLastLocation.getLatitude()));
                MultipartBody.Part longitudePart = MultipartBody.Part.createFormData("longitude", String.valueOf(mLastLocation.getLongitude()));
                long epoch = System.currentTimeMillis();
                MultipartBody.Part epochPart = MultipartBody.Part.createFormData("epoch", String.valueOf(epoch));
                MultipartBody.Part tagsPart = MultipartBody.Part.createFormData("tags", tags);
                RestClient restClient = new RestClient();
                TrashService trashService = restClient.getTrashService();
                Call<ResponseBody> call = trashService.postTrash(imagePart,userIdPart,latitudePart,longitudePart,epochPart,tagsPart);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d(TAG,"Call Success!");
                        Log.d(TAG,"Response message: " + response.message());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG,"Call failed: " + t.getMessage());
                    }
                });
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(AfterPhotoActivity.this, MainActivity.class);
                finish();
                startActivity(back);
            }
        });
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = checkPermissionGranted(grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (granted) {
                    getLastLocation();
                }
                break;
            case RPS_REQUEST_CODE:
                if (granted) {
                    getDeviceId();
                }
                break;
            default: break;
        }
        return;
    }

    private boolean checkPermissionGranted(int[] grantResults) {
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLastLocation();
    }

    private void getDeviceId() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.READ_PHONE_STATE};
            ActivityCompat.requestPermissions(this,permissions,RPS_REQUEST_CODE);
            return;
        } else {
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceId = tm.getDeviceId();
            sendPicBtn.setEnabled(true);
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions,LOCATION_REQUEST_CODE);
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
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG,"Connection Failed: " + connectionResult.getErrorMessage());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, LOCATION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,e.getMessage());
            }
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this,connectionResult.getErrorCode(),LOCATION_REQUEST_CODE).show();
        }
    }

    private String processTags(String tags) {
        String[] tagArray = tags.split(",");
        int len = tagArray.length;
        for (int i = 0; i < len; i++) {
            String tag = tagArray[i];
            tag = tag.replaceAll("\\s+","");
            tagArray[i] = tag;
        }
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String tag = tagArray[i];
            tagBuilder.append(tag);
            if (i != tagArray.length - 1) {
                tagBuilder.append(",");
            }
        }
        String processedTags = tagBuilder.toString();
        return processedTags;
    }


}
