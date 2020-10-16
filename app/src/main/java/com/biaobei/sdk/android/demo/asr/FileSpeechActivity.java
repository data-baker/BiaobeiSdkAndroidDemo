package com.biaobei.sdk.android.demo.asr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.baker.sdk.basecomponent.bean.BakerError;
import com.baker.sdk.basecomponent.util.HLogger;
import com.baker.sdk.basecomponent.writelog.WriteLog;
import com.baker.speech.asr.BakerRecognizer;
import com.baker.speech.asr.basic.BakerRecognizerCallback;
import com.biaobei.sdk.android.demo.Constants;
import com.biaobei.sdk.android.demo.R;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;
import java.util.List;

public class FileSpeechActivity extends AppCompatActivity implements BakerRecognizerCallback, View.OnClickListener {
    private BakerRecognizer bakerRecognizer;
    private TextView resultTv, statusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_speech);

        resultTv = findViewById(R.id.tv_Result);
        resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusTv = findViewById(R.id.tv_Status);
        statusTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        String clientId = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.ASR_ONLINE_CLIENT_ID, "");//需要连续标贝申请
        String clientSecret = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.ASR_ONLINE_CLIENT_SECRET, "");//需要连续标贝申请
        bakerRecognizer = BakerRecognizer.getInstance(FileSpeechActivity.this, clientId, clientSecret);
        bakerRecognizer.setCallback(this);
//        bakerRecognizer.setDebug(FileSpeechActivity.this);
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
        WriteLog.writeLogs("识别开始");
    }

    @Override
    public void onEndOfSpeech() {
        appendStatus("\n识别结束");
        HLogger.d("--onEndOfSpeech--");
    }

    @Override
    public void onError(BakerError error) {
        appendStatus("\n识别错误 : " + error.getCode() + ", " + error.getMessage());
        HLogger.d("code=" + error.getCode() + ", message=" + error.getMessage() + ",trace_id=" + error.getTrace_id());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.asr_assets_pcm_file) {
            bakerRecognizer.startRecognize("asset://yinpin.pcm");
        } else if (id == R.id.stopRecognize) {
            bakerRecognizer.stopRecognition();
        } else if (id == R.id.startRecognizeUseCustomFile) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //允许多选 长按多选
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            //不限制选取类型
            intent.setType("*/*");
            startActivityForResult(intent, 333);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        HLogger.d("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 333:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //当单选选了一个文件后返回
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        File file = UriUtils.uri2File(uri);
                        HLogger.d(file.getPath());

                        int result = bakerRecognizer.startRecognize(file.getAbsolutePath());
                        HLogger.d("result==" + result);
                    }
                }
                break;
        }
    }
}