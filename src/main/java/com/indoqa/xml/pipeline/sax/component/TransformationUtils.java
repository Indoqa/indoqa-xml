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
package com.indoqa.xml.pipeline.sax.component;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import com.indoqa.xml.pipeline.NonCachingPipeline;
import com.indoqa.xml.pipeline.Pipeline;
import com.indoqa.xml.pipeline.sax.SAXPipelineComponent;

/**
 * This class contains utility methods to transform an XML string by using XSLT. It hides all the details of the Java TRaX API by using
 * SAX pipeline components that are linked to each other. This means that the SAX events stream through the pipeline and are serialized
 * at the end.
 *
 * NOTE: Exception handling needs to be discussed and will probably change.
 */
public final class TransformationUtils {

    private TransformationUtils() {
        // hide utility class constructor
    }

    /**
     * Transform an XML string and write the result into an {@link OutputStream}.
     *
     * @param xmlString The String to be transformed.
     * @param xsltParameters Parameters that are available in all XSLT stylesheets.
     * @param outputProperties XML output properties. See {@link "http://www.w3.org/TR/xslt#output"}.
     * @param outputStream The result is written into this {@link OutputStream}.
     * @param xsltUrls The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     *
     * @throws Exception
     */
    public static void transform(String xmlString, Map<String, Object> xsltParameters, Properties outputProperties,
            OutputStream outputStream, URL... xsltUrls) throws Exception {
        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();

        pipeline.addComponent(new XMLGenerator(xmlString));
        for (URL xsltUrl : xsltUrls) {
            XSLTTransformer xsltTransformer = new XSLTTransformer(xsltUrl);
            xsltTransformer.setParameters(xsltParameters);
            pipeline.addComponent(xsltTransformer);
        }

        if (outputProperties == null) {
            outputProperties = new Properties();
        }
        pipeline.addComponent(new XMLSerializer(outputProperties));

        pipeline.setup(outputStream, null);
        pipeline.execute();
    }

    /**
     * Transform an XML string by using XSLT. This method allows defining the output properties and also allows passing parameters to
     * the stylesheets.
     *
     * @param xmlString The String to be transformed.
     * @param xsltParameters Parameters that are available in all XSLT stylesheets.
     * @param outputProperties XML output properties. See {@link "http://www.w3.org/TR/xslt#output"}.
     * @param xsltUrls The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @return The transformed result as {@link String}.
     * @throws Exception
     */
    public static String transform(String xmlString, Map<String, Object> xsltParameters, Properties outputProperties, URL... xsltUrls)
            throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        transform(xmlString, xsltParameters, outputProperties, byteArrayOutputStream, xsltUrls);
        return new String(byteArrayOutputStream.toByteArray(), UTF_8);
    }

    /**
     * Transform an XML string by using XSLT. This method also allows passing parameters to the stylesheets.
     *
     * @param xmlString The String to be transformed.
     * @param xsltParameters Parameters that are available in all XSLT stylesheets.
     * @param xsltUrls The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @return The transformed result as {@link String}.
     * @throws Exception
     */
    public static String transform(String xmlString, Map<String, Object> xsltParameters, URL... xsltUrls) throws Exception {
        return transform(xmlString, xsltParameters, (Properties) null, xsltUrls);
    }

    /**
     * Transform an XML string by using XSLT and write the result into an {@link OutputStream}. This method also allows passing
     * parameters to the stylesheets.
     *
     * @param xmlString The String to be transformed.
     * @param outputStream The result is written into this {@link OutputStream}.
     * @param xsltParameters Parameters that are available in all XSLT stylesheets.
     * @param xsltURLs The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @throws Exception
     */
    public static void transform(String xmlString, OutputStream outputStream, Map<String, Object> xsltParameters, URL... xsltURLs)
            throws Exception {
        transform(xmlString, xsltParameters, (Properties) null, outputStream, xsltURLs);
    }

    /**
     * Transform an XML string by using XSLT and write the result into an {@link OutputStream}. This method allows defining the output
     * properties.
     *
     * @param xmlString The String to be transformed.
     * @param outputStream The result is written into this {@link OutputStream}.
     * @param outputProperties XML output properties. See {@link "http://www.w3.org/TR/xslt#output"}.
     * @param xsltURLs The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @throws Exception
     */
    public static void transform(String xmlString, OutputStream outputStream, Properties outputProperties, URL... xsltURLs)
            throws Exception {
        transform(xmlString, (Map<String, Object>) null, outputProperties, outputStream, xsltURLs);
    }

    /**
     * Transform an XML string by using XSLT and write the result into an {@link OutputStream}.
     *
     * @param xmlString The String to be transformed.
     * @param outputStream The result is written into this {@link OutputStream}.
     * @param xsltURLs The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @throws Exception
     */
    public static void transform(String xmlString, OutputStream outputStream, URL... xsltURLs) throws Exception {
        transform(xmlString, (Map<String, Object>) null, (Properties) null, outputStream, xsltURLs);
    }

    /**
     * Transform an XML string by using XSLT. This method allows defining the output properties.
     *
     * @param xmlString The String to be transformed.
     * @param outputProperties XML output properties. See {@link "http://www.w3.org/TR/xslt#output"}.
     * @param xsltUrls The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @return The transformed result as {@link String}.
     * @throws Exception
     */
    public static String transform(String xmlString, Properties outputProperties, URL... xsltUrls) throws Exception {
        return transform(xmlString, (Map<String, Object>) null, outputProperties, xsltUrls);
    }

    /**
     * Transform an XML string by using XSLT.
     *
     * @param xmlString The String to be transformed.
     * @param xsltUrls The {@link URL}s of all XSLT stylesheets that are used in the order they are passed to transform the passed XML
     *            string.
     * @return The transformed result as {@link String}.
     *
     * @throws Exception
     */
    public static String transform(String xmlString, URL... xsltUrls) throws Exception {
        return transform(xmlString, (Map<String, Object>) null, (Properties) null, xsltUrls);
    }
}
