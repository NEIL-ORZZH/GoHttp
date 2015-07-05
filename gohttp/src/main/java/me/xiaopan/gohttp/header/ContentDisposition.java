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

public class ContentDisposition extends BasicHeader{
	public static final String NAME = "Content-Disposition";
	private String disposition;
	private String fileName;
	
	public ContentDisposition(String value) {
        super(NAME, value);

        String[] element = value.split(";");
        if(element.length > 0){
            disposition = element[0];
        }else{
            new IllegalArgumentException("value is not valid ("+value+")").printStackTrace();
            return;
        }
        if(element.length > 1){
            element = element[1].split("=");
            if(element.length > 1){
                fileName = element[1];
            }
        }else{
            new IllegalArgumentException("No second elements ("+value+")").printStackTrace();
        }
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

    public String getDisposition() {
        return disposition;
    }

    public String getFileName() {
        return fileName;
    }
}
