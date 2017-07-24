
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.pipeline.util.StringRepresentation;

/**
 * This {@link CacheKey} will store the keys in a {@link SimpleCache} using an {@link ObjectCacheKey} to identify the cache entry.
 *
 * This object is for components that need to be executed to be able to create a valid {@link CacheKey} entry.
 */
public class CachedCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CachedCacheKey.class);

    /**
     * Simple cache to store the {@link CacheKey}
     */
    private static final Cache CACHED_CACHE = new SimpleCache();

    /**
     * {@link CacheKey} that will be used as key of the cache store.
     */
    private CacheKey internalCacheKey;

    /**
     * {@link CacheKey} to query for real cache info.
     */
    private CacheKey cachedCacheKey;

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CachedCacheKey)) {
            return false;
        }

        final CachedCacheKey other = (CachedCacheKey) obj;
        return this.internalCacheKey.equals(other.internalCacheKey);
    }

    /**
     * Delegates on cached key.
     */
    @Override
    public long getLastModified() {
        return this.cachedCacheKey == null ? -1 : this.cachedCacheKey.getLastModified();
    }

    /**
     * Delegates on cached key.
     */
    @Override
    public int hashCode() {
        return this.cachedCacheKey == null ? super.hashCode() : this.cachedCacheKey.hashCode();
    }

    /**
     * Delegates on cached key.
     */
    @Override
    public boolean isValid(final CacheKey cacheKey) {
        if (cacheKey == null || !this.equals(cacheKey)) {
            return false;
        }

        final CachedCacheKey otherCachedCacheKey = (CachedCacheKey) cacheKey;
        return this.cachedCacheKey != null && this.cachedCacheKey.isValid(otherCachedCacheKey.cachedCacheKey);
    }

    /**
     * Set internal key to store the real component {@link CacheKey}.
     *
     * @param key Simple {@link CacheKey} that can be easily generated for the component
     */
    public void setKey(final String key) {
        this.internalCacheKey = new ObjectCacheKey(key);
        if (CACHED_CACHE.get(this.internalCacheKey, true) == null) {
            // First time processed. Return invalid to update with real one.
            this.cachedCacheKey = new InvalidCacheKey(this.getClass().getName());
            LOG.debug("{} first time processed. Returning InvalidCacheKey.", key);
        } else {
            this.cachedCacheKey = (URLListCacheKey) CACHED_CACHE.get(this.internalCacheKey, true).getValue();
        }
    }

    /**
     * Set the real {@link CacheKey}.
     *
     * @param value {@link CacheKey}
     */
    public void setValue(final URLListCacheKey value) {
        this.cachedCacheKey = value;
        CACHED_CACHE.put(this.internalCacheKey, new CachedCacheValue(this.cachedCacheKey));
    }

    @Override
    public String toString() {
        return StringRepresentation
            .buildString(this, "internalCacheKey=" + this.internalCacheKey, "cachedCacheKey=" + this.cachedCacheKey);
    }
}
