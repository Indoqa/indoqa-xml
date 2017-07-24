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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.pipeline.util.StringRepresentation;

public class CompleteCacheValue extends AbstractCacheValue {

    private static final long serialVersionUID = 7956334917534138387L;

    private static final Logger LOG = LoggerFactory.getLogger(CompleteCacheValue.class);
    private byte[] content;

    public CompleteCacheValue(final byte[] content, final CacheKey cacheKey) {
        super(cacheKey);

        this.content = content.clone();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#getValue()
     */
    @Override
    public Object getValue() {
        return this.content.clone();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#setValue()
     */
    @Override
    public void setValue(final Object value) {
        if (value instanceof String) {
            this.content = ((String) value).getBytes(UTF_8);
        } else {
            // or maybe we should throw exception instead of
            // serializing object ?
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                final ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
                objectOut.writeObject(value);
                objectOut.flush();
                objectOut.close();
            } catch (IOException e) {
                LOG.error("Some thing goes wrong during calculating " + "setting value of: " + this.getCacheKey(), e);
                return;
            }
            this.content = byteOut.toByteArray();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#size()
     */
    @Override
    public double size() {
        return this.content.length;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "content.length=" + this.content.length, "cacheKey=" + this.getCacheKey());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue #writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
        outputStream.write(this.content);
    }
}
