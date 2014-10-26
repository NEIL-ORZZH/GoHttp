package me.xiaopan.android.gohttp;

import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * 缓存管理器
 */
public interface CacheManager {
    /**
     * 保存HTTP响应到本地缓存
     * @param request 请求
     * @param response 响应
     */
    public void saveHttpResponseToCache(HttpRequest request, HttpResponse response)  throws IOException;

    /**
     * 根据请求判断其是否有可用缓存
     * @param request 请求
     * @return 是否有可用缓存
     */
    public boolean isHasAvailableCache(HttpRequest request);

    /**
     * 从缓存中读取HTTP响应
     * @param request 请求
     * @return HTTP响应
     */
    public HttpResponse readHttpResponseFromCache(HttpRequest request);

    /**
     * 设置缓存目录
     * @param cacheDirectory 缓存目录
     */
    public void setCacheDirectory(String cacheDirectory);
}
