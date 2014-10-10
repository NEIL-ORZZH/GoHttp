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

/**
 * 缓存配置信息
 */
public class CacheConfig {
	private String id; 
	
    /**
     * 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     */
    private int periodOfValidity = -1;

    /**
     * 当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     */
    private boolean refreshCache;

    /**
     * 当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     */
    private boolean refreshCallback;
    
    /**
     * 缓存目录
     */
    private String cacheDirectory;
    
    /**
     * 
     * @param periodOfValidity 有效期，单位毫秒
     */
    public CacheConfig(int periodOfValidity){
    	this.periodOfValidity = periodOfValidity;
    }

    public CacheConfig(){}

    /**
     * 获取ID
     * @return
     */
    public String getId() {
		return id;
	}

	/**
	 * 设置ID
	 * @param id
	 */
	public CacheConfig setId(String id) {
		this.id = id;
		return this;
	}
	
	/**
     * 获取本地缓存缓存有效期
     * @return 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     */
    public int getPeriodOfValidity() {
        return periodOfValidity;
    }

    /**
     * 设置本地缓存缓存有效期
     * @param periodOfValidity 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     */
    public CacheConfig setPeriodOfValidity(int periodOfValidity) {
        this.periodOfValidity = periodOfValidity;
		return this;
    }

    /**
     * 当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     * @return
     */
    public boolean isRefreshCache() {
        return refreshCache;
    }

    /**
     * 设置当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     * @param refreshCache
     */
    public CacheConfig setRefreshCache(boolean refreshCache) {
        this.refreshCache = refreshCache;
		return this;
    }

    /**
     * 当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     * @return
     */
    public boolean isRefreshCallback() {
        return refreshCallback;
    }

    /**
     * 设置当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     * @param refreshCallback
     */
    public CacheConfig setRefreshCallback(boolean refreshCallback) {
        this.refreshCallback = refreshCallback;
		return this;
    }
    
    /**
     * 获取缓存目录
     * @return
     */
    public String getCacheDirectory() {
		return cacheDirectory;
	}

	/**
	 * 设置缓存目录
	 * @param cacheDirectory
	 */
	public CacheConfig setCacheDirectory(String cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
		return this;
	}
}
