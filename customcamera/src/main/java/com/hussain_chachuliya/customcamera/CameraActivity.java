package com.hussain_chachuliya.customcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private String imagePath;
    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 72;

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
        mCamera = getCameraInstance(getIntent().getFloatExtra("megapixels", 0));
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        Button captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }

    private Camera getCameraInstance(float requiredMegapixel) {
        Camera camera = null;
        try {
            camera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
            Camera.Parameters params = camera.getParameters();

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
            Log.i("CUSTOM_CAMERA", "Chosen resolution: " + mSize.width + " " + mSize.height);
            mCamera.setParameters(params);
        } catch (Exception e) {
            // cannot get camera or does not exist
            Log.e("CUSTOM_CAMERA", e.getMessage());
        }

        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(imagePath);
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                Intent intent = new Intent();
                intent.putExtra(CustomCamera.IMAGE_PATH, pictureFile.getAbsolutePath());
                setResult(RESULT_OK, intent);
                CameraActivity.this.finish();
            } catch (FileNotFoundException e) {
                Log.e("ERROR_FILE_NOT_FOUND", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR_IO_EXCEPTION", e.getMessage());
            }
        }
    };

    private static File getOutputMediaFile(String imagePath) {
        File mediaStorageDir = new File(imagePath);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
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

}
