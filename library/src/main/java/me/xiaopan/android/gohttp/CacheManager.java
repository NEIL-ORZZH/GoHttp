/*
 * Copyright 2013 Peng fei Pan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
