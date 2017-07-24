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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link CacheValue} interface that can hold an arbitrary value.
 */
public class ObjectCacheValue extends AbstractCacheValue {

    private static final long serialVersionUID = -5740629911408166518L;
    private static final Logger LOG = LoggerFactory.getLogger(ObjectCacheValue.class);

    private final Object value;

    public ObjectCacheValue(final Object value, final CacheKey cacheKey) {
        super(cacheKey);

        this.value = value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#getValue()
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#setValue()
     */
    @Override
    public void setValue(final Object value) {
        throw new UnsupportedOperationException("Cannot set the content of ObjectCacheValue to OutputStream.");
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#size()
     */
    @Override
    public double size() {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(this.value);
            objectOut.flush();
            objectOut.close();
        } catch (IOException e) {
            LOG.error("Some thing goes wrong during calculating size of: " + this.getCacheKey(), e);
            return -1;
        }
        return byteOut.toByteArray().length;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Cannot write the content of ObjectCacheValue to OutputStream.");
    }
}
