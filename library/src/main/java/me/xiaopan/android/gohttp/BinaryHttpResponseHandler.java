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

import java.io.FileNotFoundException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;

/**
 * 默认的二进制Http响应处理器
 */
public abstract class BinaryHttpResponseHandler extends HttpResponseHandler{
	
	public BinaryHttpResponseHandler() {
		super();
	}

	public BinaryHttpResponseHandler(boolean enableProgressCallback) {
		super(enableProgressCallback);
	}

	@Override
	public BinaryHttpResponseHandler setEnableProgressCallback(boolean enableProgressCallback) {
		super.setEnableProgressCallback(enableProgressCallback);
		return this;
	}

	@Override
	public BinaryHttpResponseHandler setSynchronizationCallback(boolean synchronizationCallback) {
		super.setSynchronizationCallback(synchronizationCallback);
		return this;
	}

	@Override
	protected final void onStart(final Handler handler) {
		if(isCancelled()) return;
		
		if(!isSynchronizationCallback()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) return;
					onStart();
				}
			});
		}else{
			onStart();
		}
	}

	@Override
	protected final void onHandleResponse(final Handler handler, HttpUriRequest request, final HttpResponse httpResponse, final boolean isNotRefresh, final boolean isOver) throws Throwable {
		if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
			if(httpResponse.getStatusLine().getStatusCode() == 404){
				throw new FileNotFoundException("请求地址错误："+request.getURI().toString());
			}else{
				throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode()+"："+request.getURI().toString());
			}
		}
		
		HttpEntity httpEntity = httpResponse.getEntity();
		if(httpEntity == null){
            throw new Exception("没有响应体："+request.getURI().toString());
		}
		
		BaseUpdateProgressCallback updateProgressCallback = isEnableProgressCallback()?new BaseUpdateProgressCallback(this, handler):null;
		final byte[] data = ProgressEntityUtils.toByteArray(new ProgressBufferedHttpEntity(httpEntity, this, updateProgressCallback), this, updateProgressCallback);
		if(isCancelled()) return;
		
		if(!isSynchronizationCallback()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) return;
					onSuccess(httpResponse, data, isNotRefresh, isOver);
				}
			});
		}else{
			onSuccess(httpResponse, data, isNotRefresh, isOver);
		}
	}
    
    @Override
    protected final void onUpdateProgress(Handler handler, final long totalLength, final long completedLength){
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onUpdateProgress(totalLength, completedLength);
    			}
    		});
    	}else{
    		onUpdateProgress(totalLength, completedLength);
    	}
    }

	@Override
	protected final void onException(final Handler handler, final Throwable e, final boolean isNotRefresh) {
		if(isCancelled()) return;
		
		if(!isSynchronizationCallback()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) return;
					onFailure(e, isNotRefresh);
				}
			});
		}else{
			onFailure(e, isNotRefresh);
		}
	}
	
	@Override
	protected final void onCancel(Handler handler) {
		if(!isSynchronizationCallback()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					onCancel();
				}
			});
		}else{
			onCancel();
		}
	}
	
	/**
	 * 请求开始
	 */
	protected abstract void onStart();

	/**
	 * 更新进度
	 * @param totalLength 总长度
	 * @param completedLength 已完成长度
	 */
    public void onUpdateProgress(long totalLength, long completedLength){}
	
	/**
	 * 请求成功
	 * @param httpResponse  Http响应
	 * @param binaryData 响应内容
     * @param isNotRefresh 本次响应不是刷新
	 * @param isOver 本次执行是否是最后一次
	 */
	protected abstract void onSuccess(HttpResponse httpResponse, byte[] binaryData, boolean isNotRefresh, boolean isOver);
	
	/**
	 * 请求失败
	 * @param throwable 异常
	 * @param isNotRefresh 本次异常不是在刷新缓存数据的时候发生的
	 */
	protected abstract void onFailure(Throwable throwable, boolean isNotRefresh);

    /**
     * 请求取消
     */
    protected void onCancel(){

    }
}
