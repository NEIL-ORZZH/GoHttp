/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
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

package me.xiaopan.android.gohttp.header;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class ContentLength extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Content-Length";
	/**
	 * 值
	 */
	private String value;
	/**
	 * 长度
	 */
	private long length;
	
	public ContentLength(String value) {
		setValue(value);
	}
	
	public ContentLength(long length) {
		setLength(length);
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
		if(value != null){
			this.length = Long.valueOf(value);
		}
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}
	
	public static ContentLength getContentLength(HttpResponse httpResponse){
		Header[] contentTypeString = httpResponse.getHeaders(ContentLength.NAME);
		if(contentTypeString.length > 0){
			return new ContentLength(contentTypeString[0].getValue());
		}else{
			return null;
		}
	}
}
