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

import static java.io.File.createTempFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

import com.indoqa.xml.pipeline.NonCachingPipeline;
import com.indoqa.xml.pipeline.Pipeline;
import com.indoqa.xml.pipeline.ProcessingException;
import com.indoqa.xml.pipeline.SetupException;
import com.indoqa.xml.pipeline.sax.AbstractSAXSerializer;
import com.indoqa.xml.pipeline.sax.SAXPipelineComponent;
import com.indoqa.xml.pipeline.sax.SAXProducer;
import com.indoqa.xml.sax.SAXBuffer;

public class SAXGeneratorTest {

    private static final String INVALID_XML_STRING = "<?xml version=\"1.0\"?><test>text<test>";
    private static final String VALID_XML_STRING = "<?xml version=\"1.0\"?><test>text</test>";

    private static File createXMLFile(String xmlString) throws IOException {
        File temp = createTempFile("cocoon", ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(temp), UTF_8)) {
            writer.write(xmlString);
        }
        return temp;
    }

    private static void runPipeline(SAXProducer generator, String expectedContent) throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(generator);
        pipeline.addComponent(new XMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        assertTrue(new Diff(expectedContent, baos.toString(UTF_8.toString())).similar());
    }

    @Test
    public void execByteArrayGenerator() throws Exception {
        runPipeline(new XMLGenerator(VALID_XML_STRING.getBytes(UTF_8.toString())), VALID_XML_STRING);
    }

    @Test
    public void execFileGenerator() throws Exception {
        runPipeline(new XMLGenerator(createXMLFile(VALID_XML_STRING)), VALID_XML_STRING);
    }

    @Test
    public void execInputStreamGenerator() throws Exception {
        runPipeline(new XMLGenerator(new FileInputStream(createXMLFile(VALID_XML_STRING))), VALID_XML_STRING);
    }

    @Test
    public void execSAXBufferGenerator() throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(VALID_XML_STRING));
        final SAXBuffer saxBuffer = new SAXBuffer();
        pipeline.addComponent(new AbstractSAXSerializer() {

            @Override
            public void setup(Map<String, Object> inputParameters) {
                super.setup(inputParameters);
                this.contentHandler = saxBuffer;
            }
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        runPipeline(new XMLGenerator(saxBuffer), VALID_XML_STRING);
    }

    @Test
    public void execStringGenerator() throws Exception {
        runPipeline(new XMLGenerator(VALID_XML_STRING), VALID_XML_STRING);
    }

    @Test
    public void execURLGenerator() throws Exception {
        runPipeline(new XMLGenerator(createXMLFile(VALID_XML_STRING).toURI().toURL()), VALID_XML_STRING);
    }

    @Test
    public void execURLGeneratorBySeparatelyPassingURL() throws Exception {
        XMLGenerator generator = new XMLGenerator();

        Map<String, Object> configurationParams = new HashMap<String, Object>();
        configurationParams.put("source", createXMLFile(VALID_XML_STRING).toURI().toURL());
        generator.setConfiguration(configurationParams);

        runPipeline(generator, VALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void execURLGeneratorWithEmptySource() throws Exception {
        runPipeline(new XMLGenerator((URL) null), VALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecByteArrayGenerator() throws Exception {
        runPipeline(new XMLGenerator(INVALID_XML_STRING.getBytes(UTF_8.toString())), INVALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecFileGenerator() throws Exception {
        runPipeline(new XMLGenerator(createXMLFile(INVALID_XML_STRING)), INVALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecInputStreamGenerator() throws Exception {
        runPipeline(new XMLGenerator(new FileInputStream(createXMLFile(INVALID_XML_STRING))), INVALID_XML_STRING);
    }

    @Test(expected = ProcessingException.class)
    public void invalidExecStringGenerator() throws Exception {
        runPipeline(new XMLGenerator(INVALID_XML_STRING), INVALID_XML_STRING);
    }

    @Test(expected = SetupException.class)
    public void newStringGenerator() {
        new XMLGenerator((String) null);
    }

    @Test(expected = SetupException.class)
    public void testByteArrayGenerator() {
        new XMLGenerator((byte[]) null);
    }

    @Test(expected = SetupException.class)
    public void testFileGenerator() {
        new XMLGenerator((File) null);
    }

    @Test(expected = SetupException.class)
    public void testInputStreamGenerator() {
        new XMLGenerator((InputStream) null);
    }

    @Test(expected = SetupException.class)
    public void testSAXBufferGenerator() {
        new XMLGenerator((SAXBuffer) null);
    }

    public void testURLGenerator() {
        new XMLGenerator((URL) null);
    }
}
