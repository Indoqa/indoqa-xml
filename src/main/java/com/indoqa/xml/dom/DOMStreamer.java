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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.w3c.dom.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.indoqa.xml.sax.AbstractSAXProducer;
import com.indoqa.xml.sax.EmbeddedSAXPipe;

/**
 * The <code>DOMStreamer</code> is a utility class that will generate SAX events from a W3C DOM Document.
 *
 * <p>
 * The DOMStreamer uses a different strategy based on the value of the normalizeNamespaces property:
 * <ul>
 * <li>if true (the default), the DOMStreamer will normalize namespace declarations (i.e. add missing xmlns attributes or correct
 * them). See also {@link NamespaceNormalizingDOMStreamer}.
 * <li>if false, the standard JAXP identity transformer is used.
 * </ul>
 *
 * @version $Id: DOMStreamer.java 729283 2008-12-24 09:25:21Z cziegeler $
 */
public class DOMStreamer {

    /** The transformer factory shared by all instances (only used by DefaultDOMStreamer) */
    private static final TransformerFactory FACTORY = TransformerFactory.newInstance();

    /** Default value for normalizeNamespaces. */
    private static final boolean DEFAULT_NORMALIZE_NAMESPACES = true;

    /** Indicates whether namespace normalization should happen. */
    protected boolean normalizeNamespaces = DEFAULT_NORMALIZE_NAMESPACES;

    /** DOMStreamer used in case of namespace normalization. */
    protected NamespaceNormalizingDOMStreamer namespaceNormalizingDOMStreamer = new NamespaceNormalizingDOMStreamer();

