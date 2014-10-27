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
import org.apache.http.HeaderElement;
import org.apache.http.HttpMessage;
import org.apache.http.message.BasicHeader;

public class ContentDisposition extends BasicHeader{
	public static final String NAME = "Content-Disposition";
	private String disposition;
	private String fileName;
	
	public ContentDisposition(String value) {
        super(NAME, value);
        HeaderElement[] elements = getElements();
        if(elements == null || elements.length <= 0){
            new IllegalArgumentException("value is not valid : "+value).printStackTrace();
            return;
        }

        disposition = elements[0].toString();

        if(elements.length < 2){
            new IllegalArgumentException("No second elements"+value).printStackTrace();
            return;
        }

        fileName = elements[1].getValue();
    }

    public ContentDisposition(String disposition, String fileName){
        super(NAME, disposition+"; filename=\""+fileName+"\"");
        this.disposition = disposition;
        this.fileName = fileName;
    }

    public static ContentDisposition fromHttpMessage(HttpMessage httpMessage){
        Header firstHeader = httpMessage.getFirstHeader(NAME);
        if(firstHeader == null){
            return null;
        }
        return new ContentDisposition(firstHeader.getValue());
    }
}
