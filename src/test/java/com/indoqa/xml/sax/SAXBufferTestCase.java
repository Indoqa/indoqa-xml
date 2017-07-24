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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import com.indoqa.xml.dom.DOMBuilder;

/**
 * Testcase for SaxBuffer
 */
public final class SAXBufferTestCase extends AbstractXMLTestCase {

    private static final Logger LOGGER = getLogger(SAXBufferTestCase.class);

    public SAXBufferTestCase(String s) {
        super(s);
    }

    public void testCompareDOM() throws Exception {
        DOMBuilder in = new DOMBuilder();
        this.generateLargeSAX(in);

        SAXBuffer sb = new SAXBuffer();
        this.generateLargeSAX(sb);

        DOMBuilder out = new DOMBuilder();
        sb.toSAX(out);

        this.assertXMLEqual(in.getDocument(), out.getDocument());
    }

    public void testCompareToParsing() throws Exception {
        DOMBuilder in = new DOMBuilder();
        this.generateSmallSAX(in);

        SAXParserFactory pfactory = SAXParserFactory.newInstance();
        SAXParser p = pfactory.newSAXParser();

        DefaultHandler ch = new DefaultHandler();

        SAXBuffer b = new SAXBuffer();
        ByteArrayInputStream bis = new ByteArrayInputStream(this.generateByteArray());

        long loop = 10000;

        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            b.recycle();
            bis.reset();
            p.parse(bis, ch);
        }
        long stop = System.currentTimeMillis() + 1;

        double r = 1000 * loop / (stop - start);
        LOGGER.debug("parsed:" + r + " documents per second");

        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            b.toSAX(ch);
        }
        stop = System.currentTimeMillis() + 1;

        r = 1000 * loop / (stop - start);
        LOGGER.debug("recalling: " + r + " documents per second");
    }

    public void testStressLoop() throws Exception {
        SAXBuffer sb = new SAXBuffer();

        long loop = 10000;

        // simply consume documents
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            this.generateSmallSAX(sb);
            sb.recycle();
        }
        long stop = System.currentTimeMillis() + 1;

        double r = 1000 * loop / (stop - start);
        LOGGER.debug("consuming: " + r + " documents per second");
    }
}
