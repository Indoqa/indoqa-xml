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

public interface Cache {

    /**
     * Stores the given <code>cacheValue</code> at the given <code>cacheKey</code>.<br>
     * <br>
     * If this cache already contains the given <code>cacheKey</code> it will be replaced.<br>
     * If two {@link CacheKey}s represent the same data is ultimately depending on the implementation, but usually relies on the
     * <code>equals</code> an/or <code>hashcode</code> methods.
     * 
     * @param cacheKey The {@link CacheKey} to be used for storing.
     * @param cacheValue The {@link CacheValue} to be stored.
     */
    void put(CacheKey cacheKey, CacheValue cacheValue);

    /**
     * Retrieves the {@link CacheValue} previously stored using the given <code>cacheKey</code>.<br>
     * If the <code>cacheKey</code> is not stored in this {@link Cache} this method will return <code>null</code>.<br>
     * <br>
     * Furthermore the {@link Cache} will check if the stored {@link CacheValue} is still valid (using the
     * {@link CacheValue#isValid(CacheKey)} method). If the {@link CacheValue} is considered to be invalid this method will return
     * <code>null</code> as well (indicating that no valid {@link CacheValue} is available).<br>
     * <br>
     * To retrieve CacheValues even if they are invalid, use the {@link #get(CacheKey, boolean)} method.
     * 
     * @param cacheKey The {@link CacheKey} defining which {@link CacheValue} to retrieve.
     * @return The previously stored {@link CacheValue} or <code>null</code> if no or no valid {@link CacheValue} is stored at the
     *         given <code>cacheValue</code>.
     */
    CacheValue get(CacheKey cacheKey);

    /**
     * Retrieves the {@link CacheValue} previously stored using the given <code>cacheKey</code>.<br>
     * If the <code>cacheKey</code> is not stored in this {@link Cache} this method will return <code>null</code>.<br>
     * <br>
     * This method will omit the check for validity if <code>includeInvalid</code> is <code>true</code> (i.e. the returned CacheValue
     * <b>might</b> be invalid in this case).
     * 
     * @param cacheKey The {@link CacheKey} defining which {@link CacheValue} to retrieve.
     * @param includeInvalid Defines whether invalid {@link CacheValue} should be returned or not. Using <code>true</code> will also
     *            return invalid {@link CacheValue}. Using <code>false</code> will yield the same results as {@link #get(CacheKey)}.
     * @return The previously stored {@link CacheValue} or <code>null</code> if and only if no {@link CacheValue} is stored at the
     *         given <code>cacheValue</code>.
     */
    CacheValue get(CacheKey cacheKey, boolean includeInvalid);

    /**
     * Returns {@link Set} of {@link CacheKey} contained in this Cache
     *
     * @return a set CacheKey contained in this Cache
     */
    Set<CacheKey> keySet();

    /**
     * Removes that CacheKey from this Cache.
     *
     * @param cacheKey
     */
    boolean remove(CacheKey cacheKey);

    /**
     * Removes all data coneined in this Cache
     */
    void clear();
}
