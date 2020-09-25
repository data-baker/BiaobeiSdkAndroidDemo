package com.biaobei.sdk.android.demo;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.baker.sdk.basecomponent.BakerBaseConstants;
import com.baker.sdk.basecomponent.bean.BakerError;
import com.databaker.synthesizer.BakerMediaCallback;
import com.databaker.synthesizer.BakerSynthesizer;

public class TtsActivity extends AppCompatActivity {

    private static String TAG = TtsActivity.class.getName();
    private String clientId = "";
    private String clientSecret = "";
    private BakerSynthesizer bakerSynthesizer;
    private EditText editText;
    private TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        clientId = getIntent().getStringExtra("ClientId");
        clientSecret = getIntent().getStringExtra("ClientSecret");

        editText = findViewById(R.id.edit_content);
        resultTv = findViewById(R.id.tv);
        resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        bakerSynthesizer = new BakerSynthesizer(this, clientId, clientSecret);
        bakerSynthesizer.setDebug(this, false);

    }

    public void start(View view) {
//        //开始合成，合成结束后会自动stop
        if (bakerSynthesizer == null) {
            bakerSynthesizer = new BakerSynthesizer(this);
            bakerSynthesizer.setDebug(this, false);
        }
        setParams();
        bakerSynthesizer.start();
        Log.d(TAG, "--start--");
    }

    public void stop(View view) {
        if (bakerSynthesizer != null) {
            bakerSynthesizer.bakerStop();
            appendResult("\n停止播放");
        }
    }

    public void pauseOrPlay(View view) {
        if (bakerSynthesizer != null) {
            boolean isPlaying = bakerSynthesizer.isPlaying();
            if (isPlaying) {
                bakerSynthesizer.bakerPause();
                appendResult("\n暂停");
            } else {
                bakerSynthesizer.bakerPlay();
                appendResult("\n播放");
            }
        }
    }

    public void isPlay(View view) {
        if (bakerSynthesizer != null) {
            boolean isPlaying = bakerSynthesizer.isPlaying();
            appendResult("\n当前播放状态：" + isPlaying);
        }
    }

    public void playDuration(View view) {
        if (bakerSynthesizer != null) {
            int currentPosition = bakerSynthesizer.getCurrentPosition();
            appendResult("\n当前播放至：" + currentPosition + "秒");
        }
    }

    public void duration(View view) {
        if (bakerSynthesizer != null) {
            int duration = bakerSynthesizer.getDuration();
            appendResult("\n音频总长度：" + duration + "秒");
        }
    }

    /**
     * 设置相关参数
     */
    private void setParams() {
        if (bakerSynthesizer == null) {
            return;
        }
        /**********************以下是必填参数**************************/
        //设置要转为语音的合成文本
        bakerSynthesizer.setText(editText.getText().toString().trim());
        //设置返回数据的callback
        bakerSynthesizer.setBakerCallback(bakerMediaCallback);
        /**********************以下是选填参数**************************/
        //设置发音人声音名称，默认：标准合成_模仿儿童_果子
        bakerSynthesizer.setVoice("新闻合成_天天");
        //合成请求文本的语言，目前支持ZH(中文和中英混)和ENG(纯英文，中文部分不会合成),默认：ZH
        bakerSynthesizer.setLanguage(BakerBaseConstants.LANGUAGE_ZH);
        //设置播放的语速，在0～9之间（支持浮点值），不传时默认为5
        bakerSynthesizer.setSpeed(5.0f);
        //设置语音的音量，在0～9之间（只支持整型值），不传时默认值为5
        bakerSynthesizer.setVolume(5);
        //设置语音的音调，取值0-9，不传时默认为5中语调
        bakerSynthesizer.setPitch(5);
        /**
         * 可不填，不填时默认为4, 16K采样率的pcm格式
         * audiotype=4 ：返回16K采样率的pcm格式
         * audiotype=5 ：返回8K采样率的pcm格式
         * audiotype=6 ：返回16K采样率的wav格式
         * audiotype=6&rate=1 ：返回8K的wav格式
         */
        bakerSynthesizer.setAudioType(BakerBaseConstants.AUDIO_TYPE_PCM_16K);
//        bakerSynthesizer.setRate(1);
    }


    BakerMediaCallback bakerMediaCallback = new BakerMediaCallback() {

        @Override
        public void onPrepared() {
            appendResult("\n合成准备就绪");
            if (bakerSynthesizer != null) {
                bakerSynthesizer.bakerPlay();
            }
        }

        @Override
        public void onCacheAvailable(int percentsAvailable) {
            appendResult("\n缓存进度：" + percentsAvailable + "%");
        }

        @Override
        public void onCompletion() {
            appendResult("\n播放结束");
        }

        @Override
        public void onError(BakerError errorBean) {
            Log.d(TAG, "--onError-- errorCode=" + errorBean.getCode() + ", errorMsg=" + errorBean.getMessage() + ",traceId==" + errorBean.getTrace_id());
        }

        @Override
        public void playing() {
            appendResult("\n播放啦");
        }

        @Override
        public void noPlay() {
            appendResult("\n没有播放啦");
        }
    };

    private void appendResult(final String str) {
//        buffer.append(str);
//        resultTv.setText(str);
        resultTv.post(new Runnable() {
            @Override
            public void run() {
                resultTv.append(str);
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
    protected void onDestroy() {

        if (bakerSynthesizer != null) {
            if (bakerSynthesizer.isPlaying()) {
                bakerSynthesizer.bakerStop();
            }
            bakerSynthesizer.onDestroy();
        }
        super.onDestroy();
    }

}