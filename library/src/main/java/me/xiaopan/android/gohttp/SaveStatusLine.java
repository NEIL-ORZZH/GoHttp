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

package me.xiaopan.android.gohttp;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;

class SaveStatusLine {
	private String reasonPhrase;
	private int statusCode;
	private int major;
	private int minor;
	private String protocol;
	
	public SaveStatusLine(StatusLine statusLine){
		this.reasonPhrase = statusLine.getReasonPhrase();
		this.statusCode = statusLine.getStatusCode();
		ProtocolVersion protocolVersion = statusLine.getProtocolVersion();
		this.major = protocolVersion.getMajor();
		this.minor = protocolVersion.getMinor();
		this.protocol = protocolVersion.getProtocol();
	}
	
	public SaveStatusLine(){
		
	}
	
	public StatusLine toStatusLine(){
		return new BasicStatusLine(new ProtocolVersion(protocol, major, minor), statusCode, reasonPhrase);
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
