package com.biaobei.sdk.android.demo;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.databaker.synthesizer.bean.Token;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Author yanteng on 2020/9/3.
 * @Email 1019395018@qq.com
 */

public class NetUtils {
    private static Gson gson = new Gson();
    private static String url = "https://openapi.data-baker.com/oauth/2.0/token?grant_type=client_credentials&client_secret=%s&client_id=%s";
    private static OkHttpClient mClient;
    private static Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String s);

        void onError(String s);
    }

    public static void getToken(String mSecret, String mClientId, final Callback listener) {
        if (null == mClient) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            // 允许重定向
            clientBuilder.followRedirects(true);

            // https支持
            clientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            mClient = clientBuilder.build();
        }
        mClient.newCall(new Request.Builder().url(String.format(url, mSecret, mClientId)).get().build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (listener != null) {
                    final Token token = gson.fromJson(response.body().string(), Token.class);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (token != null && !TextUtils.isEmpty(token.getAccess_token())) {
                                listener.onSuccess("获取成功");
                            } else {
                                listener.onError("获取失败");
                            }
                        }
                    });

                }
            }
        });
    }
}
