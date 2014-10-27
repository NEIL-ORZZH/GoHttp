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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 默认的字符串Http响应处理器
 */
public class BinaryHttpResponseHandler implements HttpResponseHandler {

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

        return toByteArray(httpRequest, httpEntity);
	}

    private byte[] toByteArray(HttpRequest httpRequest, final HttpEntity entity) throws IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream inputStream = entity.getContent();
        if (inputStream == null) {
            return new byte[] {};
        }
        if (entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        int contentLength = (int)entity.getContentLength();
        if (contentLength < 0) {
            contentLength = 4096;
        }

        long averageLength = contentLength/httpRequest.getProgressCallbackNumber();
        int callbackNumber = 0;
        ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
        HttpRequest.ProgressListener progressListener = httpRequest.getProgressCallback();
        try {
            byte[] tmp = new byte[4096];
            int readLength;
            long completedLength = 0;
            while(!httpRequest.isStopReadData() && (readLength = inputStream.read(tmp)) != -1) {
                buffer.append(tmp, 0, readLength);
                completedLength += readLength;
                if(progressListener != null && !httpRequest.isCanceled() && (completedLength >= (callbackNumber+1)*averageLength || completedLength == contentLength)){
                    callbackNumber++;
                    new HttpRequestHandler.UpdateProgressRunnable(httpRequest, contentLength, completedLength).execute();
                }
            }
        } finally {
            inputStream.close();
        }
        return buffer.toByteArray();
    }
}
