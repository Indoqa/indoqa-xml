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
package com.indoqa.xml.pipeline.sax.component;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import com.indoqa.xml.pipeline.CachingPipeline;
import com.indoqa.xml.pipeline.caching.Cache;
import com.indoqa.xml.pipeline.caching.SimpleCache;
import com.indoqa.xml.pipeline.sax.SAXPipelineComponent;
import com.indoqa.xml.pipeline.util.URLConnectionUtils;

public class XSLTTransformerTest {

    private static void cacheInternal(final CachingPipeline<SAXPipelineComponent> pipeline1,
            final CachingPipeline<SAXPipelineComponent> pipeline2) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline1.setup(baos);
        pipeline1.execute();
        final String result1 = new String(baos.toByteArray(), "UTF-8");

        baos = new ByteArrayOutputStream();
        pipeline2.setup(baos);
        pipeline2.execute();
        final String result2 = new String(baos.toByteArray(), "UTF-8");

        assertFalse("Pipeline caching is not working as expected", result1.equals(result2));
    }

    @Test
    public void cache() throws Exception {
        Cache cache = new SimpleCache();
        cacheInternal(this.makeURLPipeline(cache, "value1"), this.makeURLPipeline(cache, "value2"));

        cache = new SimpleCache();
        cacheInternal(this.makeSourcePipeline(cache, "value1"), this.makeSourcePipeline(cache, "value2"));
    }

    private CachingPipeline<SAXPipelineComponent> makePipelineInternal(final Cache cache, final String paramValue,
            final XSLTTransformer xslt) {
        final CachingPipeline<SAXPipelineComponent> pipeline = new CachingPipeline<SAXPipelineComponent>();
        pipeline.setCache(cache);

        pipeline.addComponent(new XMLGenerator(this.getClass().getResource("/test.xml")));

        xslt.setParameters(Collections.singletonMap("myParam", paramValue));
        pipeline.addComponent(xslt);

        pipeline.addComponent(XMLSerializer.createXMLSerializer());

        return pipeline;
    }

    private CachingPipeline<SAXPipelineComponent> makeSourcePipeline(final Cache cache, final String paramValue) {
        return this.makePipelineInternal(
            cache,
            paramValue,
            new XSLTTransformer(
                new StreamSource(this.getClass().getResourceAsStream("/test.xslt")),
                URLConnectionUtils.getLastModified(this.getClass().getResource("/test.xslt"))));
    }

    private CachingPipeline<SAXPipelineComponent> makeURLPipeline(final Cache cache, final String paramValue) {
        return this.makePipelineInternal(cache, paramValue, new XSLTTransformer(this.getClass().getResource("/test.xslt")));
    }
}
