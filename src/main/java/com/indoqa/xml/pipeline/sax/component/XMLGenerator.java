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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.indoqa.xml.dom.DOMStreamer;
import com.indoqa.xml.pipeline.PipelineException;
import com.indoqa.xml.pipeline.ProcessingException;
import com.indoqa.xml.pipeline.SetupException;
import com.indoqa.xml.pipeline.caching.CacheKey;
import com.indoqa.xml.pipeline.caching.TimestampURLCacheKey;
import com.indoqa.xml.pipeline.component.CachingPipelineComponent;
import com.indoqa.xml.pipeline.component.Starter;
import com.indoqa.xml.pipeline.sax.AbstractSAXGenerator;
import com.indoqa.xml.pipeline.sax.AbstractSAXProducer;
import com.indoqa.xml.pipeline.sax.XMLUtils;
import com.indoqa.xml.pipeline.util.StringRepresentation;
import com.indoqa.xml.pipeline.util.URLConnectionUtils;
import com.indoqa.xml.sax.SAXBuffer;

/**
 * General purpose SAX generator that produces SAX events from different sources.
 */
public class XMLGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

    private static final Logger LOG = LoggerFactory.getLogger(XMLGenerator.class);

    private transient Starter generator;

    public XMLGenerator() {
        this((URL) null);
    }

    public XMLGenerator(final byte[] bytes) {
        super();
        this.generator = new ByteArrayGenerator(bytes == null ? null : bytes.clone());
    }

    public XMLGenerator(final byte[] bytes, final String encoding) {
        super();
        this.generator = new ByteArrayGenerator(bytes == null ? null : bytes.clone(), encoding);
    }

    public XMLGenerator(final File file) {
        super();
        this.generator = new FileGenerator(file);
    }

    public XMLGenerator(final InputStream inputStream) {
        super();
        this.generator = new InputStreamGenerator(inputStream);
    }

    public XMLGenerator(final Node node) {
        super();
        this.generator = new NodeGenerator(node);
    }

    public XMLGenerator(final SAXBuffer saxBuffer) {
        super();
        this.generator = new SAXBufferGenerator(saxBuffer);
    }

    public XMLGenerator(final String xmlString) {
        super();
        this.generator = new StringGenerator(xmlString);
    }

    public XMLGenerator(final URL url) {
        super();
        this.generator = new URLGenerator(url);
    }

    @Override
    public CacheKey constructCacheKey() {
        return this.generator instanceof CachingPipelineComponent ? ((CachingPipelineComponent) this.generator).constructCacheKey()
            : null;
    }

    @Override
    public void execute() {
        this.generator.execute();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.sax.AbstractSAXProducer#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        ((URLGenerator) this.generator).setSource((URL) configuration.get("source"));
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "internalGenerator=" + this.generator);
    }

    private class ByteArrayGenerator extends AbstractSAXGenerator {

        private final transient byte[] bytes;

        private final transient String encoding;

        public ByteArrayGenerator(final byte[] bytes) {
            this(bytes, null);
        }

        public ByteArrayGenerator(final byte[] bytes, final String encoding) {
            super();
            if (bytes == null) {
                throw new SetupException("A byte array has to be passed.");
            }

            this.bytes = bytes.clone();
            this.encoding = encoding;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using a byte array as source to produce SAX events.");

                if (this.encoding == null) {
                    XMLUtils.toSax(new ByteArrayInputStream(this.bytes), XMLGenerator.this.getSAXConsumer());
                } else {
                    XMLUtils.toSax(new String(this.bytes, this.encoding), XMLGenerator.this.getSAXConsumer());
                }
            } catch (PipelineException e) {
                LOG.error("Pipeline expcetion thrown", e);
                throw e;
            } catch (UnsupportedEncodingException e) {
                throw new ProcessingException("The encoding " + this.encoding + " is not supported.", e);
            } catch (Exception e) {
                throw new ProcessingException("Can't parse byte array " + Arrays.toString(this.bytes), e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "bytes=" + Arrays.toString(this.bytes), "encoding=" + this.encoding);
        }
    }

    private class FileGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

        private final transient File file;

        public FileGenerator(final File file) {
            super();
            if (file == null) {
                throw new SetupException("A file has to be passed.");
            }

            this.file = file;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.indoqa.xml.pipeline.sax.pipeline.component.CachingPipelineComponent #constructCacheKey()
         */
        @Override
        public CacheKey constructCacheKey() {
            if (this.file == null) {
                throw new SetupException(this.getClass().getSimpleName() + " has no file.");
            }

            CacheKey cacheKey = null;
            try {
                cacheKey = new TimestampURLCacheKey(this.file.toURI().toURL(), this.file.lastModified());
            } catch (MalformedURLException e) {
                LOG.error("Can't construct cache key. " + "Error while converting to " + this.file + " to URL", e);
            }

            return cacheKey;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using file {} as source to produce SAX events.", this.file.getAbsolutePath());

                XMLUtils.toSax(new FileInputStream(this.file), XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                LOG.error("Pipeline expcetion thrown", e);
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't read or parse file " + this.file.getAbsolutePath(), e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "file=" + this.file);
        }
    }

    private class InputStreamGenerator extends AbstractSAXGenerator {

        private final transient InputStream inputStream;

        public InputStreamGenerator(final InputStream inputStream) {
            super();
            if (inputStream == null) {
                throw new SetupException("An input stream has to be passed.");
            }

            this.inputStream = inputStream;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using input stream {} as source to produce SAX events.", this.inputStream);

                XMLUtils.toSax(this.inputStream, XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                LOG.error("Pipeline expcetion thrown", e);
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't read or parse file " + this.inputStream, e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "inputStream=" + this.inputStream);
        }
    }

    private class NodeGenerator extends AbstractSAXGenerator {

        private final transient Node node;

        public NodeGenerator(final Node document) {
            super();
            if (document == null) {
                throw new SetupException("A DOM document has to be passed.");
            }

            this.node = document;
        }

        @Override
        public void execute() {
            LOG.debug("Using a DOM node to produce SAX events.");

            final DOMStreamer streamer = new DOMStreamer(XMLGenerator.this.getSAXConsumer());
            try {
                streamer.stream(this.node);
            } catch (SAXException e) {
                throw new SetupException("Can't stream DOM node " + this.node, e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "node=" + this.node);
        }
    }

    private class SAXBufferGenerator extends AbstractSAXGenerator {

        private final transient SAXBuffer saxBuffer;

        public SAXBufferGenerator(final SAXBuffer saxBuffer) {
            super();
            if (saxBuffer == null) {
                throw new SetupException("A SAXBuffer has to be passed.");
            }

            this.saxBuffer = saxBuffer;
        }

        @Override
        public void execute() {
            LOG.debug("Using a SAXBuffer to produce SAX events.");

            try {
                this.saxBuffer.toSAX(XMLGenerator.this.getSAXConsumer());
            } catch (SAXException e) {
                throw new ProcessingException("Can't stream " + this + " into the content handler.", e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "saxBuffer=" + this.saxBuffer);
        }
    }

    private class StringGenerator extends AbstractSAXProducer implements Starter {

        private final transient String xmlString;

        public StringGenerator(final String xmlString) {
            super();
            if (xmlString == null) {
                throw new SetupException("An XML string has to be passed.");
            }

            this.xmlString = xmlString;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.indoqa.xml.pipeline.sax.pipeline.component.Starter#execute()
         */
        @Override
        public void execute() {
            try {
                LOG.debug("Using a string to produce SAX events.");

                XMLUtils.toSax(this.xmlString, XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                LOG.error("Pipeline exception thrown", e);
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't parse the XML string.", e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "xmlString=" + this.xmlString);
        }
    }

    private class URLGenerator extends AbstractSAXGenerator implements CachingPipelineComponent {

        private transient URL source;

        public URLGenerator(final URL source) {
            super();
            this.source = source;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.indoqa.xml.pipeline.sax.pipeline.component.CachingPipelineComponent #constructCacheKey()
         */
        @Override
        public CacheKey constructCacheKey() {
            return new TimestampURLCacheKey(this.source, URLConnectionUtils.getLastModified(this.source));
        }

        /**
         * {@inheritDoc}
         *
         * @see com.indoqa.xml.pipeline.sax.pipeline.component.Starter#execute()
         */
        @Override
        public void execute() {
            if (this.source == null) {
                throw new ProcessingException(this.getClass().getSimpleName() + " has no source.");
            }

            LOG.debug("Using the URL {} to produce SAX events.", this.source.toExternalForm());

            try {
                XMLUtils.toSax(this.source.openConnection(), XMLGenerator.this.getSAXConsumer());
            } catch (IOException e) {
                throw new ProcessingException("Can't open connection to " + this.source.toExternalForm(), e);
            }
        }

        public void setSource(final URL source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "source=" + this.source);
        }
    }
}
