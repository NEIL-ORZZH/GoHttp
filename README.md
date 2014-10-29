# ![Logo](https://github.com/xiaopansky/GoHttp/raw/master/res/drawable-mdpi/ic_launcher.png) HappyNetwork

HappyNetwork是Android上的一个网络访问库，旨在用最简单、最快捷的方式来访问网络，其参考了android-async-http

##Features
>* 异步发送Http请求，并用ThreadPool来维护每一个请求；
>* 内置多种Http响应处理器，方便开发者处理不同类型的数据，并且都支持回调进度；
>* 重新封装了HttpRequest，使用更方便；
>* 支持以请求对象的方式来发送Http请求，请求对象中的注解还支持使用String资源，方便开发者根据不同的环境发送不同的请求；
>* 支持持久缓存Http响应到本地，还可以配置缓存有效期、缓存有效时再刷新缓存等；

##Usage Guide

###1. 发送请求

####使用普通方式发送请求
```java
HappyHttpClient.getInstance(getBaseContext()).get("http://www.miui.com/forum.php", new StringHttpResponseHandler(true){
	@Override
	protected void onStart() {
		// 提示开始
	}
	
	@Override
	public void onUpdateProgress(long totalLength, long completedLength) {
		// 更新进度
	}

	@Override
	protected void onSuccess(HttpResponse httpResponse, String responseContent, boolean isNotRefresh, boolean isOver) {
		// 加载成功，使用webview显示html源码
		Header contentTypeHeader = httpResponse.getEntity().getContentType();
        ContentType contentType = new ContentType(contentTypeHeader.getValue());
        webViewManager.getWebView().loadDataWithBaseURL(null, responseContent, contentType.getMimeType(), contentType.getCharset("UTF-8"), null);
	}
	
	@Override
	protected void onFailure(Throwable throwable, boolean isNotRefresh) {
		// 加载失败了，你可以根据throwable的具体类型提示用户失败的原因
	}
});
```
示例中演示的是以get()方式发送请求，其他例如post、put、delete使用方式都非常类似

####使用请求对象发送请求
##### 第一步：配置请求对象
例如：
```java
@Name("百度搜索")
@Url("http://www.baidu.com/s")
public class BaiduSearchRequest implements Request {
    @Param
    public String rsv_spt = "1";

    @Param
    public String issp = "1";

    @Param
    public String rsv_bp = "0";

    @Param
	public String ie = "utf-8";

	@Param
	public String tn = "98012088_3_dg";

	@Param
	public String rsv_sug3 = "4";

	@Param
	public String rsv_sug = "0";

	@Param
	public String rsv_sug1 = "3";

	@Param
	public String rsv_sug4 = "481";

	@Param("wd")
	public String keyword;

	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		this.keyword = keyword;
	}
}
```
#####第二步：发送请求
例如：
```java
HappyHttpClient.getInstance(getBaseContext()).execute(new BaiduSearchRequest("王力宏"), new StringHttpResponseHandler(true){
	@Override
	protected void onStart() {
		// 提示开始
	}
	
	@Override
	public void onUpdateProgress(long totalLength, long completedLength) {
		// 更新进度
	}

	@Override
	protected void onSuccess(HttpResponse httpResponse, String responseContent, boolean isNotRefresh, boolean isOver) {
	    // 加载成功，使用webview显示html源码
		Header contentTypeHeader = httpResponse.getEntity().getContentType();
		ContentType contentType = new ContentType(contentTypeHeader.getValue());
		webViewManager.getWebView().loadDataWithBaseURL(null, responseContent, contentType.getMimeType(), contentType.getCharset("UTF-8"), null);
	}
	
	@Override
	protected void onFailure(Throwable throwable, boolean isNotRefresh) {
		// 提示失败
	}
});
```

##### 扩展：请求对象配置详解
**1.请求名称的配置：**

在请求对象上加上``@Name``注解即可，例如：``@Name("百度搜索")``。请求名称将用于在log中区分不同的请求。
```java
@Name("百度搜索")
@Url("http://www.baidu.com/s")
public class BaseduSearchRequest implements Request {
    // ...
}
```

