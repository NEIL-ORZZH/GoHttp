package me.xiaopan.gohttpsample;

import android.app.Activity;

import me.xiaopan.gohttpsample.widget.HintView;

public abstract class MyActivity extends Activity {
	/**
	 * 获取提示视图
	 * @return
	 */
	public HintView getHintView(){
		return (HintView) findViewById(R.id.hint);
	}
}
