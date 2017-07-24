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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.indoqa.xml.pipeline.caching.CacheKey;
import com.indoqa.xml.pipeline.caching.SimpleCacheKey;
import com.indoqa.xml.pipeline.component.CachingPipelineComponent;
import com.indoqa.xml.pipeline.sax.AbstractSAXTransformer;

public class CleaningTransformer extends AbstractSAXTransformer implements CachingPipelineComponent {

    private static final String EMPTY_NS = "";

    private char[] characters = new char[8192];
    private int position;

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        // no prefix
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // no prefix
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        // no comment
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        this.maybeWriteCharacters();

        // remove namespace from attributes
        AttributesImpl noNamespaceAtts = new AttributesImpl();
        String attrName;
        String attrValue;
        String attrType;
        for (int i = 0; i < atts.getLength(); i++) {
            attrName = atts.getLocalName(i);
            attrValue = atts.getValue(i);
            attrType = atts.getType(i);
            if (attrValue != null) {
                noNamespaceAtts.addAttribute(EMPTY_NS, attrName, attrName, attrType, attrValue);
            }
        }

        super.startElement(EMPTY_NS, localName, localName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        this.maybeWriteCharacters();

        super.endElement(EMPTY_NS, localName, localName);
    }

    @Override
    public void characters(char[] c, int start, int len) throws SAXException {
        int remaining = this.characters.length - this.position;
        if (len > remaining) {
            // not enough capacity remaining, allocate a new array and copy the
            // current contents
            int newCapacity = this.characters.length + len + 1000;
            char[] nextCharacters = new char[newCapacity];
            System.arraycopy(this.characters, 0, nextCharacters, 0, this.position);
            this.characters = nextCharacters;
        }

        // simply append at the current position (the check above ensures
        // there's enough space)
        System.arraycopy(c, start, this.characters, this.position, len);
        // update current position
        this.position += len;
    }

    private void maybeWriteCharacters() throws SAXException {
        if (this.position == 0) {
            // no characters pending
            return;
        }

        for (int i = 0; i < this.position; i++) {
            char eachChar = this.characters[i];
            if (!Character.isWhitespace(eachChar) || eachChar == '\u00A0') {
                super.characters(this.characters, 0, this.position);
                break;
            }
        }

        this.position = 0;
    }

    public CacheKey constructCacheKey() {
        return new SimpleCacheKey();
    }
}
