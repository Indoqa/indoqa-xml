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
package com.indoqa.xml.dom;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Testcase for DOMStreamer and DOMBuilder.
 */
public class DOMBuilderStreamerTestCase extends XMLTestCase {

    public DOMBuilderStreamerTestCase(String name) {
        super(name);
    }

    public static final void print(Document document) {
        // TransformerFactory factory = TransformerFactory.newInstance();
        // try {
        // javax.xml.transform.Transformer serializer = factory.newTransformer();
        // serializer.transform(new DOMSource(document), new StreamResult(System.out));
        // } catch (TransformerException te) {
        // throw new RuntimeException(te);
        // }
    }

    public void testBuilderWithComments() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.comment("abcd".toCharArray(), 0, 4);
        builder.endElement("", "root", "node");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root><!--abcd--></root>");

        this.assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithCommentWithinDocType() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startDTD("skinconfig", null, null);
        builder.comment("abcd".toCharArray(), 0, 4);
        builder.endDTD();
        builder.startElement("", "root", "root", atts);
        builder.endElement("", "root", "node");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<!DOCTYPE skinconfig [<!--abcd-->]><root></root>");

        print(document);
        print(builder.getDocument());

        this.assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithMoreElements() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.startElement("", "node", "node", atts);
        builder.endElement("", "node", "node");
        builder.startElement("", "node", "node", atts);
        builder.endElement("", "node", "node");
        builder.endElement("", "root", "root");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root><node/><node/></root>");
        this.assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithOneElement() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.endElement("", "root", "root");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root/>");
        this.assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithText() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.characters("abcd".toCharArray(), 0, 4);
        builder.endElement("", "root", "node");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root>abcd</root>");
        this.assertXMLEqual(document, builder.getDocument());
    }

    public void testStreamer() throws Exception {
        Document document = XMLUnit.newControlParser().newDocument();
        Element elemA = document.createElement("root");
        document.appendChild(elemA);

        Element elemB = document.createElement("node");
        elemA.appendChild(elemB);

        elemB = document.createElement("node");
        elemA.appendChild(elemB);

        DOMBuilder builder = new DOMBuilder();
        DOMStreamer streamer = new DOMStreamer(builder);

        streamer.stream(document);

        document = builder.getDocument();

        Document moreElementDocument = XMLUnit.buildControlDocument("<root><node/><node/></root>");
        this.assertXMLEqual(moreElementDocument, document);
    }

    public void testTestFacility() throws Exception {
        Document document = XMLUnit.newControlParser().newDocument();
        Element elemA = document.createElement("root");
        document.appendChild(elemA);

        Document oneElementDocument = XMLUnit.buildControlDocument("<root/>");
        this.assertXMLEqual(oneElementDocument, document);

        document = XMLUnit.newControlParser().newDocument();
        elemA = document.createElement("node");
        document.appendChild(elemA);

        oneElementDocument = XMLUnit.buildControlDocument("<root/>");
        this.assertXMLNotEqual(oneElementDocument, document);
    }
}
