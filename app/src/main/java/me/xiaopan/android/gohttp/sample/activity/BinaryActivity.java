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

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import org.apache.http.HttpResponse;

import me.xiaopan.android.gohttp.BinaryHttpResponseHandler;
import me.xiaopan.android.gohttp.GoHttp;
import me.xiaopan.android.gohttp.HttpRequest;
import me.xiaopan.android.gohttp.HttpRequestFuture;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.R;
import me.xiaopan.android.gohttp.sample.net.Failure;

/**
 * 使用BinaryResponseHandler下载图片
 */
public class BinaryActivity extends MyActivity {
    private HttpRequestFuture httpRequestFuture;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_binary);
		load();
	}
	
	private void load(){
        String url = "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg";

        httpRequestFuture = GoHttp.with(getBaseContext()).newRequest(url, new BinaryHttpResponseHandler(), new HttpRequest.Listener<byte[]>() {
            @Override
            public void onStarted(HttpRequest httpRequest) {
				getHintView().loading("图片");
            }

            @Override
            public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, byte[] responseContent, boolean isCache, boolean isContinueCallback) {
				((ImageView) findViewById(R.id.image_binary)).setImageBitmap(BitmapFactory.decodeByteArray(responseContent, 0, responseContent.length));
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
        }).progressCallback(new HttpRequest.ProgressListener() {
            @Override
            public void onUpdateProgress(HttpRequest httpRequest, long totalLength, long completedLength) {
                getHintView().setProgress((int) totalLength, (int) completedLength);
            }
        }).go();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(httpRequestFuture != null && !httpRequestFuture.isFinished()){
            httpRequestFuture.cancel(true);
        }
    }
}