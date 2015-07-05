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

package me.xiaopan.gohttp.header;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.message.BasicHeader;

public class LastModified extends BasicHeader{
	public static final String NAME = "Last-Modified";

	public LastModified(String value) {
		super(NAME, value);
	}

    public static LastModified fromHttpMessage(HttpMessage httpMessage){
        Header firstHeader = httpMessage.getFirstHeader(NAME);
        if(firstHeader == null){
            return null;
        }
        return new LastModified(firstHeader.getValue());
    }
}
