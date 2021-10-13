package io.agora.interactivepodcast.data;



import io.agora.interactivepodcast.AppApplication;
import io.agora.interactivepodcast.common.PreferenceManager;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class CmosRequestOkImpl<T> implements CmosRequest<T> {


    @Override
    public void requestPostAsyncWithHeader(String url, String jsonStringParmas, Object tag, Headers headers, final JsonCallback<T> requestCallback) {
        OkHttpClient client = AppApplication.getDefaultRequestClient();
        Request.Builder builder = getDefaultBuild(tag, url);
        RequestBody requestBody = null;
        if (jsonStringParmas != null) {
            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStringParmas);
        }
        builder.post(requestBody);
        if (headers != null && headers.size() > 0) {
            builder.headers(headers);
        }

        Call call = client.newCall(builder.build());
        call.enqueue(requestCallback);
    }

    @Override
    public void requestPostAsyncWithToken(String url, String jsonStringParmas, Object tag, Headers headers, final JsonCallback<T> requestCallback) {
//        OkHttpClient client = AppApplication.getDefaultRequestClient();
//        Request.Builder builder = getDefaultBuild(tag, url);
//        RequestBody requestBody = null;
//        if (jsonStringParmas != null) {
//            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStringParmas);
//        }
//        builder.post(requestBody);
//        if (headers != null && headers.size() > 0) {
//            builder.headers(headers);
//        }
//
//        String token = PreferenceManager.getInstance().getSuperToken();
//        if (token == null || token.equals("")) {
//            requestCallback.onError(401, "token removed");
//            return;
//        }
//        String basicAuth = "Bearer " + token;
//        builder.addHeader("Authorization", basicAuth);
//        Call call = client.newCall(builder.build());
//        call.enqueue(requestCallback);
    }

    @Override
    public void requestPostAsyncWithUserToken(String url, String jsonStringParmas, Object tag, Headers headers, JsonCallback<T> requestCallback) {
//        OkHttpClient client = AppApplication.getDefaultRequestClient();
//        Request.Builder builder = getDefaultBuild(tag, url);
//        RequestBody requestBody = null;
//        if (jsonStringParmas != null) {
//            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStringParmas);
//        }
//        builder.post(requestBody);
//        if (headers != null && headers.size() > 0) {
//            builder.headers(headers);
//        }
//
//        String token = PreferenceManager.getInstance().getUserToken();
//        if (token == null || token.equals("")) {
//            requestCallback.onError(401, "token removed");
//            return;
//        }
//        String basicAuth = "Bearer " + token;
//        builder.addHeader("Authorization", basicAuth);
//        Call call = client.newCall(builder.build());
//        call.enqueue(requestCallback);
    }

    @Override
    public void requestAsyncWithAdminToken(String url, String jsonStringParmas, Object tag, Headers headers, int flag, JsonCallback<T> requestCallback) {
        OkHttpClient client = AppApplication.getDefaultRequestClient();
        Request.Builder builder = getDefaultBuild(tag, url);
        RequestBody requestBody = null;
        if (jsonStringParmas != null) {
            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStringParmas);
        }
        if (flag == 1){//禁言
            builder.post(requestBody);
        }else if(flag == 2){
            builder.delete(requestBody);
        }
        if (headers != null && headers.size() > 0) {
            builder.headers(headers);
        }

        String token = PreferenceManager.getInstance().getAdminToken();

        if (token == null || token.equals("")) {
            requestCallback.onError(401, "token removed");
            return;
        }
        String basicAuth = "Bearer " + token;
        builder.addHeader("Authorization", basicAuth);
        Call call = client.newCall(builder.build());
        call.enqueue(requestCallback);
    }


    @Override
    public void requestPutAsyncWithHeader(String url, String jsonStringParmas, Object tag, Headers headers, final JsonCallback<T> requestCallback) {
        OkHttpClient client = AppApplication.getDefaultRequestClient();
        Request.Builder builder = getDefaultBuild(tag, url);
        RequestBody requestBody = null;
        if (jsonStringParmas != null) {
            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStringParmas);
        }
        builder.put(requestBody);
        if (headers != null && headers.size() > 0) {
            builder.headers(headers);
        }

        String token = PreferenceManager.getInstance().getAdminToken();

        if (token == null || token.equals("")) {
            requestCallback.onError(401, "token removed");
            return;
        }
        String basicAuth = "Bearer " + token;
        builder.addHeader("Authorization", basicAuth);
        Call call = client.newCall(builder.build());
        call.enqueue(requestCallback);
    }


    @Override
    public void requestGetAsyncWithHeader(String url, Object tag, Headers headers, JsonCallback<T> requestCallback) {
        OkHttpClient client = AppApplication.getDefaultRequestClient();
        Request.Builder builder = getDefaultBuild(tag, url);
        builder.get();
        if (headers != null && headers.size() > 0) {
            builder.headers(headers);
        }
        String token = PreferenceManager.getInstance().getAdminToken();
        if (token == null || token.equals("")) {
            requestCallback.onError(401, "token removed");
            return;
        }
        String basicAuth = "Bearer " + token;
        builder.addHeader("Authorization", basicAuth);
        Call call = client.newCall(builder.build());
        call.enqueue(requestCallback);
    }


    @Override
    public void requestGetAsyncWithHeader(String url, Object tag, Headers headers, int flag, JsonCallback<T> requestCallback) {
        OkHttpClient client = AppApplication.getDefaultRequestClient();
        Request.Builder builder = getDefaultBuild(tag, url);
        builder.get();
        if (headers != null && headers.size() > 0) {
            builder.headers(headers);
        }
//        String token = PreferenceManager.getInstance().getAdminToken();
//        if (token == null || token.equals("")) {
//            requestCallback.onError(401, "token removed");
//            return;
//        }
//        String basicAuth = "Bearer " + token;
//        builder.addHeader("Authorization", basicAuth);

        Call call = client.newCall(builder.build());
        call.enqueue(requestCallback);
    }

    private Request.Builder getDefaultBuild(Object tag, String url) {
        Request.Builder builder = new Request.Builder();
        builder.tag(tag);
        builder.url(url);
        builder.header("Content-Type", "application/json");
        builder.header("Accept", "application/json");
        return builder;
    }

    @Override
    public void cancelRequest(Object tag) {

    }
}
