/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.utils.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 */
@Provider
public class ReaderInterceptorGzip implements ReaderInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderInterceptorGzip.class);

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext interceptorContext) throws IOException {
        String encoding = interceptorContext.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        LOGGER.trace("Encoding: {}", encoding);
        final InputStream inputStream = interceptorContext.getInputStream();

        if (encoding == null || !encoding.contains("gzip")) {
            interceptorContext.setInputStream(inputStream);
            return interceptorContext.proceed();
        } else {
            interceptorContext.setInputStream(new GZIPInputStream(new InputStream() {
                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }
            }));
            return interceptorContext.proceed();
        }
    }
}
