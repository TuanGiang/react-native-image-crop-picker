package com.yalantis.ucrop.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ipusic on 12/27/16.
 */

public class Compression {

    public  static File resize(String originalImagePath, int maxWidth, int maxHeight, int quality) throws IOException {
        Bitmap original = BitmapFactory.decodeFile(originalImagePath);

        int width = original.getWidth();
        int height = original.getHeight();

        // Use original image exif orientation data to preserve image orientation for the resized bitmap
        ExifInterface originalExif = new ExifInterface(originalImagePath);
        int originalOrientation = originalExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

        Matrix rotationMatrix = new Matrix();
        int rotationAngleInDegrees = getRotationInDegreesForOrientationTag(originalOrientation);
        rotationMatrix.postRotate(rotationAngleInDegrees);

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        Bitmap resized = Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true);
        resized = Bitmap.createBitmap(resized, 0, 0, finalWidth, finalHeight, rotationMatrix, true);
        
        File imageDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        if(!imageDirectory.exists()) {
            Log.d("image-crop-picker", "Pictures Directory is not existing. Will create this directory.");
            imageDirectory.mkdirs();
        }

        File resizeImageFile = new File(imageDirectory, UUID.randomUUID() + ".jpg");

        OutputStream os = new BufferedOutputStream(new FileOutputStream(resizeImageFile));
        resized.compress(Bitmap.CompressFormat.JPEG, quality, os);

        os.close();
        original.recycle();
        resized.recycle();

        return resizeImageFile;
    }


    private static int getRotationInDegreesForOrientationTag(int orientationTag) {
        switch(orientationTag){
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return -90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            default:
                return 0;
        }
    }
}
