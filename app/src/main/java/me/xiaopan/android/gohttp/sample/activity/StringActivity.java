/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaopan.android.gohttp.sample.activity;

import me.xiaopan.android.gohttp.GoHttp;
import me.xiaopan.android.gohttp.HttpRequest;
import me.xiaopan.android.gohttp.HttpRequestFuture;
import me.xiaopan.android.gohttp.NewStringHttpResponseHandler;
import me.xiaopan.android.gohttp.RequestFuture;
import me.xiaopan.android.gohttp.sample.R;
import me.xiaopan.android.gohttp.CacheConfig;
import me.xiaopan.android.gohttp.header.ContentType;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.net.Failure;
import me.xiaopan.android.gohttp.sample.util.WebViewManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

/**
 * 字符串
 */
public class StringActivity extends MyActivity {
	private WebViewManager webViewManager;
    private HttpRequestFuture httpRequestFuture;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));
		load();
	}
	
	private void load(){
        httpRequestFuture = GoHttp.with(getBaseContext()).newRequest("http://www.miui.com/forum.php", new NewStringHttpResponseHandler(), new HttpRequest.Listener<String>() {
            @Override
            public void onStarted(HttpRequest httpRequest) {
				getHintView().loading("MIUI首页");
            }

            @Override
            public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, String responseContent, boolean isCache, boolean isContinueCallback) {
				Header contentTypeHeader = httpResponse.getEntity().getContentType();
				ContentType contentType = new ContentType(contentTypeHeader.getValue());
				webViewManager.getWebView().loadDataWithBaseURL(null, responseContent, contentType.getMimeType(), contentType.getCharset("UTF-8"), null);
				getHintView().hidden();
            }

            @Override
            public void onFailed(HttpRequest httpRequest, HttpResponse httpResponse, HttpRequest.Failure failure, boolean isCache, boolean isContinueCallback) {
				getHintView().failure(Failure.buildByException(getBaseContext(), failure.getException()), new OnClickListener() {
					@Override
					public void onClick(View v) {
						load();
					}
				});
            }

            @Override
            public void onCanceled(HttpRequest httpRequest) {

            }
        }).progressListener(new HttpRequest.ProgressListener() {
            @Override
            public void onUpdateProgress(HttpRequest httpRequest, long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
            }
        }).cacheConfig(new CacheConfig(20 * 1000))
          .go();
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
        if(httpRequestFuture != null && !httpRequestFuture.isFinished()){
            httpRequestFuture.cancel(true);
        }
    }
}