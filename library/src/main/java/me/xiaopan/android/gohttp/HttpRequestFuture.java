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

public class HttpRequestFuture {
    private HttpRequest httpRequest;

    public HttpRequestFuture(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * 获取请求地址
     */
    public String getUrl(){
        return httpRequest.getUrl();
    }

    /**
     * 获取请求名称
     */
    public String getName(){
        return httpRequest.getName();
    }

    /**
     * 是否已经取消了
     */
    public boolean isCanceled() {
        return httpRequest.isCanceled();
    }

    /**
     * 取消请求
     * @param stopReadData 如果当前正在读取数据是否立即停止
     */
    public void cancel(boolean stopReadData){
        httpRequest.cancel(stopReadData);
    }

    /**
     * 是否已经完成了
     */
    public boolean isFinished() {
        return httpRequest.isFinished();
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}