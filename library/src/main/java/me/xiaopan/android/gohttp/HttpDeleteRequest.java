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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

import android.content.Context;

/**
 * Http Delete请求
 */
public class HttpDeleteRequest {
    private String name;    //本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private String baseUrl; //请求地址
    private List<Header> headers;   //请求头信息
    private RequestParams params;   //请求参数

    public HttpDeleteRequest(String baseUrl){
        this.baseUrl = baseUrl;
        setName(GeneralUtils.getCurrentDateTimeBy24Hour() + " DELETE");
    }

    /**
     * 获取请求名称
     * @return 请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
     */
    public String getName() {
        return name;
    }

    /**
     * 设置请求名称
     * @param name 请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
     */
    public HttpDeleteRequest setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 获取基本的地址
     * @return 基本的地址
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置基本的地址
     * @param baseUrl 基本的地址
     */
    public HttpDeleteRequest setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * 获取所有的请求头
     * @return 所有的请求头
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * 批量添加请求头
     * @param headers 请求头集合
     */
    public HttpDeleteRequest addHeader(Header... headers){
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
     * 设置请求头
     * @param headers 请求头集合
     */
    public HttpDeleteRequest setHeaders(List<Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 获取请求参数集
     * @return 请求参数集
     */
    public RequestParams getParams() {
        return params;
    }

    /**
     * 添加请求参数
     * @param key 键
     * @param value 值
     */
    public HttpDeleteRequest addParam(String key, String value){
        if(GeneralUtils.isNotEmpty(key, value)){
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
    public HttpDeleteRequest addParam(String key, ArrayList<String> values){
        if(GeneralUtils.isNotEmpty(key) && values != null && values.size() > 0){
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
    public HttpDeleteRequest addParam(String key, File value){
        if(GeneralUtils.isNotEmpty(key) && value != null && value.exists()){
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
    public HttpDeleteRequest addParam(String key, InputStream value){
        if(GeneralUtils.isNotEmpty(key) && value != null){
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
     * @param fileName 文件名称
     */
    public HttpDeleteRequest addParam(String key, InputStream value, String fileName){
        if(GeneralUtils.isNotEmpty(key, fileName) && value != null){
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
    public HttpDeleteRequest addParam(String key, InputStream value, String fileName, String contentType){
        if(GeneralUtils.isNotEmpty(key, fileName, contentType) && value != null){
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
    public HttpDeleteRequest setParams(RequestParams params) {
        this.params = params;
        return this;
    }


    /**
     * 通过解析一个请求对象来创建一个Http Delete 请求
     * @param context 上下文
     * @param request 请求对象
     * @return Http Delete 请求
     */
    public static HttpDeleteRequest valueOf(Context context, Request request){
        Class<? extends Request> requestClass = request.getClass();

        String baseUrl1 = RequestParser.parseBaseUrl(context, requestClass);
        if(GeneralUtils.isEmpty(baseUrl1)){
            throw new IllegalArgumentException("你必须在Request上使有Url注解或者Host加Path注解指定请求地址");
        }
        HttpDeleteRequest httpRequest = new HttpDeleteRequest(baseUrl1);

        String requestName = RequestParser.parseNameAnnotation(context, requestClass);
        if(GeneralUtils.isNotEmpty(requestName)){
            httpRequest.setName(httpRequest.getName() + " " + requestName);
        }

        httpRequest.setParams(RequestParser.parseRequestParams(context, request, httpRequest.getParams()));
        httpRequest.addHeader(RequestParser.parseRequestHeaders(request));

        return httpRequest;
    }
}
