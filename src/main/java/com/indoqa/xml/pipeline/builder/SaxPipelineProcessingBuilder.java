/*
 * Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Indoqa licenses this file to You under the Apache License, Version 2.0
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
package com.indoqa.xml.pipeline.builder;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.w3c.dom.Document;

import com.indoqa.xml.dom.DOMBuilder;
import com.indoqa.xml.pipeline.Pipeline;
import com.indoqa.xml.pipeline.sax.AbstractSAXSerializer;
import com.indoqa.xml.pipeline.sax.AbstractSAXTransformer;
import com.indoqa.xml.pipeline.sax.SAXPipelineComponent;
import com.indoqa.xml.pipeline.sax.component.TextSerializer;
import com.indoqa.xml.pipeline.sax.component.XMLSerializer;
import com.indoqa.xml.pipeline.sax.component.XSLTTransformer;

public class SaxPipelineProcessingBuilder {

    private Pipeline<SAXPipelineComponent> pipeline;

    protected SaxPipelineProcessingBuilder(Pipeline<SAXPipelineComponent> pipeline) {
        this.pipeline = pipeline;
    }

    public Document asDocument() {
        DOMBuilder domBuilder = new DOMBuilder();
        this.pipeline.addComponent(new DomSerializer(domBuilder));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            this.pipeline.setup(baos);
            this.pipeline.execute();
        } catch (Exception e) {
            throw new PipelineExecutionException(e);
        }

        return domBuilder.getDocument();
    }

    public String asText() {
        return this.serialize(new TextSerializer());
    }

    public String asXmlString() {
        return this.serialize(new XMLSerializer());
    }

    public SaxPipelineProcessingBuilder transform(AbstractSAXTransformer transformer) {
        this.pipeline.addComponent(transformer);
        return this;
    }

    public SaxPipelineProcessingBuilder transformWithXSLT(URL xsltStylesheet) {
        this.pipeline.addComponent(new XSLTTransformer(xsltStylesheet));
        return this;
    }

    private String serialize(XMLSerializer serializer) {
        this.pipeline.addComponent(serializer);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            this.pipeline.setup(baos);
            this.pipeline.execute();
            return baos.toString(UTF_8.toString());
        } catch (Exception e) {
            throw new PipelineExecutionException(e);
        }
    }

    private static final class DomSerializer extends AbstractSAXSerializer {

        public DomSerializer(DOMBuilder domBuilder) {
            this.setContentHandler(domBuilder);
        }
    }
}
