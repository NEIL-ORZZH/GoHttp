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
import me.xiaopan.android.gohttp.BinaryHttpResponseHandler;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.net.Failure;

import org.apache.http.HttpResponse;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 使用BinaryResponseHandler下载图片
 */
public class BinaryActivity extends MyActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_binary);
		load();
	}
	
	private void load(){
		GoHttp.with(getBaseContext()).get("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg", new BinaryHttpResponseHandler(true) {
			@Override
			protected void onStart() {
				getHintView().loading("图片");
			}
			
			@Override
			public void onUpdateProgress(long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
			}
			
			@Override
			protected void onSuccess(HttpResponse httpResponse, byte[] binaryData, boolean isNotRefresh, boolean isOver) {
				((ImageView) findViewById(R.id.image_binary)).setImageBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
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
}