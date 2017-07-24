/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.indoqa.xml.pipeline.caching;

import java.io.IOException;
import java.io.OutputStream;

public class CachingOutputStream extends OutputStream {

    private byte[] buffer;

    private int length;

    private OutputStream outputStream;

    public CachingOutputStream(final OutputStream outputStream) {
        super();

        this.outputStream = outputStream;
        this.buffer = new byte[1024];
        this.length = 0;
    }

    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    public byte[] getContent() {
        final byte[] result = new byte[this.length];
        System.arraycopy(this.buffer, 0, result, 0, this.length);
        return result;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.outputStream.write(b, off, len);

        if (this.length + len >= this.buffer.length) {
            final byte[] nextBuffer = new byte[this.length + len + 1024];
            System.arraycopy(this.buffer, 0, nextBuffer, 0, this.buffer.length);
            this.buffer = nextBuffer;
        }

        System.arraycopy(b, off, this.buffer, this.length, len);
        this.length += len;
    }

    @Override
    public void write(final int b) throws IOException {
        this.outputStream.write(b);

        if (this.length + 1 == this.buffer.length) {
            final byte[] nextBuffer = new byte[this.buffer.length + 1024];
            System.arraycopy(this.buffer, 0, nextBuffer, 0, this.buffer.length);
            this.buffer = nextBuffer;
        }

        this.buffer[this.length] = (byte) b;
        this.length++;
    }
}
