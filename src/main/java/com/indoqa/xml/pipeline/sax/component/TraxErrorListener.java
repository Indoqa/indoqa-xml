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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;

/**
 * A smart error listener for <code>javax.xml.tranform</code> that does its best to provide useful error messages.
 * 
 * @version $Id: TraxErrorListener.java 1155368 2011-08-09 13:56:40Z ilgrosso $
 */
public class TraxErrorListener implements ErrorListener {

    /** The exception we had from error() or fatalError() */
    private TransformerException exception;
    private final Logger log;
    private String uri;
    /** The exception we had from warning() */
    private TransformerException warningEx;

    public TraxErrorListener(Logger log, String uri) {
        this.log = log;
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.transform.ErrorListener#error(javax.xml.transform.TransformerException)
     */
    public void error(TransformerException ex) throws TransformerException {
        // If we had a warning previoulsy, and the current exception has no cause, then use the warning.
        // This is how Xalan behaves on <xsl:message terminate="yes">: it first issues a warning with all
        // the useful information, then a useless "stylesheed directed termination" error.
        if (this.warningEx != null && ex.getCause() == null) {
            ex = this.warningEx;
        }
        this.warningEx = null;

        // Keep the exception for later use.
        this.exception = ex;
        // and rethrow it
        throw ex;
    }

    public void fatalError(TransformerException ex) throws TransformerException {
        if (this.warningEx != null && ex.getCause() == null) {
            ex = this.warningEx;
        }
        this.warningEx = null;

        this.exception = ex;
        throw ex;
    }

    /**
     * Get the exception that was catched by this listener, if any.
     * 
     * @return the exception
     */
    public Throwable getThrowable() {
        if (this.exception == null) {
            return null;
        }

        // Location loc = LocationUtils.getLocation(this.exception);
        // if (LocationUtils.isKnown(loc)) {
        // // Has a location: don't loose this precious information!
        // return this.exception;
        // }

        // No location: if it's just a wrapper, consider only the wrapped exception
        if (this.exception.getCause() != null) {
            return this.exception.getCause();
        }

        // That's the actual exception!
        return this.exception;
    }

    public void warning(TransformerException ex) throws TransformerException {
        // TODO: We may want here to allow some special formatting of the messages, such as
        // "DEBUG:A debug message" or "INFO:Transforming <foo> in mode 'bar'" to use the different
        // log levels. This can include also deprecation logs for system-defined stylesheets
        // using "DEPRECATED:WARN:Styling 'foo' is replaced by 'bar'".

        if (this.log.isWarnEnabled()) {
            // Location loc = LocationUtils.getLocation(ex);
            // getLogger().warn(ex.getMessage() + " at " + (loc == null ? this.uri : loc.toString()));
            this.log.warn(ex.getMessage() + " at " + this.uri);
        }

        // Keep the warning (see below)
        this.warningEx = ex;
    }
}
