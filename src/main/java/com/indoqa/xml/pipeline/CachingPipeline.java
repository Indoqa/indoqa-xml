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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;

import com.indoqa.xml.pipeline.caching.*;
import com.indoqa.xml.pipeline.component.CachingPipelineComponent;
import com.indoqa.xml.pipeline.component.PipelineComponent;

/**
 * <p>
 * A {@link Pipeline} implementation that returns a cached result if, and only if all its components support caching. A
 * {@link PipelineComponent} is cacheable if it implements the interface {@link CachingPipelineComponent}.
 * </p>
 */
public class CachingPipeline<T extends PipelineComponent> extends AbstractPipeline<T> {

    private static final Logger LOG = getLogger(CachingPipeline.class);

    protected Cache cache;
    protected CacheKey cacheKey;
    protected CachingOutputStream cachingOutputStream;

    /**
     * Expires time in seconds
     */
    private String expires;

    /**
     * Expires pipelines that have non-cacheable pipeline components require an explicit cache key
     */
    private Serializable expiresCacheKey;

    private String jmxGroupName;

    @Override
    public void execute() throws Exception {
        LOG.debug("Used cache: {}", this.cache);

        // checked for a cached value first
        final CacheValue cachedValue = this.getCachedValue(this.cacheKey);
        if (this.isCacheKeyValid(cachedValue)) {
            // cached value found
            LOG.debug("Write cache value to output stream: " + cachedValue);

            cachedValue.writeTo(this.cachingOutputStream.getOutputStream());
            return;
        }

        // execute the pipeline
        this.invokeStarter();

        // cache the result
        final CompleteCacheValue cacheValue = new CompleteCacheValue(this.cachingOutputStream.getContent(), this.cacheKey);
        this.setCachedValue(this.cacheKey, cacheValue);
    }

    public CacheKey getCacheKey() {
        return this.cacheKey;
    }

    public String getExpires() {
        return this.expires;
    }

    @Override
    public long getLastModified() {
        if (this.cacheKey == null) {
            return -1;
        }

        return this.cacheKey.getLastModified();
    }

    public void setCache(final Cache cache) {
        this.cache = cache;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.AbstractPipeline#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(final Map<String, ? extends Object> parameters) {
        this.expires = (String) parameters.get("expires");
        this.expiresCacheKey = (Serializable) parameters.get("expires-cache-key");
        this.jmxGroupName = (String) parameters.get("jmx-group-name");

        super.setConfiguration(parameters);
    }

    public void setExpires(final String expires) {
        this.expires = expires;
    }

    public void setExpiresCacheKey(final Serializable expiresCacheKey) {
        this.expiresCacheKey = expiresCacheKey;
    }

    @Override
    public void setup(final OutputStream outputStream, final Map<String, Object> parameters) {
        // create a caching output stream to intercept the result
        this.cachingOutputStream = new CachingOutputStream(outputStream);

        super.setup(this.cachingOutputStream, parameters);

        this.cacheKey = this.constructCacheKey();
    }

    protected CacheKey constructCacheKey() {
        final CompoundCacheKey result = new CompoundCacheKey();
        LOG.debug("Creating " + result + ": ");

        for (PipelineComponent pipelineComponent : this.getComponents()) {
            if (pipelineComponent instanceof CachingPipelineComponent) {
                final CachingPipelineComponent component = (CachingPipelineComponent) pipelineComponent;

                final CacheKey componentKey = component.constructCacheKey();
                if (componentKey != null) {
                    result.addCacheKey(componentKey);
                    LOG.debug("  ~ adding " + componentKey + " for component " + pipelineComponent);

                    continue;
                }
            }

            // support expires caching
            if (this.expires != null) {
                LOG.debug(
                    "  ~ adding " + ExpiresCacheKey.class.getSimpleName() + " for component: " + pipelineComponent
                        + " (the component doesn't support caching " + "but expires caching is activated)");

                return new ExpiresCacheKey(new InvalidCacheKey(this.expiresCacheKey), this.expires);
            }

            // component does not support caching
            LOG.debug("  ~ no caching: " + pipelineComponent);
            LOG.debug("Aborting cache key construction");

            return null;
        }

        // support expires caching
        if (this.expires != null) {
            final CacheKey expiresCacheKey = new ExpiresCacheKey(result, this.expires);
            LOG.debug("Creating  " + expiresCacheKey + " for pipeline " + this);

            return expiresCacheKey;
        }

        LOG.debug("Creating  " + result + " for pipeline " + this);
        return result;
    }

    protected CacheValue getCachedValue(final CacheKey cacheKey) {
        if (cacheKey == null) {
            return null;
        }

        if (this.cache == null) {
            LOG.warn("Caching pipeline has no cache configured. Falling back to non-caching behavior.");
            return null;
        }

        final CacheValue cacheValue = this.cache.get(cacheKey, true);
        if (cacheValue == null) {
            LOG.debug("No cache value available for " + cacheKey);
        } else {
            LOG.debug("Retrieved content from cache: " + cacheValue);
        }
        return cacheValue;
    }

    protected boolean isCacheKeyValid(final CacheValue cachedValue) {
        return cachedValue != null && cachedValue.isValid(this.cacheKey);
    }

    protected void setCachedValue(final CacheKey cacheKey, final CacheValue cacheValue) {
        if (cacheKey == null) {
            return;
        }

        if (this.cache == null) {
            LOG.warn("Caching pipeline has no cache configured. Falling back to non-caching behavior.");
            return;
        }

        LOG.debug("Putting result into pipeline cache: " + cacheValue + ")");
        cacheKey.setJmxGroupName(this.jmxGroupName);
        this.cache.put(cacheKey, cacheValue);
    }
}
