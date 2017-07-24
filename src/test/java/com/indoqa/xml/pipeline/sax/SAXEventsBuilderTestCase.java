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

import static com.indoqa.xml.pipeline.sax.SAXEventsBuilder.newDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class SAXEventsBuilderTestCase {

    @Test
    public void pomAlike() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SAXTransformerFactory transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
        transformerHandler.setResult(new StreamResult(baos));

        newDocument(transformerHandler)
            .start("project")
            .start("modelVersion")
            .body("4.0.0")
            .end()
            .start("groupId")
            .body("com.indoqa.xml.pipeline.sax.sax")
            .end()
            .start("artifactId")
            .body("cocoon-sax")
            .end()
            .start("version")
            .body("3.0.0-beta-1")
            .end()
            .start("dependencies")
            .start("dependency")
            .start("groupId")
            .body("com.indoqa.xml.pipeline.sax.pipeline")
            .end()
            .start("artifactId")
            .body("cocoon-pipeline")
            .end()
            .end()
            .start("dependency")
            .start("groupId")
            .body("com.indoqa.xml.pipeline.sax")
            .end()
            .start("artifactId")
            .body("cocoon-xml")
            .end()
            .end()
            .end()
            .end()
            .endDocument();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><modelVersion>4.0.0</modelVersion><groupId>com.indoqa.xml.pipeline.sax.sax</groupId><artifactId>cocoon-sax</artifactId><version>3.0.0-beta-1</version><dependencies><dependency><groupId>com.indoqa.xml.pipeline.sax.pipeline</groupId><artifactId>cocoon-pipeline</artifactId></dependency><dependency><groupId>com.indoqa.xml.pipeline.sax</groupId><artifactId>cocoon-xml</artifactId></dependency></dependencies></project>";
        String actual = new String(baos.toByteArray(), UTF_8);

        Diff diff = new Diff(expected, actual);
        assertTrue("SAX wrapper didn't work as expected " + diff, diff.identical());
    }
}
