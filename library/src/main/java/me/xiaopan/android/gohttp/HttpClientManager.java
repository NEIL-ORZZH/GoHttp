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

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import java.util.HashMap;
import java.util.Map;

import me.xiaopan.android.gohttp.httpclient.AddRequestHeaderRequestInterceptor;
import me.xiaopan.android.gohttp.httpclient.GzipProcessRequestInterceptor;
import me.xiaopan.android.gohttp.httpclient.GzipProcessResponseInterceptor;
import me.xiaopan.android.gohttp.httpclient.MySSLSocketFactory;
import me.xiaopan.android.gohttp.httpclient.RetryHandler;

/**
 * Http客户端配置
 */
public class HttpClientManager {
    private final static int DEFAULT_WAIT_TIMEOUT = 60*1000;   // 默认从连接池中获取连接的最大等待时间
    private final static int DEFAULT_READ_TIMEOUT = 20*1000;   // 默认读取超时时间
    private final static int DEFAULT_CONNECT_TIMEOUT = 20*1000;    // 默认连接超时时间
    private final static int DEFAULT_MAX_ROUTE_CONNECTIONS = 400;    // 默认每个路由的最大连接数
    private static final int DEFAULT_MAX_CONNECTIONS = 800;  // 默认最大连接数
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;  // 默认Socket缓存大小
    private static final int DEFAULT_MAX_RETRIES = 1;	//默认最大重试次数
    private static final int DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24";
	
    private HttpContext httpContext;	//Http上下文
    private DefaultHttpClient httpClient;	//Http客户端
    private Map<String, String> clientHeaderMap;	//请求头Map
	
