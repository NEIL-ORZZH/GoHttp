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

import org.apache.http.message.BasicHeader;

public class Range extends BasicHeader {
	public static final String NAME = "Range";
	/**
	 * 开始位置
	 */
	private long startLocation;
	/**
	 * 结束位置
	 */
	private long endLocation;
	
	public Range(long startLocation, long endLocation) {
        super(NAME, "bytes=" + startLocation + "-"+ endLocation);
		this.startLocation = startLocation;
		this.endLocation = endLocation;
	}
	
	public long getStartLocation() {
		return startLocation;
	}

	public long getEndLocation() {
		return endLocation;
	}
}
