package com.hussain_chachuliya.customcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class CustomCamera {

    private float requiredMegaPixel;
    private Activity activity;
    private String path;
    public static final int IMAGE_SAVE_REQUEST = 72;
    public static final String IMAGE_PATH = "imagePath";
    private String imageName;

    public static CustomCamera init(){
        return new CustomCamera();
    }

    public CustomCamera with(Activity activity){
        this.activity = activity;
        return this;
    }

    public CustomCamera setRequiredMegaPixel(float megaPixel){
        this.requiredMegaPixel = megaPixel;
        return this;
    }

    public CustomCamera setPath(String path){
        this.path = path;
        return this;
    }

    public CustomCamera setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public void start() {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra("megapixels", requiredMegaPixel);
        intent.putExtra("imageName", getImageName());
        intent.putExtra(IMAGE_PATH, path);
        activity.startActivityForResult(intent, IMAGE_SAVE_REQUEST);
    }

    public String getImageName() {
        return imageName;
    }
}
