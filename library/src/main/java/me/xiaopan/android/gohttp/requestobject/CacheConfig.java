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

package me.xiaopan.android.gohttp.requestobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应缓存配置注解
 * Created by xiaopan on 13-11-27.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CacheConfig {
    /**
     * 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     * @return
     */
    public int periodOfValidity() default -1;

    /**
     * 当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     * @return
     */
    public boolean isRefreshCache() default false;

    /**
     * 当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     * @return
     */
    public boolean isRefreshCallback() default false;
    
    /**
     * 缓存目录
     * @return
     */
    public String cacheDirectory() default "";
    
    /**
     * 缓存目录ID
     * @return
     */
    public int cacheDirectoryResId() default 0;
}
