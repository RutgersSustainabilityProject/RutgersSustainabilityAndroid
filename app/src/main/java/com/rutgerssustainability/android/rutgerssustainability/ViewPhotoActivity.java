package com.rutgerssustainability.android.rutgerssustainability;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.rutgerssustainability.android.rutgerssustainability.adapter.TrashListAdapter;
import com.rutgerssustainability.android.rutgerssustainability.api.RestClient;
import com.rutgerssustainability.android.rutgerssustainability.api.TrashService;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;
import com.rutgerssustainability.android.rutgerssustainability.utils.SharedPreferenceUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewPhotoActivity extends AppCompatActivity {

    //list related objects
    private ListView trashListView;
    private TrashListAdapter trashListAdapter;

    //variables
    private String mDeviceId;

    //constants
    private static final String TAG = "ViewPhotoActivity";

    //utils
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        trashListView = (ListView)findViewById(R.id.trash_list);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        mDeviceId = sharedPreferenceUtil.getDeviceId();
        triggerList();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        final boolean granted = checkPermissionGranted(grantResults);
        switch (requestCode){
            case Constants.PERMISSIONS.RPS_REQUEST_CODE:
                if (granted) {
                    getDeviceId();
                }
                break;
            default: break;
        }
        return;
    }

    private void triggerList() {
        getDeviceId();
    }

    private void getTrashRequest() {
        final RestClient restClient = new RestClient();
        final TrashService trashService =  restClient.getTrashService();
        final Call<TrashWrapper> call = trashService.getTrash(mDeviceId);
        call.enqueue(new Callback<TrashWrapper>() {
            @Override
            public void onResponse(final Call<TrashWrapper> call, final Response<TrashWrapper> response) {
                final TrashWrapper trashWrapper = response.body();
                final Trash[] trash = trashWrapper.getTrash();
                trashListAdapter = new TrashListAdapter(trash,ViewPhotoActivity.this);
                trashListView.setAdapter(trashListAdapter);
            }

            @Override
            public void onFailure(final Call<TrashWrapper> call, final Throwable t) {
                Log.e(TAG,"Call error:" + t.getMessage());
            }
        });
    }

    private boolean checkPermissionGranted(final int[] grantResults) {
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
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
            }
            getTrashRequest();
        }
    }
}
