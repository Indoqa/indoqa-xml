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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A class that can record SAX events and replay them later.
 *
 * <p>
 * Compared to the old Cocoon.XMLByteStreamCompiler, this class is many times faster at sending out the recorded SAX events since it
 * doesn't need to convert between byte and char representations etc. On the other hand, its data structure is more complex then a
 * simple byte array, making XMLByteStreamCompiler better in case the recorded SAX should be stored long-term.
 *
 * <p>
 * Use this class if you need to frequently generate smaller amounts of SAX events, or replay a set of recorded start events
 * immediately.
 * </p>
 *
 * <p>
 * Both {@link ContentHandler} and {@link LexicalHandler} are supported, the only exception is that the setDocumentLocator event is not
 * recorded.
 * </p>
 *
 * @version $Id: SAXBuffer.java 729287 2008-12-24 09:44:56Z cziegeler $
 */
public class SAXBuffer implements ContentHandler, LexicalHandler, Serializable {

    private static final long serialVersionUID = 1013417760084931174L;

    /**
     * Stores list of {@link SaxBit} objects.
     */
    protected List<SaxBit> saxbits;

    /**
     * Creates empty SaxBuffer
     */
    public SAXBuffer() {
        this.saxbits = new ArrayList<SaxBit>();
    }

    /**
     * Creates SaxBuffer based on the provided bits list.
     */
    public SAXBuffer(List<SaxBit> bits) {
        this.saxbits = bits;
    }

    /**
     * Creates copy of another SaxBuffer
     */
    public SAXBuffer(SAXBuffer saxBuffer) {
        this.saxbits = new ArrayList<SaxBit>(saxBuffer.saxbits);
    }

