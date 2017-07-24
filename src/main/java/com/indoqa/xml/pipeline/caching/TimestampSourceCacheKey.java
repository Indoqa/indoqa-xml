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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.Source;

import com.indoqa.xml.murmurhash.MurmurHashCodeBuilder;
import com.indoqa.xml.pipeline.util.StringRepresentation;

public class TimestampSourceCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 79587530920810913L;

    private long timestamp;

    private final Source source;

    public TimestampSourceCacheKey(final Source source, final long timestamp) {
        super();

        this.source = source;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TimestampSourceCacheKey)) {
            return false;
        }

        final TimestampSourceCacheKey other = (TimestampSourceCacheKey) obj;
        return this.source.equals(other.source);
    }

    @Override
    public long getLastModified() {
        return this.getTimestamp();
    }

    public Source getSource() {
        return this.source;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.source.toString()).append(this.timestamp).toHashCode();
    }

    @Override
    public boolean isValid(final CacheKey cacheKey) {
        if (!(cacheKey instanceof TimestampSourceCacheKey) || !this.equals(cacheKey)) {
            return false;
        }

        final TimestampSourceCacheKey other = (TimestampSourceCacheKey) cacheKey;
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
            "source=" + this.source.toString(),
            "timestamp=" + this.timestamp + " (" + dateFormat.format(new Date(this.timestamp)) + ")");
    }
}
