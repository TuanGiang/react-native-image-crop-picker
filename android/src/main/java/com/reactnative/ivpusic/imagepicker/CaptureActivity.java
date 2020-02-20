package com.reactnative.ivpusic.imagepicker;

import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

public class CaptureActivity extends AppCompatActivity {

    private TextureView textureView;
    private Button btnCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textureView = findViewById(R.id.texture);
        btnCapture = findViewById(R.id.btn_take_picture);

        textureView.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
        Log.i("AAAAAAAAAAAAA", "BBBBBBBBBBBBBBBBBBBB");

    }

    private void startCamera() {

        DisplayMetrics metrics = new DisplayMetrics();

        textureView.getDisplay().getRealMetrics(metrics);

        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);

        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .setTargetResolution(screenSize)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .setTargetRotation(textureView.getDisplay().getRotation())
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                textureView.setSurfaceTexture(output.getSurfaceTexture());
                Log.i("AAAAAAAAAAAAA", "CCCCCCCCCCCCCCCCC");
                updateTransform();
            }
        });

        // Create configuration object for the image capture use case
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()

                .setLensFacing(CameraX.LensFacing.BACK)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(textureView.getDisplay().getRotation())
                .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                .build();

        // Build the image capture use case and attach button click listener
        final ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(
                        Environment.getExternalStorageDirectory().toString() +
                                "${MainActivity.folderPath}${System.currentTimeMillis()}.jpg"
                );
                imageCapture.takePicture(file,
                        new ImageCapture.OnImageSavedListener() {
                            @Override
                            public void onImageSaved(@NonNull File file) {

                            }

                            @Override
                            public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                            }
                        });
            }
        });

        CameraX.bindToLifecycle(this, preview, imageCapture);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        float centerX = textureView.getWidth() / 2f;
        float centerY = textureView.getHeight() / 2f;

        float rotationDegrees = 0f;
        switch (textureView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0f;
                break;

            case Surface.ROTATION_90:
                rotationDegrees = 90f;
                break;

            case Surface.ROTATION_180:
                rotationDegrees = 180f;
                break;

            case Surface.ROTATION_270:
                rotationDegrees = 270f;
                break;

        }

        matrix.postRotate(-rotationDegrees, centerX, centerY);
        textureView.setTransform(matrix);
    }
}
