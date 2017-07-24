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
package com.indoqa.xml.pipeline;

import java.io.OutputStream;
import java.util.Map;

import com.indoqa.xml.pipeline.component.*;

/**
 * <p>
 * A pipeline expects one or more {@link PipelineComponent}s that passed by using {@link #addComponent(PipelineComponent)}. Then these
 * components get linked with each other in the order they were added. The {@link #setup(OutputStream, Map)} method, calls the setup
 * method on all pipeline components and assembles the pipeline. Finally the {@link #execute()} method produces the result and writes
 * it to the {@link OutputStream} which has been passed to the {@link #setup(OutputStream, Map)} method.
 * </p>
 *
 * <p>
 * A pipeline works based on two fundamental concepts:
 *
 * <ul>
 * <li>The first component of a pipeline is of type {@link Starter}. The last component is of type {@link Finisher}.</li>
 * <li>In order to link components with each other, the first has to be a {@link Producer}, the latter {@link Consumer}.</li>
 * </ul>
 *
 * <p>
 * When the pipeline links the components, it merely checks whether the above mentioned interfaces are present. So the pipeline does
 * not know about the specific capabilities or the compatibility of the components. It is the responsibility of the {@link Producer} to
 * decide whether a specific {@link Consumer} can be linked to it or not (that is, whether it can produce output in the desired format
 * of the {@link Consumer} or not). It is also conceivable that a {@link Producer} is capable of accepting different types of
 * {@link Consumer}s and adjust the output format according to the actual {@link Consumer}.
 * </p>
 */
public interface Pipeline<T extends PipelineComponent> {

    /**
     * Add a {@link PipelineComponent} to the pipeline. The order of when the components are passed is significant.
     *
     * @param pipelineComponent The {@link PipelineComponent}.
     */
    void addComponent(T pipelineComponent);

    /**
     * After the pipeline has been setup ({@link #setup(OutputStream, Map)}, this method can be invoked in order to produce the result.
     *
     * @throws Exception Any problem that might occur while processing the pipeline.
     */
    void execute() throws Exception;

    /**
     * Get the mime-type {@linkref http://tools.ietf.org/html/rfc2046} of the content produced by the pipeline.
     *
     * @return The mime-type of the content.
     */
    String getContentType();

    /**
     * Get the time of the last modification.
     *
     * @return The last modification date
     */
    long getLastModified();

    /**
     * After the pipeline has been prepared ({@link #addComponent(PipelineComponent)}, this method can be invoked in order to setup and
     * initialize the pipeline and its components.
     *
     * @param outputStream An {@link OutputStream} where the pipeline execution result is written.
     */
    void setup(OutputStream outputStream);

    /**
     * The same as {@link #setup(OutputStream)} but also allows passing parameters to the pipeline components.
     *
     * @param outputStream An {@link OutputStream} where the pipeline execution result is written.
     * @param parameters A {@link Map} of parameters that are available to all {@link PipelineComponent}s.
     */
    void setup(OutputStream outputStream, Map<String, Object> parameters);

    /**
     * Pass pipeline specific configuration parameters to the pipeline component in a generic way. This is useful in environments that
     * automatically assemble pipelines with their components and can't use the pipeline's constructors or setters.
     *
     * @param configuration The {@link Map} of configuration parameters.
     */
    void setConfiguration(Map<String, ? extends Object> parameters);
}
