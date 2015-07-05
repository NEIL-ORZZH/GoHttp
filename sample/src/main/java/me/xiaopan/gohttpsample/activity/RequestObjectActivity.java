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

package me.xiaopan.gohttpsample.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;

import me.xiaopan.gohttp.GoHttp;
import me.xiaopan.gohttp.HttpRequest;
import me.xiaopan.gohttp.StringHttpResponseHandler;
import me.xiaopan.gohttp.header.ContentType;
import me.xiaopan.gohttpsample.MyActivity;
import me.xiaopan.gohttpsample.R;
import me.xiaopan.gohttpsample.net.Failure;
import me.xiaopan.gohttpsample.net.request.BaiduSearchRequest;
import me.xiaopan.gohttpsample.util.Utils;
import me.xiaopan.gohttpsample.util.WebViewManager;

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
        GoHttp.with(getBaseContext()).newRequest(new BaiduSearchRequest(keyword), new StringHttpResponseHandler(), new HttpRequest.Listener<String>() {
            @Override
            public void onStarted(HttpRequest httpRequest) {
				searchButton.setEnabled(false);
				getHintView().loading(keyword+"相关信息");
            }

            @Override
            public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, String responseContent, boolean isCache, boolean isContinueCallback) {
				ContentType contentType = ContentType.fromHttpMessage(httpResponse);
				webViewManager.getWebView().loadDataWithBaseURL(null, responseContent, contentType.getMimeType(), contentType.getCharset("UTF-8"), null);
				if(!isCache || !isContinueCallback){
					searchButton.setEnabled(true);
					getHintView().hidden();
				}
            }

            @Override
            public void onFailed(HttpRequest httpRequest, HttpResponse httpResponse, HttpRequest.Failure failure, boolean isCache, boolean isContinueCallback) {
				searchButton.setEnabled(true);
				if(!isCache){
					getHintView().failure(Failure.buildByException(getBaseContext(), failure.getException()), new OnClickListener() {
						@Override
						public void onClick(View v) {
							search(keyword);
						}
					});
				}
            }

            @Override
            public void onCanceled(HttpRequest httpRequest) {

            }
        }).progressListener(new HttpRequest.ProgressListener() {
            @Override
            public void onUpdateProgress(HttpRequest httpRequest, long totalLength, long completedLength) {
                getHintView().setProgress((int) totalLength, (int) completedLength);
            }
        }).go();
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