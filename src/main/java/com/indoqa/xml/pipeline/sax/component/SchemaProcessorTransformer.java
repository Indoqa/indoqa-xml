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
package com.indoqa.xml.pipeline.sax.component;

import java.net.URL;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.indoqa.xml.pipeline.SetupException;
import com.indoqa.xml.pipeline.caching.CacheKey;
import com.indoqa.xml.pipeline.caching.TimestampSourceCacheKey;
import com.indoqa.xml.pipeline.caching.TimestampURLCacheKey;
import com.indoqa.xml.pipeline.component.CachingPipelineComponent;
import com.indoqa.xml.pipeline.sax.*;
import com.indoqa.xml.pipeline.util.StringRepresentation;
import com.indoqa.xml.pipeline.util.URLConnectionUtils;

public final class SchemaProcessorTransformer extends AbstractSAXTransformer implements CachingPipelineComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaProcessorTransformer.class);
    private static final String SOURCE = "source";
    private static final InMemoryLRUResourceCache<Schema> SCHEMA_CACHE = new InMemoryLRUResourceCache<Schema>();
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private Schema schema;
    private URL url;
    private long lastModified;
    private Source source;

    public SchemaProcessorTransformer() {
        super();
    }

    public SchemaProcessorTransformer(final Source source, final long lastModified) {
        super();
        this.load(source, lastModified);
    }

    public SchemaProcessorTransformer(final URL url) {
        super();
        this.load(url);
    }

    @Override
    public CacheKey constructCacheKey() {
        if (this.url == null && this.source == null) {
            throw new SetupException(this.getClass().getSimpleName() + " has no source.");
        }

        return this.url == null ? new TimestampSourceCacheKey(this.source, this.lastModified)
            : new TimestampURLCacheKey(this.url, this.lastModified);
    }

    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        try {
            this.load((URL) configuration.get(SOURCE));
        } catch (ClassCastException cce) {
            throw new SetupException("The configuration value of '" + SOURCE + "' can't be cast to " + URL.class.getName(), cce);
        }
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(
            this,
            "src=" + (this.url == null ? "<" + this.source + "," + this.lastModified + ">" : this.url.toExternalForm()));
    }

    @Override
    protected void setSAXConsumer(final SAXConsumer xmlConsumer) {
        final ValidatorHandler validatorHandler = this.schema.newValidatorHandler();
        validatorHandler
            .setErrorHandler(new SchemaErrorHandler(LOG, this.url == null ? this.source.toString() : this.url.toExternalForm()));
        validatorHandler.setContentHandler(xmlConsumer);

        final SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
        saxConsumerAdapter.setContentHandler(validatorHandler);
        super.setSAXConsumer(saxConsumerAdapter);
    }

    private void load(final Source source, final long lastModified) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot load schema from null Source");
        }

        this.source = source;
        this.lastModified = lastModified;

        this.load(this.source, this.source.toString());
    }

    private void load(final Source source, final String localCacheKey) {
        if (SCHEMA_CACHE.containsKey(localCacheKey)) {
            final ValidityValue<Schema> cacheEntry = SCHEMA_CACHE.get(localCacheKey);
            if (cacheEntry.getLastModified() >= this.lastModified) {
                LOG.debug("{} local cache hit: {}", this.getClass().getSimpleName(), localCacheKey);

                this.schema = cacheEntry.getValue();
            }
        }
        if (this.schema == null) {
            LOG.debug("{} local cache miss: {}", this.getClass().getSimpleName(), localCacheKey);

            try {
                this.schema = SCHEMA_FACTORY.newSchema(source);

                LOG.debug("{} local cache put: {}", this.getClass().getSimpleName(), localCacheKey);

                final ValidityValue<Schema> cacheEntry = new ValidityValue<Schema>(this.schema, this.lastModified);
                SCHEMA_CACHE.put(localCacheKey, cacheEntry);
            } catch (SAXException e) {
                throw new SetupException("Impossible to read Schema from '" + source + "', see nested exception", e);
            }
        }
    }

    private void load(final URL url) {
        if (url == null) {
            throw new IllegalArgumentException("Cannot load schema from null URL");
        }

        this.url = url;
        this.lastModified = URLConnectionUtils.getLastModified(this.url);

        this.load(new StreamSource(this.url.toExternalForm()), this.url.toExternalForm());
    }
}
