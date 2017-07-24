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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.indoqa.xml.pipeline.sax.XMLUtils;

/**
 * Converts XML into plain text. It omits all XML tags and writes only character events to the output. Input document must have at
 * least one element - root element - which should wrap all the text inside it.
 *
 */
public class TextSerializer extends XMLSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(TextSerializer.class);

    /**
     * Set to true after first XML element.
     */
    private boolean hasRootElement;

    private boolean hadNoRootElement;

    private static final String UTF_8 = "UTF-8";

    private static final String TXT = "text";

    public TextSerializer() {
        super();
        super.setOmitXmlDeclaration(true);
        this.hasRootElement = false;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        // nothing
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        // nothing
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        // nothing
    }

    @Override
    public void endDTD() throws SAXException {
        // nothing
    }

    @Override
    public void startElement(final String uri, final String loc, final String raw, final Attributes atts) throws SAXException {

        this.hasRootElement = true;
    }

    @Override
    public void endElement(final String uri, final String name, final String raw) throws SAXException {
        // nothing
    }

    @Override
    public void endDocument() throws SAXException {
        if (this.hadNoRootElement) {
            super.endElement("", TXT, TXT);
        }
        super.endDocument();
    }

    /**
     * @throws SAXException if text is encountered before root element.
     */
    @Override
    public void characters(final char buffer[], final int start, final int len) throws SAXException {
        if (!this.hasRootElement) {
            this.hasRootElement = this.hadNoRootElement = true;
            LOG.warn("Encountered text before root element. Creating <{}> wrapper element.", TXT);
            super.startElement("", TXT, TXT, XMLUtils.EMPTY_ATTRIBUTES);
        }
        super.characters(buffer, start, len);
    }

    @Override
    public void recycle() {
        this.hasRootElement = false;
        super.recycle();
    }

    public static TextSerializer createPlainSerializer() {
        final TextSerializer serializer = new TextSerializer();
        serializer.setContentType("text/plain; charset=" + UTF_8);
        serializer.setEncoding(UTF_8);
        serializer.setMethod(TXT);
        return serializer;
    }
}
