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
import me.xiaopan.android.gohttp.JsonHttpResponseHandler;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.beans.Weather;
import me.xiaopan.android.gohttp.sample.net.Failure;
import me.xiaopan.android.gohttp.sample.net.request.BeijingWeatherRequest;

import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 获取天气信息
 */
public class JsonActivity extends MyActivity {
	private TextView text;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);
		text = (TextView) findViewById(R.id.text1);
		load();
	}
	
	private void load(){
		GoHttp.getInstance(getBaseContext()).execute(new BeijingWeatherRequest(), new JsonHttpResponseHandler<Weather>(getBaseContext(), Weather.class, true) {
			@Override
			protected void onStart() {
				getHintView().loading("天气信息");
			}
			
			@Override
			public void onUpdateProgress(long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
			}
			
			@Override
			protected void onSuccess(HttpResponse httpResponse, Weather responseObject, boolean isNotRefresh, boolean isOver) {
				text.setText(Html.fromHtml("<h2>" + responseObject.getCity() + "</h2>"
						+ "<br>" + responseObject.getDate_y() + " " + responseObject.getWeek()
						+ "<br>" + responseObject.getTemp1() + " " + responseObject.getWeather1()
						+ "<p><br>风力：" + responseObject.getWind1()
						+ "<br>紫外线：" + responseObject.getIndex_uv()
						+ "<br>紫外线（48小时）：" + responseObject.getIndex48_uv()
						+ "<br>穿衣指数：" + responseObject.getIndex() + "，" + responseObject.getIndex_d()
						+ "<br>穿衣指数（48小时）：" + responseObject.getIndex48() + "，" + responseObject.getIndex48_d()
						+ "<br>舒适指数：" + responseObject.getIndex_co()
						+ "<br>洗车指数：" + responseObject.getIndex_xc()
						+ "<br>旅游指数：" + responseObject.getIndex_tr()
						+ "<br>晨练指数：" + responseObject.getIndex_cl()
						+ "<br>晾晒指数：" + responseObject.getIndex_ls()
						+ "<br>过敏指数：" + responseObject.getIndex_ag() + "</p>"
						));
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