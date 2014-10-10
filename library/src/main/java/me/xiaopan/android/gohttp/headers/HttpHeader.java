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

/**
 * Http属性
 */
public abstract class HttpHeader {
	/**
	 * 获取字段名字
	 * @return 字段名字
	 */
	public abstract String getName();

	/**
	 * 获取字段的值
	 * @return 字段的值
	 */
	public abstract String getValue();

	/**
	 * 设置字段的值
	 * @param value 字段的值
	 */
	public abstract void setValue(String value);
}
