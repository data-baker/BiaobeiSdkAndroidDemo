package com.biaobei.sdk.android.demo.longtime;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baker.sdk.longtime.asr.LongTimeAsr;
import com.baker.sdk.longtime.asr.listener.LongTimeAsrCallBack;
import com.biaobei.sdk.android.demo.Constants;
import com.biaobei.sdk.android.demo.R;
import com.biaobei.sdk.android.demo.permission.PermissionFail;
import com.biaobei.sdk.android.demo.permission.PermissionSuccess;
import com.biaobei.sdk.android.demo.permission.PermissionUtil;

public class LongTimeActivity extends AppCompatActivity {

    private TextView textView;
    private LongTimeAsr longTimeAsr;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            textView.setText((String) msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_time);

        setTitle(getString(R.string.app_name) + "（在线长语音asr体验页）");

        requestPermissions();

        textView = findViewById(R.id.text);

        longTimeAsr = new LongTimeAsr();
        longTimeAsr.isDebug(true);

        String clientId = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.LONG_TIME_ASR_ONLINE_CLIENT_ID, "");//需要连续标贝申请
        String clientSecret = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.LONG_TIME_ASR_ONLINE_CLIENT_SECRET, "");//需要连续标贝申请
        Log.d("hsj", "clientId=" + clientId);
        Log.d("hsj", "clientSecret=" + clientSecret);
        longTimeAsr.initSdk(LongTimeActivity.this, clientId, clientSecret, callBack);

    }

    public void start(View view) {
        startRecord();
    }

    private void startRecord() {
        if (longTimeAsr != null) {
            //*********************************设置参数****************************
            //音频采样率，支持16000(默认)，8000
            longTimeAsr.setSampleRate(8000);
            //是否在短静音处添加标点，默认true
            longTimeAsr.setAddPct(true);
            //模型名称，必须填写公司购买的语言模型，默认为common
            longTimeAsr.setDomain("common");
            //*********************************结束设置参数****************************

            longTimeAsr.startAsr();

        }
    }

    public void stop(View view) {
        longTimeAsr.stopAsr();
    }

    private LongTimeAsrCallBack callBack = new LongTimeAsrCallBack() {
        @Override
        public void onError(String code, String errorMessage) {
            Message message = Message.obtain();
            message.what = 0;
            message.obj = "error. code = " + code + ", message = " + errorMessage;
            handler.sendMessage(message);
        }

        @Override
        public void onReady() {
        }

        @Override
        public void onRecording(String result, boolean isLast) {
            Message message = Message.obtain();
            message.what = 0;
            message.obj = result;
            handler.sendMessage(message);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (longTimeAsr != null) {
            longTimeAsr.release();
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(LongTimeActivity.this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtil.needPermission(LongTimeActivity.this, 89, Manifest.permission.RECORD_AUDIO
            );
        }
    }
}