**2.请求方式的配置：**

在请求对象上加上``@Method``注解即可，例如：``@Method(MethodType.POST)``；目前支持GET、POST、PUT、DELETE四种请求，缺省值是``MethodType.GET``。
```java
@Method(MethodType.POST)
public class BaseRequest implements Request {
    // ...
}
```
你可以定义一个BaseRequest，在BaseRequest上配置请求方式为post，然后其它所有请求都继承于BaseRequest，由于@Method注解可继承所以所有BaseRequest的子类就都是post方式了
    
**3.请求地址的配置：**

3.1 使用``@Url``注解来指定完整的请求地址。例如：``@Url("http://m.weather.com.cn/data/101010100.html")``；
```java
@Url("http://m.weather.com.cn/data/101010100.html")
public class BeijingWeatherRequest implements Request {
    // ...
}
```

3.2 你还可以选择使用``@Host``加``@Path``注解来指定完整的请求地址，其中``@Host``负责不可变部分（例如``http://m.weather.com.cn``）；``@Path``负责变化部分（例如：``data/101010100.html``）；值得注意的是请不要在``@Host``的末尾或者``@Path``的开头加``/``，因为在解析的时候会自动加上；
```java
@Host("http://m.weather.com.cn")
public class WeatherRequest implements Request{
    // ...
}
```

以上``@Url``、``@Host``以及``@Path``注解都是可以继承的，因此你可以弄一个BaseRequest然后把请求地址的不可变部分用``@Host``注解加在BaseRequest上，然后其他的请求都继承BaseRequest，这样一来其它的请求就只需添加``@Path``注解即可，同时也可以保证主机地址只会在一个地方定义；另外，``@Url``的优先级高于``@Host``加``@Path``。

**4.请求头的配置：**

1.你可以在请求对象中定义一个字段，字段类型可以是Header、Header[]或者Collection<Header>，然后在此字段上加上``@Header`注解即可。
```java
/**
 * 基本请求
 */
@Method(MethodType.POST)
public class BaseRequest implements Request {
    @Header
    private org.apache.http.Header header = new BasicHeader("Connection", "Keep-Alive");

    @Header
    private org.apache.http.Header[] headers = new org.apache.http.Header[]{new BasicHeader("Connection", "Keep-Alive"), new BasicHeader("Content-Length", "22")};

    @Header
    private List<org.apache.http.Header> headerList;

    public BaseRequest() {
        headerList = new ArrayList<org.apache.http.Header>(2);
        headerList.add(new BasicHeader("Connection", "Keep-Alive"));
        headerList.add(new BasicHeader("Content-Length", "22"));
    }
}
```
示例中的三种方式任意一种即可。
    
**5.请求参数的配置：**

1. 将请求对象中需要转换成请求参数的字段加上``@Param``注解即可；
2. 默认请求参数名称是字段的名称，如果你想自定义名称就给``@Param``注解附上值，例如：``@Param("wd")``
3. 默认使用字段的toString()方法来获取请求参数值，但以下几种情况将会被特殊处理：
    * 字段上有``@Value``注解
        有``@Value``注解时，将会用``@Value``注解的值来作为请求参数值，而不再考虑字段的值
    * 字段类型为Map
        对于``Map``类型的字段``HappyHttpClient``会将其每一对键值对都转换成请求参数，而每一对键值对的键将作为参数名，键值对的值将作为参数值；
    * 字段类型为File
        对于``File``类型的字段HappyHttpClient将使用``RequestParams``的``put(String key, File file)``方法将其添加到``RequestParams``中；
    * 字段类型为ArrayList
        对于``ArrayList``类型的字段HappyHttpClient将使用``RequestParams``的``put(String key, ArrayList<String> values)``方法将其添加到``RequestParams``中；
    * 字段类型为Boolean
        对于``Boolean``类型的字段你可以通过``@True``和``@False``注解来指定当字段值是``true``或``false``的时候其对应的转换成请求参数时的参数值；
    * 字段类型为Enum
        对于``Enum``类型的参数你可以使用``@Value``注解来指定其参数值，如果没有```@Value``注解将使用Enum对象的name来作为参数值。

