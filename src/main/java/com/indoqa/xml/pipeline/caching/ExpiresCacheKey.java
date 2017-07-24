/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package com.indoqa.xml.pipeline.caching;

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;

/**
 * A cache key implementation that expires after a defined number of seconds.
 */
public final class ExpiresCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 6336351832100762643L;

    private final CacheKey cacheKey;

    private final long timestamp;

    private final long expirationTimestamp;

    public ExpiresCacheKey(final CacheKey cacheKey, final String expires) {
        this.cacheKey = cacheKey;
        this.timestamp = System.currentTimeMillis();
        this.expirationTimestamp = this.timestamp + Long.parseLong(expires) * 1000;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ExpiresCacheKey)) {
            return false;
        }

        final ExpiresCacheKey other = (ExpiresCacheKey) obj;
        return this.cacheKey.equals(other.cacheKey);
    }

    public long getExpirationTimestamp() {
        return this.expirationTimestamp;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#getLastModified()
     */
    @Override
    public long getLastModified() {
        return this.cacheKey.getLastModified();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.getClass().getName()).append(this.cacheKey.hashCode()).toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#isValid(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public boolean isValid(final CacheKey cacheKey) {
        if (!(cacheKey instanceof ExpiresCacheKey)) {
            return false;
        }

        final ExpiresCacheKey other = (ExpiresCacheKey) cacheKey;

        if (this.expirationTimestamp > other.timestamp) {
            return true;
        }

        return this.cacheKey.isValid(other.cacheKey);
    }
}
