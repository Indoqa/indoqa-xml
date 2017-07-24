/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.xml.pipeline.caching;

import java.util.*;
import java.util.Map.Entry;

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;
import com.indoqa.xml.pipeline.util.StringRepresentation;

/**
 * A {@link CacheKey} that contains a {@link Map} of parameters.
 */
public class ParameterCacheKey extends AbstractCacheKey {

    /**
     * Set of parameter names that must not be considered for caching when in Sitemap context.
     */
    private static final Set<String> SITEMAP_PARAM_NON_CACHABLE_NAMES = new HashSet<String>(
        Arrays.asList(
            new String[] {"cocoon", "com.indoqa.xml.pipeline.sax.configuration.Settings", "javax.servlet.ServletContext",
                "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse"}));

    private static final long serialVersionUID = -6069623439023188073L;

    private final Map<String, String> parameters;

    public ParameterCacheKey() {
        this(new HashMap<String, String>());
    }

    public ParameterCacheKey(final Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("A map of parameters has to be passed.");
        }

        this.parameters = parameters;
    }

    public ParameterCacheKey(final String name, Map<?, ?> value) {
        this();
        this.addParameter(name, value);
    }

    public ParameterCacheKey(final String name, String value) {
        this();
        this.addParameter(name, value);
    }

    /**
     * Build an instance only when parameter names are safe in Sitemap context.
     *
     * @param parameters to be considered for caching
     * @return a Sitemap-safe instance
     */
    public static ParameterCacheKey getSitemapSafeInstance(final Map<String, Object> parameters) {
        final Map<String, String> safeParams = new HashMap<String, String>();
        if (parameters != null) {
            for (Entry<String, Object> entry : parameters.entrySet()) {
                if (!SITEMAP_PARAM_NON_CACHABLE_NAMES.contains(entry.getKey())) {
                    safeParams.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return new ParameterCacheKey(Collections.unmodifiableMap(safeParams));
    }

    public void addParameter(final String name, final boolean value) {
        this.parameters.put(name, Boolean.toString(value));
    }

    public void addParameter(final String name, final int value) {
        this.parameters.put(name, Integer.toString(value));
    }

    public final void addParameter(final String name, final Map<?, ?> value) {
        for (Entry<?, ?> object : value.entrySet()) {
            this.parameters.put(name + "_" + object.getKey().toString(), object.getValue().toString());
        }
    }

    public final void addParameter(final String name, final String value) {
        this.parameters.put(name, value);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ParameterCacheKey)) {
            return false;
        }

        final ParameterCacheKey other = (ParameterCacheKey) obj;
        return this.parameters != null && this.parameters.equals(other.parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#getLastModified()
     */
    @Override
    public long getLastModified() {
        return -1;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final MurmurHashCodeBuilder hashCodeBuilder = new MurmurHashCodeBuilder();
        for (Entry<String, String> parameterEntry : this.parameters.entrySet()) {
            hashCodeBuilder.append(parameterEntry.getKey()).append(parameterEntry.getValue());
        }
        return hashCodeBuilder.toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey#isValid(com.indoqa.xml.pipeline.sax.pipeline.caching.CacheKey)
     */
    @Override
    public boolean isValid(final CacheKey cacheKey) {
        return this.equals(cacheKey);
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "parameters=" + this.parameters);
    }
}