    /** DOMStreamer used when namespace normalization should not explicitely happen. */
    protected DefaultDOMStreamer defaultDOMStreamer = new DefaultDOMStreamer();

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer() {
        super();
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(ContentHandler content) {
        this.setContentHandler(content);
    }

    public boolean isNormalizeNamespaces() {
        return this.normalizeNamespaces;
    }

    public void recycle() {
        this.defaultDOMStreamer.recycle();
        this.namespaceNormalizingDOMStreamer.recycle();
        this.normalizeNamespaces = DEFAULT_NORMALIZE_NAMESPACES;
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     */
    public void setContentHandler(ContentHandler handler) {
        this.defaultDOMStreamer.setContentHandler(handler);
        this.namespaceNormalizingDOMStreamer.setContentHandler(handler);
    }

    public void setNormalizeNamespaces(boolean normalizeNamespaces) {
        this.normalizeNamespaces = normalizeNamespaces;
    }

    /**
     * Start the production of SAX events.
     */
    public void stream(Node node) throws SAXException {
        if (this.normalizeNamespaces) {
            this.namespaceNormalizingDOMStreamer.stream(node);
        } else {
            this.defaultDOMStreamer.stream(node);
        }
    }

    /**
     * The <code>DefaultDOMStreamer</code> is a utility class that will generate SAX events from a W3C DOM Document.
     */
    public static class DefaultDOMStreamer extends AbstractSAXProducer {

        /** The private transformer for this instance */
        protected Transformer transformer;

        /**
         * Start the production of SAX events.
         */
        public void stream(Node node) throws SAXException {
            if (this.transformer == null) {
                try {
                    this.transformer = FACTORY.newTransformer();
                } catch (TransformerConfigurationException e) {
                    throw new SAXException(e);
                }
            }
            DOMSource source = new DOMSource(node);

            ContentHandler handler;
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                // Pass all SAX events
                handler = this.contentHandler;
            } else {
                // Strip start/endDocument
                handler = new EmbeddedSAXPipe(this.contentHandler);
            }

            SAXResult result = new SAXResult(handler);
            result.setLexicalHandler(this.lexicalHandler);

            try {
                this.transformer.transform(source, result);
            } catch (TransformerException e) {
                throw new SAXException(e);
            }
        }
    }

    /**
     * Streams a DOM tree to SAX events and normalizes namespace declarations on the way.
     *
     * <p>
     * The code in this class is based on the org.apache.xml.utils.TreeWalker class from Xalan, though it differs in some important
     * ways.
     *
     * <p>
     * This class will automatically fix up ("normalize") namespace declarations while streaming to SAX. The original DOM-tree is not
     * modified. The algorithm used is described in
     * <a href="http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20021022/namespaces-algorithms.html#normalizeDocumentAlgo">an appendix of
     * the DOM Level 3 spec</a>.
     *
     * <p>
     * This class will NOT check the correctness of namespaces, e.g. it will not check that the "xml" prefix is not misused etc.
     *
     */
    public static class NamespaceNormalizingDOMStreamer extends AbstractSAXProducer {

        /**
         * Information about the current element. Used to remember the localName, qName and namespaceURI for generating the endElement
         * event, and holds the namespaces declared on the element. This extra class is needed because we don't want to modify the
         * DOM-tree itself. The currentElementInfo has a pointer to its parent elementInfo.
         */
        protected NamespaceNormalizingDOMStreamer.ElementInfo currentElementInfo;

        /** Counter used when generating new namespace prefixes. */
        protected int newPrefixCounter;

        /**
         * Splits a nodeName into a prefix and a localName
         *
         * @return an array containing two elements, the first one is the prefix (can be null), the second one is the localName
         */
        private static String[] getPrefixAndLocalName(String nodeName) {
            String prefix, localName;
            int colonPos = nodeName.indexOf(":");
            if (colonPos != -1) {
                prefix = nodeName.substring(0, colonPos);
                localName = nodeName.substring(colonPos + 1, nodeName.length());
            } else {
                prefix = null;
                localName = nodeName;
            }
            return new String[] {prefix, localName};
        }

        /**
         * Searches the namespace for a given namespace prefix starting from a given Element.
         *
         * <p>
         * Note that this resolves the prefix in the orginal DOM-tree, not in the {@link ElementInfo} objects. This is used to resolve
         * prefixes of elements or attributes created with createElement or setAttribute instead of createElementNS or setAttributeNS.
         *
         * <p>
         * The code in this method is largely based on org.apache.xml.utils.DOMHelper.getNamespaceForPrefix() (from Xalan).
         *
         * @param prefix the prefix to look for, can be empty or null to find the default namespace
         *
         * @return the namespace, or null if not found.
         */
        public String getNamespaceForPrefix(String prefix, Element namespaceContext) {
            int type;
            Node parent = namespaceContext;
            String namespace = null;

            if (prefix == null) {
                prefix = "";
            }

            if (prefix.equals("xml")) {
                namespace = "http://www.w3.org/XML/1998/namespace";
            } else if (prefix.equals("xmlns")) {
                namespace = "http://www.w3.org/2000/xmlns/";
            } else {
                // Attribute name for this prefix's declaration
                String declname = prefix.length() == 0 ? "xmlns" : "xmlns:" + prefix;

                // Scan until we run out of Elements or have resolved the namespace
                while (null != parent && ((type = parent.getNodeType()) == Node.ELEMENT_NODE || type == Node.ENTITY_REFERENCE_NODE)) {
                    if (type == Node.ELEMENT_NODE) {
                        Attr attr = ((Element) parent).getAttributeNode(declname);
                        if (attr != null) {
                            namespace = attr.getNodeValue();
                            break;
                        }
                    }
                    parent = parent.getParentNode();
                }
            }
            return namespace;
        }

        @Override
        public void recycle() {
            super.recycle();
            this.currentElementInfo = null;
            this.newPrefixCounter = 0;
        }

        /**
         * End processing of given node
         *
         * @param node Node we just finished processing
         */
        protected void endNode(Node node) throws org.xml.sax.SAXException {

            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    this.contentHandler.endElement(
                        this.currentElementInfo.namespaceURI,
                        this.currentElementInfo.localName,
                        this.currentElementInfo.qName);

                    // generate endPrefixMapping events if needed
                    if (this.currentElementInfo.namespaceDeclarations != null
                        && this.currentElementInfo.namespaceDeclarations.size() > 0) {
                        Iterator<Map.Entry<String, String>> namespaceIt = this.currentElementInfo.namespaceDeclarations
                            .entrySet()
                            .iterator();
                        while (namespaceIt.hasNext()) {
                            Map.Entry<String, String> entry = namespaceIt.next();
                            this.contentHandler.endPrefixMapping(entry.getKey());
                            // System.out.println("ending prefix mapping " + (String) entry.getKey());
                        }
                    }
                    this.currentElementInfo = this.currentElementInfo.parent;
                    break;
                case Node.DOCUMENT_NODE:
                case Node.CDATA_SECTION_NODE:
                    break;
                case Node.ENTITY_REFERENCE_NODE: {
                    EntityReference eref = (EntityReference) node;

                    if (this.lexicalHandler != null) {
                        this.lexicalHandler.endEntity(eref.getNodeName());
                    }
                }
                    break;
                default:
            }
        }

        /**
         * Start processing given node
         *
         * @param node Node to process
         */
        protected void startNode(Node node) throws SAXException {

            switch (node.getNodeType()) {
                case Node.COMMENT_NODE: {
                    if (this.lexicalHandler != null) {
                        final String data = ((Comment) node).getData();
                        if (data != null) {
                            this.lexicalHandler.comment(data.toCharArray(), 0, data.length());
                        }
                    }
                }
                    break;
                case Node.DOCUMENT_FRAGMENT_NODE:
                    // ??;
                case Node.DOCUMENT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    NamedNodeMap atts = node.getAttributes();
                    int nAttrs = atts.getLength();

                    // create a list of localy declared namespace prefixes
                    this.currentElementInfo = new NamespaceNormalizingDOMStreamer.ElementInfo(this.currentElementInfo);
                    for (int i = 0; i < nAttrs; i++) {
                        Node attr = atts.item(i);
                        String attrName = attr.getNodeName();

                        if (attrName.equals("xmlns") || attrName.startsWith("xmlns:")) {
                            int index;
                            String prefix = (index = attrName.indexOf(":")) < 0 ? "" : attrName.substring(index + 1);

                            this.currentElementInfo.put(prefix, attr.getNodeValue());
                        }
                    }

                    String namespaceURI = node.getNamespaceURI();
                    String prefix = node.getPrefix();
                    String localName = node.getLocalName();

                    if (localName == null) {
                        // this is an element created with createElement instead of createElementNS
                        String[] prefixAndLocalName = getPrefixAndLocalName(node.getNodeName());
                        prefix = prefixAndLocalName[0];
                        localName = prefixAndLocalName[1];
                        // note: if prefix is null, there can still be a default namespace...
                        namespaceURI = this.getNamespaceForPrefix(prefix, (Element) node);
                    }

                    if (namespaceURI != null) {
                        // no prefix means: make this the default namespace
                        if (prefix == null) {
                            prefix = "";
                        }
                        // check that is declared
                        String uri = this.currentElementInfo.findNamespaceURI(prefix);
                        if (uri != null && uri.equals(namespaceURI)) {
                            // System.out.println("namespace is declared");
                            // prefix is declared correctly, do nothing
                            // } else if (uri != null) {
                            // System.out.println("prefix is declared with other namespace, overwriting it");
                            // prefix exists but is bound to another namespace, overwrite it
                            // currentElementInfo.put(prefix, namespaceURI);
                        } else {
                            // System.out.println("prefix is not yet declared, declaring it now");
                            this.currentElementInfo.put(prefix, namespaceURI);
                        }
                    } else {
                        // element has no namespace
                        // check if there is a default namespace, if so undeclare it
                        String uri = this.currentElementInfo.findNamespaceURI("");
                        if (uri != null && uri.trim().length() > 0) {
                            // System.out.println("undeclaring default namespace");
                            this.currentElementInfo.put("", "");
                        }
                    }

                    // SAX uses empty string to denote no namespace, while DOM uses null.
                    if (namespaceURI == null) {
                        namespaceURI = "";
                    }

                    String qName;
                    if (prefix != null && prefix.trim().length() > 0) {
                        qName = prefix + ":" + localName;
                    } else {
                        qName = localName;
                    }

                    // make the attributes
                    AttributesImpl newAttrs = new AttributesImpl();
                    for (int i = 0; i < nAttrs; i++) {
                        Node attr = atts.item(i);
                        String attrName = attr.getNodeName();
                        String assignedAttrPrefix = null;

                        // only do non-namespace attributes
                        if (!(attrName.equals("xmlns") || attrName.startsWith("xmlns:"))) {
                            String attrPrefix;
                            String attrLocalName;
                            String attrNsURI;

                            if (attr.getLocalName() == null) {
                                // this is an attribute created with setAttribute instead of setAttributeNS
                                String[] prefixAndLocalName = getPrefixAndLocalName(attrName);
                                attrPrefix = prefixAndLocalName[0];
                                // the statement below causes the attribute to keep its prefix even if it is not
                                // bound to a namespace (to support pre-namespace XML).
                                assignedAttrPrefix = attrPrefix;
                                attrLocalName = prefixAndLocalName[1];
                                // note: if prefix is null, the attribute has no namespace (namespace defaulting
                                // does not apply to attributes)
                                if (attrPrefix != null) {
                                    attrNsURI = this.getNamespaceForPrefix(attrPrefix, (Element) node);
                                } else {
                                    attrNsURI = null;
                                }
                            } else {
                                attrLocalName = attr.getLocalName();
                                attrPrefix = attr.getPrefix();
                                attrNsURI = attr.getNamespaceURI();
                            }

                            if (attrNsURI != null) {
                                String declaredUri = this.currentElementInfo.findNamespaceURI(attrPrefix);
                                // if the prefix is null, or the prefix has not been declared, or conflicts with an in-scope binding
                                if (declaredUri == null || !declaredUri.equals(attrNsURI)) {
                                    String availablePrefix = this.currentElementInfo.findPrefix(attrNsURI);
                                    if (availablePrefix != null && !availablePrefix.equals("")) {
                                        assignedAttrPrefix = availablePrefix;
                                    } else {
                                        if (attrPrefix != null && declaredUri == null) {
                                            // prefix is not null and is not yet declared: declare it
                                            assignedAttrPrefix = attrPrefix;
                                            this.currentElementInfo.put(assignedAttrPrefix, attrNsURI);
                                        } else {
                                            // attribute has no prefix (which is not allowed for namespaced attributes) or
                                            // the prefix is already bound to something else: generate a new prefix
                                            this.newPrefixCounter++;
                                            assignedAttrPrefix = "NS" + this.newPrefixCounter;
                                            this.currentElementInfo.put(assignedAttrPrefix, attrNsURI);
                                        }
                                    }
                                } else {
                                    assignedAttrPrefix = attrPrefix;
                                }
                            }

                            String assignedAttrNsURI = attrNsURI != null ? attrNsURI : "";
                            String attrQName;
                            if (assignedAttrPrefix != null) {
                                attrQName = assignedAttrPrefix + ":" + attrLocalName;
                            } else {
                                attrQName = attrLocalName;
                            }
                            newAttrs.addAttribute(assignedAttrNsURI, attrLocalName, attrQName, "CDATA", attr.getNodeValue());
                        }
                    }

                    // add local namespace declaration and fire startPrefixMapping events
                    if (this.currentElementInfo.namespaceDeclarations != null
                        && this.currentElementInfo.namespaceDeclarations.size() > 0) {
                        Iterator<Map.Entry<String, String>> localNsDeclIt = this.currentElementInfo.namespaceDeclarations
                            .entrySet()
                            .iterator();
                        while (localNsDeclIt.hasNext()) {
                            Map.Entry<String, String> entry = localNsDeclIt.next();
                            String pr = entry.getKey();
                            String ns = entry.getValue();
                            // the following lines enable the creation of explicit xmlns attributes
                            // String pr1 = pr.equals("") ? "xmlns" : pr;
                            // String qn = pr.equals("") ? "xmlns" : "xmlns:" + pr;
                            // newAttrs.addAttribute("", pr1, qn, "CDATA", ns);
                            // System.out.println("starting prefix mapping for prefix " + pr + " for " + ns);
                            this.contentHandler.startPrefixMapping(pr, ns);
                        }
                    }

                    this.contentHandler.startElement(namespaceURI, localName, qName, newAttrs);

                    this.currentElementInfo.localName = localName;
                    this.currentElementInfo.namespaceURI = namespaceURI;
                    this.currentElementInfo.qName = qName;
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE: {
                    ProcessingInstruction pi = (ProcessingInstruction) node;
                    this.contentHandler.processingInstruction(pi.getNodeName(), pi.getData());
                }
                    break;
                case Node.CDATA_SECTION_NODE: {
                    if (this.lexicalHandler != null) {
                        this.lexicalHandler.startCDATA();
                    }

                    this.dispatchChars(node);

                    if (this.lexicalHandler != null) {
                        this.lexicalHandler.endCDATA();
                    }
                }
                    break;
                case Node.TEXT_NODE: {
                    this.dispatchChars(node);
                }
                    break;
                case Node.ENTITY_REFERENCE_NODE: {
                    EntityReference eref = (EntityReference) node;

                    if (this.lexicalHandler != null) {
                        this.lexicalHandler.startEntity(eref.getNodeName());
                    } else {
                        // warning("Can not output entity to a pure SAX ContentHandler");
                    }
                }
                    break;
                default:
            }
        }

        /**
         * Start the production of SAX events.
         *
         * <p>
         * Perform a pre-order traversal non-recursive style.
         *
         * <p>
         * Note that TreeWalker assumes that the subtree is intended to represent a complete (though not necessarily well-formed)
         * document and, during a traversal, startDocument and endDocument will always be issued to the SAX listener.
         *
         * @param pos Node in the tree where to start traversal
         */
        protected void stream(Node pos) throws SAXException {

            // Start document only if we're streaming a document
            boolean isDoc = pos.getNodeType() == Node.DOCUMENT_NODE;
            if (isDoc) {
                this.contentHandler.startDocument();
            }

            Node top = pos;
            while (null != pos) {
                this.startNode(pos);

                Node nextNode = pos.getFirstChild();
                while (null == nextNode) {
                    this.endNode(pos);

                    if (top.equals(pos)) {
                        break;
                    }

                    nextNode = pos.getNextSibling();
                    if (null == nextNode) {
                        pos = pos.getParentNode();

                        if (null == pos || top.equals(pos)) {
                            if (null != pos) {
                                this.endNode(pos);
                            }

                            break;
                        }
                    }
                }

                pos = nextNode;
            }

            if (isDoc) {
                this.contentHandler.endDocument();
            }
        }

        private final void dispatchChars(Node node) throws SAXException {
            final String data = ((Text) node).getData();
            if (data != null) {
                this.contentHandler.characters(data.toCharArray(), 0, data.length());
            }
        }

        public static class ElementInfo {

            public String localName;
            public String namespaceURI;
            public String qName;
            public Map<String, String> namespaceDeclarations;
            public ElementInfo parent;

            public ElementInfo(ElementInfo parent) {
                this.parent = parent;
            }

            /**
             * Finds a namespace declaration on this element or containing elements.
             */
            public String findNamespaceURI(String prefix) {
                if (this.namespaceDeclarations != null && this.namespaceDeclarations.size() != 0) {
                    String uri = this.namespaceDeclarations.get(prefix);
                    if (uri != null) {
                        return uri;
                    }
                }
                if (this.parent != null) {
                    return this.parent.findNamespaceURI(prefix);
                }
                return null;
            }

            /**
             * Finds a prefix declaration on this element or containing elements.
             */
            public String findPrefix(String namespaceURI) {
                if (this.namespaceDeclarations != null && this.namespaceDeclarations.size() != 0) {
                    String prefix = this.getPrefix(namespaceURI);
                    if (prefix != null) {
                        return prefix;
                    }
                }
                if (this.parent != null) {
                    return this.parent.findPrefix(namespaceURI);
                }
                return null;
            }

            /**
             * Finds a namespace URI declared on this element.
             */
            public String getNamespaceURI(String prefix) {
                if (this.namespaceDeclarations == null || this.namespaceDeclarations.size() == 0) {
                    return null;
                }

                return this.namespaceDeclarations.get(prefix);
            }

            /**
             * Finds a prefix declared on this element.
             */
            public String getPrefix(String namespaceURI) {
                if (this.namespaceDeclarations == null || this.namespaceDeclarations.size() == 0) {
                    return null;
                }
                // note: there could be more than one prefix for the same namespaceURI, but
                // we return the first found one.
                Iterator<Map.Entry<String, String>> it = this.namespaceDeclarations.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    if (entry.getValue().equals(namespaceURI)) {
                        return entry.getKey();
                    }
                }
                return null;
            }

            /**
             * Declare a new namespace prefix on this element, possibly overriding an existing one.
             */
            public void put(String prefix, String namespaceURI) {
                if (this.namespaceDeclarations == null) {
                    this.namespaceDeclarations = new HashMap<String, String>();
                }
                this.namespaceDeclarations.put(prefix, namespaceURI);
            }
        }
    }
}
