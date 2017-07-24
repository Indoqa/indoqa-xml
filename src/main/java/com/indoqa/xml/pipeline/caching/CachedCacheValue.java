/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import com.indoqa.xml.pipeline.util.StringRepresentation;

public class CachedCacheValue extends AbstractCacheValue {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CachedCacheValue.class);

    public CachedCacheValue(final CacheKey cacheKey) {
        super(cacheKey);
    }

    @Override
    public Object getValue() {
        return this.getCacheKey();
    }

    @Override
    public void setValue(final Object value) {
        throw new UnsupportedOperationException("Cannot set the content of CachedCacheValue to OutputStream.");
    }

    @Override
    public double size() {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(this.getValue());
            objectOut.flush();
            objectOut.close();
        } catch (IOException e) {
            LOG.error("Some thing goes wrong during calculating size of: " + this.getCacheKey(), e);
            return -1;
        }
        return byteOut.toByteArray().length;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "cacheKey=" + this.getCacheKey());
    }

    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Cannot write the content of CachedCacheValue to OutputStream.");
    }
}
