package com.hussain_chachuliya.customcamera;

import android.app.Activity;
import android.content.Intent;

public class CustomCamera {

    private float requiredMegaPixel;
    private Activity activity;
    private String path;
    public static final int IMAGE_SAVE_REQUEST = 72;
    public static final String IMAGE_PATH = "imagePath";
    private String imageName;

    public CustomCamera(Activity activity,
                        float requiredMegaPixel,
                        String path) {
        this.requiredMegaPixel = requiredMegaPixel;
        this.activity = activity;
        this.path = path;
    }

    public void openCamera() {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra("megapixels", requiredMegaPixel);
        intent.putExtra("imageName", getImageName());
        intent.putExtra(IMAGE_PATH, path);
        activity.startActivityForResult(intent, IMAGE_SAVE_REQUEST);
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
