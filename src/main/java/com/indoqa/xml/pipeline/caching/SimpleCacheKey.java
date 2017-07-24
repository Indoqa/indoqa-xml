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

public class SimpleCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 6668460290536876103L;

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SimpleCacheKey;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.getClass().getName()).toHashCode();
    }

    @Override
    public boolean isValid(final CacheKey cacheKey) {
        return true;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this);
    }
}
