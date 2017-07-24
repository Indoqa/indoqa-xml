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
package com.indoqa.xml.pipeline.sax.component;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.pipeline.NonCachingPipeline;
import com.indoqa.xml.pipeline.Pipeline;
import com.indoqa.xml.pipeline.sax.SAXPipelineComponent;

public final class LinkRewriterTransformerTest {

    private static final Logger LOG = LoggerFactory.getLogger(LinkRewriterTransformerTest.class);

    @BeforeClass
    public static void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        final SAXParserFactory saxPFactory = SAXParserFactory.newInstance();
        saxPFactory.setValidating(false);
        try {
            saxPFactory.setFeature("http://apache.org/xml/features/" + "nonvalidating/load-external-dtd", false);
        } catch (Exception e) {
            LOG.error("While setting up SAX parser factory", e);
        }
        XMLUnit.setSAXParserFactory(saxPFactory);

        final DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
        docBuildFactory.setNamespaceAware(true);
        docBuildFactory.setValidating(false);
        try {
            docBuildFactory.setFeature("http://apache.org/xml/features/" + "nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            LOG.error("While setting up Document builder factory", e);
        }
        XMLUnit.setControlDocumentBuilderFactory(docBuildFactory);
        XMLUnit.setTestDocumentBuilderFactory(docBuildFactory);
    }

    @Test
    public void testRegexpLinkRewriting() throws Exception {
        final URL source = this.getClass().getResource("/apache_home.html");

        final Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(source));

        Map<String, String> regexpMap = new HashMap<String, String>();
        regexpMap.put("^\\./([\\.]*)", "http://www.apache.org/$1");
        regexpMap.put("^/([\\.]*)", "http://www.apache.org/$1");

        final RegexpLinkRewriterTransformer regexpLrt = new RegexpLinkRewriterTransformer(regexpMap);

        regexpLrt.addElement("a", "href");
        regexpLrt.addElement("http://www.w3.org/1999/xhtml", "link", AbstractLinkRewriterTransformer.ALL_NAMESPACES, "href");
        pipeline.addComponent(regexpLrt);

        pipeline.addComponent(new XMLSerializer());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        final String actualDocument = new String(baos.toByteArray(), "UTF-8");

        final StringBuilder expectedDocBuf = new StringBuilder();
        final BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream(), UTF_8));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            expectedDocBuf.append(inputLine);
            expectedDocBuf.append('\n');
        }
        in.close();

        String expectedDocument = expectedDocBuf.toString();
        expectedDocument = expectedDocument
            .replaceAll("href=\"\\./", "href=\"http://www.apache.org/")
            .replaceAll("href=\"/", "href=\"http://www.apache.org/");

        final Diff diff = new Diff(expectedDocument, actualDocument);

        Assert.assertTrue("RegexpLinkRewrite transformation didn't work as expected " + diff, diff.identical());
    }

    /**
     * Test that all non-absolute HTTP links are rewritten as absolute links.
     * 
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testXhtmlLinkRewriting() throws Exception {

        final URL source = this.getClass().getResource("/apache_home.html");

        final Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(source));

        final AbstractLinkRewriterTransformer lrt = new AbsoluteLinkRewriterTransformer();
        lrt.addElement("a", "href");
        lrt.addElement("http://www.w3.org/1999/xhtml", "link", AbstractLinkRewriterTransformer.ALL_NAMESPACES, "href");
        pipeline.addComponent(lrt);

        pipeline.addComponent(new XMLSerializer());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        final String actualDocument = new String(baos.toByteArray(), UTF_8);

        final StringBuilder expectedDocBuf = new StringBuilder();
        final BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream(), UTF_8));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            expectedDocBuf.append(inputLine);
            expectedDocBuf.append('\n');
        }
        in.close();

        String expectedDocument = expectedDocBuf.toString();
        expectedDocument = expectedDocument
            .replaceAll("href=\"/", "href=\"http://www.apache.org/")
            .replaceAll("href=\"\\./", "href=\"http://www.apache.org/")
            .replaceAll("href=\"foundation", "href=\"http://www.apache.org/foundation");

        final Diff diff = new Diff(expectedDocument, actualDocument);

        Assert.assertTrue("LinkRewrite transformation didn't work as expected " + diff, diff.identical());
    }

    static class AbsoluteLinkRewriterTransformer extends AbstractLinkRewriterTransformer {

        @Override
        public String rewrite(final String elementNS, final String elementName, final String attributeNS, final String attributeName,
                final String link) throws LinkRewriterException {

            String result;
            if (link.startsWith("http")) {
                result = link;
            } else {
                result = "http://www.apache.org"
                    + (link.charAt(0) == '.' ? link.substring(1) : link.charAt(0) == '/' ? link : "/" + link);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Link '" + link + "' rewritten to '" + result + "'");
                }
            }
            return result;
        }
    }
}
