package com.rutgerssustainability.android.rutgerssustainability;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.rutgerssustainability.android.rutgerssustainability.aws.AmazonService;
import com.rutgerssustainability.android.rutgerssustainability.db.DataSource;
import com.rutgerssustainability.android.rutgerssustainability.utils.ActivityHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;
import com.rutgerssustainability.android.rutgerssustainability.utils.NoiseHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.SharedPreferenceUtil;

import java.io.IOException;

public class AudioRecorderActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //constants
    private static final String TAG = "AudioRecorderActivity";
    private static final String RECORDING = "Recording";
    private static final String NOT_RECORDING = "Not Recording";
    private static final int REF_AMPLITUDE = 32767;

    //UI
    private Button startRecordingBtn;
    private Button startPlayingBtn;
    private Button sendAudioBtn;
    private TextView recordingStatusTxt;
    private TextView decibelValueTxt;
    private EditText tagsField;

    //media objects
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;

    //persistence
    private SharedPreferenceUtil sharedPreferenceUtil;
    private DataSource dataSource;

    //variables
    private String deviceId = null;
    private String fileName = null;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private double decibleMeasureCount = 0.0;
    private double totalDecibels = 0.0;
    private double avgDecibel = 0.0;

    //thread stuff for measuring db
    final Handler handler = new Handler();
    final NoiseHelper noiseHelper = new NoiseHelper();
    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            try {
                getDecibelsFromRecording();
            } finally {
                handler.postDelayed(updater, 500);
            }
        }
    };

    //API objects
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);
        startRecordingBtn = (Button)findViewById(R.id.start_recording_btn);
        startPlayingBtn = (Button)findViewById(R.id.start_playing_btn);
        sendAudioBtn = (Button)findViewById(R.id.send_audio_btn);
        sendAudioBtn.setEnabled(false);
        tagsField = (EditText)findViewById(R.id.tags_field_2);
        recordingStatusTxt = (TextView)findViewById(R.id.recording_status_txt);
        recordingStatusTxt.setTextColor(Color.RED);
        recordingStatusTxt.setText(NOT_RECORDING);
        decibelValueTxt = (TextView)findViewById(R.id.decibel_value_txt);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        dataSource = new DataSource(this);
        final boolean hasDeviceId = ActivityHelper.getDeviceId(this);
        final boolean hasAudioPermission = getAudioPermission();
        if (hasDeviceId) {
            deviceId = sharedPreferenceUtil.getDeviceId();
        }
        final boolean enable = hasDeviceId && hasAudioPermission;
        startRecordingBtn.setEnabled(enable);
        startRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
        startPlayingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    startPlaying();
                } else {
                    stopPlaying();
                }
            }
        });
        sendAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tags = tagsField.getText().toString();
                if (fileName == null) {
                    Toast.makeText(AudioRecorderActivity.this,"No recording available!",Toast.LENGTH_SHORT).show();
                } else {
                    new AmazonService(AudioRecorderActivity.this, fileName, tags, dataSource, deviceId, lastLocation,avgDecibel).execute();
                }
            }
        });
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final boolean granted = ActivityHelper.checkPermissionGranted(grantResults);
        switch (requestCode) {
            case Constants.PERMISSIONS.AUDIO_REQUEST_CODE:
                if (granted && deviceId != null) {
                    startRecordingBtn.setEnabled(true);
                }
                break;
            case Constants.PERMISSIONS.RPS_REQUEST_CODE:
                if (granted) {
                    deviceId = sharedPreferenceUtil.getDeviceId();
                    if (!startRecordingBtn.isEnabled() && getAudioPermission()) {
                        startRecordingBtn.setEnabled(true);
                    }
                }
                break;
            case Constants.PERMISSIONS.LOCATION_REQUEST_CODE:
                if (granted) {
                    getLastLocation();
                }
                break;
        }
        return;
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        fileName = getExternalCacheDir().getAbsolutePath() + "/" + deviceId + "_decibelMeasurement.3gp";
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (final IOException e) {
            Log.e(TAG,e.getMessage());
        }
        mediaRecorder.start();
        recordingStatusTxt.setTextColor(Color.GREEN);
        recordingStatusTxt.setText(RECORDING);
        isRecording = true;
        startRecordingBtn.setText(getString(R.string.stop_record_title));
        decibelValueTxt.setText("");
        avgDecibel = 0.0;
        updater.run();
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            recordingStatusTxt.setTextColor(Color.RED);
            recordingStatusTxt.setText(NOT_RECORDING);
            isRecording = false;
            startRecordingBtn.setText(getString(R.string.start_record_title));
            handler.removeCallbacks(updater);
            avgDecibel = totalDecibels / decibleMeasureCount;
            avgDecibel = Math.abs(avgDecibel);
            decibelValueTxt.setText("Average Decibel Value: " + avgDecibel + " dB");
        }
    }

    private void startPlaying() {
        mediaPlayer = new MediaPlayer();
        try {
            if (fileName != null) {
                mediaPlayer.setDataSource(fileName);
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying = true;
                startPlayingBtn.setText(getString(R.string.stop_playing_title));
            } else {
                Toast.makeText(this,"No recording available!",Toast.LENGTH_SHORT).show();
            }
        } catch (final IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            startPlayingBtn.setText(getString(R.string.start_playing_title));
        }
    }

    private boolean getAudioPermission() {
        if (ActivityCompat.checkSelfPermission(AudioRecorderActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(AudioRecorderActivity.this, permissions, Constants.PERMISSIONS.AUDIO_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void getDecibelsFromRecording() {
        if (mediaRecorder != null) {
            totalDecibels += noiseHelper.getDecibels(mediaRecorder, REF_AMPLITUDE);
            decibleMeasureCount++;
            Log.d(TAG,"total decibels = " + totalDecibels);
            Log.d(TAG, "measurement count = " + decibleMeasureCount);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions,Constants.PERMISSIONS.LOCATION_REQUEST_CODE);
            return;
        }
        else {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                if (deviceId != null) {
                    sendAudioBtn.setEnabled(true);
                } else {
                    final boolean deviceIdExists = ActivityHelper.getDeviceId(this);
                    if (deviceIdExists) {
                        deviceId = sharedPreferenceUtil.getDeviceId();
                        sendAudioBtn.setEnabled(true);
                    }
                }
            }
        }
    }

}
