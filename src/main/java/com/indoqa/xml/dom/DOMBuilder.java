/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
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
package com.indoqa.xml.dom;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.indoqa.xml.sax.AbstractSAXPipe;

/**
 * The <code>DOMBuilder</code> is a utility class that will generate a W3C DOM Document from SAX events.
 */
public class DOMBuilder extends AbstractSAXPipe {

    /** The default transformer factory shared by all instances */
    protected static final SAXTransformerFactory FACTORY = (SAXTransformerFactory) TransformerFactory.newInstance();

    /** The transformer factory */
    protected SAXTransformerFactory factory;

    /** The listener */
    protected Listener listener;

    /** The result */
    protected DOMResult result;

    /** The parentNode */
    protected Node parentNode;

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder() {
        this((Listener) null, (Node) null);
    }

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder(Listener listener) {
        this(listener, null);
    }

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder(Listener listener, Node parentNode) {
        this((SAXTransformerFactory) null, listener, parentNode);
    }

    /**
     * Constructs a new instance that appends nodes to the given parent node. <br/>
     * <strong>Note:</strong> You cannot use a <code>Listener<code> when appending to a
     * <code>Node</code>, because the notification occurs at <code>endDocument()</code> which does not happen here.
     */
    public DOMBuilder(Node parentNode) {
        this(null, parentNode);
    }

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder(SAXTransformerFactory factory) {
        this(factory, null, null);
    }

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder(SAXTransformerFactory factory, Listener listener, Node parentNode) {
        super();
        this.factory = factory == null ? FACTORY : factory;
        this.listener = listener;
        this.parentNode = parentNode;
        this.setup();
    }

    /**
     * Receive notification of the end of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        // Notify the listener
        this.notifyListener();
    }

    /**
     * Return the newly built Document.
     */
    public Document getDocument() {
        if (this.result == null || this.result.getNode() == null) {
            return null;
        } else if (this.result.getNode().getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) this.result.getNode();
        } else {
            return this.result.getNode().getOwnerDocument();
        }
    }

    /**
     * Recycle this builder, prepare for re-use.
     */
    @Override
    public void recycle() {
        super.recycle();
        this.setup();
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    protected void notifyListener() throws SAXException {
        if (this.listener != null) {
            this.listener.notify(this.getDocument());
        }
    }

    /**
     * Setup this instance transformer and result objects.
     */
    private void setup() {
        try {
            TransformerHandler handler = this.factory.newTransformerHandler();
            this.setContentHandler(handler);
            if (this.parentNode != null) {
                this.result = new DOMResult(this.parentNode);
            } else {
                this.result = new DOMResult();
            }
            handler.setResult(this.result);
        } catch (TransformerException local) {
            throw new RuntimeException("Fatal-Error: Unable to get transformer handler", local);
        }
    }

    /**
     * The Listener interface must be implemented by those objects willing to be notified of a successful DOM tree generation.
     */
    public interface Listener {

        /**
         * Receive notification of a successfully completed DOM tree generation.
         */
        void notify(Document doc) throws SAXException;
    }
}
