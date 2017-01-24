package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

/**
 * Created by shreyashirday on 1/24/17.
 */
public class ImageHelper {

    public static Bitmap rotateImage(final String photoPath, final String TAG) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        try {
            final ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            final Matrix matrix = new Matrix();
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
        return bitmap;
    }

}