```java
@Name("百度搜索")
@Url("http://www.baidu.com/s")
public class BaiduSearchRequest implements Request {
    @Param
    public String rsv_spt = "1";

	@Param("wd")
	public String keyword;

	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		this.keyword = keyword;
	}
}
```

**6.HttpResponse缓存的配置：**

使用``@CacheConfig``注解来配置响应缓存，有四个参数可供配置
1. periodOfValidity：int型，指定缓存有效期，单位毫秒，小于等于0表示永久有效，默认值为0；
2. isRefreshCache：boolean型，指定当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存，默认值为false；
3. isRefreshCallback：boolean型，指定当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()，默认值为false；
4. cacheDirectory：String型，指定缓存目录，默认值为``""``。
5. 使用``@CacheIgnore``来配置在组织缓存ID的时候需要忽略的参数（这个很重要）

```java
@Url("http://www.qiushibaike.com/article/52638010")
@CacheConfig(periodOfValidity = 1000 * 60 * 60 * 24, isRefreshCache = true)
public class QiuBaiRequest extends BaseRequest{
    @Param
    private String list = "8hr";

    @Param
    private String s = "4618412";
}
```
更加详细的配置方式请参考示例程序。

###2. 处理响应
####使用HttpResponseHandler
不管你用何种方式发送请求，都会要求传一个HttpResponseHandler，来处理Http响应。
HttpResponseHandler的特点：
>* 提供完整的声明周期回调函数onStart()、onHandleResponse()、onUpdateProgress()、onException()、onCancel()；
>* 可随时取消，在执行玩get()请求后悔返回一个RequestHandle给你，你可以通过RequestHandle来查看请求是否执行完毕，或者取消请求，被取消的请求将不再发生任何回调，且会根据mayInterruptIfRunning参数决定是否立即停止接收数据；
>* 进度回调功能，此功能默认是关闭的，当你通过HttpResponseHandler(boolean enableUpdateProgress)构造函数或调用setEnableUpdateProgress()方法开启此功能后，将会回调onUpdateProgress()方法来回调进度；
>* HttpResponseHandler默认所有的回调都通过Handler来执行，不过有时候的确不需要在Handler中回调，这时候你可以通过setSynchronizationCallback()方法切换到同步回调模式。

目前内置的HttpResponseHandler有四种分别是：

>* BinaryHttpResponseHandler：读取http响应后直接存在一个字节数组中返回，特别注意由于是要存在内存中，所以http响应不可以太大；
>* DownloadHttpResponseHandler：读取HTTP响应并一边读一边保存到本地文件中，适用于下载比较大的文件；
>* JsonHttpResponseHandler：会将HTTP响应转换成指定类型的对象返回，适用于http响应是json字符串；
>* StringHttpResponseHandler：读取HTTP响应并转换成字符串返回。

####缓存Http响应
>* 在使用普通方式发送请求的时候你可以传一个CacheConfig类型的参数来定义缓存配置;
>* 使用请求对象方式发送请求的时候你可以在请求对象上加上@CacheConfig注解来定义缓存配置。
>* 另外你可以使用``@CacheIgnore``来配置在组织缓存ID的时候需要忽略的参数
            
###3. 取消请求
#### 单个取消
execute()、get()、post()等方法都会返回一个ReponseHandle类型的对象，此对象持有当前任务，你可以通过此对象判断任务是否执行完成，或者取消任务
```java
private void load(){
	File file = new File(getExternalCacheDir(), "800x600.jpg");
	final RequestHandle requestHandle = HappyHttpClient.getInstance(getBaseContext()).get("http://b.zol-img.com.cn/desk/bizhi/image/4/1600x900/1386814415425.jpg", new DownloadHttpResponseHandler(file, true) {
		// ...
	}, this);

	// 在一秒钟后如果没有下载完成就取消下载，并不再接收数据
	new Handler().postDelayed(new Runnable() {
		@Override
		public void run() {
			if(!requestHandle.isFinished()){
				requestHandle.cancel(true);
			}
		}
	}, 1000);
}
```

