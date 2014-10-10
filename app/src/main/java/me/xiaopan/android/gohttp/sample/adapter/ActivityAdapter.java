/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.gohttp.sample.adapter;

import me.xiaopan.android.gohttp.sample.R;
import me.xiaopan.android.gohttp.sample.beans.ActivityItem;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityAdapter extends BaseAdapter {

	private Context context;
	private ActivityItem[] activityItems;
	
	public ActivityAdapter(final Context context, final ListView listView, final ActivityItem... activityItems){
		this.context = context;
		this.activityItems = activityItems;
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(context, activityItems[position - listView.getHeaderViewsCount()].getTargetClass());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		});
	}
	
	@Override
	public int getCount() {
		return activityItems.length;
	}

	@Override
	public Object getItem(int position) {
		return activityItems[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text = null;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_text, null);
		}
		
		text = (TextView) convertView;
		text.setText(activityItems[position].getTitle());
		
		return convertView;
	}
}