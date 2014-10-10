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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

/***
 * 带有进度的实体工具箱
 */
public final class ProgressEntityUtils {

    private ProgressEntityUtils() {
    }
    
    public static byte[] toByteArray(final HttpEntity entity, HttpResponseHandler httpResponseHandler, UpdateProgressCallback updateProgressCallback) throws IOException {
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
        ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
        try {
            byte[] tmp = new byte[4096];
            int readLength;
            long completedLength = 0;
            while(!httpResponseHandler.isStopReadData() && (readLength = inputStream.read(tmp)) != -1) {
                buffer.append(tmp, 0, readLength);
                completedLength += readLength;
                if(!httpResponseHandler.isCancelled() && updateProgressCallback != null && !updateProgressCallback.isMarkRead()){
                	updateProgressCallback.onUpdateProgress(contentLength, completedLength);
                }
            }
            if(updateProgressCallback != null){
            	updateProgressCallback.setMarkRead(true);
            }
        } finally {
            inputStream.close();
        }
        return !httpResponseHandler.isCancelled()?buffer.toByteArray():null;
    }
    
    public static byte[] toByteArray(final HttpEntity entity, HttpResponseHandler httpResponseHandler) throws IOException {
        return toByteArray(entity, httpResponseHandler, null);
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

    public static String toString(final HttpEntity entity, final String defaultCharset, HttpResponseHandler httpResponseHandler, UpdateProgressCallback updateProgressCallback) throws IOException, ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
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
        Reader reader = new InputStreamReader(inputStream, charset);
        CharArrayBuffer buffer = new CharArrayBuffer(contentLength); 
        try {
            char[] tmp = new char[1024];
            int readLength;
            long completedLength = 0;
            while(!httpResponseHandler.isStopReadData() && (readLength = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, readLength);
                completedLength += readLength;
                if(!httpResponseHandler.isCancelled() && updateProgressCallback != null && !updateProgressCallback.isMarkRead()){
                	updateProgressCallback.onUpdateProgress(contentLength, completedLength);
                }
            }
            if(updateProgressCallback != null){
            	updateProgressCallback.setMarkRead(true);
            }
        } finally {
            reader.close();
        }
        return !httpResponseHandler.isCancelled()?buffer.toString():null;
    }

    public static String toString(final HttpEntity entity, final String defaultCharset, HttpResponseHandler httpResponseHandler) throws IOException, ParseException {
        return toString(entity, defaultCharset, httpResponseHandler, null);
    }

    public static String toString(final HttpEntity entity, HttpResponseHandler httpResponseHandler, final UpdateProgressCallback updateProgressListener) throws IOException, ParseException {
        return toString(entity, null, httpResponseHandler, updateProgressListener);
    }

    public static String toString(final HttpEntity entity, final HttpResponseHandler httpResponseHandler) throws IOException, ParseException {
        return toString(entity, null, httpResponseHandler, null);
    }
    
    public static boolean read(final HttpEntity entity, OutputStream outputStream, HttpResponseHandler httpResponseHandler, UpdateProgressCallback updateProgressCallback) throws IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream inputStream = entity.getContent();
        if (inputStream == null || outputStream == null) {
            return false;
        }
        long contentLength = entity.getContentLength();
        if (contentLength < 0) {
        	throw new IllegalArgumentException("HTTP entity length is 0");
        }
        try {
            byte[] tmp = new byte[4096];
            int readLength;
            long completedLength = 0;
            while(!httpResponseHandler.isStopReadData() && (readLength = inputStream.read(tmp)) != -1) {
                outputStream.write(tmp, 0, readLength);
                completedLength += readLength;
                if(!httpResponseHandler.isCancelled() && updateProgressCallback != null && !updateProgressCallback.isMarkRead()){
                    updateProgressCallback.onUpdateProgress(contentLength, completedLength);
                }
            }
            if(updateProgressCallback != null){
            	updateProgressCallback.setMarkRead(true);
            }
        } finally {
            inputStream.close();
            outputStream.flush();
        }
        return !httpResponseHandler.isCancelled();
    }
    
    public static boolean read(final HttpEntity entity, OutputStream outputStream, HttpResponseHandler httpResponseHandler) throws IOException {
        return read(entity, outputStream, httpResponseHandler, null);
    }
}