package me.xiaopan.gohttpsample;

import android.app.ListActivity;

import me.xiaopan.gohttpsample.widget.HintView;

public class MyListActivity extends ListActivity {

	/**
	 * 获取提示视图
	 * @return
	 */
	public HintView getHintView(){
		return (HintView) findViewById(R.id.hint);
	}
}
