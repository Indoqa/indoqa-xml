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

import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import com.indoqa.xml.pipeline.SetupException;
import com.indoqa.xml.pipeline.caching.CacheKey;
import com.indoqa.xml.pipeline.caching.ParameterCacheKey;
import com.indoqa.xml.pipeline.component.CachingPipelineComponent;
import com.indoqa.xml.pipeline.sax.AbstractSAXSerializer;

public class XMLSerializer extends AbstractSAXSerializer implements CachingPipelineComponent {

    private static final SAXTransformerFactory SAX_TRANSFORMER_FACTORY = (SAXTransformerFactory) TransformerFactory.newInstance();

    private static final String EMPTY = "";

    private static final String YES = "yes";

    private static final String NO = "no";

    private static final String METHOD = "method";

    private static final String UTF_8 = "UTF-8";

    private static final String XML = "xml";

    private static final String HTML = "html";

    private static final String TEXT_XML = "text/xml";

    private static final String TEXT_HTML_UTF_8 = "text/html;charset=utf-8";

    private static final String XHTML_STRICT_DOCTYPE_PUBLIC = "-//W3C//DTD XHTML 1.0 Strict//EN";

    private static final String HTML_TRANSITIONAL_DOCTYPE_PUBLIC = "-//W3C//DTD HTML 4.01 Transitional//EN";

    private static final String XHTML_STRICT_DOCTYPE_SYSTEM = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

    private Properties format;

    private TransformerHandler transformerHandler;

    public XMLSerializer() {
        this(new Properties());
    }

    public XMLSerializer(Properties format) {
        super();

        if (format == null) {
            throw new SetupException("No format properites passed as argument.");
        }

        this.format = format;
    }

    public CacheKey constructCacheKey() {
        ParameterCacheKey parameterCacheKey = new ParameterCacheKey();
        for (Entry<Object, Object> property : this.format.entrySet()) {
            parameterCacheKey.addParameter(property.getKey().toString(), property.getValue().toString());
        }
        return parameterCacheKey;
    }

    public XMLSerializer setCDataSectionElements(String cdataSectionElements) {
        if (cdataSectionElements == null || EMPTY.equals(cdataSectionElements)) {
            throw new SetupException("A ... has to be passed as argument.");
        }

        this.format.put(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
        return this;
    }

    public XMLSerializer setDoctypePublic(String doctypePublic) {
        if (doctypePublic == null || EMPTY.equals(doctypePublic)) {
            throw new SetupException("A doctype-public has to be passed as argument.");
        }

        this.format.put(OutputKeys.DOCTYPE_PUBLIC, doctypePublic);
        return this;
    }

    public XMLSerializer setDoctypeSystem(String doctypeSystem) {
        if (doctypeSystem == null || EMPTY.equals(doctypeSystem)) {
            throw new SetupException("A doctype-system has to be passed as argument.");
        }

        this.format.put(OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
        return this;
    }

    public XMLSerializer setEncoding(String encoding) {
        if (encoding == null || EMPTY.equals(encoding)) {
            throw new SetupException("A encoding has to be passed as argument.");
        }

        this.format.put(OutputKeys.ENCODING, encoding);
        return this;
    }

    public void setFormat(Properties format) {
        this.format = format;
    }

    public XMLSerializer setIndent(boolean indent) {
        this.format.put(OutputKeys.INDENT, indent ? YES : NO);
        return this;
    }

    public XMLSerializer setMediaType(String mediaType) {
        if (mediaType == null || EMPTY.equals(mediaType)) {
            throw new SetupException("A media-type has to be passed as argument.");
        }

        this.format.put(OutputKeys.MEDIA_TYPE, mediaType);
        return this;
    }

    public XMLSerializer setMethod(String method) {
        if (method == null || EMPTY.equals(method)) {
            throw new SetupException("A method has to be passed as argument.");
        }

        this.format.put(OutputKeys.METHOD, method);
        return this;
    }

    public XMLSerializer setOmitXmlDeclaration(boolean omitXmlDeclration) {
        this.format.put(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclration ? YES : NO);
        return this;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.transformerHandler.setResult(new StreamResult(outputStream));
    }

    public XMLSerializer setStandAlone(boolean standalone) {
        this.format.put(OutputKeys.STANDALONE, standalone ? YES : NO);
        return this;
    }

    @Override
    public void setup(Map<String, Object> inputParameters) {
        try {
            this.transformerHandler = SAX_TRANSFORMER_FACTORY.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new SetupException("Can't setup transformer handler for the serializer.", e);
        }

        // set a default method because some transformer implementations run
        // into NPEs if it is missing
        if (!this.format.containsKey(METHOD)) {
            this.format.put(METHOD, XML);
        }

        this.transformerHandler.getTransformer().setOutputProperties(this.format);

        this.setContentHandler(this.transformerHandler);
    }

    public XMLSerializer setVersion(String version) {
        if (version == null || EMPTY.equals(version)) {
            throw new SetupException("A version has to be passed as argument.");
        }

        this.format.put(OutputKeys.VERSION, version);
        return this;
    }

    protected Properties getFormat() {
        return this.format;
    }

    public static XMLSerializer createXHTMLSerializer() {
        XMLSerializer serializer = new XMLSerializer();

        serializer.setContentType(TEXT_HTML_UTF_8);
        serializer.setDoctypePublic(XHTML_STRICT_DOCTYPE_PUBLIC);
        serializer.setDoctypeSystem(XHTML_STRICT_DOCTYPE_SYSTEM);
        serializer.setEncoding(UTF_8);
        serializer.setMethod(XML);

        return serializer;
    }

    public static XMLSerializer createHTML4Serializer() {
        XMLSerializer serializer = new XMLSerializer();

        serializer.setContentType(TEXT_HTML_UTF_8);
        serializer.setDoctypePublic(HTML_TRANSITIONAL_DOCTYPE_PUBLIC);
        serializer.setEncoding(UTF_8);
        serializer.setMethod(HTML);

        return serializer;
    }

    public static XMLSerializer createXMLSerializer() {
        XMLSerializer serializer = new XMLSerializer();

        serializer.setContentType(TEXT_XML);
        serializer.setEncoding(UTF_8);
        serializer.setMethod(XML);

        return serializer;
    }
}
