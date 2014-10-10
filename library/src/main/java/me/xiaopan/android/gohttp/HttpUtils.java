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

import java.util.List;

import me.xiaopan.android.gohttp.headers.ContentType;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

class HttpUtils {
	/**
	 * 获取响应编码，首先会尝试从响应体的Content-Type中获取，如果获取不到的话就返回默认的UTF-8
	 * @param httpResponse
	 * @return
	 */
	public static final String getResponseCharset(HttpResponse httpResponse){
		ContentType contentType = ContentType.valueOf(httpResponse);
		if(contentType != null){
			return contentType.getCharset("UTF-8");
		}else{
			return "UTF-8";
		}
	}
	
	/**
	 * 拼接Url和参数
	 * @param shouldEncodeUrl
	 * @param url
	 * @param params
	 * @return
	 */
	public static final String getUrlByParams(boolean shouldEncodeUrl, String url, RequestParams params) {
		if (shouldEncodeUrl){
			url = url.replace(" ", "%20");
		}
        if(params != null) {
            String paramString = params.getParamString();
            if(GeneralUtils.isNotEmpty(paramString)){
            	if (url.indexOf("?") == -1) {
            		url += "?" + paramString;
            	} else {
            		url += "&" + paramString;
            	}
            }
        }
        return url;
    }
    
    /**
     * 追加Http头信息
     * @param httpRequest
     * @param headers
     * @return
     */
    public static final HttpRequestBase appendHeaders(HttpRequestBase httpRequest, List<Header> headers){
        if(httpRequest != null && headers != null && headers.size() > 0){
            for(Header header : headers){
                httpRequest.addHeader(header);
            }
        }
        return httpRequest;
    }
}
