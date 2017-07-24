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
package com.indoqa.xml.pipeline.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.indoqa.xml.sax.AttributesImpl;

public class SAXEventsBuilder {

    private static final String EMPTY_NS = "";

    private static final String CDATA_TYPE = "CDATA";

    public static SAXEventsBuilder newDocument(ContentHandler contentHandler) throws SAXException {
        contentHandler.startDocument();
        return wrap(contentHandler);
    }

    public static SAXEventsBuilder wrap(ContentHandler contentHandler) {
        return new SAXEventsBuilder(null, null, null, null, contentHandler);
    }

    public static <T> Attribute attribute(String name, T value) {
        return attribute(EMPTY_NS, name, name, CDATA_TYPE, value);
    }

    public static <T> Attribute attribute(String uri, String localName, String qName, String type, T value) {
        return new Attribute(uri, localName, qName, type, String.valueOf(value));
    }

    private final SAXEventsBuilder previous;

    protected final String uri;

    protected final String localName;

    protected final String qName;

    protected final ContentHandler contentHandler;

    /**
     * Hidden constructor, this class cannot be instantiated earlier;
     */
    private SAXEventsBuilder(SAXEventsBuilder previous, String uri, String localName, String qName, ContentHandler contentHandler) {
        this.previous = previous;
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.contentHandler = contentHandler;
    }

    public SAXEventsBuilder start(String localName, Attribute... attributes) throws SAXException {
        return start(EMPTY_NS, localName, localName, attributes);
    }

    public SAXEventsBuilder start(String uri, String localName, String qName, Attribute... attributes) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        for (Attribute attribute : attributes) {
            atts.addAttribute(
                attribute.uri,
                attribute.getLocalName(),
                attribute.getqName(),
                attribute.getType(),
                attribute.getValue());
        }

        contentHandler.startElement(uri, localName, qName, atts);
        return new SAXEventsBuilder(this, uri, localName, qName, contentHandler);
    }

    public <T> SAXEventsBuilder body(T elementBody) throws SAXException {
        if (elementBody == null) {
            return this;
        }
        String elementStringBody = String.valueOf(elementBody);
        contentHandler.characters(elementStringBody.toCharArray(), 0, elementStringBody.length());
        return this;
    }

    public SAXEventsBuilder end() throws SAXException {
        contentHandler.endElement(uri, localName, qName);
        return previous;
    }

    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    public static final class Attribute {

        private final String uri;

        private final String localName;

        private final String qName;

        private final String type;

        private final String value;

        private Attribute(String uri, String localName, String qName, String type, String value) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.type = type;
            this.value = value;
        }

        public String getUri() {
            return uri;
        }

        public String getLocalName() {
            return localName;
        }

        public String getqName() {
            return qName;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

    }

}
