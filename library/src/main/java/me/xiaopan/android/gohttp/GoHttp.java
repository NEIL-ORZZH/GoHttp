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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Http客户端，所有的Http操作都将由此类来异步完成，同时此类提供一个单例模式来方便直接使用
 */
public class GoHttp {
    public static final String LOG_TAG = GoHttp.class.getSimpleName();
	private static GoHttp instance;

    private boolean debugMode;	//调试模式
    private Context context;	//上下文
    private Handler handler;	//异步处理器
    private NetManager netManager;
    private CacheManager cacheManager;
    private ExecutorService executorService;	//线程池
    private HttpClientManager httpClientManager;	//Http客户端管理器

    public GoHttp(Context context){
        this.context = context;
    }
    
	/**
	 * 获取实例
	 * @return 实例
	 */
	public static GoHttp with(Context context){
		if(instance == null){
            synchronized(GoHttp.class){
		        if(instance == null){
			        instance = new GoHttp(context);
                }
            }
		}
		return instance;
	}

    /**
     * 发送请求
     * @param httpRequest 请求
     */
    public HttpRequestFuture go(final HttpRequest httpRequest){
        if(httpRequest.getListener() != null){
            if(Looper.myLooper() == Looper.getMainLooper()){
                httpRequest.getListener().onStarted(httpRequest);
            }else{
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        httpRequest.getListener().onStarted(httpRequest);
                    }
                });
            }
        }

        getExecutorService().submit(httpRequest.getExecuteRunnable());
        return new HttpRequestFuture(httpRequest);
    }

    /**
     * 新建一个请求
     * @param url 请求地址
     * @param responseHandler 响应处理器
     * @param listener 请求监听器
     * @return HttpRequest.Helper 你还可以继续设置一些参数，最后调用go()方法即可
     */
    public HttpRequest.Helper newRequest(String url, HttpResponseHandler responseHandler, HttpRequest.Listener listener){
        return new HttpRequest.Helper(this, url, responseHandler, listener);
    }

    /**
     * 新建一个请求
     * @param requestObject 请求对象
     * @param responseHandler 响应处理器
     * @param listener 请求监听器
     * @return HttpRequest.Helper 你还可以继续设置一些参数，最后调用go()方法即可
     */
    public HttpRequest.Helper newRequest(Request requestObject, HttpResponseHandler responseHandler, HttpRequest.Listener listener){
        return new HttpRequest.Helper(this, requestObject, responseHandler, listener);
    }

    /**
     * 是否是调试模式，是的话在运行过程中会在控制台打印相关LOG
     * @return 是否是调试模式
     */
    boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 设置是否是Debug模式，是的话在运行过程中会在控制台打印相关LOG
     * @param debugMode 是否是Debug模式
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    Context getContext() {
        return context;
    }

    /**
     * 获取Handler，用于在主线程回调监听器
     * @return Handler
     */
    Handler getHandler() {
        if(handler == null){
            synchronized (GoHttp.class){
                if(handler == null){
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }

    /**
     * 获取任务线程池
     * @return 任务线程池
     */
    ExecutorService getExecutorService() {
        if(executorService == null){
            synchronized (GoHttp.class){
                if(executorService == null){
                    executorService = Executors.newCachedThreadPool();
                }
            }
        }
        return executorService;
    }

    /**
     * 设置任务线程池
     * @param executorService 线程池
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    HttpClientManager getHttpClientManager() {
        if(httpClientManager == null){
            synchronized (GoHttp.class){
                if(httpClientManager == null){
                    httpClientManager = new HttpClientManager();
                }
            }
        }
        return httpClientManager;
    }

    /**
     * 获取缓存管理器
     * @return 缓存管理器
     */
    CacheManager getCacheManager() {
        if(cacheManager == null){
            synchronized (GoHttp.class){
                if(cacheManager == null){
                    cacheManager = new DefaultCacheManager();
                }
            }
        }
        return cacheManager;
    }

    /**
     * 设置缓存管理器
     * @param cacheManager 缓存管理器
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 获取网络管理器
     * @return 网络管理器
     */
    NetManager getNetManager() {
        if(netManager == null){
            synchronized (GoHttp.class){
                if(netManager == null){
                    netManager = new HttpClientNetManager();
                }
            }
        }
        return netManager;
    }

    /**
     * 设置网络管理器
     * @param netManager 网络管理器
     */
    public void setNetManager(NetManager netManager) {
        this.netManager = netManager;
    }
}