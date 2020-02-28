package com.reactnative.ivpusic.imagepicker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = "CameraXBasic";
    private static final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";
    private static final Float RATIO_4_3_VALUE = 4.0f / 3.0f;
    private static final Float RATIO_16_9_VALUE = 16.0f / 9.0f;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    private ConstraintLayout container;
    private PreviewView viewFinder;
    private DisplayManager displayManager;
    private int displayId = -1;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalyzer;
    private Executor mainExecutor;
    private Executor analysisExecutor;
    ViewfinderView viewfinderView;
    private Camera camera;
    String filePath;

    Boolean canCapture = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        filePath = getIntent().getStringExtra("FILE_PATH");
        container = findViewById(R.id.camera_container);
        viewFinder = findViewById(R.id.view_finder);
        viewfinderView = findViewById(R.id.finderView);

        mainExecutor = ContextCompat.getMainExecutor(this.getApplicationContext());
        analysisExecutor = Executors.newSingleThreadExecutor();

        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(displayListener, null);

        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                displayId = viewFinder.getDisplay().getDisplayId();
                updateCameraUi();
                bindCameraUseCases();
            }
        });
    }

    @Override
    protected void onDestroy() {
        displayManager.unregisterDisplayListener(displayListener);
        super.onDestroy();

    }

    private DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {

        }

        @Override
        public void onDisplayRemoved(int displayId) {

        }

        @Override
        public void onDisplayChanged(int id) {
            if (displayId != id) {
                Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                        .getDefaultDisplay();
                if (imageCapture != null && display != null) {
                    imageCapture.setTargetRotation(display.getRotation());
                }
                if (imageAnalyzer != null && display != null) {
                    imageAnalyzer.setTargetRotation(display.getRotation());
                }

            }
        }
    };

    private int aspectRatio(int width, int height) {
        float previewRatio = (float) Math.max(width, height) / (float) Math.min(width, height);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateCameraUi();
    }


    private ImageCapture.OnImageSavedCallback imageSavedListener = new ImageCapture.OnImageSavedCallback() {
        @Override
        public void onImageSaved(@NonNull File file) {
            canCapture = true;
            Intent result = new Intent();
            result.putExtra("FILE_PATH", file.getAbsolutePath());
            Rect rect = viewfinderView.getRect();
            result.putExtra("LEFT", rect.left);
            result.putExtra("TOP", rect.top);
            result.putExtra("RIGHT", rect.right);
            result.putExtra("BOTTOM", rect.bottom);
            setResult(RESULT_OK, result);
            setResult(RESULT_OK, result);
            finish();
        }

        @Override
        public void onError(int imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
            canCapture = true;
        }
    };


    private void updateCameraUi() {

        View view = container.findViewById(R.id.camera_ui_container);

        container.removeView(view);

        // Inflate a new view containing all UI for controlling the camera
        View controls = View.inflate(this, R.layout.camera_ui_container, container);

        // Listener for button used to capture photo

        controls.findViewById(R.id.camera_capture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageCapture != null && canCapture) {
                    canCapture = false;
                    File photoFile = new File(filePath);

                    // Setup image capture metadata
                    ImageCapture.Metadata metadata = new ImageCapture.Metadata();
                    metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
                    imageCapture.takePicture(photoFile, metadata, mainExecutor, imageSavedListener);
                }
            }
        });


//        // Listener for button used to switch cameras
//        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
//            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
//                CameraSelector.LENS_FACING_BACK
//            } else {
//                CameraSelector.LENS_FACING_FRONT
//            }
//            // Bind use cases
//            bindCameraUseCases()
//        }
//
//        // Listener for button used to view the most recent photo
//        controls.findViewById<ImageButton>(R.id.photo_view_button).setOnClickListener {
//            // Only navigate when the gallery has photos
//            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
//                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
//                        CameraFragmentDirections.actionCameraToGallery(outputDirectory.absolutePath))
//            }
//        }
    }

    private void bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        DisplayMetrics metrics = new DisplayMetrics();
        viewFinder.getDisplay().getRealMetrics(metrics);


        final int screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels);


        final int rotation = viewFinder.getDisplay().getRotation();

        // Bind the CameraProvider to the LifeCycleOwner
        final CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this.getApplicationContext());
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Preview
                    Preview preview = new Preview.Builder()
                            // We request aspect ratio but no resolution
                            // Set initial target rotation
                            .setTargetResolution(new Size(1920,1080))
                            .setTargetRotation(rotation)
                            .build();

                    preview.setPreviewSurfaceProvider(viewFinder.getPreviewSurfaceProvider());


                    // ImageCapture
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            // We request aspect ratio but no resolution to match preview config, but letting
                            // CameraX optimize for whatever specific resolution best fits requested capture mode
                            .setTargetAspectRatio(screenAspectRatio)
                            // Set initial target rotation, we will have to call this again if rotation changes
                            // during the lifecycle of this use case
                            .setTargetRotation(rotation)
                            .build();

                    imageAnalyzer = new ImageAnalysis.Builder()
                            // We request aspect ratio but no resolution
//                            setTargetAspectRatio(screenAspectRatio)
                            // Set initial target rotation, we will have to call this again if rotation changes
                            // during the lifecycle of this use case
                            .setTargetRotation(rotation)
                            .build();

                    // The analyzer can then be assigned to the instance
//                        .also {
//                    imageAnalyzer.setAnalyzer(analysisExecutor, LuminosityAnalyzer {luma ->
//                            // Values returned from our analyzer are passed to the attached listener
//                            // We log image analysis results here - you should do something useful instead!
//                            Log.d(TAG, "Average luminosity: $luma")
                    // })

                    cameraProvider.unbindAll();


                    // A variable number of use-cases can be passed here -
                    // camera provides access to CameraControl & CameraInfo
                    camera = cameraProvider.bindToLifecycle(
                            CaptureActivity.this, cameraSelector, preview, imageCapture, imageAnalyzer);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, mainExecutor);

    }

    public static Intent newInstance(Context context, String path) {
        Intent intent = new Intent(context, CaptureActivity.class);
        intent.putExtra("FILE_PATH", path);
        return intent;
    }
}
