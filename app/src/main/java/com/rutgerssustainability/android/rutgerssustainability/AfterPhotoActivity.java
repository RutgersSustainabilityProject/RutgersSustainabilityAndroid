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
import com.rutgerssustainability.android.rutgerssustainability.aws.AmazonService;
import com.rutgerssustainability.android.rutgerssustainability.db.DataSource;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.rutgerssustainability.android.rutgerssustainability.utils.ActivityHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.AlertDialogHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;
import com.rutgerssustainability.android.rutgerssustainability.utils.ImageHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.SharedPreferenceUtil;

import java.io.File;
import java.sql.SQLException;


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
                new AmazonService(AfterPhotoActivity.this,photoPath,tags,dataSource, mDeviceId, mLastLocation,-1).execute();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.leave(AfterPhotoActivity.this);
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

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        final boolean granted = ActivityHelper.checkPermissionGranted(grantResults);
        switch (requestCode){
            case Constants.PERMISSIONS.LOCATION_REQUEST_CODE:
                if (granted) {
                    getLastLocation();
                }
                break;
            case Constants.PERMISSIONS.RPS_REQUEST_CODE:
                if (granted) {
                    final boolean deviceIdExists = ActivityHelper.getDeviceId(this);
                    if (deviceIdExists) {
                        mDeviceId = sharedPreferenceUtil.getDeviceId();
                        sendPicBtn.setEnabled(true);
                    }
                }
                break;
            default: break;
        }
        return;
    }

    @Override
    public void onConnected(final Bundle bundle) {
        getLastLocation();
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
                    final boolean deviceIdExists = ActivityHelper.getDeviceId(this);
                    if (deviceIdExists) {
                        mDeviceId = sharedPreferenceUtil.getDeviceId();
                        sendPicBtn.setEnabled(true);
                    }
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



}
