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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 同步管理器
 */
public class SyncManager {
    private Map<String, ReentrantLock> cacheIdLocks;

    public SyncManager() {
        this.cacheIdLocks = Collections.synchronizedMap(new WeakHashMap<String, ReentrantLock>());
    }

    /**
     * 根据缓存ID获取锁
     * @param cacheId 缓存ID
     * @return 锁
     */
    public synchronized ReentrantLock getLockByCacheId(String cacheId){
        ReentrantLock cacheIdLock = cacheIdLocks.get(cacheId);
        if(cacheIdLock == null){
            cacheIdLock = new ReentrantLock();
            cacheIdLocks.put(cacheId, cacheIdLock);
        }
        return cacheIdLock;
    }
}
