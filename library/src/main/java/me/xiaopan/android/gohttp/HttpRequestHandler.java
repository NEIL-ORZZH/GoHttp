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

import android.util.Log;

import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * Http请求处理器
 */
public class HttpRequestHandler implements Runnable{
    private HttpRequest httpRequest;

    public HttpRequestHandler(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public void run() {
        if(httpRequest.isCanceled()){
            httpRequest.finish();
            new CancelRunnable(httpRequest).execute();
            if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 刚刚开始"+"; "+httpRequest.getUrl());
            return;
        }
        if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Started : 刚刚开始"+"; "+httpRequest.getUrl());

        boolean isCache = httpRequest.getCacheConfig() != null;
		boolean isRefreshCache = isCache && httpRequest.getCacheConfig().isRefreshCache();
        boolean isContinueCallback = true;
		HttpResponse httpResponse = null;

		// 如果需要缓存并且本地缓存可以使用，就先读取本地缓存
		if(isCache && httpRequest.getGoHttp().getCacheManager().isHasAvailableCache(httpRequest)){
            if(httpRequest.isCanceled()){
                httpRequest.finish();
                new CancelRunnable(httpRequest).execute();
                if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 判断完缓存可用"+"; "+httpRequest.getUrl());
                return;
            }
            if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Cache : 缓存可用"+"; "+httpRequest.getUrl());

			// 从本地缓存中读取Http响应
			httpResponse = httpRequest.getGoHttp().getCacheManager().readHttpResponseFromCache(httpRequest);
            if(httpRequest.isCanceled()){
                httpRequest.finish();
                new CancelRunnable(httpRequest).execute();
                if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 读取完缓存"+"; "+httpRequest.getUrl());
                return;
            }

			if(httpResponse != null){
				try {
                    Object responseObject = httpRequest.getResponseHandler().handleResponse(httpRequest, httpResponse);
                    if(responseObject == null){
                        throw new Exception("response object is null");
                    }
                    if(httpRequest.isCanceled()){
                        httpRequest.finish();
                        new CancelRunnable(httpRequest).execute();
                        if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 处理完缓存"+"; "+httpRequest.getUrl());
                        return;
                    }

                    // 如果不刷新缓存就意味着请求已经结束了
                    if(!isRefreshCache){
                        httpRequest.finish();
                    }
                    isContinueCallback = isRefreshCache && httpRequest.getCacheConfig().isRefreshCallback();
                    if(responseObject instanceof HttpRequest.Failure){
                        new FailedRunnable(httpRequest, httpResponse, (HttpRequest.Failure) responseObject, true, isContinueCallback).execute();
                    }else{
                        if(httpRequest.getResponseHandleCompletedAfterListener() != null){
                            //noinspection unchecked
                            httpRequest.getResponseHandleCompletedAfterListener().onResponseHandleAfter(httpRequest, httpResponse, responseObject, true, isContinueCallback);
                        }
                        new CompletedRunnable(httpRequest, httpResponse, responseObject, true, isContinueCallback).execute();
                    }
                    // 如果不刷新缓存就意味着请求已经结束了
                    if(!isRefreshCache){
                        if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Completed : 读取完缓存并且不需要刷新缓存"+"; "+httpRequest.getUrl());
                        return;
                    }
                    if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Cache : 已读取并处理缓存"+"; "+httpRequest.getUrl());
				} catch (Throwable e) {
					e.printStackTrace();
                    if(httpRequest.isCanceled()){
                        httpRequest.finish();
                        new CancelRunnable(httpRequest).execute();
                        if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 处理缓存的Http响应时发生异常"+"; "+httpRequest.getUrl());
                        return;
                    }
                    if(httpRequest.getGoHttp().isDebugMode()) Log.e(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Failed : 处理缓存的Http响应时发生异常"+"; "+httpRequest.getUrl());

                    new FailedRunnable(httpRequest, httpResponse, new HttpRequest.Failure(e), true, isContinueCallback).execute();
				}
			}
		}

        // 发送网络请求
        try {
            httpResponse = httpRequest.getGoHttp().getNetManager().getHttpResponse(httpRequest);
        } catch (Throwable e) {
            e.printStackTrace();

            httpRequest.finish();
            if(httpRequest.isCanceled()){
                if(isContinueCallback){
                    new CancelRunnable(httpRequest).execute();
                }
                if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 从网络读取Http响应时发生异常"+"; "+httpRequest.getUrl());
                return;
            }
            if(httpRequest.getGoHttp().isDebugMode()) Log.e(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Failed : 从网络读取Http响应时发生异常"+"; "+httpRequest.getUrl());

            if(isContinueCallback){
                new FailedRunnable(httpRequest, httpResponse, new HttpRequest.Failure(e), false, false).execute();
            }
            return;
        }
        if(httpRequest.isCanceled()){
            httpRequest.finish();
            new CancelRunnable(httpRequest).execute();
            if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 从网络读取完Http响应"+"; "+httpRequest.getUrl());
            return;
        }
        if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Net : 已从网络读取Http取响应"+"; "+httpRequest.getUrl());

        // 缓存Http响应
        if(isCache && httpRequest.getResponseHandler().canCache(httpResponse)){
            try {
                httpRequest.getGoHttp().getCacheManager().saveHttpResponseToCache(httpRequest, httpResponse);
            } catch (IOException e) {
                e.printStackTrace();

                httpRequest.finish();
                httpRequest.finish();
                if(httpRequest.isCanceled()){
                    if(isContinueCallback){
                        new CancelRunnable(httpRequest).execute();
                    }
                    if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 缓存Http响应时发生异常"+"; "+httpRequest.getUrl());
                    return;
                }
                if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Failed : 缓存Http响应时发生异常"+"; "+httpRequest.getUrl());
                if(isContinueCallback){
                    new FailedRunnable(httpRequest, httpResponse, new HttpRequest.Failure(e), false, false).execute();
                }
                return;
            }
            if(httpRequest.isCanceled()){
                httpRequest.finish();
                new CancelRunnable(httpRequest).execute();
                if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 缓存完Http响应"+"; "+httpRequest.getUrl());
                return;
            }
            if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Cache : 已缓存Http取响应"+"; "+httpRequest.getUrl());
        }

        // 不再回调了就直接结束
        if(!isContinueCallback){
            httpRequest.finish();
            if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Completed : 不需要继续回调"+"; "+httpRequest.getUrl());
            return;
        }

        Object responseObject;
        try {
            responseObject = httpRequest.getResponseHandler().handleResponse(httpRequest, httpResponse);
            if(responseObject == null){
                throw new Exception("response object is null");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            httpRequest.finish();

            if(httpRequest.isCanceled()){
                new CancelRunnable(httpRequest).execute();
                if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 处理从网络读取的Http响应时发生异常"+"; "+httpRequest.getUrl());
                return;
            }
            if(httpRequest.getGoHttp().isDebugMode()) Log.e(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Failed : 处理从网络读取的Http响应时发生异常"+"; "+httpRequest.getUrl());

            new FailedRunnable(httpRequest, httpResponse, new HttpRequest.Failure(e), false, false).execute();
            return;
        }
        if(httpRequest.isCanceled()){
            httpRequest.finish();
            new CancelRunnable(httpRequest).execute();
            if(httpRequest.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Canceled : 处理完Http响应"+"; "+httpRequest.getUrl());
            return;
        }
        if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Net : 已处理完Http响应"+"; "+httpRequest.getUrl());

        httpRequest.finish();
        if(responseObject instanceof HttpRequest.Failure){
            HttpRequest.Failure failure = (HttpRequest.Failure) responseObject;
            new FailedRunnable(httpRequest, httpResponse, failure, false, false).execute();
            if(httpRequest.getGoHttp().isDebugMode()) Log.e(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Failed : "+failure.toString()+"; "+httpRequest.getUrl());
        }else{
            if(httpRequest.getResponseHandleCompletedAfterListener() != null){
                //noinspection unchecked
                httpRequest.getResponseHandleCompletedAfterListener().onResponseHandleAfter(httpRequest, httpResponse, responseObject, false, false);
            }
            new CompletedRunnable(httpRequest, httpResponse, responseObject, false, false).execute();
            if(httpRequest.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, httpRequest.getName()+"; "+"Completed : 走到了最后"+"; "+httpRequest.getUrl());
        }
    }

    public static class CancelRunnable implements Runnable{
        private HttpRequest httpRequest;

        public CancelRunnable(HttpRequest httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        public void run() {
            httpRequest.getListener().onCanceled(httpRequest);
        }

        public void execute(){
            httpRequest.getGoHttp().getHandler().post(this);
        }
    }

    public static class FailedRunnable implements Runnable{
        private HttpRequest httpRequest;
        private HttpResponse httpResponse;
        private HttpRequest.Failure failure;
        private boolean isCache;
        private boolean isContinueCallback;

        public FailedRunnable(HttpRequest httpRequest, HttpResponse httpResponse, HttpRequest.Failure failure, boolean isCache, boolean isContinueCallback) {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
            this.failure = failure;
            this.isCache = isCache;
            this.isContinueCallback = isContinueCallback;
        }

        @Override
        public void run() {
            httpRequest.getListener().onFailed(httpRequest, httpResponse, failure, isCache, isContinueCallback);
        }

        public void execute(){
            httpRequest.getGoHttp().getHandler().post(this);
        }
    }

    public static class CompletedRunnable implements Runnable{
        private HttpRequest httpRequest;
        private HttpResponse httpResponse;
        private Object responseObject;
        private boolean isCache;
        private boolean isContinueCallback;

        public CompletedRunnable(HttpRequest httpRequest, HttpResponse httpResponse, Object responseObject, boolean isCache, boolean isContinueCallback) {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
            this.responseObject = responseObject;
            this.isCache = isCache;
            this.isContinueCallback = isContinueCallback;
        }

        @Override
        public void run() {
            //noinspection unchecked
            httpRequest.getListener().onCompleted(httpRequest, httpResponse, responseObject, isCache, isContinueCallback);
        }

        public void execute(){
            httpRequest.getGoHttp().getHandler().post(this);
        }
    }

    public static class UpdateProgressRunnable implements Runnable{
        private HttpRequest httpRequest;
        private long totalLength;
        private long completedLength;

        public UpdateProgressRunnable(HttpRequest httpRequest, long totalLength, long completedLength) {
            this.httpRequest = httpRequest;
            this.totalLength = totalLength;
            this.completedLength = completedLength;
        }

        @Override
        public void run() {
            httpRequest.getProgressListener().onUpdateProgress(httpRequest, totalLength, completedLength);
        }

        public void execute(){
            httpRequest.getGoHttp().getHandler().post(this);
        }
    }
}
