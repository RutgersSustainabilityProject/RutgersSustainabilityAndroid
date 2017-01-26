package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

/**
 * Created by shreyashirday on 1/24/17.
 */
public class ImageHelper {

    public static int getDegreesToRotate(final String photoPath, final String TAG) {
        try {
            final ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == 6) {
                return 90;
            } else if (orientation == 3) {
                return 180;
            } else if (orientation == 8) {
                return 270;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return 0;
    }

    public static Bitmap rotateImage(final String photoPath, final String TAG, final SharedPreferenceUtil sharedPreferenceUtil) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        int degreesToRotate = getDegreesToRotate(photoPath, TAG);
        sharedPreferenceUtil.insertOrientationForImage(photoPath, degreesToRotate);
        if (degreesToRotate != 0) {
            final Matrix matrix = new Matrix();
            matrix.postRotate(degreesToRotate);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

}
