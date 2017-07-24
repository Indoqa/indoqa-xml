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

import java.io.OutputStream;
import java.util.Map;

import com.indoqa.xml.pipeline.component.Finisher;
import com.indoqa.xml.pipeline.util.StringRepresentation;

public abstract class AbstractSAXSerializer extends SAXConsumerAdapter implements SAXPipelineComponent, Finisher {

    private OutputStream outputStream;

    private String contentType;

    private Map<String, ? extends Object> configuration;

    private Map<String, Object> inputParameters;

    /**
     * @return A {@link Map} that contains all configuration parameters.
     */
    protected Map<String, ? extends Object> getConfiguration() {
        return this.configuration;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.component.Finisher#getContentType()
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @return A {@link Map} that contains all pipeline input parameters.
     */
    protected Map<String, Object> getInputParameters() {
        return this.inputParameters;
    }

    /**
     * @return The {@link OutputStream} of the pipeline.
     */
    protected OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        if (configuration.containsKey("mime-type")) {
            this.contentType = (String) configuration.get("mime-type");
        }
    }

    /**
     * @param contentType The type of the content produced by this serializer.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.component.PipelineComponent#setup(java.util.Map)
     */
    @Override
    public void setup(Map<String, Object> inputParameters) {
        this.inputParameters = inputParameters;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.component.Finisher#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return StringRepresentation.buildString(this);
    }
}
