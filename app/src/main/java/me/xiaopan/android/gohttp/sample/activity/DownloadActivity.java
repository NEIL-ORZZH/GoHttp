package me.xiaopan.android.gohttp.sample.activity;

import java.io.File;

import me.xiaopan.android.gohttp.GoHttp;
import me.xiaopan.android.gohttp.HttpRequest;
import me.xiaopan.android.gohttp.HttpRequestFuture;
import me.xiaopan.android.gohttp.DownloadHttpResponseHandler;
import me.xiaopan.android.gohttp.sample.R;
import me.xiaopan.android.gohttp.sample.MyActivity;
import me.xiaopan.android.gohttp.sample.net.Failure;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import org.apache.http.HttpResponse;

public class DownloadActivity extends MyActivity {
	private ImageView imageView;
    private HttpRequestFuture httpRequestFuture;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		imageView = (ImageView) findViewById(R.id.image_download);
		load();
	}
	
	private void load(){
		File file = new File(getExternalCacheDir(), "800x600.jpg");
        String url = "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg";
        httpRequestFuture = GoHttp.with(getBaseContext()).newRequest(url, new DownloadHttpResponseHandler(file), new HttpRequest.Listener<File>() {
            @Override
            public void onStarted(HttpRequest httpRequest) {
				getHintView().loading("");
            }

            @Override
            public void onCompleted(HttpRequest httpRequest, HttpResponse httpResponse, File responseContent, boolean isCache, boolean isContinueCallback) {
				imageView.setImageURI(Uri.fromFile(responseContent));
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
				Log.e("下载", "取消");
            }
        }).progressListener(new HttpRequest.ProgressListener() {
            @Override
            public void onUpdateProgress(HttpRequest httpRequest, long totalLength, long completedLength) {
                Log.e("进度", completedLength+" / "+totalLength);
				getHintView().setProgress((int)totalLength, (int)completedLength);
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