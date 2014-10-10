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
import me.xiaopan.android.gohttp.StringHttpResponseHandler;
import me.xiaopan.android.gohttp.headers.ContentType;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.net.Failure;
import me.xiaopan.android.gohttp.sample.net.request.BaiduSearchRequest;
import me.xiaopan.android.gohttp.sample.util.Utils;
import me.xiaopan.android.gohttp.sample.util.WebViewManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

/**
 * 请求对象演示Demo
 */
public class RequestObjectActivity extends MyActivity {
	private WebViewManager webViewManager;
	private EditText keywordEdit;
	private Button searchButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_object);
		keywordEdit = (EditText) findViewById(R.id.edit_requestObject_keyword);
		searchButton = (Button) findViewById(R.id.button_requestObject_search);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.closeSoftKeyboard(RequestObjectActivity.this);
				search(keywordEdit.getEditableText().toString().trim());
			}
		});
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));
		
		keywordEdit.setText("王力宏");
		search(keywordEdit.getEditableText().toString().trim());
	}
	
	@SuppressLint("HandlerLeak")
	private void search(final String keyword){
		GoHttp.getInstance(getBaseContext()).execute(new BaiduSearchRequest(keyword), new StringHttpResponseHandler(true){
			@Override
			protected void onStart() {
				searchButton.setEnabled(false);
				getHintView().loading(keyword+"相关信息");
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
				if(isNotRefresh || isOver){
					searchButton.setEnabled(true);
					getHintView().hidden();
				}
			}
			
			@Override
			protected void onFailure(Throwable throwable, boolean isNotRefresh) {
				searchButton.setEnabled(true);
				if(isNotRefresh){
					getHintView().failure(Failure.buildByException(getBaseContext(), throwable), new OnClickListener() {
						@Override
						public void onClick(View v) {
							search(keyword);
						}
					});
				}
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