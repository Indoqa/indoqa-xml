/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.Writer;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Modification of the SAX buffer with parameterization capabilities.
 *
 * Any <code>{name}</code> expression inside of the character events can be replaced by the content of another SAXBuffer if it is
 * present in the map passed to the {@link #toSAX(ContentHandler, Map&lt;String, SAXBuffer&gt;)} method.
 */
public class ParamSAXBuffer extends SAXBuffer {

    private static final long serialVersionUID = 5817228109688511920L;

    /**
     * If ch (in characters()) contains an unmatched '{' then we save the chars from '{' onward in previous_ch. Next call to
     * characters() prepends the saved chars to ch before processing (and sets prevChar to null).
     */
    private char[] prevChar = null;

    public ParamSAXBuffer() {
        super();
    }

    public ParamSAXBuffer(final SAXBuffer saxBuffer) {
        super(saxBuffer);
    }

    /**
     * Parses text and extracts <code>{name}</code> parameters for later substitution.
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        if (this.prevChar != null) {
            // prepend char's from previous_ch to ch
            final char[] buf = new char[length + this.prevChar.length];
            System.arraycopy(this.prevChar, 0, buf, 0, this.prevChar.length);
            System.arraycopy(ch, start, buf, this.prevChar.length, length);
            ch = buf;
            start = 0;
            length += this.prevChar.length;
            this.prevChar = null;
        }

        final int end = start + length;
        StringBuilder name;
        int j;
        for (int i = start; i < end; i++) {
            if (ch[i] == '{') {
                // Send any collected characters so far
                if (i > start) {
                    this.addBit(new Characters(ch, start, i - start));
                }

                // Find closing brace, and construct parameter name
                name = new StringBuilder();
                j = i + 1;
                for (; j < end; j++) {
                    if (ch[j] == '}') {
                        break;
                    }
                    name.append(ch[j]);
                }
                if (j == end) {
                    // '{' without a closing '}'
                    // save char's from '{' in previous_ch in case the following
                    // call to characters() provides the '}'
                    this.prevChar = new char[end - i];
                    System.arraycopy(ch, i, this.prevChar, 0, end - i);
                    return;
                }
                this.addBit(new Parameter(name.toString()));

                // Continue processing
                i = j;
                start = j + 1;
                continue;
            }
        }

        // Send any tailing characters
        if (start < end) {
            this.addBit(new Characters(ch, start, end - start));
        }
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        this.flushChars();
        super.comment(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        this.flushChars();
        super.endDocument();
    }

    @Override
    public void endDTD() throws SAXException {
        this.flushChars();
        super.endDTD();
    }

    @Override
    public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
        this.flushChars();
        super.endElement(namespaceURI, localName, qName);
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        this.flushChars();
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        this.flushChars();
        super.processingInstruction(target, data);
    }

    @Override
    public void startDocument() throws SAXException {
        this.flushChars();
        super.startDocument();
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        this.flushChars();
        super.startDTD(name, publicId, systemId);
    }

    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        this.flushChars();
        super.startElement(namespaceURI, localName, qName, atts);
    }

    /**
     * @param parameters map containing SaxBuffers
     */
    public void toSAX(final ContentHandler contentHandler, final Map<String, SAXBuffer> parameters) throws SAXException {
        for (SaxBit saxbit : this.saxbits) {
            if (saxbit instanceof Parameter) {
                ((Parameter) saxbit).send(contentHandler, parameters);
            } else {
                saxbit.send(contentHandler);
            }
        }
    }

    private void flushChars() {
        // Handle saved chars (in case we had a '{' with no matching '}').
        if (this.prevChar != null) {
            this.addBit(new Characters(this.prevChar, 0, this.prevChar.length));
            this.prevChar = null;
        }
    }

    final static class Parameter implements SaxBit {

        private final String name;

        public Parameter(final String name) {
            this.name = name;
        }

        @Override
        public void dump(final Writer writer) throws IOException {

            writer.write("[Parameter] name=" + this.name);
        }

        @Override
        public void send(final ContentHandler contentHandler) {
            // empty
        }

        public void send(final ContentHandler contentHandler, final Map<String, SAXBuffer> parameters) throws SAXException {
            final SAXBuffer value = parameters.get(this.name);
            if (value != null) {
                value.toSAX(contentHandler);
            }
        }

        public void toString(final StringBuilder result, final Map<String, String> parameters) {
            final String value = parameters.get(this.name);
            if (value != null) {
                result.append(value);
            }
        }
    }
}
