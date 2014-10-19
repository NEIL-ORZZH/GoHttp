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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import me.xiaopan.android.gohttp.annotation.Method;
import me.xiaopan.android.gohttp.enums.MethodType;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.util.Log;

/**
 * Http客户端，所有的Http操作都将由此类来异步完成，同时此类提供一个单例模式来方便直接使用
 */
public class GoHttp {
	private static GoHttp instance;
	private Configuration configuration;	//配置
    private Map<Object, List<RequestFuture>> requestMap;	//请求Map
	
    public GoHttp(Context context){
    	configuration = new Configuration(context);
    	requestMap = new WeakHashMap<Object, List<RequestFuture>>();
    }
    
	/**
	 * 获取实例
	 * @return 实例
	 */
	public static GoHttp with(Context context){
		if(instance == null){
            synchronized(GoHttp.class){
		        if(instance == null){
			        instance = new GoHttp(context);
                }
            }
		}
		return instance;
	}

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param name 请求名称，在后台输出log的时候会输出此名称方便区分请求
     * @param cacheConfig 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture execute(HttpUriRequest httpRequest, String name, CacheConfig cacheConfig, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	HttpRequestExecuteRunnable httpRequestRunnable = new HttpRequestExecuteRunnable(getConfiguration(), name, httpRequest, cacheConfig, httpResponseHandler);
    	getConfiguration().getExecutorService().submit(httpRequestRunnable);
        RequestFuture requestFuture = new RequestFuture(httpRequestRunnable);
        if(requestTag != null) {
            List<RequestFuture> requestList = requestMap.get(requestTag);
            if(requestList == null) {
                requestList = new LinkedList<RequestFuture>();
                requestMap.put(requestTag, requestList);
            }
            requestList.add(requestFuture);
        }
        return requestFuture;
    }

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param name 请求名称，在后台输出log的时候会输出此名称方便区分请求
     * @param cacheConfig 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture execute(HttpUriRequest httpRequest, String name, CacheConfig cacheConfig, HttpResponseHandler httpResponseHandler) {
        return execute(httpRequest, name, cacheConfig, httpResponseHandler, null);
    }

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture execute(HttpUriRequest httpRequest, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	return execute(httpRequest, null, null, httpResponseHandler, requestTag);
    }

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture execute(HttpUriRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        return execute(httpRequest, null, null, httpResponseHandler, null);
    }

    /**
     * 执行请求
     * @param request 请求对象，将通过请求对象来解析出一个Http请求
     * @param httpResponseHandler http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture execute(Request request, HttpResponseHandler httpResponseHandler, Object requestTag){
        if(request != null){
            /* 解析请求方式 */
            MethodType methodType = MethodType.GET;
            Method method = request.getClass().getAnnotation(Method.class);
            if(method != null){
                methodType = method.value();
            }

