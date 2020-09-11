package com.biaobei.sdk.android.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 开发着无需关注此页面。SDK内部会校验token
 * 该页面是提供给体验者直接输入client_id以及client_secret体验标贝SDKDemo的
 */
public class AuthorizationActivity extends AppCompatActivity {

    public static final String EXPERIENCE_TYPE = "type";
    private String type;

    private static final String TTS_ONLINE_CLIENT_ID = "tts_online_client_id", TTS_ONLINE_CLIENT_SECRET = "tts_online_client_secret";

    private SharedPreferences mSharedPreferences;

    private EditText etClientId, etClientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        init();

    }

    @SuppressLint("CommitPrefEdits")
    private void init() {
        etClientId = findViewById(R.id.et_client_id);
        etClientSecret = findViewById(R.id.et_client_secret);
        mSharedPreferences = getSharedPreferences(AuthorizationActivity.class.getName(), Context.MODE_PRIVATE);
        type = getIntent().getStringExtra(AuthorizationActivity.EXPERIENCE_TYPE);
        if (!TextUtils.isEmpty(type)) {
            switch (type) {
                case "tts_online":
                    //体验tts,授权tts获取token
                    if (!TextUtils.isEmpty(sharedPreferencesGet(TTS_ONLINE_CLIENT_ID))) {
                        etClientId.setText(sharedPreferencesGet(TTS_ONLINE_CLIENT_ID));
                    }
                    if (!TextUtils.isEmpty(sharedPreferencesGet(TTS_ONLINE_CLIENT_SECRET))) {
                        etClientSecret.setText(sharedPreferencesGet(TTS_ONLINE_CLIENT_SECRET));
                    }
                    break;
            }
        }
    }

    public void jump(View view) {
        //TODO 校验token有效后进行存储，体验者第二次进来不再需要输入
        if (TextUtils.isEmpty(etClientSecret.getText().toString().trim())) {
            Toast.makeText(this, "请输入ClientSecret", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etClientId.getText().toString().trim())) {
            Toast.makeText(this, "请输入ClientId", Toast.LENGTH_SHORT).show();
            return;
        }
        NetUtils.getToken(etClientSecret.getText().toString().trim(), etClientId.getText().toString().trim(), new NetUtils.Callback() {
            @Override
            public void onSuccess(String s) {
                storageParameter();
                Intent mIntent = new Intent(AuthorizationActivity.this, TtsActivity.class);
                mIntent.putExtra("ClientId", etClientId.getText().toString().trim());
                mIntent.putExtra("ClientSecret", etClientSecret.getText().toString().trim());
                startActivity(mIntent);
            }

            @Override
            public void onError(String s) {
                removeParameter();
                Toast.makeText(AuthorizationActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 存储参数
     */
    private void storageParameter() {
        if (TextUtils.isEmpty(type)) return;
        String clientId = etClientId.getText().toString().trim();
        String clientSecret = etClientSecret.getText().toString().trim();
        switch (type) {
            case "tts_online":
                sharedPreferencesCommit(TTS_ONLINE_CLIENT_ID, clientId);
                sharedPreferencesCommit(TTS_ONLINE_CLIENT_SECRET, clientSecret);
                break;
        }
    }

    /**
     * 移除参数
     */
    private void removeParameter() {
        if (TextUtils.isEmpty(type)) return;
        switch (type) {
            case "tts_online":
                sharedPreferencesRemove(TTS_ONLINE_CLIENT_ID);
                sharedPreferencesRemove(TTS_ONLINE_CLIENT_SECRET);
                break;
        }
    }

    private void sharedPreferencesCommit(String mKey, String mValue) {
        if (mSharedPreferences != null && !TextUtils.isEmpty(mKey) && !TextUtils.isEmpty(mValue)) {
            mSharedPreferences.edit().putString(mKey, mValue).apply();
        }
    }

    private void sharedPreferencesRemove(String mKey) {
        if (mSharedPreferences != null && !TextUtils.isEmpty(mKey)) {
            mSharedPreferences.edit().remove(mKey).apply();
        }
    }

    private String sharedPreferencesGet(String mKey) {
        if (mSharedPreferences != null && !TextUtils.isEmpty(mKey)) {
            return mSharedPreferences.getString(mKey, "");
        }
        return null;
    }
}