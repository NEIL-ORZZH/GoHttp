package me.xiaopan.android.gohttp.sample.activity;

import java.io.File;

import me.xiaopan.android.gohttp.GoHttp;
import me.xiaopan.android.gohttp.sample.R;
import me.xiaopan.android.gohttp.DownloadHttpResponseHandler;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.net.Failure;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class DownloadActivity extends MyActivity {
	private ImageView imageView;
    private Object requestTag = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		imageView = (ImageView) findViewById(R.id.image_download);
		load();
	}
	
	private void load(){
		File file = new File(getExternalCacheDir(), "800x600.jpg");

        GoHttp.getInstance(getBaseContext()).get("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg", new DownloadHttpResponseHandler(file, true) {
			@Override
			public void onStart() {
				getHintView().loading("");
			}
			
			@Override
			public void onUpdateProgress(long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
			}

			@Override
			public void onSuccess(File file) {
				imageView.setImageURI(Uri.fromFile(file));
				getHintView().hidden();
			}

			@Override
			public void onFailure(Throwable e) {
				getHintView().failure(Failure.buildByException(getBaseContext(), e), new OnClickListener() {
					@Override
					public void onClick(View v) {
						load();
					}
				});
			}

			@Override
			protected void onCancel() {
				Log.e("下载", "取消");
			}
		}, requestTag);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GoHttp.getInstance(getBaseContext()).cancelRequests(requestTag, true);
	}
}