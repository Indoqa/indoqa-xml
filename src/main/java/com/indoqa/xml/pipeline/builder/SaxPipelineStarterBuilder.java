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

import com.indoqa.xml.pipeline.Pipeline;
import com.indoqa.xml.pipeline.sax.SAXPipelineComponent;
import com.indoqa.xml.pipeline.sax.component.XMLGenerator;

public class SaxPipelineStarterBuilder {

    private Pipeline<SAXPipelineComponent> pipeline;
    private SaxPipelineProcessingBuilder pipelineProcessingBuilder;

    protected SaxPipelineStarterBuilder(Pipeline<SAXPipelineComponent> pipeline) {
        this.pipeline = pipeline;
        this.pipelineProcessingBuilder = new SaxPipelineProcessingBuilder(this.pipeline);
    }

    public SaxPipelineProcessingBuilder of(String xmlString) {
        this.pipeline.addComponent(new XMLGenerator(xmlString));
        return this.pipelineProcessingBuilder;
    }
}
