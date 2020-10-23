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
import java.io.InputStream;
import java.util.List;

/**
 * 此页面支持两种方式作为sdk的识别输入源。分别是开发传入路径，字节流顺序的传给sdk。代码中有提现这两种方式
 */
public class FileSpeechActivity extends AppCompatActivity implements BakerRecognizerCallback, View.OnClickListener {
    private BakerRecognizer bakerRecognizer;
    private TextView resultTv, statusTv;
    private boolean isSendPcmBuffer = false;

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
        isSendPcmBuffer = false;
        appendStatus("\n识别结束");
        HLogger.d("--onEndOfSpeech--");
    }

    @Override
    public void onError(BakerError error) {
        isSendPcmBuffer = false;
        appendStatus("\n识别错误 : " + error.getCode() + ", " + error.getMessage());
        HLogger.d("code=" + error.getCode() + ", message=" + error.getMessage() + ",trace_id=" + error.getTrace_id());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.asr_send_pcm_buffer) {
            //TODO 请注意使用字节流的方式识别调用此开始方法
            bakerRecognizer.startRecognizeWithByte();
            sendPcmBuffer();
        } else if (id == R.id.asr_assets_pcm_file) {
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

    private void sendPcmBuffer() {
        /**
         * 当BakerRecognizer.startRecognizeWithByte()方法开启识别后
         * 通过bakerRecognizer.sendPcmBuffer(buffer)依次发送数据给SDK
         * buffer的大小务必固定1024(最后一包除外)
         * 传递完数据后可以自行
         * 调用 BakerRecognizer.stopRecognition()结束识别或者当识别过程中静音段超时会
         * 自动结束识别。
         * sendPcmBuffer()返回结果说明，0=正常，1=buffer是空，2=buffer超过1024
         */
        new Thread() {
            @Override
            public void run() {
                try {
                    isSendPcmBuffer = true;
                    InputStream inputStream = getAssets().open("yinpin.pcm");
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer, 0, buffer.length)) != -1 && (isSendPcmBuffer)) {
                        HLogger.d("读取音频流:" + buffer.length);
                        int result = bakerRecognizer.sendPcmBuffer(buffer);
                        if (result != 0) {
                            //返回值不等于0发送数据异常，调用关闭识别的方法
                            bakerRecognizer.stopRecognition();
                        }
                    }
                    bakerRecognizer.stopRecognition();
                    isSendPcmBuffer = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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