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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class HttpRequestExecuteRunnable implements Runnable {
	private boolean isCancelled;
	private boolean isFinished;
	private boolean isSuccess;
	private File statusLineCacheFile;
	private File responseEntityCacheFile;
	private File responseHeadersCacheFile;
	private String uri;
    private String name;    //请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private Configuration configuration;	// 配置
    private HttpUriRequest httpUriRequest;  //HttpUri请求
    private CacheConfig cacheConfig;    //响应缓存配置
    private HttpResponseHandler httpResponseHandler;    //Http响应处理器

    public HttpRequestExecuteRunnable(Configuration configuration, String name, HttpUriRequest request, CacheConfig cacheConfig, HttpResponseHandler httpResponseHandler) {
        this.httpUriRequest = request;
        this.cacheConfig = cacheConfig;
        this.httpResponseHandler = httpResponseHandler;
        this.name = name;
        this.configuration = configuration;
    }
    
    private String createLog(String type){
    	if(uri == null){
    		uri = httpUriRequest.getURI().toString();
    	}
    	return new StringBuilder().append(name).append(" ").append(type).append("（").append(uri).append("）").toString();
    }

    @Override
    public void run() {
    	isFinished = false;
		
    	// 输出开始日志和回调
		if(configuration.isDebugMode()) Log.i(configuration.getLogTag(), createLog("开始"));
		httpResponseHandler.onStart(configuration.getHandler());
		
		boolean isContinue = true;
		boolean isRefreshCache = false;
		HttpResponse httpResponse;
		
		// 如果本地缓存可以使用
		if(isAvailableByCache()){
			// 从本地缓存中读取Http响应
			httpResponse = getHttpResponseFromCacheFile();
			if(!isCancelled() && httpResponse != null){
				try {
					httpResponseHandler.onHandleResponse(configuration.getHandler(), httpUriRequest, httpResponse, true, !(isRefreshCache()&& cacheConfig.isRefreshCallback()));
					isContinue = !isCancelled() && isRefreshCache();// 如果尚未取消并且需要刷新缓存
					isRefreshCache = isRefreshCache();
					isSuccess = true;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		
		// 如果还要继续从网络读取数据
		if(!isCancelled() && isContinue){
			try {
				httpResponse = getHttpResponseFromNetwork(isRefreshCache);
				if(!isCancelled() && (!isRefreshCache || (isRefreshCache() && cacheConfig.isRefreshCallback()))){
	        		httpResponseHandler.onHandleResponse(configuration.getHandler(), httpUriRequest, httpResponse, !isRefreshCache, true);
	        	}
				isSuccess = true;
			} catch (Throwable e) {
				e.printStackTrace();
	        	httpUriRequest.abort();
	        	isSuccess = false;
	        	if(!isCancelled() && (!isRefreshCache || (isRefreshCache() && cacheConfig.isRefreshCallback()))){
	            	httpResponseHandler.onException(configuration.getHandler(), e, !isRefreshCache);
	            }
			}
		}
		
        isFinished = true;
       
        if(configuration.isDebugMode()){
        	if(isCancelled()){
        		Log.w(configuration.getLogTag(), createLog("取消"));
        	}else if(isSuccess){
        		Log.i(configuration.getLogTag(), createLog("成功"));
        	}else{
        		Log.e(configuration.getLogTag(), createLog("失败"));
        	}
        }
    }

    /**
     * 判断缓存是否可用
     */
    private boolean isAvailableByCache(){
    	try{
    		boolean isAvailable = false;
    		if(cacheConfig != null && GeneralUtils.isNotEmpty(cacheConfig.getId())){
    			statusLineCacheFile = getCacheFile(configuration, cacheConfig.getId() + ".status_line");
    			responseHeadersCacheFile = getCacheFile(configuration, cacheConfig.getId() + ".headers");
    			responseEntityCacheFile = getCacheFile(configuration, cacheConfig.getId() + ".entity");
    			isAvailable = statusLineCacheFile.exists() && responseHeadersCacheFile.exists() && responseEntityCacheFile.exists();
    			if(isAvailable){
    				if(cacheConfig.getPeriodOfValidity() > 0){
    					long cacheTime = responseEntityCacheFile.lastModified();	// 缓存时间
    					Calendar calendar = new GregorianCalendar();
    					calendar.setTime(new Date(cacheTime));
    					calendar.add(Calendar.MILLISECOND, cacheConfig.getPeriodOfValidity());
    					long outOfDateTime = calendar.getTimeInMillis();	// 过期时间
    					long currentTime = System.currentTimeMillis();	//当前时间
    					isAvailable = outOfDateTime > currentTime;
    					if(configuration.isDebugMode()){
    						if(isAvailable){
    							Log.d(configuration.getLogTag(), createLog("缓存 - 有效"));
    						}else{
    							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
    							String lastModifiedTimeString = simpleDateFormat.format(new Date(cacheTime));
    							String currentTimeString = simpleDateFormat.format(new Date());
    							String outOfDateTimeString = simpleDateFormat.format(new Date(outOfDateTime));
    							Log.w(configuration.getLogTag(), new StringBuilder().append(name).append("缓存 - 已過期").append("，")
    	    							.append("缓存时间：").append(lastModifiedTimeString)
    	    							.append("；")
    	    							.append("过期时间：").append(outOfDateTimeString)
    	    							.append("；")
    	    							.append("当前时间：").append(currentTimeString)
    	    							.append("；")
    	    							.append("缓存有效期：").append(cacheConfig.getPeriodOfValidity()).append("毫秒")
    	    							.append("（").append(uri).append("）").toString());
    						}
    					}
    				}else{
    					if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), createLog("缓存 - 永久有效"));
    				}
    			}else{
    				if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), createLog("缓存 - 文件不存在"));
    				statusLineCacheFile.delete();
    				responseHeadersCacheFile.delete();
    				responseEntityCacheFile.delete();
    			}
    		}
    		return isAvailable;
    	}catch(Throwable throwable){
    		Log.e(configuration.getLogTag(), name + " 缓存 - 异常（"+uri+"）"+throwable.toString());
    		throwable.printStackTrace();
    		return false;
    	}
    }

    
    /**
     * 从缓存文件中读取Http响应
     */
    private HttpResponse getHttpResponseFromCacheFile(){
    	try {
	    	if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), createLog("本地"));
	    	/* 读取状态行 */
			HttpResponse httpResponse = new BasicHttpResponse(new Gson().fromJson(GeneralUtils.readString(statusLineCacheFile), SaveStatusLine.class).toStatusLine());
	
			/* 读取响应头 */
	        String[] headerStrings = new Gson().fromJson(GeneralUtils.readString(responseHeadersCacheFile), new TypeToken<String[]>(){}.getType());
	        if(headerStrings != null && headerStrings.length > 0){
	            Header[] headers = new Header[headerStrings.length];
	            int w = 0;
	            for(String string : headerStrings){
	                CharArrayBuffer charArrayBuffer = new CharArrayBuffer(string.length());
	                charArrayBuffer.append(string);
	                headers[w++] = new BufferedHeader(charArrayBuffer);
	            }
	            httpResponse.setHeaders(headers);
	        }
	
			/* 设置响应体 */
	        Header contentTypeHeader = null;
	        Header[] contentTypes = httpResponse.getHeaders(HTTP.CONTENT_TYPE);
	        if(contentTypes != null && contentTypes.length > 0){
	        	contentTypeHeader = contentTypes[0];
	        }
	        httpResponse.setEntity(new FileEntity(responseEntityCacheFile, contentTypeHeader != null?contentTypeHeader.getValue():null));
	        
	        return httpResponse;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    
    /**
     * 从网络加载
     */
    private HttpResponse getHttpResponseFromNetwork(boolean isRefreshCache) throws IOException{
    	if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), createLog("网络"+(isRefreshCache?" - 刷新缓存":"")));
        HttpResponse httpResponse = configuration.getHttpClientManager().getHttpClient().execute(httpUriRequest, configuration.getHttpClientManager().getHttpContext());
        
        if(isCancelled()){
        	return null;
        }

        //尝试缓存
        if(isCache() && httpResponseHandler.isCanCache(httpResponse)){
        	saveHttpResponseToCacheFile(httpResponse);
        }
        
        return httpResponse;
    }
    
    /**
     * 保存Http响应
     * @throws IOException
     */
    private void saveHttpResponseToCacheFile(HttpResponse httpResponse) throws IOException{
    	HttpEntity httpEntity = httpResponse.getEntity();
        if(httpEntity != null){
        	if(GeneralUtils.createFile(statusLineCacheFile) != null && GeneralUtils.createFile(responseHeadersCacheFile) != null && GeneralUtils.createFile(responseEntityCacheFile) != null){
        		InputStream inputStream = null;
        		FileOutputStream fileOutputStream = null;
        		try{
        			/* 保存状态行 */
        			SaveStatusLine saveStatusLine = new SaveStatusLine(httpResponse.getStatusLine());
        			GeneralUtils.writeString(statusLineCacheFile, new Gson().toJson(saveStatusLine), false);
        			
        			/* 保存响应头 */
        			Header[] headers = httpResponse.getAllHeaders();
        			String[] headerStrings = new String[headers.length];
        			for(int w = 0; w < headers.length; w++){
        				Header header = headers[w];
        				if(header instanceof BufferedHeader){
        					headerStrings[w] = header.toString();
        				}else{
        					headers[w] = null;
        				}
        			}
        			GeneralUtils.writeString(responseHeadersCacheFile, new Gson().toJson(headerStrings), false);
        			
        			/* 保存响应体 */
        			inputStream = httpEntity.getContent();
        			fileOutputStream = new FileOutputStream(responseEntityCacheFile);
        			GeneralUtils.outputFromInput(inputStream, fileOutputStream);
        			inputStream.close();
        			fileOutputStream.flush();
        			fileOutputStream.close();
        			
        			//将响应实体替换为本地文件
        			Header contentTypeHeader = httpEntity.getContentType();
        			httpResponse.setEntity(new FileEntity(responseEntityCacheFile,contentTypeHeader != null?contentTypeHeader.getValue():null));
        			if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), createLog("网络 - 响应已缓存"));
        		}catch(IOException exception){
        			if(inputStream != null){ try{inputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
        			if(fileOutputStream != null){try{fileOutputStream.flush();fileOutputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
        			if(responseEntityCacheFile.delete() || responseHeadersCacheFile.delete()){
        				if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), createLog("网络 - 缓存响应失败，缓存文件已刪除"));
        			}else{
        				if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), createLog("网络 - 缓存响应失败，缓存文件刪除失敗"));
        			}
        			throw exception;
        		}
        	}else{
        		if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), createLog("网络 - 创建缓存文件失败"));
        	}
        }else{
        	if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), createLog("网络 - 缓存失败，原因：Http实体是null"));
        }
    }
    
    /**
     * 是否缓存
     */
    public boolean isCache(){
    	return cacheConfig != null && GeneralUtils.isNotEmpty(cacheConfig.getId());
    }
    
    /**
     * 是否刷新缓存
     */
    public boolean isRefreshCache(){
    	return cacheConfig != null && GeneralUtils.isNotEmpty(cacheConfig.getId()) && cacheConfig.isRefreshCache();
    }

    /**
     * 是否已经取消
     */
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * 是否完成
     */
    public boolean isDone() {
        return isFinished;
    }

    /**
     * 取消
     * @param isStopReadData 是否停止接收数据
     */
    public void cancel(boolean isStopReadData) {
        if(!isFinished){
        	isCancelled = true;
        	httpResponseHandler.cancel(configuration.getHandler(), isStopReadData);
        }
    }
    
    /**
     * 获取缓存文件
     */
    private static File getCacheFile(Configuration configuration, String fileName){
    	if(GeneralUtils.isNotEmpty(configuration.getDefaultCacheDirectory())){
    		return new File(configuration.getDefaultCacheDirectory() + File.separator + "easy_http_client" + File.separator  + fileName);
    	}else{
    		return new File(GeneralUtils.getDynamicCacheDir(configuration.getContext()).getPath() + File.separator + "easy_http_client" + File.separator  + fileName);
    	}
    }
}