    public HttpClientManager(SchemeRegistry schemeRegistry){
    	BasicHttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, DEFAULT_WAIT_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(DEFAULT_MAX_ROUTE_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_READ_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_CONNECT_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(httpParams, DEFAULT_USER_AGENT);
        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
        httpClient.addRequestInterceptor(new GzipProcessRequestInterceptor());
        httpClient.addRequestInterceptor(new AddRequestHeaderRequestInterceptor(clientHeaderMap = new HashMap<String, String>()));
        httpClient.addResponseInterceptor(new GzipProcessResponseInterceptor());
        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_SLEEP_TIME_MILLIS));
		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
	}
    
    /**
     * Creates a new AsyncHttpClient.
     */
    public HttpClientManager() {
        this(false, 80, 443);
    }
    
    /**
     * Creates a new AsyncHttpClient.
     * @param httpPort non-standard HTTP-only port
     */
    public HttpClientManager(int httpPort) {
        this(false, httpPort, 443);
    }

    /**
     * Creates a new HttpClientConfig
     * @param httpPort  non-standard HTTP-only port
     * @param httpsPort non-standard HTTPS-only port
     */
    public HttpClientManager(int httpPort, int httpsPort) {
        this(false, httpPort, httpsPort);
    }

    /**
     * Creates new HttpClientConfig using given params
     * @param fixNoHttpResponseException Whether to fix or not issue, by ommiting SSL verification
     * @param httpPort                   HTTP port to be used, must be greater than 0
     * @param httpsPort                  HTTPS port to be used, must be greater than 0
     */
    public HttpClientManager(boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        this(getDefaultSchemeRegistry(fixNoHttpResponseException, httpPort, httpsPort));
    }
    
	/**
	 * 获取Http上下文
	 * @return the httpContext
	 */
	public HttpContext getHttpContext() {
		if(httpContext == null){
			
		}
		return httpContext;
	}

	/**
	 * 获取Http客户端
	 * @return
	 */
	public final DefaultHttpClient getHttpClient() {
		return httpClient;
	}

    /**
     * 添加一个请求头参数，这些参数都会在发送请求之前添加到请求体中
     * @param header 参数名
     * @param value 参数值
     */
    public void addHeader(String header, String value) {
    	clientHeaderMap.put(header, value);
    }

    /**
     * Remove header from all requests this client makes (before sending).
     *
     * @param header the name of the header
     */
    public void removeHeader(String header) {
        clientHeaderMap.remove(header);
    }

	/**
	 * 获取通用请求头集合
	 * @return
	 */
	public Map<String, String> getHeaderMap() {
		return clientHeaderMap;
	}

    /**
     * Set the connection and socket timeout. By default, 10 seconds.
     *
     * @param connectTimeout the connect/socket timeout in milliseconds, at least 1 second
     */
    public void setConnectTimeout(int connectTimeout) {
        final HttpParams httpParams = this.httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
    }

    /**
     * Sets maximum limit of parallel connections
     *
     * @param maxConnections maximum parallel connections, must be at least 1
     */
    public void setMaxConnections(int maxConnections) {
        if (maxConnections < 1){
        	maxConnections = DEFAULT_MAX_CONNECTIONS;
        }
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
    }

	/**
	 * 设置Socket缓存池大小
	 * @param socketBufferSize
	 */
	public void setSocketBufferSize(int socketBufferSize) {
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setSocketBufferSize(httpParams, socketBufferSize);
	}

    /**
     * Simple interface method, to enable or disable redirects. If you set manually RedirectHandler
     * on underlying HttpClient, effects of this method will be canceled.
     *
     * @param enableRedirects boolean
     */
    public void setEnableRedirects(final boolean enableRedirects) {
        httpClient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                return enableRedirects;
            }
        });
    }
	
	/**
     * 设置Cookie仓库，将在发送请求时使用此Cookie仓库
     * @param cookieStore 另请参见 {@link me.xiaopan.android.gohttp.httpclient.PersistentCookieStore}
     */
    public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
	/**
     * 设置浏览器标识
     * @param userAgent 浏览器标识信息将会添加在“User-Agent”请求头中
     */
    public void setUserAgent(String userAgent) {
    	HttpProtocolParams.setUserAgent(httpClient.getParams(), userAgent);
    }
    
	/**
     * Sets the SSLSocketFactory to user when making requests. By default,
     * a new, default SSLSocketFactory is used.
     * @param sslSocketFactory the socket factory to use for https requests.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
    }
    
    /**
     * Sets basic authentication for the request. Uses AuthScope.ANY. This is the same as
     * setBasicAuth('username','password',AuthScope.ANY)
     *
     * @param username Basic Auth username
     * @param password Basic Auth password
     */
    public void setBasicAuth(String username, String password) {
        AuthScope scope = AuthScope.ANY;
        setBasicAuth(username, password, scope);
    }

    /**
     * Sets basic authentication for the request. You should pass in your AuthScope for security. It
     * should be like this setBasicAuth("username","password", new AuthScope("host",port,AuthScope.ANY_REALM))
     *
     * @param username Basic Auth username
     * @param password Basic Auth password
     * @param scope    - an AuthScope object
     */
    public void setBasicAuth(String username, String password, AuthScope scope) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        this.httpClient.getCredentialsProvider().setCredentials(scope, credentials);
    }

    /**
     * Removes set basic auth credentials
     */
    public void clearBasicAuth() {
        this.httpClient.getCredentialsProvider().clear();
    }

    /**
     * Sets the Proxy by it's hostname and port
     *
     * @param hostname the hostname (IP or DNS name)
     * @param port     the port number. -1 indicates the scheme default port.
     */
    public void setProxy(String hostname, int port) {
        final HttpHost proxy = new HttpHost(hostname, port);
        final HttpParams httpParams = this.httpClient.getParams();
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    /**
     * Sets the Proxy by it's hostname,port,username and password
     *
     * @param hostname the hostname (IP or DNS name)
     * @param port     the port number. -1 indicates the scheme default port.
     * @param username the username
     * @param password the password
     */
    public void setProxy(String hostname, int port, String username, String password) {
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(hostname, port),
                new UsernamePasswordCredentials(username, password));
        final HttpHost proxy = new HttpHost(hostname, port);
        final HttpParams httpParams = this.httpClient.getParams();
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    /**
     * Sets the maximum number of retries and timeout for a particular Request.
     *
     * @param retries maximum number of retries per request
     * @param timeout sleep between retries in milliseconds
     */
    public void setMaxRetriesAndTimeout(int retries, int timeout) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(retries, timeout));
    }

    public static void allowRetryExceptionClass(Class<?> cls) {
        if (cls != null) {
            RetryHandler.addClassToWhitelist(cls);
        }
    }

    public static void blockRetryExceptionClass(Class<?> cls) {
        if (cls != null) {
            RetryHandler.addClassToBlacklist(cls);
        }
    }
    
    /**
     * Returns default instance of SchemeRegistry
     *
     * @param fixNoHttpResponseException Whether to fix or not issue, by ommiting SSL verification
     * @param httpPort                   HTTP port to be used, must be greater than 0
     * @param httpsPort                  HTTPS port to be used, must be greater than 0
     */
    private static SchemeRegistry getDefaultSchemeRegistry(boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        if (fixNoHttpResponseException) {
            Log.d(GoHttp.class.getSimpleName(), "Beware! Using the fix is insecure, as it doesn't verify SSL certificates.");
        }

        if (httpPort < 1) {
            httpPort = 80;
            Log.d(GoHttp.class.getSimpleName(), "Invalid HTTP port number specified, defaulting to 80");
        }

        if (httpsPort < 1) {
            httpsPort = 443;
            Log.d(GoHttp.class.getSimpleName(), "Invalid HTTPS port number specified, defaulting to 443");
        }

        // Fix to SSL flaw in API < ICS
        // See https://code.google.com/p/android/issues/detail?id=13117
        SSLSocketFactory sslSocketFactory;
        if (fixNoHttpResponseException){
            sslSocketFactory = MySSLSocketFactory.getFixedSocketFactory();
        }else{
            sslSocketFactory = SSLSocketFactory.getSocketFactory();
        }
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
        schemeRegistry.register(new Scheme("https", sslSocketFactory, httpsPort));

        return schemeRegistry;
    }
}
