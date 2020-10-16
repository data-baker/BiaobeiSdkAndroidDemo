package com.biaobei.sdk.android.demo.asr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.baker.sdk.basecomponent.bean.BakerError;
import com.baker.sdk.basecomponent.util.HLogger;
import com.baker.speech.asr.BakerRecognizer;
import com.baker.speech.asr.basic.BakerRecognizerCallback;
import com.biaobei.sdk.android.demo.Constants;
import com.biaobei.sdk.android.demo.R;
import com.biaobei.sdk.android.demo.permission.PermissionUtil;

import java.util.List;

public class AsrActivity extends AppCompatActivity implements BakerRecognizerCallback, View.OnClickListener {
    private static String TAG = AsrActivity.class.getName();
    private BakerRecognizer bakerRecognizer;
    private TextView resultTv, statusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(AsrActivity.this, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionUtil.needPermission(AsrActivity.this, 89, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE
            );
        }

        resultTv = findViewById(R.id.tv_Result);
        resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusTv = findViewById(R.id.tv_Status);
        statusTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        String clientId = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.ASR_ONLINE_CLIENT_ID, "");//需要连续标贝申请
        String clientSecret = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.ASR_ONLINE_CLIENT_SECRET, "");//需要连续标贝申请

        bakerRecognizer = BakerRecognizer.getInstance(AsrActivity.this, clientId, clientSecret);
        bakerRecognizer.setCallback(this);
//        bakerRecognizer.setDebug(AsrActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bakerRecognizer != null) {
            bakerRecognizer.cancelRecognize();
        }
    }

    @Override
    public void onReadyOfSpeech() {
        statusTv.setText("");
        resultTv.setText("");
        appendStatus("\n麦克风已经准备好");
        HLogger.d("--onReadyOfSpeech--");
    }

    @Override
    public void onVolumeChanged(float volume, byte[] data) {
//        HLogger.d("--onVolumeChanged--" + volume);
    }

    @Override
    public void onResult(List<String> nbest, List<String> uncertain, boolean isLast) {
        HLogger.d("--onResult--");
        if (nbest != null && nbest.size() > 0) {
            appendResult(nbest.get(0));
        }
    }

    @Override
    public void onBeginOfSpeech() {
        appendStatus("\n识别开始");
        HLogger.d("--onBeginOfSpeech--");
    }

    @Override
    public void onEndOfSpeech() {
        appendStatus("\n识别结束");
        HLogger.d("--onEndOfSpeech--");
    }

    @Override
    public void onError(BakerError error) {
        appendStatus("\n识别错误 : " + error.getCode() + ", " + error.getMessage());
        HLogger.d("code=" + error.getCode() + ", message=" + error.getMessage());
    }


    private void setParams() {
        //设置采样率 目前只支持16000  默认16000
        bakerRecognizer.setSample(16000);

        //是否添加标点 true=返回标点，false=不返回标点，默认false
        bakerRecognizer.addPct(false);
        //是否执行归一化处理
        bakerRecognizer.enableItn(false);
        //设置领域、场景
//        bakerRecognizer.setDomain(1);
        //识别类型  0：一句话识别，sdk做vad   1：长语音识别，服务端做vad  默认为0
        bakerRecognizer.setRecognizeType(0);

        //设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        bakerRecognizer.setVadSos(110);
        //设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        bakerRecognizer.setVadEos(50);
        //设置语音最大识别时长，最长60s=1800
        bakerRecognizer.setVadWait(1800);

        //设置语句间停顿间隔，默认45
        bakerRecognizer.setVadPause(45);
    }

    private void appendStatus(final String str) {
        statusTv.post(new Runnable() {
            @Override
            public void run() {
                statusTv.append(str);
                int scrollAmount = statusTv.getLayout().getLineTop(statusTv.getLineCount())
                        - statusTv.getHeight();
                if (scrollAmount > 0)
                    statusTv.scrollTo(0, scrollAmount);
                else
                    statusTv.scrollTo(0, 0);
            }
        });
    }

    private void appendResult(final String str) {
        resultTv.post(new Runnable() {
            @Override
            public void run() {
                resultTv.setText(str);
                int scrollAmount = resultTv.getLayout().getLineTop(resultTv.getLineCount())
                        - resultTv.getHeight();
                if (scrollAmount > 0)
                    resultTv.scrollTo(0, scrollAmount);
                else
                    resultTv.scrollTo(0, 0);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startRecognize:
                if (bakerRecognizer != null) {
                    setParams();
                    //返回0启动成功，返回1=callback为空，未启动成功
                    int result = bakerRecognizer.startRecognize();
                    HLogger.d("result==" + result);
                }
                break;
            case R.id.stopRecognize:
                bakerRecognizer.stopRecognition();
                break;
            default:
                break;
        }
    }
}