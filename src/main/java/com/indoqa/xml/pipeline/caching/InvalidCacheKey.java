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
package com.indoqa.xml.pipeline.caching;

import java.io.Serializable;

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;

public class InvalidCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = -3236728925974662802L;

    private final Serializable expiresCacheKey;

    public InvalidCacheKey(final Serializable expiresCacheKey) {
        if (expiresCacheKey == null) {
            throw new IllegalArgumentException("An explicit cache key has to be provided.");
        }

        this.expiresCacheKey = expiresCacheKey;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof InvalidCacheKey)) {
            return false;
        }

        InvalidCacheKey other = (InvalidCacheKey) obj;
        return this.expiresCacheKey.equals(other.expiresCacheKey);
    }

    @Override
    public long getLastModified() {
        return -1;
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.getClass().getName()).append(this.expiresCacheKey.hashCode()).toHashCode();
    }

    @Override
    public boolean isValid(final CacheKey cacheKey) {
        return false;
    }
}
