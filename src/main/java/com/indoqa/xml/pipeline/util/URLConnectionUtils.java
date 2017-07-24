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

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class URLConnectionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(URLConnectionUtils.class);

    /**
     * Private constructor, suggested for classes with static methods only.
     */
    private URLConnectionUtils() {
        // hide utility class constructor
    }

    /**
     * Close a {@link URLConnection} quietly and take care of all the exception handling.
     *
     * @param urlConnection {@link URLConnection} to be closed.
     */
    public static void closeQuietly(final URLConnection urlConnection) {
        if (urlConnection == null) {
            return;
        }

        if (urlConnection.getDoInput()) {
            InputStream inputStream = null;
            try {
                inputStream = urlConnection.getInputStream();
            } catch (Exception e) {
                LOG.warn("Can't close input stream from " + urlConnection.getURL(), e);
            } finally {
                close(urlConnection, inputStream);
            }
        }

        if (urlConnection.getDoOutput()) {
            OutputStream outputStream = null;
            try {
                outputStream = urlConnection.getOutputStream();
            } catch (Exception e) {
                LOG.warn("Can't close output stream to " + urlConnection.getURL(), e);
            } finally {
                close(urlConnection, outputStream);
            }
        }
    }

    /**
     * Find the actual last modification timestamp for the given URL, reverting to java.io.File#lastModified() when applicable.
     *
     * @param url URL to be examined for last modified
     * @return File.lastModified() when applicable or URLconnection.getLastModified()
     */
    public static long getLastModified(final URL url) {
        if (url == null) {
            throw new IllegalArgumentException("URL source cannot be null");
        }

        long lastModified = -1;

        if ("file".equals(url.getProtocol())) {
            try {
                lastModified = new File(url.toURI()).lastModified();
            } catch (URISyntaxException e) {
                LOG.error("Error while opening {} as file", url, e);
            }
        } else {
            URLConnection connection = null;
            try {
                connection = url.openConnection();
                lastModified = connection.getLastModified();
            } catch (IOException e) {
                LOG.error("Error while connecting to {}", url, e);
            } finally {
                if (connection != null) {
                    URLConnectionUtils.closeQuietly(connection);
                }
            }
        }

        return lastModified;
    }

    private static void close(final URLConnection urlConnection, final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.warn("Can't close stream (" + closeable.getClass().getSimpleName() + "): " + urlConnection.getURL(), e);
            }
        }
    }
}