#### 批量取消
在execute()、get()、post()等方法的最后都允许你传一个Object类型的参数，这个参数被用来标记请求，之后你就可以通过cancelRequests()方法使用你之前传入的请求标签来取消请求，示例如下：
```java
public class DownloadActivity extends MyActivity {
    private Object requestTag = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		File file = new File(getExternalCacheDir(), "800x600.jpg");
		
		// 第一次发送请求
        HappyHttpClient.getInstance(getBaseContext()).get("http://b.zol-img.com.cn/desk/bizhi/image/4/1600x900/1386814415425.jpg", new DownloadHttpResponseHandler(file, true) {
			// ...
		}, requestTag);

        // 第二次发送请求
        HappyHttpClient.getInstance(getBaseContext()).get("http://img.pconline.com.cn/800x600.jpg", new DownloadHttpResponseHandler(file, true) {
            // ...
        }, requestTag);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 取消与requestTag相关的所有请求
		HappyHttpClient.getInstance(getBaseContext()).cancelRequests(requestTag, true);
	}
}
```

####自定义HttpResponseHandler
自定HttpResponseHandler需要有几点需要注意

1. 首先你要不断通过isCancelled()方法检查是否已经取消，一旦发现已经取消就结束执行并且做好善后处理
    ```java
    OutputStream outputStream = null;
	try{
		outputStream = new BufferedOutputStream(new FileOutputStream(file), 8*1024);
		BaseUpdateProgressCallback baseUpdateProgressCallback = isEnableUpdateProgress()?new BaseUpdateProgressCallback(this, handler):null;
		ProgressEntityUtils.read(httpEntity, outputStream, this, baseUpdateProgressCallback);
	}finally{
		GeneralUtils.close(outputStream);
	}
	if(isCancelled()){
		if(file.exists()){
			file.delete();
		}
		return;
	}
	
	if(!isSynchronizationCallback()){
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(isCancelled()) return;
				onSuccess(file);
			}
		});
	}else{
		onSuccess(file);
	}
    ```

2. 其次在循环读取数据的时候要通过isStopReadData()方法判断是否需要停止读取，一旦发现需要停止读取数据就必须立即停止
    ```java
    while(!httpResponseHandler.isStopReadData() && (readLength = inputStream.read(tmp)) != -1) {
        outputStream.write(tmp, 0, readLength);
        completedLength += readLength;
        if(!httpResponseHandler.isCancelled() && updateProgressCallback != null && !updateProgressCallback.isMarkRead()){
        	updateProgressCallback.onUpdateProgress(contentLength, completedLength);
        }
    }
    ```

3. 在回调相关方法的时候要通过isSynchronizationCallback()方法来决定同步回调还是异步回调，例如：
    ```java
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
    ```

4. 在读取数据的时候根据isEnableProgressCallback()方法来决定是否开启回调进度功能，例如：
    ```java
    outputStream = new BufferedOutputStream(new FileOutputStream(file), 8*1024);
	BaseUpdateProgressCallback baseUpdateProgressCallback = isEnableUpdateProgress()?new BaseUpdateProgressCallback(this, handler):null;
	ProgressEntityUtils.read(httpEntity, outputStream, this, baseUpdateProgressCallback);
    ```

5. 当你需要用到缓存功能时可以重写isCanCache()方法，来决定当前HttpResponse是否可以缓存，默认的是当状态码大于等于200且小于三百就可以缓存

最好的的示例就是内置的HttpResponseHandler

