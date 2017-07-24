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

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;
import com.indoqa.xml.pipeline.util.StringRepresentation;

public class ObjectCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 7475542996963722056L;

    private final Object obj;

    public ObjectCacheKey(final Object obj) {
        this.obj = obj;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ObjectCacheKey)) {
            return false;
        }

        final ObjectCacheKey otherCacheKey = (ObjectCacheKey) other;
        return this.obj.equals(otherCacheKey.getObj());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#isValid(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public long getLastModified() {
        return 0;
    }

    public Object getObj() {
        return this.obj;
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.getClass().getName()).append(this.obj.toString()).toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#isValid(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public boolean isValid(final CacheKey other) {
        return this.equals(other);
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "obj=" + this.obj);
    }
}