            //根据不同的请求方式选择不同的方法执行
            if(methodType == MethodType.GET){
            	 return get(HttpGetRequest.valueOf(configuration.getContext(), request), httpResponseHandler, requestTag);
            }else if(methodType == MethodType.POST){
            	 return post(HttpPostRequest.valueOf(configuration.getContext(), request), httpResponseHandler, requestTag);
            }else if(methodType == MethodType.PUT){
            	 return put(HttpPutRequest.valueOf(configuration.getContext(), request), httpResponseHandler, requestTag);
            }else if(methodType == MethodType.DELETE){
            	 return delete(HttpDeleteRequest.valueOf(configuration.getContext(), request), httpResponseHandler, requestTag);
            }else{
            	 return null;
            }
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("request 不能为null");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }

    /**
     * 执行请求
     * @param request 请求对象，将通过请求对象来解析出一个Http请求
     * @param httpResponseHandler http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture execute(Request request, HttpResponseHandler httpResponseHandler){
        return execute(request, httpResponseHandler, null);
    }
	
    /**
     * 执行一个Get请求
     * @param httpRequest Http Get请求
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture get(HttpGetRequest httpRequest, HttpResponseHandler httpResponseHandler, Object requestTag) {
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpGet httGet = new HttpGet(HttpUtils.getUrlByParams(getConfiguration().isUrlEncodingEnabled(), httpRequest.getBaseUrl(), httpRequest.getParams()));
            HttpUtils.appendHeaders(httGet, httpRequest.getHeaders());
            if(httpRequest.getCacheConfig() != null && GeneralUtils.isEmpty(httpRequest.getCacheConfig().getId())){
            	httpRequest.getCacheConfig().setId(GeneralUtils.createCacheId(httpRequest.getCacheConfig(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()));
            }
            return execute(httGet, httpRequest.getName(), httpRequest.getCacheConfig(), httpResponseHandler, requestTag);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }
	
    /**
     * 执行一个Get请求
     * @param httpRequest Http Get请求
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture get(HttpGetRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        return get(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture get(String url, RequestParams params, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	 return get(new HttpGetRequest(url).setParams(params), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture get(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	 return get(new HttpGetRequest(url).setParams(params), httpResponseHandler, null);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture get(String url, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	 return get(new HttpGetRequest(url), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture get(String url, HttpResponseHandler httpResponseHandler) {
    	 return get(new HttpGetRequest(url), httpResponseHandler, null);
    }
    
    /**
     * 执行一个Post请求
     * @param httpRequest Http Post请求
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture post(HttpPostRequest httpRequest, HttpResponseHandler httpResponseHandler, Object requestTag){
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpPost httPost = new HttpPost(httpRequest.getBaseUrl());
            HttpUtils.appendHeaders(httPost, httpRequest.getHeaders());

            HttpEntity httpEntity = httpRequest.getHttpEntity();
            if(httpEntity == null && httpRequest.getParams() != null){
                if(getConfiguration().isDebugMode()){
                    Log.d(getConfiguration().getLogTag(), new StringBuilder().append(httpRequest.getName()).append(" ").append("请求实体").append("：").append(httpRequest.getParams().toString()).toString());
                }
                httpEntity = httpRequest.getParams().getEntity();
            }
            if(httpEntity != null){
                httPost.setEntity(httpEntity);
            }
            if(httpRequest.getCacheConfig() != null && GeneralUtils.isEmpty(httpRequest.getCacheConfig().getId())){
            	httpRequest.getCacheConfig().setId(GeneralUtils.createCacheId(httpRequest.getCacheConfig(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()));
            }
            return execute(httPost, httpRequest.getName(), httpRequest.getCacheConfig(), httpResponseHandler, requestTag);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }
    
    /**
     * 执行一个Post请求
     * @param httpRequest Http Post请求
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture post(HttpPostRequest httpRequest, HttpResponseHandler httpResponseHandler){
        return post(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture post(String url, RequestParams params, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	 return post(new HttpPostRequest(url).setParams(params), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture post(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	 return post(new HttpPostRequest(url).setParams(params), httpResponseHandler, null);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture post(String url, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	 return post(new HttpPostRequest(url), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture post(String url, HttpResponseHandler httpResponseHandler) {
    	 return post(new HttpPostRequest(url), httpResponseHandler, null);
    }

    /**
     * 执行一个Put请求
     * @param httpRequest Http Put请求
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture put(HttpPutRequest httpRequest, HttpResponseHandler httpResponseHandler, Object requestTag){
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpPut httPut = new HttpPut(httpRequest.getBaseUrl());
            HttpUtils.appendHeaders(httPut, httpRequest.getHeaders());

            HttpEntity httpEntity = httpRequest.getHttpEntity();
            if(httpEntity == null && httpRequest.getParams() != null){
                if(getConfiguration().isDebugMode()){
                    Log.d(getConfiguration().getLogTag(), new StringBuilder().append(httpRequest.getName()).append(" ").append("请求实体").append("：").append(httpRequest.getParams().toString()).toString());
                }
                httpEntity = httpRequest.getParams().getEntity();
            }
            if(httpEntity != null){
                httPut.setEntity(httpEntity);
            }
            if(httpRequest.getCacheConfig() != null && GeneralUtils.isEmpty(httpRequest.getCacheConfig().getId())){
            	httpRequest.getCacheConfig().setId(GeneralUtils.createCacheId(httpRequest.getCacheConfig(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()));
            }
            return execute(httPut, httpRequest.getName(), httpRequest.getCacheConfig(), httpResponseHandler, requestTag);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }

    /**
     * 执行一个Put请求
     * @param httpRequest Http Put请求
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture put(HttpPutRequest httpRequest, HttpResponseHandler httpResponseHandler){
        return put(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture put(String url, RequestParams params, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	 return put(new HttpPutRequest(url).setParams(params), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture put(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	 return put(new HttpPutRequest(url).setParams(params), httpResponseHandler, null);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture put(String url, HttpResponseHandler httpResponseHandler, Object requestTag) {
    	 return put(new HttpPutRequest(url), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture put(String url, HttpResponseHandler httpResponseHandler) {
    	 return put(new HttpPutRequest(url), httpResponseHandler, null);
    }

    /**
     * 执行一个Delete请求
     * @param httpRequest Http Delete请求
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture delete(HttpDeleteRequest httpRequest, HttpResponseHandler httpResponseHandler, Object requestTag) {
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpDelete httDelete = new HttpDelete(HttpUtils.getUrlByParams(getConfiguration().isUrlEncodingEnabled(), httpRequest.getBaseUrl(), httpRequest.getParams()));
            HttpUtils.appendHeaders(httDelete, httpRequest.getHeaders());
            return execute(httDelete, httpRequest.getName(), null, httpResponseHandler, requestTag);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("你必须指定url");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }

    /**
     * 执行一个Delete请求
     * @param httpRequest Http Delete请求
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture delete(HttpDeleteRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        return delete(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Delete请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param requestTag 给当前请求打上一个标签，稍后你可以通过cancelRequests()方法传入这个标签来取消请求。
     *                <br>通过此功能你可以实现批量取消请求，比如：同一个Activity中你都用同一个requestTag提交请求，那么在Activity销毁的时候你就可以通过这个requestTag取消与之相关的所有请求
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture delete(String url, HttpResponseHandler httpResponseHandler, Object requestTag) {
        return delete(new HttpDeleteRequest(url), httpResponseHandler, requestTag);
    }

    /**
     * 执行一个Delete请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return 请求处理对象，你可以通过此对象取消请求或判断请求是否完成
     */
    public RequestFuture delete(String url, HttpResponseHandler httpResponseHandler) {
        return delete(new HttpDeleteRequest(url), httpResponseHandler, null);
    }

    /**
     * 取消所有的请求，请求如果尚未开始就不再执行，如果已经开始就尝试中断
     * <br>你可以在onDestory的时候调用此方法来取消与之相关的所有请求
     * @param requestTag 请求标签
     * @param isStopReadData 如果有请求正在运行中的话是否立即停止读取数据
     */
    public void cancelRequests(Object requestTag, boolean isStopReadData) {
        List<RequestFuture> requestList = requestMap.get(requestTag);
        if(requestList != null) {
            for(RequestFuture requestFuture : requestList) {
                requestFuture.cancel(isStopReadData);
            }
        }
        requestMap.remove(requestTag);
    }

    /**
     * 获取配置
     * @return 配置
     */
	public Configuration getConfiguration() {
		return configuration;
	}
}