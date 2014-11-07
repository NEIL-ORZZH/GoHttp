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

package me.xiaopan.android.gohttp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 下载Http响应处理器，用于下载文件
 */
public class DownloadHttpResponseHandler implements HttpResponseHandler {
    private File file;

    public DownloadHttpResponseHandler(File file){
    	if(file == null){
            throw new IllegalArgumentException("file cannot be null");
    	}
        this.file = file;
    }

    @Override
    public boolean canCache(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300;
    }

    @Override
    public Object handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) throws Throwable {
        if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getStatusCode()+"："+ httpRequest.getUrl());
        }

        HttpEntity httpEntity = httpResponse.getEntity();
        if(httpEntity == null){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "HttpEntity is null");
        }

		if(createFile(file) == null){
			throw new IllegalArgumentException("创建文件失败："+file.getPath());
		}

		OutputStream outputStream = null;
		try{
			outputStream = new BufferedOutputStream(new FileOutputStream(file), 8*1024);
			read(httpRequest, httpEntity, outputStream);

            try{
                outputStream.flush();
            }catch(IOException e){
                e.printStackTrace();
            }
            try{
                outputStream.close();
            }catch(IOException e){
                e.printStackTrace();
            }
		}catch(IOException e){
            e.printStackTrace();
            if(outputStream != null){
                try{
                    outputStream.flush();
                }catch(IOException e1){
                    e1.printStackTrace();
                }
                try{
                    outputStream.close();
                }catch(IOException e2){
                    e2.printStackTrace();
                }
            }
            if(file.exists()){
                file.delete();
            }
            throw e;
        }

		if(httpRequest.isCanceled()){
			if(file.exists()){
				file.delete();
			}
			return null;
		}else{
            return file;
        }
    }

    private void read(HttpRequest httpRequest, final HttpEntity entity, OutputStream outputStream) throws IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream inputStream = entity.getContent();
        if (inputStream == null || outputStream == null) {
            throw new IllegalArgumentException("inputStream or outputStream is null");
        }
        long contentLength = entity.getContentLength();
        if (contentLength < 0) {
            throw new IllegalArgumentException("HTTP entity length is 0");
        }

        long averageLength = contentLength/httpRequest.getProgressCallbackNumber();
        int callbackNumber = 0;
        HttpRequest.ProgressListener progressListener = httpRequest.getProgressListener();
        try {
            byte[] tmp = new byte[4096];
            int readLength;
            long completedLength = 0;
            while(!httpRequest.isStopReadData() && (readLength = inputStream.read(tmp)) != -1) {
                outputStream.write(tmp, 0, readLength);
                completedLength += readLength;
                if(progressListener != null && !httpRequest.isCanceled() && (completedLength >= (callbackNumber+1)*averageLength || completedLength == contentLength)){
                    callbackNumber++;
                    new HttpRequestHandler.UpdateProgressRunnable(httpRequest, contentLength, completedLength).execute();
                }
            }
        } finally {
            inputStream.close();
            outputStream.flush();
        }
    }

    /**
	 * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
	 * @throws java.io.IOException
	 */
	private File createFile(File file) throws IOException{
		if(!file.exists()){
			boolean createSuccess = true;
			File parentFile = file.getParentFile();
			if(!parentFile.exists()){
				createSuccess = parentFile.mkdirs();
			}
			if(createSuccess){
				try{
					file.createNewFile();
					return file;
				}catch(IOException exception){
					exception.printStackTrace();
					return null;
				}
			}else{
				return null;
			}
		}else{
			return file;
		}
	}
}
