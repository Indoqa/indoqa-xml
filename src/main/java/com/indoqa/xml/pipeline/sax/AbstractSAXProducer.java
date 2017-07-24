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

import com.indoqa.xml.pipeline.SetupException;
import com.indoqa.xml.pipeline.component.Consumer;
import com.indoqa.xml.pipeline.util.StringRepresentation;

public abstract class AbstractSAXProducer extends AbstractSAXPipelineComponent implements SAXProducer {

    private SAXConsumer saxConsumer;

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.component.Producer#setConsumer(com.indoqa.xml.pipeline.sax.pipeline.component.Consumer)
     */
    public final void setConsumer(Consumer consumer) {
        if (!(consumer instanceof SAXConsumer)) {
            throw new SetupException("SAXProducer requires a SAXConsumer.");
        }

        this.setSAXConsumer((SAXConsumer) consumer);
    }

    protected SAXConsumer getSAXConsumer() {
        return this.saxConsumer;
    }

    protected void setSAXConsumer(SAXConsumer xmlConsumer) {
        this.saxConsumer = xmlConsumer;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this);
    }
}
