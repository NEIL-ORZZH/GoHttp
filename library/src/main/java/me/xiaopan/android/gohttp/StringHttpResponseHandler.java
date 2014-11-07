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

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 默认的字符串Http响应处理器，用于将Http响应转成字符串
 */
public class StringHttpResponseHandler implements HttpResponseHandler {

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

        return toString(httpRequest, httpEntity, "UTF-8");
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
        String charset = getContentCharSet(entity);
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

    public static String getContentCharSet(final HttpEntity entity)
            throws ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }
}
