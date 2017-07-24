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
package com.indoqa.xml.pipeline.sax;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

import com.indoqa.xml.pipeline.NonCachingPipeline;
import com.indoqa.xml.pipeline.Pipeline;
import com.indoqa.xml.pipeline.sax.component.SchemaProcessorTransformer;
import com.indoqa.xml.pipeline.sax.component.SchemaValidationException;
import com.indoqa.xml.pipeline.sax.component.XMLGenerator;
import com.indoqa.xml.pipeline.sax.component.XMLSerializer;
import com.indoqa.xml.pipeline.util.URLConnectionUtils;

public class SchemaProcessorTransformerTest {

    private static Pipeline<SAXPipelineComponent> createValidatinPipeline(final String xmlInput,
            final SchemaProcessorTransformer spt) {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(new XMLGenerator(xmlInput));
        pipeline.addComponent(spt);
        pipeline.addComponent(new XMLSerializer());

        return pipeline;
    }

    private static void testPipelineWithValidationInternal(final Pipeline<SAXPipelineComponent> pipeline) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        pipeline.execute();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><x></x>";
        String actual = new String(baos.toByteArray(), UTF_8);

        Diff diff = new Diff(expected, actual);
        assertTrue("XSL transformation didn't work as expected " + diff, diff.identical());
    }

    private static void testPipelineWithWrongValidationInternal(final Pipeline<SAXPipelineComponent> pipeline) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        try {
            pipeline.execute();
            fail("Not expected to reach this point");
        } catch (Exception e) {
            assertTrue(e instanceof SchemaValidationException);
        }
    }

    /**
     * A pipeline that performs an identity transformation, using the validation: generator -&gt; validator -&gt; serializer
     */
    @Test
    public void testPipelineWithValidation() throws Exception {
        testPipelineWithValidationInternal(this.createValidatingURLPipeline("<x></x>"));
        testPipelineWithValidationInternal(this.createValidatingSourcePipeline("<x></x>"));
    }

    /**
     * A pipeline that performs an identity transformation, using the validation: generator -&gt; validator -&gt; serializer. An error
     * is expected performing <code>execute</code> method due to not valid xml input.
     */
    @Test
    public void testPipelineWithWrongValidation() {
        testPipelineWithWrongValidationInternal(this.createValidatingURLPipeline("<y><z/></y>"));
        testPipelineWithWrongValidationInternal(this.createValidatingSourcePipeline("<y><z/></y>"));
    }

    private Pipeline<SAXPipelineComponent> createValidatingSourcePipeline(final String xmlInput) {
        return createValidatinPipeline(
            xmlInput,
            new SchemaProcessorTransformer(
                new StreamSource(this.getClass().getResourceAsStream("/test.xsd")),
                URLConnectionUtils.getLastModified(this.getClass().getResource("/test.xsd"))));
    }

    private Pipeline<SAXPipelineComponent> createValidatingURLPipeline(final String xmlInput) {
        return createValidatinPipeline(xmlInput, new SchemaProcessorTransformer(this.getClass().getResource("/test.xsd")));
    }
}
