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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;

/**
 * 成功Http响应处理器，只要状态码大于100小于300即可，返回的永远是null
 */
public class EmptyHttpResponseHandler implements HttpResponseHandler {

    @Override
    public boolean canCache(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300;
    }

    @Override
	public Object handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) throws Throwable{
        if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getStatusCode()+"："+ httpRequest.getUrl());
        }
        return null;
	}
}
