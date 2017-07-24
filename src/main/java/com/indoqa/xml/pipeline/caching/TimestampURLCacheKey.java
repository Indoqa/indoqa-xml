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

import static java.util.Locale.US;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;
import com.indoqa.xml.pipeline.util.StringRepresentation;

public class TimestampURLCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = -2790160879189162411L;

    private long timestamp;

    private final URL url;

    public TimestampURLCacheKey(final URL url, final long timestamp) {
        super();

        this.url = url;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TimestampURLCacheKey)) {
            return false;
        }

        final TimestampURLCacheKey other = (TimestampURLCacheKey) obj;
        return this.url.toExternalForm().equals(other.url.toExternalForm());
    }

    @Override
    public long getLastModified() {
        return this.getTimestamp();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public URL getUrl() {
        return this.url;
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.url.toExternalForm()).append(this.timestamp).toHashCode();
    }

    @Override
    public boolean isValid(final CacheKey cacheKey) {
        if (!(cacheKey instanceof TimestampURLCacheKey) || !this.equals(cacheKey)) {
            return false;
        }

        final TimestampURLCacheKey other = (TimestampURLCacheKey) cacheKey;
        return this.timestamp == other.timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", US);
        return StringRepresentation.buildString(
            this,
            "url=" + this.url.toExternalForm(),
            "timestamp=" + this.timestamp + " (" + dateFormat.format(new Date(this.timestamp)) + ")");
    }
}
