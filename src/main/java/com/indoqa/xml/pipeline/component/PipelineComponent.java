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
package com.indoqa.xml.pipeline.component;

import java.util.Map;

/**
 * This is a component used in a pipeline.
 * 
 * If the environment has some configuration for this component {@link #setConfiguration(Map)} is called as the first step. After the
 * component has been added to pipeline, {@link #setup(Map)} is called before the pipeline is executed. When the pipeline is finished
 * or if any error occurred {@link #finish} is called.
 */
public interface PipelineComponent {

    /**
     * This method is called after pipeline run - regardless if the run was successful or an exception was thrown.
     */
    void finish();

    /**
     * Pass component specific configuration parameters to the pipeline component in a generic way. This is useful in environments that
     * automatically assemble pipelines with their components and can't use the components constructors or setters.
     * 
     * @param configuration The {@link Map} of configuration parameters.
     */
    void setConfiguration(Map<String, ? extends Object> configuration);

    /**
     * The shared object map for this pipeline run.
     * 
     * @param parameters A {@link Map} of parameters that are available to all {@link PipelineComponent}s. This is a modifiable map
     *            that can be changed by this pipeline component.
     */
    void setup(Map<String, Object> parameters);
}
