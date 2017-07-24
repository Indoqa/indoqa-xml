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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.indoqa.xml.pipeline.ProcessingException;
import com.indoqa.xml.sax.SAXBuffer;

public abstract class AbstractSAXTransformer extends AbstractSAXProducer implements SAXConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSAXTransformer.class);
    private List<String[]> namespaces = new ArrayList<String[]>();

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.getSAXConsumer().characters(ch, start, length);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        this.getSAXConsumer().comment(ch, start, length);
    }

    public void endCDATA() throws SAXException {
        this.getSAXConsumer().endCDATA();
    }

    public void endDocument() throws SAXException {
        this.getSAXConsumer().endDocument();
    }

    public void endDTD() throws SAXException {
        this.getSAXConsumer().endDTD();
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        this.getSAXConsumer().endElement(uri, localName, name);
    }

    public void endEntity(String name) throws SAXException {
        this.getSAXConsumer().endEntity(name);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (prefix != null) {
            // Find and remove the namespace prefix
            boolean found = false;
            for (int i = this.namespaces.size() - 1; i >= 0; i--) {
                final String[] prefixAndUri = this.namespaces.get(i);
                if (prefixAndUri[0].equals(prefix)) {
                    this.namespaces.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new SAXException("Namespace for prefix '" + prefix + "' not found.");
            }
        }

        this.getSAXConsumer().endPrefixMapping(prefix);
    }

    /**
     * Find prefix mapping for the given namespace URI.
     * 
     * @return Prefix mapping or null if no prefix defined
     */
    protected String findPrefixMapping(String uri) {
        final int l = this.namespaces.size();
        for (int i = 0; i < l; i++) {
            String[] prefixAndUri = this.namespaces.get(i);
            if (prefixAndUri[1].equals(uri)) {
                return prefixAndUri[0];
            }
        }

        return null;
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.getSAXConsumer().ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.getSAXConsumer().processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        this.getSAXConsumer().setDocumentLocator(locator);
    }

    public void skippedEntity(String name) throws SAXException {
        this.getSAXConsumer().skippedEntity(name);
    }

    public void startCDATA() throws SAXException {
        this.getSAXConsumer().startCDATA();
    }

    public void startDocument() throws SAXException {
        this.getSAXConsumer().startDocument();
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.getSAXConsumer().startDTD(name, publicId, systemId);
    }

    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        this.getSAXConsumer().startElement(uri, localName, name, atts);
    }

    public void startEntity(String name) throws SAXException {
        this.getSAXConsumer().startEntity(name);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (prefix != null) {
            this.namespaces.add(new String[] {prefix, uri});
        }

        this.getSAXConsumer().startPrefixMapping(prefix, uri);
    }

    private SAXConsumer originalSAXConsumer;

    private boolean isRecording;

    /**
     * Set a recorder. Do not invoke this method directly.
     * 
     * @param recorder
     */
    protected void setRecorder(ContentHandler recorder) {
        if (this.isRecording) {
            throw new ProcessingException("Only one recorder can be set.");
        }

        this.isRecording = true;
        this.originalSAXConsumer = this.getSAXConsumer();
        SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
        saxConsumerAdapter.setContentHandler(recorder);
        this.setSAXConsumer(saxConsumerAdapter);
    }

    /**
     * Remove a recorder. Do not invoke this method directly.
     */
    protected ContentHandler removeRecorder() {
        SAXConsumerAdapter saxBufferAdapter = (SAXConsumerAdapter) this.getSAXConsumer();
        this.setSAXConsumer(this.originalSAXConsumer);
        this.isRecording = false;
        return saxBufferAdapter.getContentHandler();
    }

    /**
     * Start recording of SAX events. All incoming events are recorded and not forwarded. The resulting SAXBuffer can be obtained by
     * the matching {@link #endSAXRecording} call.
     */
    public void startSAXRecording() throws SAXException {
        this.setRecorder(new SAXBuffer());
        this.sendStartPrefixMapping();
    }

    /**
     * Stop recording of SAX events. This method returns the resulting XMLizable.
     */
    public SAXBuffer endSAXRecording() throws SAXException {
        this.sendEndPrefixMapping();
        return (SAXBuffer) this.removeRecorder();
    }

    /**
     * Send all start prefix mapping events to the current content handler
     */
    protected void sendStartPrefixMapping() throws SAXException {
        final int l = this.namespaces.size();
        for (int i = 0; i < l; i++) {
            String[] prefixAndUri = this.namespaces.get(i);
            super.getSAXConsumer().startPrefixMapping(prefixAndUri[0], prefixAndUri[1]);
        }
    }

    /**
     * Send all end prefix mapping events to the current content handler
     */
    protected void sendEndPrefixMapping() throws SAXException {
        final int l = this.namespaces.size();
        for (int i = 0; i < l; i++) {
            String[] prefixAndUri = this.namespaces.get(i);
            this.getSAXConsumer().endPrefixMapping(prefixAndUri[0]);
        }
    }

    /**
     * Start recording of a text. No events forwarded, and all characters events are collected into a string.
     */
    public void startTextRecording() throws SAXException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start text recording");
        }
        setRecorder(new TextRecorder());
        sendStartPrefixMapping();
    }

    /**
     * Stop recording of text and return the recorded information.
     * 
     * @return The String, trimmed.
     */
    public String endTextRecording() throws SAXException {
        sendEndPrefixMapping();

        TextRecorder recorder = (TextRecorder) removeRecorder();
        String text = recorder.getText();
        if (LOG.isDebugEnabled()) {
            LOG.debug("End text recording. Text=" + text);
        }
        return text;
    }

    @SuppressWarnings("serial")
    class TextRecorder extends SAXBuffer {

        /**
         * Buffer collecting all character events.
         */
        private StringBuffer buffer;

        public TextRecorder() {
            super();
            this.buffer = new StringBuffer();
        }

        public void characters(char ary[], int start, int length) {
            this.buffer.append(ary, start, length);
        }

        /**
         * @return Recorded text so far, trimmed.
         */
        public String getText() {
            return this.buffer.toString().trim();
        }
    }
}
