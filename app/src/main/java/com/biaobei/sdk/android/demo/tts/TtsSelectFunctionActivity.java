package com.biaobei.sdk.android.demo.tts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.biaobei.sdk.android.demo.R;

public class TtsSelectFunctionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_function_tts);
        setTitle(getString(R.string.app_name) + "（在线长tts体验页）");
    }

    public void toAudioTrackPlayerActivity(View view) {
        startActivity(new Intent(this, AudioTrackPlayerActivity.class));
    }

    public void toMediaTrackPlayerActivity(View view) {
        startActivity(new Intent(this, TtsActivity.class));
    }
}