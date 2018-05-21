package com.hussain_chachuliya.customcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private String imagePath, imageName, capturedImagePath;
    private float megapixels;
    private Group confirmGroup;
    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 72;
    private byte[] imageData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }

        imagePath = getIntent().getStringExtra(CustomCamera.IMAGE_PATH);
        imageName = getIntent().getStringExtra("imageName");
        megapixels = getIntent().getFloatExtra("megapixels", 0);

        confirmGroup = findViewById(R.id.confirmGroup);
        confirmGroup.setVisibility(View.GONE);

        final ImageButton captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmGroup.getVisibility() == View.GONE) {
                    confirmGroup.setVisibility(View.VISIBLE);
                    captureButton.setImageDrawable(ContextCompat.getDrawable(CameraActivity.this,
                            R.drawable.round_replay_black_36dp));

                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if(success)
                                mCamera.takePicture(null, null, mPicture);
                        }
                    });
                } else {
                    clearImageData();
                    CameraActivity.this.recreate();
                }
            }
        });

        findViewById(R.id.button_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        findViewById(R.id.button_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImageData();
                CameraActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = getCameraInstance(megapixels);
        CameraPreview mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mCameraPreview);

        // Update UI after resume.
        clearImageData();
        confirmGroup.setVisibility(View.GONE);
        ImageButton captureButton = findViewById(R.id.button_capture);
        captureButton.setImageDrawable(ContextCompat.getDrawable(CameraActivity.this,
                R.drawable.round_photo_camera_black_36dp));

    }

    private void saveImage() {
        File pictureFile = getOutputMediaFile(imagePath);
        if (pictureFile == null) {
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(imageData);
            fos.close();
            capturedImagePath = pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.e("ERROR_FILE_NOT_FOUND", e.getMessage());
        } catch (IOException e) {
            Log.e("ERROR_IO_EXCEPTION", e.getMessage());
        }

        clearImageData();
        Intent intent = new Intent();
        intent.putExtra(CustomCamera.IMAGE_PATH, capturedImagePath);
        setResult(RESULT_OK, intent);
        CameraActivity.this.finish();
    }

    private void clearImageData() {
        imageData = null;
    }

    private Camera getCameraInstance(float requiredMegapixel) {
        Camera camera = null;
        try {
            camera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.startPreview();

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(findCameraFacing(), info);
            int rotation = getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);

            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

            // Check what resolutions are supported by your camera
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Log.i("CUSTOM_CAMERA", "Available resolution: " + sizes);

            // Iterate through all available resolutions and choose one.
            // The chosen resolution will be stored in mSize.
            Camera.Size mSize = null;
            for (Camera.Size size : sizes) {
                float megapixel = (float) size.width * (float) size.height / 1000000;
                if (megapixel < requiredMegapixel) {
                    mSize = size;
                    params.setPictureSize(mSize.width, mSize.height);
                    break;
                }
            }
            if (mSize != null) {
                Log.i("CUSTOM_CAMERA", "Chosen resolution: " + mSize.width + " " + mSize.height);
            }

            camera .setParameters(params);
        } catch (Exception e) {
            // cannot get camera or does not exist
            Log.e("CUSTOM_CAMERA", e.getMessage());
        }

        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            imageData = data;
            camera.stopPreview();
        }
    };

    private File getOutputMediaFile(String imagePath) {
        File mediaStorageDir = new File(imagePath);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        if (TextUtils.isEmpty(imageName))
            return createImageWithTimeStamp(mediaStorageDir.getPath());
        return createImageWithCustomName(mediaStorageDir.getPath(), imageName);
    }

    private File createImageWithTimeStamp(String imagePath) {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
                .format(new Date());
        File mediaFile;
        mediaFile = new File(imagePath + File.separator
                + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    private File createImageWithCustomName(String imagePath, String imageName) {
        File mediaFile;
        mediaFile = new File(imagePath + File.separator
                + imageName + ".jpg");
        return mediaFile;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearImageData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(CameraActivity.this,
                                "Insufficient Privileges. Please grant requested permissions.",
                                Toast.LENGTH_SHORT).show();
                        this.finish();
                        break;
                    }
                }
                break;
        }
    }

    private int findCameraFacing()

    {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
}
