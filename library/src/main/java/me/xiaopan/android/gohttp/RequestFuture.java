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

import java.lang.ref.WeakReference;

public class RequestFuture {
    private final WeakReference<HttpRequestExecuteRunnable> requestReference;

    public RequestFuture(HttpRequestExecuteRunnable request) {
        this.requestReference = new WeakReference<HttpRequestExecuteRunnable>(request);
    }

    /**
     * 取消请求
     * @param isStopReadData 是否停止接收数据
     */
    public void cancel(boolean isStopReadData) {
    	HttpRequestExecuteRunnable request = requestReference.get();
    	if(request != null){
    		request.cancel(isStopReadData);
    	}
    }

    /**
     * 是否完成
     */
    public boolean isFinished() {
    	HttpRequestExecuteRunnable request = requestReference.get();
        return request == null || request.isDone();
    }

    /**
     * 是否已经取消
     */
    public boolean isCancelled() {
    	HttpRequestExecuteRunnable request = requestReference.get();
        return request == null || request.isCancelled();
    }

    public boolean shouldBeGarbageCollected() {
        boolean should = isCancelled() || isFinished();
        if (should){
            requestReference.clear();
        }
        return should;
    }
}