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

package me.xiaopan.gohttp.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

/**
 * 添加请求头的请求拦截器
 */
public class AddRequestHeaderRequestInterceptor implements HttpRequestInterceptor{
	private Map<String, String> requestHeaderMap;
	
	public AddRequestHeaderRequestInterceptor(Map<String, String> clientHeaderMap){
		this.requestHeaderMap = clientHeaderMap;
	}
	
	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		if(requestHeaderMap != null  && requestHeaderMap.size() > 0){
			for (String header : requestHeaderMap.keySet()) {
				request.addHeader(header, requestHeaderMap.get(header));
			}
		}
	}
}
