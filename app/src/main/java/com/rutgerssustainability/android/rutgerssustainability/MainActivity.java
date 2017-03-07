package com.rutgerssustainability.android.rutgerssustainability;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


import com.rutgerssustainability.android.rutgerssustainability.utils.ActivityHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;
import com.rutgerssustainability.android.rutgerssustainability.utils.SharedPreferenceUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button takePicBtn;
    private Button viewPicsBtn;
    private Button takeAudioBtn;
    private Switch deviceIdSwitch;
    private TextView deviceIdTxt;

    private static final String TAG = "MainActivity";
    private final static int REQUEST_TAKE_PHOTO = 1;

    private SharedPreferenceUtil sharedPreferenceUtil;

    String mCurrentPhotoPath = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePicBtn = (Button)findViewById(R.id.take_pic_btn);
        viewPicsBtn = (Button)findViewById(R.id.view_pics_btn);
        takeAudioBtn = (Button)findViewById(R.id.take_audio_btn);
        deviceIdSwitch = (Switch)findViewById(R.id.device_id_switch);
        deviceIdTxt = (TextView)findViewById(R.id.device_id_txt);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeTakePictureIntent();
            }
        });
        viewPicsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeViewPicturesIntent();
            }
        });
        takeAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeTakeAudioIntent();
            }
        });
        deviceIdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    final boolean deviceIdExists = ActivityHelper.getDeviceId(MainActivity.this);
                    if (deviceIdExists) {
                        final String deviceId = sharedPreferenceUtil.getDeviceId();
                        deviceIdTxt.setText(deviceId);
                    }
                } else {
                    deviceIdTxt.setText("");
                }
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            final Bundle extras = new Bundle();
            final Intent afterPhoto = new Intent(MainActivity.this, AfterPhotoActivity.class);
            extras.putString("path",mCurrentPhotoPath);
            afterPhoto.putExtras(extras);
            startActivity(afterPhoto);
        }

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        final boolean granted = ActivityHelper.checkPermissionGranted(grantResults);
        switch (requestCode){
            case Constants.PERMISSIONS.RPS_REQUEST_CODE:
                if (granted) {
                    final boolean deviceIdExists = ActivityHelper.getDeviceId(this);
                    if (deviceIdExists) {
                        final String deviceId = sharedPreferenceUtil.getDeviceId();
                        deviceIdTxt.setText(deviceId);
                    }
                }
                break;
            default: break;
        }
        return;
    }

    private void executeTakePictureIntent(){
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(MainActivity.this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG,ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(this,
                        "com.rutgerssustainability.android.fileprovider",
                      photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void executeTakeAudioIntent() {
        final Intent intent = new Intent(MainActivity.this, AudioRecorderActivity.class);
        startActivity(intent);
    }

    private void executeViewPicturesIntent() {
        final Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);
        startActivity(intent);
    }

    private File createImageFile(final Context context) throws IOException {
        // Create an image file name
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "JPEG_" + timeStamp + "_";
        final File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }


}