##Downloads
>* [android-happy-network-2.4.2.jar](https://github.com/xiaopansky/HappyNetwork/raw/master/releases/android-happy-network-2.4.2.jar)
>* [android-happy-network-2.4.2-with-src.jar](https://github.com/xiaopansky/HappyNetwork/raw/master/releases/android-happy-network-2.4.2-with-src.jar)

Dependencies
>* **[gson-2.2.4.jar](https://github.com/xiaopansky/HappyNetwork/raw/master/libs/gson-2.2.4.jar)** 可选的。如果你要使用JsonHttpResponseHandler和缓存功能的话就必须引入此类库 

##Change Log
####2.4.2
>* 去除了一个潜在的BUG，之前如果你第一次调用HappyHttpClient.getInstance(getBaseContext())是在非主线程中的话将会抛出异常，现在不会了

####2.4.1
>* 包名从me.xiaopan.android.happynetwork.http改为me.xiaopan.android.net.http

####2.4.0
>* 项目更名为HappyNetwork
>* 包更名为me.xiaopan.android.happynetwork.http
>* EasyHttpClient更名为HappyHttpClient

####2.3.0
>* ResponseCache类和注解都改名为CacheConfig；
>* 修复加载进度在回调之前会等待很长一段时间的BUG，原因前由于使用了BufferedHttpEntity，而BufferedHttpEntity要先将数据读完保存在一个字节数组中，然后才给你读；
>* 修复在使用DownloadHttpResponseHandler时如果要下载的文件特别大的话会造成系统崩溃的BUG，原因是DownloadHttpResponseHandler使用了BufferedHttpEntity，而BufferedHttpEntity是要先把数据给读到一个字节数组中的，如果文件长度超过了数组长度的最大限制，那么就会造成崩溃；
>* HttpGetRequest、HttpDeleteRequest、HttpPostRequest、HttpPutRequest都去掉了Builder；
>* HttpResponseHandler增加了同步回调开关和更新进度回调开关，默认将不再回调进度。

####2.2.7
>* HttpResponseHandler的onHandleResponse()方法增加HttpUriRequest参数
>* 优化请求处理逻辑，增加失败LOG
>* 优化缓存过期判定逻辑，并优化LOG

####2.2.6
>* 修复DownloadHttpResponseHandler已完成进度参数传递错误BUG
>* 完善所有内置HttpResponseHandler实现的取消处理

####2.2.5
>* 当已经取消时，就不在回调handleResponse()方法

####2.2.4
>* 增加``@Value``注解，用来配置请求参数值
>* 枚举类型的字段的请求参数值配置注解由``@Param``替换为``@Value``
>* 增加DownloadHttpResponseHandler

####2.2.3
>* 所有有字符串参数的注解都支持使用String资源来配置，例如之前是``@Param("loginName")``，现在你还可以这样写`@Param(R.string.login_name)``，然后字符串资源的内容是``<string name="login_name">loginName</string>``

####2.2.2
>* 优化HttpResponseHandler回调方法命名逻辑并优化异常处理

###2.2.1
>* 优化HttpClient组织方式，采用HttpClientManager来管理和配置HttpClient
>* getInstance()方法增加Context参数
>* 所有发送请求的方法的参数重新规划
>* 恢复默认开启Gzip高速传输
>* 修复默认超时时间只有两秒的BUG

####2.1.8
>* 增加初始化功能，在使用之前必须调用init()方法初始化，并且初始化方法只能在主线程中调用

###2.1.7
>* 优化缓存功能，支持配置忽略请求参数

####2.1.6
>* 优化缓存控制功能，并优化BinaryHttpResponseHandler、JsonHttpResponseHandler、StringHttpResponseHandler等Handler相关回调方法的参数，使之表达更准确，更容易理解

####2.1.5
>* 不再默认支持Gzip超高速传输，因为在实际使用中由于使用了Gzip超高速传输出现了java.io.IOException: unknown format (magic number 227b)异常，此异常出现频率大概20%，并且到现在位置我尚未发现其规律，所以目前无法解决。如果你想开启Gzip超高速传输可通过下面代码实现
```java
HappyHttpClient.getInstance().getConfiguration().getDefaultHttpClient().addRequestInterceptor(new GzipProcessRequestInterceptor());
HappyHttpClient.getInstance().getConfiguration().getDefaultHttpClient().addResponseInterceptor(new GzipProcessResponseInterceptor());
```

####2.1.4
>* 优化RequestPaser

####2.1.3
>* 修复GzipProcessResponseInterceptor引发的请求失败的BUG

####2.1.2
>* 修复ResponseCache注解没有加运行时标记的BUG

####2.1.1
>* 注解的序列化名称注解由SerializedName替换为Param

##License
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
