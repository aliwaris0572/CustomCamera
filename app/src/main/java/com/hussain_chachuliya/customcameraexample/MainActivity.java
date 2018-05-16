package com.hussain_chachuliya.customcameraexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hussain_chachuliya.customcamera.CustomCamera;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomCamera customCamera = new CustomCamera(
                        MainActivity.this,
                        1.5f,
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/CustomCamera");
                customCamera.openCamera();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CustomCamera.IMAGE_SAVE_REQUEST){
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this,
                        "Image is saved at: " + data.getStringExtra(CustomCamera.IMAGE_PATH),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
