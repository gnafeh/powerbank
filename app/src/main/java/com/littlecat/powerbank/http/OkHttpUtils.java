package com.littlecat.powerbank.http;

import android.graphics.Bitmap;

import com.littlecat.powerbank.http.OkHttpRequest.HttpMethodType;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class OkHttpUtils {

    private static OkHttpUtils mInstance;
    private static OkHttpClient mOkHttpClient;
    private static Platform mPlatform;

    private OkHttpUtils(OkHttpClient okHttpClient) {
        if (okHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
        } else {
            mOkHttpClient = okHttpClient;
        }
        mPlatform = Platform.get();
    }

    public static OkHttpUtils initClient(OkHttpClient okHttpClient) {
        if (mInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtils(okHttpClient);
                }
            }
        }
        return mInstance;
    }

    public static OkHttpUtils getInstance() {
        return initClient(null);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public static void sendFailResultCallback(final int code, final String message, final HttpCallback callback) {
        if (callback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(code, message);
            }
        });
    }

    public static void sendSuccessResultCallback(final ResultDesc result, final HttpCallback callback) {
        if (callback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result);
            }
        });
    }

    public static void sendProgressResultCallback(final long currentTotalLen, final long totalLen, final HttpCallback callback) {
        if (callback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onProgress(currentTotalLen, totalLen);
            }
        });
    }

    public static void sendBitmapSuccessResultCallback(final Bitmap bitmap, final HttpCallback callback) {
        if (callback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onBitmapSuccess(bitmap);
            }
        });
    }


    /**
     * @param url      请求地址
     * @param callback 请求回调
     * @Description GET请求
     */
    public static void getSync(String url, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doExecute(request, callback);
    }

    /**
     * @param url      请求地址
     * @param params   请求参数
     * @param callback 请求回调
     * @Description GET请求
     */
    public static void getSync(String url, Map<String, String> params, HttpCallback callback) {
        if (params != null && !params.isEmpty()) {
            url = OkHttpRequest.appendGetParams(url, params);
        }
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doExecute(request, callback);
    }

    /**
     * @param url      请求地址
     * @param params   请求参数
     * @param callback 请求回调
     * @Description POST请求
     */
    public static void postSync(String url,String json, Map<String, String> params, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.POST, url, params, json);
        OkHttpRequest.doExecute(request, callback);
    }

    /**--------------------    异步数据请求    --------------------**/

    /**
     * @param url      请求地址
     * @param callback 请求回调
     * @Description GET请求
     */
    public static void getAsyn(String url, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doEnqueue(request, callback);
    }

    /**
     * @param url      请求地址
     * @param params   请求参数
     * @param callback 请求回调
     * @Description GET请求
     */
    public static void getAsyn(String url, Map<String, String> params, HttpCallback callback) {
        if (params != null && !params.isEmpty()) {
            url = OkHttpRequest.appendGetParams(url, params);
        }
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doEnqueue(request, callback);
    }


    public static void postAsyn(String url, Map<String, String> params,HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.POST, url, params,null);
        OkHttpRequest.doEnqueue(request, callback);
    }

    public static void postAsync(String url, String json, Map<String, String> params,HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.POST, url, params, json);
        OkHttpRequest.doEnqueue(request, callback);
    }

    public static void postAync(String url, String json, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.POST, url, null, json);
        OkHttpRequest.doEnqueue(request, callback);
    }


    public static void postAsynFile(String url, File file, HttpCallback callback) {
        if (!file.exists()) {
//            ToastUtil.showText(UIUtils.getString(R.string.file_does_not_exist));
            return;
        }
        Request request = OkHttpRequest.builderFileRequest(url, file, null, null, null, callback);
        OkHttpRequest.doEnqueue(request, callback);
    }

    public static void postAsynFiles(String url, String pic_key, List<File> files, Map<String, String> params, HttpCallback callback) {
        Request request = OkHttpRequest.builderFileRequest(url, null, pic_key, files, params, callback);
        OkHttpRequest.doEnqueue(request, callback);
    }

    public static void downloadAsynFile(String url, String destFileDir, String destFileName, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doDownloadEnqueue(request, destFileDir, destFileName, callback);
    }


    public void downloadAsynFile(String url, String destFileDir, String destFileName, Map<String, String> params, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.POST, url, params, null);
        OkHttpRequest.doDownloadEnqueue(request, destFileDir, destFileName, callback);
    }


    public static void displayAsynImage(String url, HttpCallback callback) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doDisplayEnqueue(request, callback);
    }

    public static void postAsynStream(String url, String content, HttpCallback callback) {
        Request request = OkHttpRequest.builderStreamRequest(url, content);
        OkHttpRequest.doEnqueue(request, callback);
    }

    public static void websocket(String url) {
        Request request = OkHttpRequest.builderRequest(HttpMethodType.GET, url, null, null);
        OkHttpRequest.doNewWebSocket(request);
    }


    public static void cancelTag(Object tag) {
        if (tag == null) {
            return;
        }
        synchronized (mOkHttpClient.dispatcher().getClass()) {
            for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
            for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }
}