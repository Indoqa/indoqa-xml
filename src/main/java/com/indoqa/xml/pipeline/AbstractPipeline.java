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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.xml.pipeline.component.*;
import com.indoqa.xml.pipeline.util.StringRepresentation;

/**
 * Basic pipeline implementation that collects the {@link PipelineComponent}s and connects them with each other.
 */
public abstract class AbstractPipeline<T extends PipelineComponent> implements Pipeline<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPipeline.class);
    private static final String ERR_MSG_FIRST_NOT_STARTER = "Cannot execute pipeline, first pipeline component is not starter";
    private static final String ERR_MSG_LAST_NOT_FINISHER = "Cannot execute pipeline, last pipeline component is not finisher";

    private final LinkedList<T> components = new LinkedList<T>();
    private boolean setupDone;

    private static void linkComponents(final PipelineComponent firstComponent, final PipelineComponent secondComponent) {
        LOG.debug("Going to link the component " + firstComponent + " with " + secondComponent + ".");

        // first component must be a Producer
        if (!(firstComponent instanceof Producer)) {
            final String msg = "Cannot link components: First component (" + firstComponent + ") is no producer.";
            throw new SetupException(new IllegalStateException(msg));
        }

        // second component must be a Consumer
        if (!(secondComponent instanceof Consumer)) {
            final String msg = "Cannot link components: Second component (" + secondComponent + ") is no consumer.";
            throw new SetupException(new IllegalStateException(msg));
        }

        // let the Producer accept the Consumer (the Producer might reject it)
        ((Producer) firstComponent).setConsumer((Consumer) secondComponent);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.Pipeline#addComponent(com.indoqa.xml.pipeline.sax.pipeline.component.PipelineComponent)
     */
    @Override
    public void addComponent(final T pipelineComponent) {
        if (this.setupDone) {
            throw new SetupException(
                new IllegalStateException("Pass all pipeline components to the pipeline before calling this method."));
        }

        LOG.debug("Adding component " + pipelineComponent + " to pipeline [" + this + "].");

        this.components.add(pipelineComponent);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.Pipeline#execute()
     */
    @Override
    public void execute() throws Exception {
        if (!this.setupDone) {
            throw new ProcessingException(new IllegalStateException("The pipeline wasn't setup correctly. Call #setup() first."));
        }

        this.invokeStarter();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.Pipeline#getContentType()
     */
    @Override
    public String getContentType() {
        return this.getFinisher().getContentType();
    }

    @Override
    public long getLastModified() {
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.Pipeline#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(final Map<String, ? extends Object> parameters) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.Pipeline#setup(java.io.OutputStream, java.util.Map)
     */
    @Override
    public void setup(final OutputStream outputStream) {
        this.setup(outputStream, null);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.indoqa.xml.pipeline.sax.pipeline.Pipeline#setup(java.io.OutputStream, java.util.Map)
     */
    @Override
    public void setup(final OutputStream outputStream, final Map<String, Object> parameters) {
        if (outputStream == null) {
            throw new SetupException("An output stream must be passed.");
        }

        this.setupComponents(outputStream, parameters);
        this.setupDone = true;
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "components=" + this.getComponents());
    }

    protected LinkedList<T> getComponents() {
        return this.components;
    }

    protected Finisher getFinisher() {
        return (Finisher) this.components.getLast();
    }

    protected void invokeStarter() {
        LOG.debug("Invoking first component of " + this);

        try {
            final Starter starter = (Starter) this.components.getFirst();
            starter.execute();
        } finally {
            for (PipelineComponent pipelineComponent : this.getComponents()) {
                pipelineComponent.finish();
            }
        }
    }

    protected void setupComponents(final OutputStream outputStream, final Map<String, Object> parameters) {
        final PipelineComponent first = this.components.getFirst();

        // first component must be a Starter
        if (!(first instanceof Starter)) {
            LOG.error(ERR_MSG_FIRST_NOT_STARTER);
            throw new SetupException(new IllegalStateException(ERR_MSG_FIRST_NOT_STARTER));
        }

        // last component must be a Finisher
        final PipelineComponent last = this.components.getLast();
        if (!(last instanceof Finisher)) {
            LOG.error(ERR_MSG_LAST_NOT_FINISHER);
            throw new SetupException(new IllegalStateException(ERR_MSG_LAST_NOT_FINISHER));
        }

        // now try to link the components, always two components at a time
        // start at the first component
        PipelineComponent currentComponent = first;
        first.setup(parameters);

        // next component to link is the second in the list
        for (final ListIterator<T> itor = this.components.listIterator(1); itor.hasNext();) {
            // link the current with the next component
            final PipelineComponent nextComponent = itor.next();
            linkComponents(currentComponent, nextComponent);

            // now advance to the next component
            currentComponent = nextComponent;
            currentComponent.setup(parameters);
        }

        // configure the finisher
        ((Finisher) last).setOutputStream(outputStream);
    }
}
