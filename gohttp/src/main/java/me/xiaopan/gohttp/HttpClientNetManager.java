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

package me.xiaopan.gohttp;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.List;

/**
 * HttpClient版
 */
public class HttpClientNetManager implements NetManager{
    private HttpClientManager httpClientManager;

    public HttpClientManager initHttpClientManager() {
        if(httpClientManager == null){
            synchronized (GoHttp.class){
                if(httpClientManager == null){
                    httpClientManager = new HttpClientManager();
                }
            }
        }
        return httpClientManager;
    }

    @Override
    public HttpResponse getHttpResponse(HttpRequest request) throws Exception {
        HttpUriRequest httpUriRequest = httpRequest2HttpUriRequest(request);
        if(httpUriRequest == null){
            throw new Exception("哦no！怎么会没有HttpUriRequest呢？一定是你新增加了一种请求方式，确没有增加转换的实现");
        }
        initHttpClientManager();
        return httpClientManager.getHttpClient().execute(httpUriRequest, httpClientManager.getHttpContext());
    }

    private HttpUriRequest httpRequest2HttpUriRequest(HttpRequest httpRequest){
        HttpUriRequest httpUriRequest = null;
        switch(httpRequest.getMethod()){
            case GET:
                String url = getUrlByParams(true, httpRequest.getUrl(), httpRequest.getParams());
                HttpGet httGet = new HttpGet(url);
                appendHeaders(httGet, httpRequest.getHeaders());
                httpUriRequest = httGet;
                break;
            case POST:
                HttpPost httPost = new HttpPost(httpRequest.getUrl());
                appendHeaders(httPost, httpRequest.getHeaders());

                HttpEntity httpPostEntity = httpRequest.getHttpEntity();
                if(httpPostEntity == null && httpRequest.getParams() != null){
                    httpPostEntity = httpRequest.getParams().getEntity();
                }
                if(httpPostEntity != null){
                    httPost.setEntity(httpPostEntity);
                }
                httpUriRequest = httPost;
                break;
            case DELETE:
                String deleteUrl = getUrlByParams(true, httpRequest.getUrl(), httpRequest.getParams());
                HttpDelete httDelete = new HttpDelete(deleteUrl);
                appendHeaders(httDelete, httpRequest.getHeaders());
                httpUriRequest = httDelete;
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(httpRequest.getUrl());
                appendHeaders(httpPut, httpRequest.getHeaders());

                HttpEntity httpPutEntity = httpRequest.getHttpEntity();
                if(httpPutEntity == null && httpRequest.getParams() != null){
                    httpPutEntity = httpRequest.getParams().getEntity();
                }
                if(httpPutEntity != null){
                    httpPut.setEntity(httpPutEntity);
                }
                httpUriRequest = httpPut;
                break;
        }
        return httpUriRequest;
    }

    /**
     * 拼接Url和参数
     */
    private String getUrlByParams(boolean shouldEncodeUrl, String url, RequestParams params) {
        if (shouldEncodeUrl){
            url = url.replace(" ", "%20");
        }
        if(params != null) {
            String paramString = params.getParamString();
            if(paramString != null && !"".equals(paramString)){
                if (url.contains("?")) {
                    url += "&" + paramString;
                } else {
                    url += "?" + paramString;
                }
            }
        }
        return url;
    }

    /**
     * 追加Http头信息
     */
    private HttpRequestBase appendHeaders(HttpRequestBase httpRequest, List<Header> headers){
        if(httpRequest != null && headers != null && headers.size() > 0){
            for(Header header : headers){
                httpRequest.addHeader(header);
            }
        }
        return httpRequest;
    }
}
