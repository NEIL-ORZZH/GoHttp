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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import org.apache.http.HttpResponse;

import me.xiaopan.gohttp.GoHttp;
import me.xiaopan.gohttp.HttpRequest;
import me.xiaopan.gohttp.HttpRequestFuture;
import me.xiaopan.gohttp.JsonHttpResponseHandler;
import me.xiaopan.gohttpsample.MyActivity;
import me.xiaopan.gohttpsample.R;
import me.xiaopan.gohttpsample.beans.Weather;
import me.xiaopan.gohttpsample.net.Failure;

/**
 * 获取天气信息
 */
public class JsonActivity extends MyActivity {
	private TextView text;
    private HttpRequestFuture httpRequestFuture;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);
		text = (TextView) findViewById(R.id.text1);
		load();
	}
	
	private void load(){
        String url = "http://m.weather.com.cn/data/101010100.html";
        httpRequestFuture = GoHttp.with(getBaseContext()).newRequest(url, new JsonHttpResponseHandler(Weather.class), new HttpRequest.Listener<Weather>() {
            @Override
            public void onStarted(HttpRequest httpRequest) {
				getHintView().loading("天气信息");
            }

            @Override
            public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, Weather responseContent, boolean isCache, boolean isContinueCallback) {
				text.setText(Html.fromHtml("<h2>" + responseContent.getCity() + "</h2>"
						+ "<br>" + responseContent.getDate_y() + " " + responseContent.getWeek()
						+ "<br>" + responseContent.getTemp1() + " " + responseContent.getWeather1()
						+ "<p><br>风力：" + responseContent.getWind1()
						+ "<br>紫外线：" + responseContent.getIndex_uv()
						+ "<br>紫外线（48小时）：" + responseContent.getIndex48_uv()
						+ "<br>穿衣指数：" + responseContent.getIndex() + "，" + responseContent.getIndex_d()
						+ "<br>穿衣指数（48小时）：" + responseContent.getIndex48() + "，" + responseContent.getIndex48_d()
						+ "<br>舒适指数：" + responseContent.getIndex_co()
						+ "<br>洗车指数：" + responseContent.getIndex_xc()
						+ "<br>旅游指数：" + responseContent.getIndex_tr()
						+ "<br>晨练指数：" + responseContent.getIndex_cl()
						+ "<br>晾晒指数：" + responseContent.getIndex_ls()
						+ "<br>过敏指数：" + responseContent.getIndex_ag() + "</p>"
						));
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
                Log.e("进度", completedLength + " / " + totalLength);
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