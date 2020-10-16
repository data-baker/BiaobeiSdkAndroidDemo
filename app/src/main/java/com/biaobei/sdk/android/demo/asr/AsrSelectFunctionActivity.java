package com.biaobei.sdk.android.demo.asr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.biaobei.sdk.android.demo.R;
import com.biaobei.sdk.android.demo.permission.PermissionFail;
import com.biaobei.sdk.android.demo.permission.PermissionSuccess;
import com.biaobei.sdk.android.demo.permission.PermissionUtil;

public class AsrSelectFunctionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_function_asr);
        setTitle(getString(R.string.app_name) + "（在线asr体验页）");
        requestPermissions();
    }

    /**
     * 检测录音权限
     */
    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS}, 0x0010);
                }

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.file_speech:
                startActivity(new Intent(AsrSelectFunctionActivity.this, FileSpeechActivity.class));
                break;
            case R.id.voice_speech:
                startActivity(new Intent(AsrSelectFunctionActivity.this, AsrActivity.class));
                break;
        }
    }

    /**
     * 授权成功
     */
    @PermissionSuccess(requestCode = 89)
    public void camouflageCallSuccess() {
    }

    /**
     * 授权失败
     */
    @PermissionFail(requestCode = 89)
    public void camouflageCallFail() {
        //刷新token
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(AsrSelectFunctionActivity.this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtil.needPermission(AsrSelectFunctionActivity.this, 89, Manifest.permission.RECORD_AUDIO
            );
        }
    }
}