package com.biaobei.sdk.android.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.biaobei.sdk.android.demo.longtime.LongTimeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void tts(View view) {
        startActivity(new Intent(MainActivity.this, AuthorizationActivity.class).putExtra(AuthorizationActivity.EXPERIENCE_TYPE, "tts_online"));
    }

    public void asr(View v) {
        startActivity(new Intent(MainActivity.this, AuthorizationActivity.class).putExtra(AuthorizationActivity.EXPERIENCE_TYPE, "asr_online"));
    }

    public void longTimeAsr(View v) {
        startActivity(new Intent(MainActivity.this, AuthorizationActivity.class).putExtra(AuthorizationActivity.EXPERIENCE_TYPE, "long_time_asr_online"));
    }
}