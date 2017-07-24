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
package com.indoqa.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This abstract class provides default implementation of the methods specified by the <code>ContentHandler</code> and the
 * <code>LexicalHandler</code> interface.
 *
 * @version $Id: AbstractSAXConsumer.java 729283 2008-12-24 09:25:21Z cziegeler $
 */
public abstract class AbstractSAXConsumer implements ContentHandler, LexicalHandler {

    /**
     * Receive notification of character data.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    @Override
    public void characters(char ch[], int start, int len) throws SAXException {
        // empty
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    @Override
    public void comment(char ch[], int start, int len) throws SAXException {
        // empty
    }

    /**
     * Report the end of a CDATA section.
     */
    @Override
    public void endCDATA() throws SAXException {
        // empty
    }

    /**
     * Receive notification of the end of a document.
     */
    @Override
    public void endDocument() throws SAXException {
        // empty
    }

    /**
     * Report the end of DTD declarations.
     */
    @Override
    public void endDTD() throws SAXException {
        // empty
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param loc The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if raw names are not available.
     */
    @Override
    public void endElement(String uri, String loc, String raw) throws SAXException {
        // empty
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    @Override
    public void endEntity(String name) throws SAXException {
        // empty
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // empty
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    @Override
    public void ignorableWhitespace(char ch[], int start, int len) throws SAXException {
        // empty
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was supplied.
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        // empty
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX document event.
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        // empty
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity. If it is a parameter entity, the name will begin with '%'.
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        // empty
    }

    /**
     * Report the start of a CDATA section.
     */
    @Override
    public void startCDATA() throws SAXException {
        // empty
    }

    /**
     * Receive notification of the beginning of a document.
     */
    @Override
    public void startDocument() throws SAXException {
        // empty
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD subset, or null if none was declared.
     */
    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        // empty
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param loc The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if raw names are not available.
     * @param a The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
     */
    @Override
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        // empty
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the name will begin with '%'.
     */
    @Override
    public void startEntity(String name) throws SAXException {
        // empty
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // empty
    }
}
