package com.zhaoyuntao.androidutils.tools;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ZHttp {
    private OkHttpClient client;

    private static ZHttp zHttp;

    private ZHttp() {
        client = new OkHttpClient();
    }

    public static ZHttp getInstance() {
        if (zHttp == null) {
            synchronized (ZHttp.class) {
                if (zHttp == null) {
                    zHttp = new ZHttp();
                }
            }
        }
        return zHttp;
    }


    public void request(final String url, final CallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).get().build();
                Call call = client.newCall(request);

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        if (callBack != null) {
                            callBack.onFailure(e);
                        }
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (callBack != null) {
                            callBack.onResponse(response.body().bytes());
                        }
                    }
                });
            }
        }).start();

    }


    public interface CallBack {
        void onFailure(IOException e);

        void onResponse(byte[] result);
    }
}
