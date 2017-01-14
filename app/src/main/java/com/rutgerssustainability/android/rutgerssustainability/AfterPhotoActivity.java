package com.rutgerssustainability.android.rutgerssustainability;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.okhttp.MediaType;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AfterPhotoActivity extends AppCompatActivity {

    private ImageView trashImageView;
    private Button sendPicBtn;
    private Button cancelBtn;
    private EditText tagsField;
    private static final String TAG = "AfterPhotoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_photo);
        trashImageView = (ImageView)findViewById(R.id.trash_pic_view);
        sendPicBtn = (Button)findViewById(R.id.send_pic_btn);
        cancelBtn = (Button)findViewById(R.id.cancel_btn);
        tagsField = (EditText)findViewById(R.id.tags_field);
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
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        trashImageView.setImageBitmap(bitmap);
        sendPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = tagsField.getText().toString();
                tags = processTags(tags);
                File file = new File(photoPath);
                RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),file);
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("trashPhoto", file.getName(), requestBody);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(AfterPhotoActivity.this,MainActivity.class);
                finish();
                startActivity(back);
            }
        });
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
