package com.rutgerssustainability.android.rutgerssustainability;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.rutgerssustainability.android.rutgerssustainability.adapter.TrashListAdapter;
import com.rutgerssustainability.android.rutgerssustainability.api.RestClient;
import com.rutgerssustainability.android.rutgerssustainability.api.TrashService;
import com.rutgerssustainability.android.rutgerssustainability.db.DataSource;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.pojos.TrashWrapper;
import com.rutgerssustainability.android.rutgerssustainability.utils.ActivityHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.AlertDialogHelper;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;
import com.rutgerssustainability.android.rutgerssustainability.utils.SharedPreferenceUtil;

import java.sql.SQLException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPhotoActivity extends AppCompatActivity {

    //list related objects
    private ListView trashListView;
    private TrashListAdapter trashListAdapter;

    //variables
    private String mDeviceId;

    //constants
    private static final String TAG = "ViewPhotoActivity";

    //persistence
    private SharedPreferenceUtil sharedPreferenceUtil;
    private DataSource dataSource;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        trashListView = (ListView)findViewById(R.id.trash_list);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        dataSource = new DataSource(this);
        mDeviceId = sharedPreferenceUtil.getDeviceId();
        triggerList();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        final boolean granted = ActivityHelper.checkPermissionGranted(grantResults);
        switch (requestCode){
            case Constants.PERMISSIONS.RPS_REQUEST_CODE:
                if (granted) {
                    final boolean deviceIdExists = ActivityHelper.getDeviceId(this);
                    if (deviceIdExists) {
                        mDeviceId = sharedPreferenceUtil.getDeviceId();
                        getTrashRequest();
                    }
                }
                break;
            default: break;
        }
        return;
    }

    private void triggerList() {
        final boolean deviceIdExists = ActivityHelper.getDeviceId(this);
        if (deviceIdExists) {
            mDeviceId = sharedPreferenceUtil.getDeviceId();
            getTrashRequest();
        }
    }

    private void getTrashRequest() {
        final RestClient restClient = new RestClient();
        final TrashService trashService =  restClient.getTrashService();
        Log.d(TAG,"device id = " + mDeviceId);
        final Call<TrashWrapper> call = trashService.getTrash(mDeviceId);
        Log.d(TAG,"enqueing call");
        call.enqueue(new Callback<TrashWrapper>() {
            @Override
            public void onResponse(final Call<TrashWrapper> call, final Response<TrashWrapper> response) {
                Log.d(TAG, "onResponse called");
                final TrashWrapper trashWrapper = response.body();
                Trash[] trash = trashWrapper.getTrash();
                Log.d(TAG, "entering if statement");
                if (trash == null || trash.length < 1) {
                    Log.d(TAG, "no trash received from server, using local data");
                    trash = getTrashFromDB();
                } else {
                    Log.d(TAG, "trash received from server");
                    try {
                        Log.d(TAG, "dataSource opening");
                        dataSource.open();
                        Log.d(TAG, "dataSource opened");
                        dataSource.deleteTrashTable();
                        Log.d(TAG, "adding objects to local data");
                        for (final Trash trashObj : trash) {
                            dataSource.addTrash(trashObj);
                        }
                        dataSource.close();
                    } catch (final SQLException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                if (trash != null) {
                    Log.d(TAG, "Trash is NOT null");
                    trashListAdapter = new TrashListAdapter(trash, ViewPhotoActivity.this);
                    trashListView.setAdapter(trashListAdapter);
                } else {
                    Log.e(TAG, "Trash is null");
                    createErrorDialog(null);
                }

            }

            @Override
            public void onFailure(final Call<TrashWrapper> call, final Throwable t) {
                Log.e(TAG, "Call error:" + t.getMessage());
                final Trash[] trash = getTrashFromDB();
                if (trash != null) {
                    trashListAdapter = new TrashListAdapter(trash, ViewPhotoActivity.this);
                    trashListView.setAdapter(trashListAdapter);
                } else {
                    createErrorDialog(t);
                }
            }
        });
    }

    private void createErrorDialog(final Throwable t) {
        final String dialogTitle = "Oops!";
        final StringBuilder errorMsgBuilder = new StringBuilder();
        errorMsgBuilder.append("There was an error");
        if (t == null) {
            errorMsgBuilder.append("!");
        } else {
            errorMsgBuilder.append(": " + t.getMessage());
        }
        final String errorMsg = errorMsgBuilder.toString();
        final AlertDialog dialog = AlertDialogHelper.createAlertDialog(ViewPhotoActivity.this, dialogTitle, errorMsg, false);
        dialog.show();
    }

    private Trash[] getTrashFromDB() {
        final List<Trash> trashList = dataSource.getAllTrash();
        final Trash[] trashs = trashList.toArray(new Trash[trashList.size()]);
        return trashs;
    }



}
