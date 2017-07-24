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

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;
import com.indoqa.xml.pipeline.util.StringRepresentation;
import com.indoqa.xml.pipeline.util.URLConnectionUtils;

/**
 * A <code>CacheKey</code> holding a list of URLs. Similar to CompoundCacheKey, this class will update URL timestamps before going into
 * getLastModified().
 *
 * @see CompoundCacheKey
 */
public class URLListCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = -5384157447531102615L;
    private static final Logger LOG = LoggerFactory.getLogger(URLListCacheKey.class);

    private final List<TimestampURLCacheKey> cacheKeys = new LinkedList<TimestampURLCacheKey>();

    public void addURL(final URL url) {
        this.cacheKeys.add(new TimestampURLCacheKey(url, URLConnectionUtils.getLastModified(url)));
    }

    @Override
    public boolean equals(final Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Comparing two cache keys: ");
            LOG.debug("  this=" + this);
            LOG.debug("  other=" + obj);
        }

        if (!(obj instanceof URLListCacheKey)) {
            return false;
        }

        final URLListCacheKey other = (URLListCacheKey) obj;
        if (this.cacheKeys.size() != other.cacheKeys.size()) {
            return false;
        }

        final Iterator<TimestampURLCacheKey> myIterator = this.cacheKeys.iterator();
        final Iterator<TimestampURLCacheKey> otherIterator = other.cacheKeys.iterator();

        while (myIterator.hasNext()) {
            final TimestampURLCacheKey myCacheKey = myIterator.next();
            final TimestampURLCacheKey otherCacheKey = otherIterator.next();

            if (myCacheKey == null || !myCacheKey.equals(otherCacheKey)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cache keys are not equal: ");
                    LOG.debug("  myCacheKey=" + myCacheKey);
                    LOG.debug("  otherCacheKey=" + otherCacheKey);
                }
                return false;
            }
        }

        return true;
    }

    @Override
    public long getLastModified() {
        // update last-modificatied for each URL
        for (TimestampURLCacheKey tCacheKey : this.cacheKeys) {
            tCacheKey.setTimestamp(URLConnectionUtils.getLastModified(tCacheKey.getUrl()));
        }

        long lastModified = 0;
        long eachLastModified;
        for (CacheKey eachKey : this.cacheKeys) {
            eachLastModified = eachKey.getLastModified();
            if (eachLastModified == -1) {
                return -1;
            }
            if (eachLastModified > lastModified) {
                lastModified = eachLastModified;
                continue;
            }
        }
        return lastModified;
    }

    @Override
    public int hashCode() {
        final MurmurHashCodeBuilder hashCodeBuild = new MurmurHashCodeBuilder();

        for (CacheKey cacheKey : this.cacheKeys) {
            hashCodeBuild.append(cacheKey.hashCode());
        }

        return hashCodeBuild.toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#isValid(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public boolean isValid(final CacheKey cacheKey) {
        if (!(cacheKey instanceof URLListCacheKey) || !this.equals(cacheKey)) {
            return false;
        }

        final URLListCacheKey other = (URLListCacheKey) cacheKey;
        final Iterator<TimestampURLCacheKey> myIterator = this.cacheKeys.iterator();
        final Iterator<TimestampURLCacheKey> otherIterator = other.cacheKeys.iterator();

        while (myIterator.hasNext()) {
            final TimestampURLCacheKey myCacheKey = myIterator.next();
            final TimestampURLCacheKey otherCacheKey = otherIterator.next();

            if (!myCacheKey.isValid(otherCacheKey)) {
                LOG.debug("Cache key is not valid:\nmyCacheKey={}\notherCacheKey={}", myCacheKey, otherCacheKey);

                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "urls=" + this.cacheKeys);
    }
}
