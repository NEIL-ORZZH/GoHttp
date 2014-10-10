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
import me.xiaopan.android.gohttp.sample.R;
import me.xiaopan.android.gohttp.CacheConfig;
import me.xiaopan.android.gohttp.HttpGetRequest;
import me.xiaopan.android.gohttp.StringHttpResponseHandler;
import me.xiaopan.android.gohttp.headers.ContentType;
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
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));
		load();
	}
	
	private void load(){
		GoHttp.getInstance(getBaseContext()).get(new HttpGetRequest("http://www.miui.com/forum.php").setCacheConfig(new CacheConfig(20 * 1000)), new StringHttpResponseHandler(true){
			@Override
			protected void onStart() {
				getHintView().loading("MIUI首页");
			}
			
			@Override
			public void onUpdateProgress(long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
			}

			@Override
			protected void onSuccess(HttpResponse httpResponse, String responseContent, boolean isNotRefresh, boolean isOver) {
				Header contentTypeHeader = httpResponse.getEntity().getContentType();
				ContentType contentType = new ContentType(contentTypeHeader.getValue());
				webViewManager.getWebView().loadDataWithBaseURL(null, responseContent, contentType.getMimeType(), contentType.getCharset("UTF-8"), null);
				getHintView().hidden();
			}
			
			@Override
			protected void onFailure(Throwable throwable, boolean isNotRefresh) {
				getHintView().failure(Failure.buildByException(getBaseContext(), throwable), new OnClickListener() {
					@Override
					public void onClick(View v) {
						load();
					}
				});
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(webViewManager.getWebView().canGoBack()){
			webViewManager.getWebView().goBack();
		}else{
			super.onBackPressed();
		}
	}
}