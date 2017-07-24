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

import static org.junit.Assert.*;

import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.junit.Test;

import com.indoqa.xml.pipeline.SetupException;
import com.indoqa.xml.pipeline.sax.component.XMLSerializer;

public class XMLSerializerTest {

    @Test
    public void outputPropertiesAvailable() {
        XMLSerializer serializer = new XMLSerializer();
        assertNotNull(serializer.getFormat());
    }

    @Test(expected = SetupException.class)
    public void outputPropertiesAvailable2() {
        new XMLSerializer(null);
    }

    @Test
    public void outputPropertiesSpecificMethods() {
        XMLSerializer serializer = new XMLSerializer();
        Properties format = serializer.getFormat();
        assertNotNull(format);
        assertEquals(0, format.size());

        serializer.setCDataSectionElements("p");
        assertEquals(1, format.size());
        assertEquals("p", format.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));

        serializer.setMediaType("media");
        assertEquals(2, format.size());
        assertEquals("media", format.getProperty(OutputKeys.MEDIA_TYPE));

        serializer.setOmitXmlDeclaration(true);
        assertEquals(3, format.size());
        assertEquals("yes", format.getProperty(OutputKeys.OMIT_XML_DECLARATION));

        serializer.setDoctypePublic("foo");
        assertEquals(4, format.size());
        assertEquals("foo", format.getProperty(OutputKeys.DOCTYPE_PUBLIC));

        serializer.setDoctypeSystem("bar");
        assertEquals(5, format.size());
        assertEquals("bar", format.getProperty(OutputKeys.DOCTYPE_SYSTEM));

        serializer.setIndent(true);
        assertEquals(6, format.size());
        assertEquals("yes", format.getProperty(OutputKeys.INDENT));

        serializer.setEncoding("utf-8");
        assertEquals(7, format.size());
        assertEquals("utf-8", format.getProperty(OutputKeys.ENCODING));

        serializer.setVersion("1.0");
        assertEquals(8, format.size());
        assertEquals("1.0", format.getProperty(OutputKeys.VERSION));

        serializer.setStandAlone(true);
        assertEquals(9, format.size());
        assertEquals("yes", format.getProperty(OutputKeys.STANDALONE));

        serializer.setMethod("abc");
        assertEquals(10, format.size());
        assertEquals("abc", format.getProperty(OutputKeys.METHOD));
    }
}
