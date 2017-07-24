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
package com.indoqa.xml.pipeline.sax.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.indoqa.xml.pipeline.caching.CacheKey;
import com.indoqa.xml.pipeline.caching.SimpleCacheKey;
import com.indoqa.xml.pipeline.component.CachingPipelineComponent;
import com.indoqa.xml.pipeline.sax.AbstractSAXTransformer;

public abstract class AbstractLinkRewriterTransformer extends AbstractSAXTransformer implements CachingPipelineComponent {

    protected static final String ALL_NAMESPACES = "*";

    protected static final String EMPTY_NAMESPACE = "";

    private static final String ELEMENT_PREFIX = "element";

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractLinkRewriterTransformer.class);

    private final Map<Element, Set<Element>> elements = new HashMap<Element, Set<Element>>();

    public final void addElement(final String elementName, final String attributeName) {
        this.addElement(ALL_NAMESPACES, elementName, ALL_NAMESPACES, attributeName);
    }

    public final void addElement(final String elementNS, final String elementName, final String attributeNS,
            final String attributeName) {

        if (elementName == null) {
            throw new IllegalArgumentException("Parameter 'elementName' must not be null");
        }
        final Element key = new Element(elementNS == null ? EMPTY_NAMESPACE : elementNS, elementName);

        if (attributeName == null) {
            throw new IllegalArgumentException("Parameter 'attributeName' must not be null");
        }

        Set<Element> attributes;
        if (this.elements.containsKey(key)) {
            attributes = this.elements.get(key);
        } else {
            attributes = new HashSet<Element>();
            this.elements.put(key, attributes);
        }

        attributes.add(new Element(attributeNS == null ? EMPTY_NAMESPACE : attributeNS, attributeName));
    }

    @Override
    public final CacheKey constructCacheKey() {
        return new SimpleCacheKey();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {

        this.setup((Map<String, Object>) configuration);
    }

    @Override
    public void setup(final Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        String[] split;
        for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getKey().startsWith(ELEMENT_PREFIX)) {
                split = ((String) parameter.getValue()).split(" ");
                if (split.length == 2) {
                    this.addElement(split[0], split[1]);
                } else if (split.length == 4) {
                    this.addElement(split[0], split[1], split[2], split[3]);
                } else {
                    LOG.error("Invalid element as parameter, ignoring: " + parameter.getValue());
                }
            }
        }
    }

    @Override
    public final void startElement(final String uri, final String localName, final String name, Attributes atts) throws SAXException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing element (namespace=" + uri + ", name=" + localName + ")");
        }

        String attributeNS;
        String attributeName;
        String attributeValue;
        for (int i = 0; i < atts.getLength(); i++) {
            attributeNS = atts.getURI(i);
            attributeName = atts.getLocalName(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing attribute ( namepsace=" + attributeNS + ", name=" + attributeName + ")");
            }

            if (this.contains(uri, localName, attributeNS, attributeName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Element has attributes to rewrite");
                }

                attributeValue = atts.getValue(i);
                atts = new AttributesImpl(atts);
                try {
                    final String rewrite = this.rewrite(uri, name, attributeNS, attributeName, attributeValue);
                    ((AttributesImpl) atts).setValue(i, rewrite);
                } catch (LinkRewriterException e) {
                    LOG.error(
                        "An error occurred while rewriting link '" + attributeValue + "' (" + name + "/@" + attributeName + ")",
                        e);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished processing (namespace=" + uri + ", name=" + localName + ")");
        }

        super.startElement(uri, localName, name, atts);
    }

    protected abstract String rewrite(String elementNS, String elementName, String attributeNS, String attributeName, String link)
            throws LinkRewriterException;

    private boolean contains(final String elementNamespace, final String elementName, final String atributeNamespace,
            final String attributeName) {

        final Element element = new Element(elementNamespace, elementName);
        return this.elements.containsKey(element)
            && this.elements.get(element).contains(new Element(atributeNamespace, attributeName));
    }

    private final class Element {

        private final String namespace;

        private final String name;

        public Element(final String namespace, final String name) {
            this.namespace = namespace;
            this.name = name;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            final Element other = (Element) obj;

            if (!(ALL_NAMESPACES.equals(this.namespace) || ALL_NAMESPACES.equals(other.getNamespace()))
                && !this.namespace.equals(other.getNamespace())) {

                return false;
            }

            if (!this.name.equals(other.getName())) {
                return false;
            }

            return true;
        }

        public String getName() {
            return this.name;
        }

        public String getNamespace() {
            return this.namespace;
        }

        @Override
        public int hashCode() {
            final int initialNonZeroOddNumber = 97;
            final int multiplierNonZeroOddNumber = 7;
            return initialNonZeroOddNumber * multiplierNonZeroOddNumber + this.name.hashCode();
        }

        @Override
        public String toString() {
            return "{ namespace='" + this.namespace + "', name='" + this.name + "' }";
        }
    }
}
