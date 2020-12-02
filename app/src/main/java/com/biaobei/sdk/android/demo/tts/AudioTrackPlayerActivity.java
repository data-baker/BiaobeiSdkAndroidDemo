package com.biaobei.sdk.android.demo.tts;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.baker.sdk.basecomponent.BakerBaseConstants;
import com.baker.sdk.basecomponent.bean.BakerError;
import com.baker.sdk.basecomponent.util.HLogger;
import com.biaobei.sdk.android.demo.Constants;
import com.biaobei.sdk.android.demo.R;
import com.databaker.synthesizer.BakerCallback;
import com.databaker.synthesizer.BakerSynthesizer;

public class AudioTrackPlayerActivity extends AppCompatActivity {
    private BakerSynthesizer bakerSynthesizer;
    private static AudioTrackPlayer audioTrackPlayer;
    private EditText editText, edtVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track_player);

        edtVoice = findViewById(R.id.edit_voice);
        editText = findViewById(R.id.edit_content);

        String clientId = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.TTS_ONLINE_CLIENT_ID, "");//需要连续标贝申请
        String clientSecret = getSharedPreferences(Constants.SP_TABLE_NAME, Context.MODE_PRIVATE).getString(Constants.TTS_ONLINE_CLIENT_SECRET, "");//需要连续标贝申请

        //初始化sdk
        bakerSynthesizer = new BakerSynthesizer(this, clientId, clientSecret);
        bakerSynthesizer.setDebug(this, false);
        audioTrackPlayer = new AudioTrackPlayer();
    }

    BakerCallback bakerCallback = new BakerCallback() {
        /**
         * 开始合成
         */
        @Override
        public void onSynthesisStarted() {
        }

        /**
         * 合成完成。
         * 当onBinaryReceived方法中endFlag参数=1，即最后一条消息返回后，会回调此方法。
         */
        @Override
        public void onSynthesisCompleted() {
        }

        /**
         * 第一帧数据返回时的回调
         */
        @Override
        public void onPrepared() {
            //清除掉播放器之前的缓存数据
            audioTrackPlayer.cleanAudioData();
        }

        /**
         * 流式持续返回数据的接口回调
         *
         * @param data 合成的音频数据
         * @param audioType  音频类型，如audio/pcm
         * @param interval  音频interval信息，
         * @param endFlag  是否时最后一个数据块，false：否，true：是
         */
        @Override
        public void onBinaryReceived(byte[] data, String audioType, String interval, boolean endFlag) {
//            HLogger.d("data.length==" + data.length + ", interval=" + interval);
            audioTrackPlayer.setAudioData(data);
        }

        /**
         * 合成失败
         */
        @Override
        public void onTaskFailed(BakerError errorBean) {
            HLogger.d("errorCode==" + errorBean.getCode() + ",errorMsg==" + errorBean.getMessage() + ",traceId==" + errorBean.getTrace_id());
        }
    };

    /**
     * 设置相关参数
     */
    private void setParams() {
        if (bakerSynthesizer == null) {
            bakerSynthesizer = new BakerSynthesizer(AudioTrackPlayerActivity.this);
            return;
        }
        /**********************以下是必填参数**************************/
        //设置要转为语音的合成文本
        bakerSynthesizer.setText(editText.getText().toString().trim());
        //设置返回数据的callback
        bakerSynthesizer.setBakerCallback(bakerCallback);
        /**********************以下是选填参数**************************/
        //设置发音人声音名称，默认：标准合成_模仿儿童_果子
        bakerSynthesizer.setVoice(edtVoice.getText().toString().trim());
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
        bakerSynthesizer.setEnableTimestamp(true);
    }

    public void startSynthesizer(View view) {
        //开始合成，合成结束后会自动stop
        setParams();
        bakerSynthesizer.start();
    }

    public void stopSynthesizer(View view) {
        audioTrackPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        audioTrackPlayer.stop();
        if (bakerSynthesizer != null) {
            bakerSynthesizer.onDestroy();
        }
        super.onDestroy();
    }
}