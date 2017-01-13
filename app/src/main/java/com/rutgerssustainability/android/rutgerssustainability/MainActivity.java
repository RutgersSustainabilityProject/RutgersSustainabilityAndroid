package com.rutgerssustainability.android.rutgerssustainability;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button takePicBtn;
    private Button viewPicsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePicBtn = (Button)findViewById(R.id.take_pic_btn);
        viewPicsBtn = (Button)findViewById(R.id.view_pics_btn);
    }
}
