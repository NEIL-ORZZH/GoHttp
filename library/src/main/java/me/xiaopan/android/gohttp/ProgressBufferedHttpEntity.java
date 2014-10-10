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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/***
 * 带有进度功能的Http实体缓冲器
 */
public class ProgressBufferedHttpEntity extends HttpEntityWrapper {

	private final byte[] buffer;

	public ProgressBufferedHttpEntity(final HttpEntity entity, HttpResponseHandler httpResponseHandler, UpdateProgressCallback updateProgressCallback) throws IOException {
		super(entity);
		if (!entity.isRepeatable() || entity.getContentLength() < 0) {
			this.buffer = ProgressEntityUtils.toByteArray(entity, httpResponseHandler, updateProgressCallback);
		} else {
			this.buffer = null;
		}
	}

	public ProgressBufferedHttpEntity(final HttpEntity entity, HttpResponseHandler httpResponseHandler) throws IOException {
		this(entity, httpResponseHandler, null);
	}

	public long getContentLength() {
		if (this.buffer != null) {
			return this.buffer.length;
		} else {
			return wrappedEntity.getContentLength();
		}
	}

	public InputStream getContent() throws IOException {
		if (this.buffer != null) {
			return new ByteArrayInputStream(this.buffer);
		} else {
			return wrappedEntity.getContent();
		}
	}

	/***
	 * Tells that this entity does not have to be chunked.
	 * 
	 * @return <code>false</code>
	 */
	public boolean isChunked() {
		return (buffer == null) && wrappedEntity.isChunked();
	}

	/***
	 * Tells that this entity is repeatable.
	 * 
	 * @return <code>true</code>
	 */
	public boolean isRepeatable() {
		return true;
	}

	public void writeTo(final OutputStream outstream) throws IOException {
		if (outstream == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		if (this.buffer != null) {
			outstream.write(this.buffer);
		} else {
			wrappedEntity.writeTo(outstream);
		}
	}

	// non-javadoc, see interface HttpEntity
	public boolean isStreaming() {
		return (buffer == null) && wrappedEntity.isStreaming();
	}

} // class BufferedHttpEntity
