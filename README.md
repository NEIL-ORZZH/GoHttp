# ![Logo](docs/logo.png) GoHttp

GoHttp是Android上的一个用来发送Http请求的库，旨在用最简单、最快捷的方式从网络上拿到我们想要的数据

###特性（Features）
>* 异步发送Http请求；
>* 全新设计的API，让发送Http请求以及处理请求结果、监听请求过程和进度更加简单、高效、舒适；
>* 支持用请求对象发送Http请求的网络框架，这将大大降低程序复杂度，让你越用越爽；
>* 支持在本地缓存Http响应；
>* 内置多种Http响应处理器，可以直接实现将Http响应转成字符串、字节数组或下载文件以及将JSON格式的字符串转成Java对象；
>* 完全的开放，你可以自定义缓存管理、请求处理、Http响应处理，甚至添加一种新的HttpRequest；
>* 内置一系列的HttpHeader，方便你往HttpRequest中添加Header或从HttpResponse中获取Header；

###使用（Usage）
####1. 导入GoHttp（Import GoHttp to your project）
**Gradle**
```groovy
compile 'me.xiaopan:gohttp:1.2.0'
```

**Eclipse**
1. 首先到[releases](https://github.com/xiaopansky/GoHttp/releases)页面下载最新版本的aar文件
2. 然后改后缀名为zip并解压
3. 最后将classes.jar重命名为gohttp-`1.2.0`.jar并拷贝到你项目的libs目录下

####2. 示例（Sample）
发送Http请求获取MIUI首页的源码
```java
public class StringActivity extends MyActivity {
	private WebViewManager webViewManager;
    private HttpRequestFuture httpRequestFuture;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));

		httpRequestFuture = GoHttp.with(getBaseContext()).newRequest("http://www.miui.com/forum.php", new StringHttpResponseHandler(), new HttpRequest.Listener<String>() {
            @Override
            public void onStarted(HttpRequest httpRequest) {
                // 提示正在加载
            }

            @Override
            public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, String responseContent, boolean isCache, boolean isContinueCallback) {
                // 显示HTML源代码
                ContentType contentType = ContentType.fromHttpMessage(httpResponse);
                webViewManager.getWebView().loadDataWithBaseURL(null, responseContent, contentType.getMimeType(), contentType.getCharset("UTF-8"), null);
            }

            @Override
            public void onFailed(HttpRequest httpRequest, HttpResponse httpResponse, HttpRequest.Failure failure, boolean isCache, boolean isContinueCallback) {
                // 显示失败提示
            }

            @Override
            public void onCanceled(HttpRequest httpRequest) {
                // 当请求被取消的时候你可以在这里做一些处理
            }
        }).progressListener(new HttpRequest.ProgressListener() {    // 设置进度监听器
            @Override
            public void onUpdateProgress(HttpRequest httpRequest, long totalLength, long completedLength) {
                // 更新进度
            }
        }).cacheConfig(new CacheConfig(20 * 1000))  // 设置缓存Http响应20秒
          .go();    // 发送请求
	}

	@Override
	public void onBackPressed() {
		if(webViewManager.getWebView().canGoBack()){
			webViewManager.getWebView().goBack();
		}else{
			super.onBackPressed();
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在Activity销毁的时候如果请求尚未完成就取消
        if(httpRequestFuture != null && !httpRequestFuture.isFinished()){
            httpRequestFuture.cancel(true);
        }
    }
}
```

###你可能还感兴趣的功能
>* [发送请求并设置相关参数](https://github.com/xiaopansky/GoHttp/wiki/send-http-request)
>* [使用内置的几种HttpResponseHandler处理响应](https://github.com/xiaopansky/GoHttp/wiki/handle-http-response)
>* [监听请求过程并处理返回结果和失败信息](https://github.com/xiaopansky/GoHttp/wiki/listener)
>* [使用请求对象来发送请求](https://github.com/xiaopansky/GoHttp/wiki/use-request-object)
>* [使用缓存功能实现离线模式](https://github.com/xiaopansky/GoHttp/wiki/cache-http-response)
>* [取消请求](https://github.com/xiaopansky/GoHttp/wiki/cancel-request)
>* [使用ResponseHandleCompletedAfterListener在异步线程中处理响应结果](https://github.com/xiaopansky/GoHttp/wiki/ResponseHandleCompletedAfterListener)

###License
```java
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
```
