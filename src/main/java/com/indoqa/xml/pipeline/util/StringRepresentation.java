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
package com.indoqa.xml.pipeline.util;

/**
 * Helper class to create the {@link String} for {@link Object#toString()}.
 */
public final class StringRepresentation {

    public static String buildString(final Object instance, final String... moreOutputStrs) {
        final StringBuilder builder = new StringBuilder();
        builder.append(instance.getClass().getSimpleName());
        builder.append("(hashCode=").append(System.identityHashCode(instance));

        if (moreOutputStrs != null) {
            for (String outputString : moreOutputStrs) {
                builder.append(" ").append(outputString);
            }
        }
        builder.append(")");

        return builder.toString();
    }

    /**
     * Private constructor, suggested for classes with static methods only.
     */
    private StringRepresentation() {
    }
}
