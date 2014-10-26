package me.xiaopan.android.gohttp;

import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * 网络管理器
 */
public interface NetManager {
    public HttpResponse getHttpResponse(HttpRequest request) throws Exception;
}
