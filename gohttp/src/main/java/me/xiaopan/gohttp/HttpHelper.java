package me.xiaopan.gohttp;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.xiaopan.gohttp.requestobject.Method;
import me.xiaopan.gohttp.requestobject.Request;
import me.xiaopan.gohttp.requestobject.RequestParser;

/**
 * HttpHelper
 */
public class HttpHelper {
    int progressCallbackNumber; // 进度回调次数，例如为100，那么在整个下载过程将分100次回调
    String url; // 请求地址
    String name;    // 本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    GoHttp goHttp;
    MethodType method;  // 请求方式
    HttpEntity entity;  // Http请求体
    HttpRequest.Listener listener;
    CacheConfig cacheConfig;    // 响应缓存配置
    List<Header> headers;   // 请求头信息
    List<String> cacheIgnoreParamNames;	// 计算缓存ID时候忽略的参数名称集
    RequestParams params;   // 请求参数
    HttpRequest.ProgressListener progressListener;  // 进度监听器
    HttpResponseHandler responseHandler;    // 响应处理器
    HttpRequest.ResponseHandleCompletedAfterListener responseHandleCompletedAfterListener;  // 响应处理完成之后

    public HttpHelper(GoHttp goHttp, String url, HttpResponseHandler responseHandler, HttpRequest.Listener listener){
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

    public HttpHelper(GoHttp goHttp, Request requestObject, HttpResponseHandler responseHandler, HttpRequest.Listener listener){
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
    public HttpHelper method(MethodType method){
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
    public HttpHelper name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 添加请求头
     * @param headers 请求头集合
     */
    public HttpHelper addHeader(Header... headers){
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
    public HttpHelper addParam(String key, String value){
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
    public HttpHelper addParam(String key, ArrayList<String> values){
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
    public HttpHelper addParam(String key, File value){
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
    public HttpHelper addParam(String key, InputStream value){
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
    public HttpHelper addParam(String key, InputStream value, String fileName){
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
    public HttpHelper addParam(String key, InputStream value, String fileName, String contentType){
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
    public HttpHelper setParams(RequestParams params) {
        this.params = params;
        return this;
    }

    /**
     * 设置缓存配置
     * @param cacheConfig 缓存配置
     */
    public HttpHelper cacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }

    /**
     * 设置请求实体
     * @param httpEntity 请求实体
     */
    public HttpHelper entity(HttpEntity httpEntity) {
        this.entity = httpEntity;
        return this;
    }

    /**
     * 添加计算缓存ID时候忽略的参数的名称
     * @param cacheIgnoreParamName 计算缓存ID时候忽略的参数的名称
     */
    public HttpHelper addCacheIgnoreParamName(String cacheIgnoreParamName) {
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
    public HttpHelper progressListener(HttpRequest.ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * 设置进度回调次数
     * @param progressCallbackNumber 进度回调次数，默认值100。意思是在整合读取数据的过程中分100次回调进度，效果就是每读取百分之一回调一次
     */
    public HttpHelper progressCallbackNumber(int progressCallbackNumber) {
        this.progressCallbackNumber = progressCallbackNumber;
        return this;
    }

    /**
     * Response处理完成之后，会在异步线程中执行ResponseHandleCompletedAfterListener，目的是为了方便在异步线程中进一步对结果进行处理
     */
    public HttpHelper responseHandleCompletedAfterListener(HttpRequest.ResponseHandleCompletedAfterListener responseHandleCompletedAfterListener) {
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
            if(goHttp.isDebugMode()){
                Log.d(GoHttp.LOG_TAG, "未知的Method Type，已设为默认值GET");
            }
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
