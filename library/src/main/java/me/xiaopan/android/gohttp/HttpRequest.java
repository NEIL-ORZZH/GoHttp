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

package me.xiaopan.android.gohttp;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import me.xiaopan.android.gohttp.requestobject.Method;
import me.xiaopan.android.gohttp.requestobject.Request;
import me.xiaopan.android.gohttp.requestobject.RequestParser;

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
    private Listener<?> listener;
    private CacheConfig cacheConfig;    // 响应缓存配置
    private List<Header> headers;   // 请求头信息
    private List<String> cacheIgnoreParamNames;	// 计算缓存ID时候忽略的参数名称集
    private RequestParams params;   // 请求参数
    private ProgressListener progressListener;
    private HttpResponseHandler responseHandler;    // 响应处理器
    private ResponseHandleCompletedAfterListener<?> responseHandleCompletedAfterListener;

    private boolean canceled;   // 是否已经取消
    private boolean finished;   // 是否已经完成
    private boolean stopReadData;   // 取消的时候是否立即停止读取数据

    private HttpRequest(Helper helper){
        this.url = helper.url;
        this.name = helper.name;
        this.goHttp = helper.goHttp;
        this.params = helper.params;
        this.method = helper.method;
        this.headers = helper.headers;
        this.listener = helper.listener;
        this.httpEntity = helper.entity;
        this.cacheConfig = helper.cacheConfig;
        this.responseHandler = helper.responseHandler;
        this.progressListener = helper.progressListener;
        this.cacheIgnoreParamNames = helper.cacheIgnoreParamNames;
        this.progressCallbackNumber = helper.progressCallbackNumber;
        this.responseHandleCompletedAfterListener = helper.responseHandleCompletedAfterListener;

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
    public Listener<?> getListener() {
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
    public ResponseHandleCompletedAfterListener<?> getResponseHandleCompletedAfterListener() {
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
     * 请求帮手
     */
    public static class Helper {
        private int progressCallbackNumber; // 进度回调次数，例如为100，那么在整个下载过程将分100次回调
        private String url; // 请求地址
        private String name;    // 本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
        private GoHttp goHttp;
        private MethodType method;  // 请求方式
        private HttpEntity entity;  // Http请求体
        private Listener<?> listener;
        private CacheConfig cacheConfig;    // 响应缓存配置
        private List<Header> headers;   // 请求头信息
        private List<String> cacheIgnoreParamNames;	// 计算缓存ID时候忽略的参数名称集
        private RequestParams params;   // 请求参数
        private ProgressListener progressListener;  // 进度监听器
        private HttpResponseHandler responseHandler;    // 响应处理器
        private ResponseHandleCompletedAfterListener<?> responseHandleCompletedAfterListener;  // 响应处理完成之后

        public Helper(GoHttp goHttp, String url, HttpResponseHandler responseHandler, Listener<?> listener){
            if(goHttp == null){
                throw new IllegalArgumentException("goHttp is null");
            }
            if(url == null){
                throw new IllegalArgumentException("url is null");
            }
            if(responseHandler == null){
                throw new IllegalArgumentException("responseObject is null");
            }
            if(listener == null){
                throw new IllegalArgumentException("listener is null");
            }
            this.url = url;
            this.goHttp = goHttp;
            this.method = MethodType.GET;
            this.listener = listener;
            this.responseHandler = responseHandler;
            this.progressCallbackNumber = 100;
        }

        public Helper(GoHttp goHttp, Request requestObject, HttpResponseHandler responseHandler, Listener<?> listener){
            if(goHttp == null){
                throw new IllegalArgumentException("goHttp is null");
            }
            if(requestObject == null){
                throw new IllegalArgumentException("requestObject is null");
            }
            if(responseHandler == null){
                throw new IllegalArgumentException("responseObject is null");
            }
            if(listener == null){
                throw new IllegalArgumentException("listener is null");
            }
            this.goHttp = goHttp;
            this.listener = listener;
            this.responseHandler = responseHandler;
            this.progressCallbackNumber = 100;
            parseRequestObject(requestObject);
        }

        /**
         * 设置请求方式
         * @param method 请求方式
         */
        public Helper method(MethodType method){
            if(method == null){
                throw new IllegalArgumentException("method is null");
            }
            this.method = method;
            return this;
        }

        /**
         * 设置请求名称
         * @param name 请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
         */
        public Helper name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 添加请求头
         * @param headers 请求头集合
         */
        public Helper addHeader(Header... headers){
            if(headers != null && headers.length > 0){
                if(this.headers == null){
                    this.headers = new LinkedList<Header>();
                }
                for(Header header : headers){
                    if(header != null){
                        this.headers.add(header);
                    }
                }
            }
            return this;
        }

        /**
         * 添加请求参数
         * @param key 键
         * @param value 值
         */
        public Helper addParam(String key, String value){
            if(key != null && !"".equals(key) && value != null && !"".equals(value)){
                if(params == null){
                    params = new RequestParams();
                }
                params.put(key, value);
            }
            return this;
        }

        /**
         * 添加请求参数
         * @param key 键
         * @param values 值
         */
        public Helper addParam(String key, ArrayList<String> values){
            if(key != null && !"".equals(key) && values != null && values.size() > 0){
                if(params == null){
                    params = new RequestParams();
                }
                params.put(key, values);
            }
            return this;
        }

        /**
         * 添加请求参数
         * @param key 键
         * @param value 值
         */
        public Helper addParam(String key, File value){
            if(key != null && !"".equals(key) && value != null && value.exists()){
                if(params == null){
                    params = new RequestParams();
                }
                try{
                    params.put(key, value);
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }
            }
            return this;
        }

        /**
         * 添加请求参数
         * @param key 键
         * @param value 值
         */
        public Helper addParam(String key, InputStream value){
            if(key != null && !"".equals(key) && value != null){
                if(params == null){
                    params = new RequestParams();
                }
                params.put(key, value);
            }
            return this;
        }

        /**
         * 添加请求参数
         * @param key 键
         * @param value 值
         * @param fileName 文件名
         */
        public Helper addParam(String key, InputStream value, String fileName){
            if(key != null && !"".equals(key) && fileName != null && !"".equals(fileName) && value != null){
                if(params == null){
                    params = new RequestParams();
                }
                params.put(key, value, fileName);
            }
            return this;
        }

        /**
         * 添加请求参数
         * @param key 键
         * @param value 值
         * @param fileName 文件名
         * @param contentType 文件类型
         */
        public Helper addParam(String key, InputStream value, String fileName, String contentType){
            if(key != null && !"".equals(key) && fileName != null && !"".equals(fileName) && contentType != null && !"".equals(contentType) && value != null){
                if(params == null){
                    params = new RequestParams();
                }
                params.put(key, value, fileName, contentType);
            }
            return this;
        }

        /**
         * 设置请求参数集
         * @param params 请求参数集
         */
        public Helper setParams(RequestParams params) {
            this.params = params;
            return this;
        }

        /**
         * 设置缓存配置
         * @param cacheConfig 缓存配置
         */
        public Helper cacheConfig(CacheConfig cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }

        /**
         * 设置请求实体
         * @param httpEntity 请求实体
         */
        public Helper entity(HttpEntity httpEntity) {
            this.entity = httpEntity;
            return this;
        }

        /**
         * 添加计算缓存ID时候忽略的参数的名称
         * @param cacheIgnoreParamName 计算缓存ID时候忽略的参数的名称
         */
        public Helper addCacheIgnoreParamName(String cacheIgnoreParamName) {
            if(cacheIgnoreParamNames == null){
                cacheIgnoreParamNames = new ArrayList<String>();
            }
            cacheIgnoreParamNames.add(cacheIgnoreParamName);
            return this;
        }

        /**
         * 设置进度监听器
         * @param progressListener 进度监听器
         */
        public Helper progressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        /**
         * 设置进度回调次数
         * @param progressCallbackNumber 进度回调次数，默认值100。意思是在整合读取数据的过程中分100次回调进度，效果就是每读取百分之一回调一次
         */
        public Helper progressCallbackNumber(int progressCallbackNumber) {
            this.progressCallbackNumber = progressCallbackNumber;
            return this;
        }

        /**
         * Response处理完成之后，会在异步线程中执行ResponseHandleCompletedAfterListener，目的是为了方便在异步线程中进一步对结果进行处理
         */
        public Helper responseHandleCompletedAfterListener(ResponseHandleCompletedAfterListener<?> responseHandleCompletedAfterListener) {
            this.responseHandleCompletedAfterListener = responseHandleCompletedAfterListener;
            return this;
        }

        /**
         * 发送请求
         */
        public HttpRequestFuture go(){
            return goHttp.go(new HttpRequest(this));
        }

        private void parseRequestObject(Request request){
            Class<? extends Request> requestClass = request.getClass();

            // 解析请求方式
            Method method = requestClass.getAnnotation(Method.class);
            if(method != null){
                this.method = method.value();
            }
            if(this.method == null){
                Log.e(GoHttp.LOG_TAG, "未知的Method，已设为默认值GET");
                this.method = MethodType.GET;
            }

            // 解析URL
            this.url = RequestParser.parseBaseUrl(goHttp.getContext(), requestClass);
            if(!(url != null && !"".equals(url))){
                throw new IllegalArgumentException("你必须在Request上使用Url注解或者Host加Path注解指定请求地址");
            }

            // 解析请求名称
            String requestName = RequestParser.parseNameAnnotation(goHttp.getContext(), requestClass);
            if(requestName != null && !"".equals(requestName)){
                this.name = this.name + " "+requestName;
            }else{
                this.name = requestName;
            }

            // 解析请求参数
            this.params = RequestParser.parseRequestParams(goHttp.getContext(), request, this.params);

            // 解析请求头
            addHeader(RequestParser.parseRequestHeaders(request));

            // GET、POST、PUT请求方式还要解析缓存
            if(this.method == MethodType.GET || this.method == MethodType.POST || this.method == MethodType.PUT){
                this.cacheIgnoreParamNames = RequestParser.parseCacheIgnoreParams(goHttp.getContext(), requestClass);
                this.cacheConfig = RequestParser.parseResponseCacheAnnotation(goHttp.getContext(), requestClass);
            }
        }
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
        public void onResponseHandleAfter(HttpRequest httpRequest, HttpResponse httpResponse, T responseContent, boolean isCache, boolean isContinueCallback);
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
