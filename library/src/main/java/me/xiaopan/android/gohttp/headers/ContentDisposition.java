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

package me.xiaopan.android.gohttp.headers;

public class ContentDisposition extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Content-Disposition";
	/**
	 * 值
	 */
	private String value;
	private String disposition;
	private String fileName;
	
	public ContentDisposition(String value) {
		setValue(value);
	}
	
	public ContentDisposition() {
		setValue("");
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
			String[] strs = GeneralUtils.split(value, ';');
			if(strs.length > 0){
				setDisposition(strs[0]);
			}
			if(strs.length > 1){
				strs = GeneralUtils.split(strs[1], '=');
				if(strs.length > 1){
					setFileName(strs[1]);
				}
			}
		}
	}

	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
