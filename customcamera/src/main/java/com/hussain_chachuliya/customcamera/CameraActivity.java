package com.hussain_chachuliya.customcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private String imagePath, imageName;
    private float megapixels;
    private final int REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePath = getIntent().getStringExtra(CustomCamera.IMAGE_PATH);
        imageName = getIntent().getStringExtra("imageName");
        megapixels = getIntent().getFloatExtra("megapixels", 0);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = createFile(imagePath, imageName);
        Uri uri = FileProvider.getUriForFile(CameraActivity.this,
                "com.hussain_chachuliya.customcamera.fileprovider", photo);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private File createFile(String folder, String fileName) {
        File myDir = new File(folder);
        myDir.mkdirs();

        File file = new File(myDir, fileName);
        if (file.exists()) file.delete();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap = getScaledBitmapFromSdcard(imagePath, imageName);
            saveImage(bitmap);
        }
        else
            this.finish();
    }

    private Bitmap getScaledBitmapFromSdcard(String folder, String fileName) {
        String filePath = String.format("%s/%s",
                folder,
                fileName);

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        // Iterate through all available resolutions and choose one.
        // The chosen resolution will be stored in mSize.
        Camera.Size mSize;
        for (Camera.Size size : getSupportedPictureSizes()) {
            float megapixel = (float) size.width * (float) size.height / 1000000;
            if (megapixel <= megapixels) {
                mSize = size;
                return ScalingUtils.createScaledBitmap(bitmap, mSize.width, mSize.height,
                        ScalingUtils.ScalingLogic.FIT);
            }
        }

        return null;
    }

    private List<Camera.Size> getSupportedPictureSizes() {
        Camera camera;
        try {
            int CAMERA_MODE = Camera.CameraInfo.CAMERA_FACING_BACK;
            camera = Camera.open(CAMERA_MODE);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(CAMERA_MODE, info);
            int rotation = getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }


            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
            // Check what resolutions are supported by your camera
            Camera.Parameters params = camera.getParameters();
            return params.getSupportedPictureSizes();
        } catch (Exception e) {
            // cannot get camera or does not exist
            Log.e("CUSTOM_CAMERA", e.getMessage());
        }
        return new ArrayList<>();
    }

    private void saveImage(Bitmap finalBitmap) {
        File myDir = new File(imagePath);
        myDir.mkdirs();

        File file = new File(myDir, imageName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

            Intent intent = new Intent();
            intent.putExtra(CustomCamera.IMAGE_PATH, file.getAbsolutePath());
            setResult(RESULT_OK, intent);
            CameraActivity.this.finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
