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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.pipeline.util.StringRepresentation;

public class CacheRefreshManagerImpl implements CacheRefreshManager {

    private static final Logger LOG = LoggerFactory.getLogger(CacheRefreshManagerImpl.class);
    private static final int THREAD_POOL_SIZE = 50;

    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private final List<CacheKey> pendingCacheKeys = Collections.synchronizedList(new LinkedList<CacheKey>());

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheRefreshManager#refreshCacheValue(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey,
     *      com.indoqa.xml.pipeline.sax.pipeline.caching.CacheRefreshJob)
     */
    @Override
    public void refreshCacheValue(final CacheKey cacheKey, final CacheRefreshJob cacheRefreshJob) {
        if (this.pendingCacheKeys.contains(cacheKey)) {
            // the refresh of this cache key is already scheduled
            LOG.debug("Refreshing of this cache key is already scheduled: {}", cacheKey);

            return;
        }

        this.pendingCacheKeys.add(cacheKey);
        this.executorService.execute(new RefreshWorker(cacheKey, cacheRefreshJob));
    }

    @Override
    public String toString() {
        return StringRepresentation
            .buildString(this, "executerService=" + this.executorService, "THREAD_POOL_SIZE=" + THREAD_POOL_SIZE);
    }

    protected void executeCacheRefreshJob(final CacheRefreshJob cacheRefreshJob, final CacheKey cacheKey) {
        LOG.debug("Execute cache refresh job for {}", cacheRefreshJob);

        cacheRefreshJob.refresh(cacheKey);
        this.pendingCacheKeys.remove(cacheKey);
    }

    private class RefreshWorker implements Runnable {

        private final CacheKey cacheKey;

        private final CacheRefreshJob cacheRefreshJob;

        public RefreshWorker(final CacheKey cacheKey, final CacheRefreshJob cacheRefreshJob) {
            this.cacheKey = cacheKey;
            this.cacheRefreshJob = cacheRefreshJob;
        }

        @Override
        public void run() {
            CacheRefreshManagerImpl.this.executeCacheRefreshJob(this.cacheRefreshJob, this.cacheKey);
        }
    }
}
