/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.gohttp;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Http请求
 */
public class HttpRequest{
    private int progressCallbackNumber; // 进度回调次数，例如为100，那么在整个下载过程将分100次回调
    private String name;    //本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private String url; //请求地址
    private GoHttp goHttp;
    private MethodType method;  // 请求方式
    private HttpEntity httpEntity;  // Http请求体
    private Listener listener;
    private CacheConfig cacheConfig;    // 响应缓存配置
    private List<Header> headers;   // 请求头信息
    private List<String> cacheIgnoreParamNames;	// 计算缓存ID时候忽略的参数名称集
    private RequestParams params;   // 请求参数
    private ProgressListener progressListener;
    private HttpResponseHandler responseHandler;    // 响应处理器
    private ResponseHandleCompletedAfterListener responseHandleCompletedAfterListener;

    private boolean canceled;   // 是否已经取消
    private boolean finished;   // 是否已经完成
    private boolean stopReadData;   // 取消的时候是否立即停止读取数据

    HttpRequest(HttpHelper httpHelper){
        this.url = httpHelper.url;
        this.name = httpHelper.name;
        this.goHttp = httpHelper.goHttp;
        this.params = httpHelper.params;
        this.method = httpHelper.method;
        this.headers = httpHelper.headers;
        this.listener = httpHelper.listener;
        this.httpEntity = httpHelper.entity;
        this.cacheConfig = httpHelper.cacheConfig;
        this.responseHandler = httpHelper.responseHandler;
        this.progressListener = httpHelper.progressListener;
        this.cacheIgnoreParamNames = httpHelper.cacheIgnoreParamNames;
        this.progressCallbackNumber = httpHelper.progressCallbackNumber;
        this.responseHandleCompletedAfterListener = httpHelper.responseHandleCompletedAfterListener;

        if(name == null){
            name = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(new Date()) + " "+method.name();
        }
        if(cacheConfig != null){
            cacheConfig.attachHttpRequest(this);
        }
    }

    /**
     * 获取GoHttp
     * @return GoHttp
     */
    public GoHttp getGoHttp() {
        return goHttp;
    }

    /**
     * 获取请求名称
     * @return 请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
     */
    public String getName() {
        return name;
    }

    /**
     * 获取请求地址
     * @return 请求地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取请求头
     * @return 请求头
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * 获取请求参数
     * @return 请求参数
     */
    public RequestParams getParams() {
        return params;
    }

    /**
     * 获取缓存配置
     * @return 缓存配置
     */
    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    /**
     * 获取请求实体
     * @return 请求实体
     */
    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    /**
     * 获取响应处理器
     */
    public HttpResponseHandler getResponseHandler() {
        return responseHandler;
    }

    /**
     * 是否已经取消了
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * 取消请求
     * @param stopReadData 如果当前正在读取数据是否立即停止
     */
    public void cancel(boolean stopReadData){
        this.canceled = true;
        this.stopReadData = stopReadData;
    }

    /**
     * 是否停止读取数据，循环读取数据的时候使用
     */
    public boolean isStopReadData() {
        return canceled && stopReadData;
    }

    /**
     * 获取计算缓存ID是忽略的参数的名称集合
     */
    public List<String> getCacheIgnoreParamNames() {
        return cacheIgnoreParamNames;
    }

    /**
     * 获取过程监听器
     * @return 过程监听器
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * 获取进度回调监听器
     * @return 进度回调监听器
     */
    public ProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * 获取请求方式
     * @return 请求方式
     */
    public MethodType getMethod() {
        return method;
    }

    /**
     * 获取ResponseHandleCompletedAfterListener
     * @return ResponseHandleCompletedAfterListener
     */
    public ResponseHandleCompletedAfterListener getResponseHandleCompletedAfterListener() {
        return responseHandleCompletedAfterListener;
    }

    /**
     * 是否已经完成了
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 设置请求已经完成了
     */
    public void finish() {
        this.finished = true;
    }

    /**
     * 获取进度回调次数，
     * @return 例如为100，那么在整个下载过程将分100次回调
     */
    public int getProgressCallbackNumber() {
        return progressCallbackNumber;
    }

    /**
     * 获取一个用于执行请求的任务
     * @return 一个用于执行请求的任务
     */
    public Runnable getExecuteRunnable(){
        return new HttpRequestHandler(this);
    }

    /**
     * 请求过程监听器
     * @param <T> 你想要把HttpResponseHandler返回的结果转成什么类型就直接在这里用泛型指定
     */
    public interface Listener<T>{
        /**
         * 请求开始
         * @param httpRequest 请求
         */
        public void onStarted(HttpRequest httpRequest);

        /**
         * 请求成功
         * @param httpRequest 请求
         * @param httpResponse 响应
         * @param responseContent 响应内容
         * @param isCache 本次响应是否是缓存数据
         * @param isContinueCallback 是否继续回调此方法
         */
        public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, T responseContent, boolean isCache, boolean isContinueCallback);

        /**
         * 请求失败
         * @param httpRequest 请求
         * @param httpResponse 响应
         * @param failure 失败信息
         * @param isCache 本次响应是否是缓存数据
         * @param isContinueCallback 是否继续回调此方法
         */
        public void onFailed(HttpRequest httpRequest, HttpResponse httpResponse, Failure failure, boolean isCache, boolean isContinueCallback);

        /**
         * 请求取消
         * @param httpRequest 请求
         */
        public void onCanceled(HttpRequest httpRequest);
    }

    /**
     * 进度监听器
     */
    public interface ProgressListener{
        /**
         * 更新进度
         * @param httpRequest 请求
         * @param totalLength 总长度
         * @param completedLength 已完成长度
         */
        public void onUpdateProgress(HttpRequest httpRequest, long totalLength, long completedLength);
    }

    /**
     * Response处理完成之后会在异步线程中回调ResponseHandleCompletedAfterListener，目的是为了方便在异步线程中进一步对结果进行处理
     * @param <T>
     */
    public interface ResponseHandleCompletedAfterListener<T>{
        public Object onResponseHandleAfter(HttpRequest httpRequest, HttpResponse httpResponse, T responseContent, boolean isCache, boolean isContinueCallback) throws Throwable;
    }

    /**
     * 请求失败，包含失败原因等相关信息
     */
    public static class Failure{
        private int code;
        private String message;
        private Throwable exception;

        /**
         * 创建一个由异常引起的失败对象
         * @param exception 引起失败的异常
         */
        public Failure(Throwable exception) {
            this.exception = exception;
        }

        /**
         * 创建一个正常失败对象
         * @param code 类型码
         * @param message 说明
         */
        public Failure(int code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * 获取类型码
         * @return 类型码
         */
        public int getCode() {
            return code;
        }

        /**
         * 获取说明
         * @return 说明
         */
        public String getMessage() {
            return message;
        }

        /**
         * 获取异常
         * @return 异常
         */
        public Throwable getException() {
            return exception;
        }

        /**
         * 判断失败是否是因为异常导致的，判断条件就是exception是否不为null
         * @return 失败是否是因为异常导致的
         */
        public boolean isException(){
            return exception != null;
        }

        @Override
        public String toString() {
            return isException()?exception.toString():code+" ( "+message+" ) ";
        }
    }
}
