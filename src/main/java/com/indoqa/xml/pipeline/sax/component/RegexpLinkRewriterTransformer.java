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
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexpLinkRewriterTransformer extends AbstractLinkRewriterTransformer {

    private static final String REGEXP_PREFIX = "regexp";

    private final transient Map<Pattern, String> matchingMap = new HashMap<Pattern, String>();

    public RegexpLinkRewriterTransformer() {
        super();
    }

    public RegexpLinkRewriterTransformer(final Map<String, String> regexpMatch) {

        super();

        init(regexpMatch);
    }

    @Override
    public void setup(final Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        super.setup(parameters);

        final Map<String, String> regexpMatch = new HashMap<String, String>();

        String[] split;
        for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getKey().startsWith(REGEXP_PREFIX)) {
                split = ((String) parameter.getValue()).split(" ");
                if (split.length == 2) {
                    regexpMatch.put(split[0], split[1]);
                } else {
                    LOG.error("Invalid regexp as parameter, ignoring: " + parameter.getValue());
                }
            }
        }

        this.init(regexpMatch);
    }

    private void init(final Map<String, String> regexpMatch) {
        if (regexpMatch != null && !regexpMatch.isEmpty()) {
            for (Map.Entry<String, String> entry : regexpMatch.entrySet()) {
                try {
                    this.matchingMap.put(Pattern.compile(entry.getKey()), entry.getValue());
                } catch (PatternSyntaxException e) {
                    LOG.error("Could not compile regular expression '" + entry.getKey() + "'", e);
                }
            }
        }
    }

    @Override
    protected String rewrite(final String elementNS, final String elementName, final String attributeNS, final String attributeName,
            final String link) throws LinkRewriterException {

        String result = link;

        Map.Entry<Pattern, String> entry;
        Matcher matcher;
        for (Iterator<Map.Entry<Pattern, String>> itor = matchingMap.entrySet().iterator(); itor.hasNext() && result.equals(link);) {

            entry = itor.next();
            matcher = entry.getKey().matcher(link);
            result = matcher.replaceAll(entry.getValue());
        }
        if (LOG.isDebugEnabled() && link.equals(result)) {
            LOG.debug("No match found for '" + link + "'");
        }

        return result;
    }
}
