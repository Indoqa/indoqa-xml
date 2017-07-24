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
package com.indoqa.xml.pipeline;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.pipeline.caching.*;
import com.indoqa.xml.pipeline.component.PipelineComponent;

/**
 * This {@link Pipeline} basically works like the {@link CachingPipeline}.
 * <p>
 * The only difference is that when the cached result isn't valid anymore, the refresh is done in a separate thread. This means that
 * the re-production of the result doesn't block the initial request. The disadvantage of this approach is that until the result is
 * being reproduced, an out-dated result is returned. If this is out of question for a use case, the {@link CachingPipeline} has to be
 * used.
 * </p>
 */
public class AsyncCachePipeline<T extends PipelineComponent> extends CachingPipeline<T> implements CacheRefreshJob {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncCachePipeline.class);

    /**
     * The component that does the refresh in a separate thread.
     */
    private CacheRefreshManager cacheRefMan;

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.CachingPipeline#execute()
     */
    @Override
    public void execute() throws Exception {
        LOG.debug("Used cache: " + this.cache);

        // construct the current cache key
        this.cacheKey = this.constructCacheKey();

        // check for a cached value first
        final CacheValue cachedValue = this.getCachedValue(this.cacheKey);
        if (cachedValue != null) {
            // cached value found -> write it
            LOG.debug("Write cache value to output stream: " + cachedValue);
            cachedValue.writeTo(this.cachingOutputStream.getOutputStream());

            if (!this.isCacheKeyValid(cachedValue)) {
                LOG.debug("Cached value is not up to date. Delegating to " + this.cacheRefMan);
                // the cached value is not valid -> refresh the value
                this.cacheRefMan.refreshCacheValue(this.cacheKey, this);
            }
            // stop here
            return;
        }

        // no cached value (not even an invalid one) was present -> execute the pipeline
        this.invokeStarter();
        // cache the result
        final CompleteCacheValue cacheValue = new CompleteCacheValue(this.cachingOutputStream.getContent(), this.cacheKey);
        this.setCachedValue(this.cacheKey, cacheValue);
    }

    public CacheRefreshManager getCacheRefreshManager() {
        return this.cacheRefMan;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheRefreshJob#refresh(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public void refresh(final CacheKey cacheKey) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.getFinisher().setOutputStream(baos);

        // execute the pipeline
        this.invokeStarter();

        this.setCachedValue(cacheKey, new CompleteCacheValue(baos.toByteArray(), cacheKey));
    }

    public void setCacheRefreshManager(final CacheRefreshManager cacheRefMan) {
        this.cacheRefMan = cacheRefMan;
    }
}