    //
    // ContentHandler Interface
    //

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.saxbits.add(new Characters(ch, start, length));
    }

    @Override
    public void comment(char ch[], int start, int length) throws SAXException {
        this.saxbits.add(new Comment(ch, start, length));
    }

    /**
     * Dump buffer contents into the provided writer.
     */
    public void dump(Writer writer) throws IOException {
        Iterator<SaxBit> i = this.saxbits.iterator();
        while (i.hasNext()) {
            final SaxBit saxbit = i.next();
            saxbit.dump(writer);
        }
        writer.flush();
    }

    @Override
    public void endCDATA() throws SAXException {
        this.saxbits.add(EndCDATA.SINGLETON);
    }

    @Override
    public void endDocument() throws SAXException {
        this.saxbits.add(EndDocument.SINGLETON);
    }

    @Override
    public void endDTD() throws SAXException {
        this.saxbits.add(EndDTD.SINGLETON);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        this.saxbits.add(new EndElement(namespaceURI, localName, qName));
    }

    @Override
    public void endEntity(String name) throws SAXException {
        this.saxbits.add(new EndEntity(name));
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.saxbits.add(new EndPrefixMapping(prefix));
    }

    /**
     * @return unmodifiable list of SAX bits
     */
    public List<SaxBit> getBits() {
        return Collections.unmodifiableList(this.saxbits);
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        this.saxbits.add(new IgnorableWhitespace(ch, start, length));
    }

    //
    // LexicalHandler Interface
    //

    /**
     * @return true if buffer is empty
     */
    public boolean isEmpty() {
        return this.saxbits.isEmpty();
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        this.saxbits.add(new PI(target, data));
    }

    /**
     * Clear this buffer
     */
    public void recycle() {
        this.saxbits.clear();
    }

    /**
     * Add a another buffer
     */
    public void saxBuffer(SAXBuffer xml) {
        this.saxbits.add(new XMLizableBit(xml));
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // Don't record this event
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        this.saxbits.add(new SkippedEntity(name));
    }

    @Override
    public void startCDATA() throws SAXException {
        this.saxbits.add(StartCDATA.SINGLETON);
    }

    //
    // Public Methods
    //

    @Override
    public void startDocument() throws SAXException {
        this.saxbits.add(StartDocument.SINGLETON);
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.saxbits.add(new StartDTD(name, publicId, systemId));
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        this.saxbits.add(new StartElement(namespaceURI, localName, qName, atts));
    }

    @Override
    public void startEntity(String name) throws SAXException {
        this.saxbits.add(new StartEntity(name));
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.saxbits.add(new StartPrefixMapping(prefix, uri));
    }

    /**
     * Stream this buffer into the provided content handler. If contentHandler object implements LexicalHandler, it will get lexical
     * events as well.
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        for (Iterator<SaxBit> i = this.saxbits.iterator(); i.hasNext();) {
            SaxBit saxbit = i.next();
            saxbit.send(contentHandler);
        }
    }

    /**
     * @return String value of the buffer
     */
    @Override
    public String toString() {
        // NOTE: This method is used in i18n XML bundle implementation
        final StringBuffer value = new StringBuffer();
        for (Iterator<SaxBit> i = this.saxbits.iterator(); i.hasNext();) {
            final SaxBit saxbit = i.next();
            if (saxbit instanceof Characters) {
                ((Characters) saxbit).toString(value);
            }
        }

        return value.toString();
    }

    //
    // Implementation Methods
    //

    /**
     * Adds a SaxBit to the bits list
     */
    protected final void addBit(SaxBit bit) {
        this.saxbits.add(bit);
    }

    /**
     * Iterates through the bits list
     */
    protected final Iterator<SaxBit> bits() {
        return this.saxbits.iterator();
    }

    public final static class Characters implements SaxBit, Serializable {

        private static final long serialVersionUID = -8209568716098389350L;

        public final char[] ch;

        public Characters(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[Characters] ch=" + new String(this.ch) + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.characters(this.ch, 0, this.ch.length);
        }

        public void toString(StringBuffer value) {
            value.append(this.ch);
        }
    }

    public final static class Comment implements SaxBit, Serializable {

        private static final long serialVersionUID = 1011723162866067820L;

        public final char[] ch;

        public Comment(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[Comment] ch=" + new String(this.ch) + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).comment(this.ch, 0, this.ch.length);
            }
        }
    }

    public final static class EndCDATA implements SaxBit, Serializable {

        private static final long serialVersionUID = -5599997301539044495L;

        public static final EndCDATA SINGLETON = new EndCDATA();

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[EndCDATA]\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).endCDATA();
            }
        }
    }

    public final static class EndDocument implements SaxBit, Serializable {

        private static final long serialVersionUID = -6281199975708504888L;

        public static final EndDocument SINGLETON = new EndDocument();

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[EndDocument]\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endDocument();
        }
    }

    public final static class EndDTD implements SaxBit, Serializable {

        private static final long serialVersionUID = -9136136230534624303L;

        public static final EndDTD SINGLETON = new EndDTD();

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[EndDTD]\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).endDTD();
            }
        }
    }

    public final static class EndElement implements SaxBit, Serializable {

        private static final long serialVersionUID = 5141883962500368223L;

        public final String namespaceURI;
        public final String localName;
        public final String qName;

        public EndElement(String namespaceURI, String localName, String qName) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write(
                "[EndElement] namespaceURI=" + this.namespaceURI + ",localName=" + this.localName + ",qName=" + this.qName + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endElement(this.namespaceURI, this.localName, this.qName);
        }
    }

    public final static class EndEntity implements SaxBit, Serializable {

        private static final long serialVersionUID = -5856073187752596195L;

        public final String name;

        public EndEntity(String name) {
            this.name = name;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[EndEntity] name=" + this.name + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).endEntity(this.name);
            }
        }
    }

    public final static class EndPrefixMapping implements SaxBit, Serializable {

        private static final long serialVersionUID = -7549231143795759493L;

        public final String prefix;

        public EndPrefixMapping(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[EndPrefixMapping] prefix=" + this.prefix + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endPrefixMapping(this.prefix);
        }
    }

    public final static class IgnorableWhitespace implements SaxBit, Serializable {

        private static final long serialVersionUID = -3117218148889658251L;

        public final char[] ch;

        public IgnorableWhitespace(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[IgnorableWhitespace] ch=" + new String(this.ch) + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.ignorableWhitespace(this.ch, 0, this.ch.length);
        }
    }

    public final static class PI implements SaxBit, Serializable {

        private static final long serialVersionUID = 2583267483272517301L;

        public final String target;
        public final String data;

        public PI(String target, String data) {
            this.target = target;
            this.data = data;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[ProcessingInstruction] target=" + this.target + ",data=" + this.data + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.processingInstruction(this.target, this.data);
        }
    }

    public final static class SkippedEntity implements SaxBit, Serializable {

        private static final long serialVersionUID = 3423551451624652324L;

        public final String name;

        public SkippedEntity(String name) {
            this.name = name;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[SkippedEntity] name=" + this.name + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.skippedEntity(this.name);
        }
    }

    public final static class StartCDATA implements SaxBit, Serializable {

        private static final long serialVersionUID = -2377064578601561555L;

        public static final StartCDATA SINGLETON = new StartCDATA();

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[StartCDATA]\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).startCDATA();
            }
        }
    }

    public final static class StartDocument implements SaxBit, Serializable {

        private static final long serialVersionUID = 4821494200186426953L;

        public static final StartDocument SINGLETON = new StartDocument();

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[StartDocument]\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startDocument();
        }
    }

    public final static class StartDTD implements SaxBit, Serializable {

        private static final long serialVersionUID = 1624503082673373607L;

        public final String name;
        public final String publicId;
        public final String systemId;

        public StartDTD(String name, String publicId, String systemId) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[StartDTD] name=" + this.name + ",publicId=" + this.publicId + ",systemId=" + this.systemId + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).startDTD(this.name, this.publicId, this.systemId);
            }
        }
    }

    public final static class StartElement implements SaxBit, Serializable {

        private static final long serialVersionUID = -1602185577460819949L;

        public final String namespaceURI;
        public final String localName;
        public final String qName;
        public final Attributes attrs;

        public StartElement(String namespaceURI, String localName, String qName, Attributes attrs) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
            this.attrs = new org.xml.sax.helpers.AttributesImpl(attrs);
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write(
                "[StartElement] namespaceURI=" + this.namespaceURI + ",localName=" + this.localName + ",qName=" + this.qName + "\n");
            for (int i = 0; i < this.attrs.getLength(); i++) {
                writer.write(
                    "      [Attribute] namespaceURI=" + this.attrs.getURI(i) + ",localName=" + this.attrs.getLocalName(i) + ",qName="
                        + this.attrs.getQName(i) + ",type=" + this.attrs.getType(i) + ",value=" + this.attrs.getValue(i) + "\n");
            }
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startElement(this.namespaceURI, this.localName, this.qName, this.attrs);
        }
    }

    public final static class StartEntity implements SaxBit, Serializable {

        private static final long serialVersionUID = 7611841806284789483L;

        public final String name;

        public StartEntity(String name) {
            this.name = name;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[StartEntity] name=" + this.name + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler) {
                ((LexicalHandler) contentHandler).startEntity(this.name);
            }
        }
    }

    public final static class StartPrefixMapping implements SaxBit, Serializable {

        private static final long serialVersionUID = 1789832542521853360L;

        public final String prefix;
        public final String uri;

        public StartPrefixMapping(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[StartPrefixMapping] prefix=" + this.prefix + ",uri=" + this.uri + "\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startPrefixMapping(this.prefix, this.uri);
        }
    }

    public final static class XMLizableBit implements SaxBit, Serializable {

        private static final long serialVersionUID = -8277975298611926064L;

        public final SAXBuffer xml;

        public XMLizableBit(SAXBuffer xml) {
            this.xml = xml;
        }

        @Override
        public void dump(Writer writer) throws IOException {
            writer.write("[XMLizable] Begin nested SaxBuffer\n");
            this.xml.dump(writer);
            writer.write("[XMLizable] End nested SaxBuffer\n");
        }

        @Override
        public void send(ContentHandler contentHandler) throws SAXException {
            this.xml.toSAX(new EmbeddedSAXPipe(contentHandler));
        }
    }

    /**
     * SaxBit is a representation of the SAX event. Every SaxBit is immutable object.
     */
    interface SaxBit {

        public void dump(Writer writer) throws IOException;

        public void send(ContentHandler contentHandler) throws SAXException;
    }
}
