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

import java.util.Set;

/**
 * An abstract implementation of the {@link Cache} interface.<br/>
 * <br/>
 * It handles the validity check for retrieving {@link CacheValue}s but relies on child classes for actually accessing the underlying
 * stores.
 */
public abstract class AbstractCache implements Cache {

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.Cache#get(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public final CacheValue get(final CacheKey cacheKey) {
        return this.get(cacheKey, false);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.Cache#get(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey, boolean)
     */
    @Override
    public final CacheValue get(final CacheKey cacheKey, final boolean includeInvalid) {
        final CacheValue cacheValue = this.retrieve(cacheKey);

        return includeInvalid || this.isValid(cacheKey, cacheValue) ? cacheValue : null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.Cache#put(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey,
     *      com.indoqa.xml.pipeline.sax.pipeline.caching.CacheValue)
     */
    @Override
    public final void put(final CacheKey cacheKey, final CacheValue cacheValue) {
        this.store(cacheKey, cacheValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.Cache#clear()
     */
    @Override
    public void clear() {
        this.doClear();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.Cache#remove(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public boolean remove(final CacheKey cacheKey) {
        return this.doRemove(cacheKey);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.Cache#keySet()
     */
    @Override
    public Set<CacheKey> keySet() {
        return this.retrieveKeySet();
    }

    /**
     * Determines if the given <code>cacheValue</code> is valid according to the given <code>cacheKey</code>.<br>
     * <br>
     * This method returns <code>true</code> if and only if the given <code>cacheValue</code> is not <code>null</code> and calling
     * {@link CacheValue#isValid(CacheKey)} with the given <code>cacheKey</code> returns <code>true</code>.
     *
     * @param cacheKey The {@link CacheKey} to be used for checking the <code>cacheValue</code>'s validity.
     * @param cacheValue The {@link CacheValue} to check for validity.
     * @return <code>true</code> if the given <code>cacheValue</code> is not <code>null</code> and valid for the given
     *         <code>cacheKey</code>.
     */
    protected boolean isValid(final CacheKey cacheKey, final CacheValue cacheValue) {
        return cacheValue == null ? false : cacheValue.isValid(cacheKey);
    }

    /**
     * Actually retrieves the {@link CacheValue} from the underlying storage.<br>
     * This method must return the previously stored value - even if it is not valid anymore.
     *
     * @param cacheKey The {@link CacheKey} to be used for retrieval.
     * @return The previously stored {@link CacheValue} or <code>null</code> if no {@link CacheValue} is stored at the given
     *         <code>cacheKey</code>.
     */
    protected abstract CacheValue retrieve(CacheKey cacheKey);

    /**
     * Actually stores the given <code>cacheValue</code> at the given <code>cacheKey</code> in the underlying storage.<br>
     * <br>
     * This method is to replace any previously stored {@link CacheValue} (if any).
     *
     * @param cacheKey The {@link CacheKey} to be used for storing.
     * @param cacheValue The {@link CacheValue} to be stored.
     */
    protected abstract void store(CacheKey cacheKey, CacheValue cacheValue);

    /**
     * Actually clears the underlying storage.
     */
    protected abstract void doClear();

    /**
     * Actually removes cached data from underlying storage.
     *
     * @param cacheKey The {@link CacheKey} to be removed.
     */
    protected abstract boolean doRemove(CacheKey cacheKey);

    /**
     * Actually retrieves the {@link Set} for {@link CacheKey} from underlying storage.
     *
     * @return The {@link Set} of {@link CacheKey} of containded data.
     */
    protected abstract Set<CacheKey> retrieveKeySet();
}
