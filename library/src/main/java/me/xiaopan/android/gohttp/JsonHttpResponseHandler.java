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

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import me.xiaopan.android.gohttp.requestobject.ResponseBody;

/**
 * 默认的JSON Http响应处理器，用于JSON格式的字符串转成Java对象
 */
public class JsonHttpResponseHandler implements HttpResponseHandler {
    private Class<?> responseClass;
    private Type responseType;
    private boolean excludeFieldsWithoutExposeAnnotation;

    public JsonHttpResponseHandler(Class<?> responseClass, boolean excludeFieldsWithoutExposeAnnotation){
        if(responseClass == null){
            throw new IllegalArgumentException("responseClass cannot be null");
        }

        this.responseClass = responseClass;
        this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
    }

    public JsonHttpResponseHandler(Type responseType, boolean excludeFieldsWithoutExposeAnnotation){
        if(responseType == null){
            throw new IllegalArgumentException("responseType cannot be null");
        }

        this.responseType = responseType;
        this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
    }

    public JsonHttpResponseHandler(Class<?> responseClass){
        this(responseClass ,false);
    }

    public JsonHttpResponseHandler(Type responseType){
        this(responseType ,false);
    }

    @Override
    public boolean canCache(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300;
    }

    @Override
	public Object handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) throws Throwable{
        if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getStatusCode()+"："+ httpRequest.getUrl());
        }

        HttpEntity httpEntity = httpResponse.getEntity();
        if(httpEntity == null){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "HttpEntity is null");
        }

        String jsonString = toString(httpRequest, httpEntity, "UTF-8");

        if(httpRequest.isCanceled()){
            return null;
        }

        if(jsonString == null || "".equals(jsonString)){
            throw new Exception("响应内容为空："+httpRequest.getUrl());
        }

        Gson gson;
        if(excludeFieldsWithoutExposeAnnotation){
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        }else{
            gson = new Gson();
        }

        if(responseClass != null){	//如果是要转换成一个对象
            String readJson;
            String bodyName = parseResponseBodyAnnotation(httpRequest.getGoHttp().getContext(), responseClass);
            if(bodyName != null){
                readJson = new JSONObject(jsonString).getString(bodyName);
            }else{
                readJson = jsonString;
            }
            return gson.fromJson(readJson, responseClass);
        }else if(responseType != null){	//如果是要转换成一个集合
            return gson.fromJson(jsonString, responseType);
        }else{
            throw new Exception("responseClass和responseType至少有一个不能为null");
        }
	}

    private String toString(HttpRequest httpRequest, final HttpEntity entity, final String defaultCharset) throws IOException, ParseException {
        InputStream inputStream = entity.getContent();
        if (inputStream == null) {
            return "";
        }
        if (entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        int contentLength = (int)entity.getContentLength();
        if (contentLength < 0) {
            contentLength = 4096;
        }
        String charset = StringHttpResponseHandler.getContentCharSet(entity);
        if (charset == null) {
            charset = defaultCharset;
        }
        if (charset == null) {
            charset = HTTP.DEFAULT_CONTENT_CHARSET;
        }

        long averageLength = contentLength/httpRequest.getProgressCallbackNumber();
        int callbackNumber = 0;
        Reader reader = new InputStreamReader(inputStream, charset);
        CharArrayBuffer buffer = new CharArrayBuffer(contentLength);
        HttpRequest.ProgressListener progressListener = httpRequest.getProgressListener();
        try {
            char[] tmp = new char[1024];
            int readLength;
            long completedLength = 0;
            while(!httpRequest.isStopReadData() && (readLength = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, readLength);
                completedLength += readLength;
                if(progressListener != null && !httpRequest.isCanceled() && (completedLength >= (callbackNumber+1)*averageLength || completedLength == contentLength)){
                    callbackNumber++;
                    new HttpRequestHandler.UpdateProgressRunnable(httpRequest, contentLength, completedLength).execute();
                }
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    /**
     * 解析响应体注解的值
     * @param context 上下文
     * @param responseClass 响应对象的class
     */
    public static String parseResponseBodyAnnotation(Context context, Class<?> responseClass){
        ResponseBody annotation = responseClass.getAnnotation(ResponseBody.class);
        if(annotation == null){
            return null;
        }
        String annotationValue = annotation.value();
        if(annotationValue != null && !"".equals(annotationValue)){
            return annotationValue;
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }
}
