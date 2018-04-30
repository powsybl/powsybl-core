/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.utils.gzip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 */
public class WriterInterceptorGzipCli implements WriterInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriterInterceptorGzipCli.class);

    @Override
    public void aroundWriteTo(WriterInterceptorContext interceptorContext) throws IOException {
        Object encoding = interceptorContext.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        Object acceptEncoding = interceptorContext.getHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING);

        LOGGER.trace("Encoding: {}", encoding);
        LOGGER.trace("Accept-Encoding: {}", acceptEncoding);

        if ((encoding == null || !encoding.toString().contains("gzip")) && (acceptEncoding == null || !acceptEncoding.toString().contains("gzip"))) {
            OutputStream os = interceptorContext.getOutputStream();
            interceptorContext.setOutputStream(os);
            interceptorContext.proceed();
        } else {
            GZIPOutputStream os = new GZIPOutputStream(interceptorContext.getOutputStream());
            interceptorContext.getHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, "gzip");

            interceptorContext.setOutputStream(os);
            interceptorContext.proceed();
            os.finish();
        }
    }
}
