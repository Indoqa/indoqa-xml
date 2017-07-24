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

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.indoqa.xml.pipeline.util.StringRepresentation;

/**
 * A very simple implementation of the {@link Cache} interface.<br/>
 * <br/>
 * It uses a {@link WeakHashMap} as internal data store.
 */
public class SimpleCache extends AbstractCache {

    private final Map<CacheKey, CacheValue> map = new WeakHashMap<CacheKey, CacheValue>();

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        synchronized (this) {
            return StringRepresentation.buildString(this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.AbstractCache#retrieve(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    protected CacheValue retrieve(final CacheKey cacheKey) {
        synchronized (this.map) {
            return this.map.get(cacheKey);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.AbstractCache#store(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey,
     *      com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue)
     */
    @Override
    protected void store(final CacheKey cacheKey, final CacheValue cacheValue) {
        synchronized (this.map) {
            this.map.put(cacheKey, cacheValue);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.AbstractCach#doClear()
     */
    @Override
    protected void doClear() {
        synchronized (this.map) {
            this.map.clear();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.AbstractCach#doRemove(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    protected boolean doRemove(final CacheKey cacheKey) {
        Object pattern;
        Object removed;
        synchronized (this.map) {
            pattern = this.map.get(cacheKey);
            removed = this.map.remove(cacheKey);
        }

        return pattern == null ? (removed == null) : pattern.equals(removed);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.AbstractCach#retrieve()
     */
    @Override
    protected Set<CacheKey> retrieveKeySet() {
        synchronized (this.map) {
            return this.map.keySet();
        }
    }
}
