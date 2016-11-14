/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl.util;

import eu.itesla_project.histodb.client.impl.HistoDbHttpClient;
import eu.itesla_project.histodb.client.impl.HistoDbUrl;
import eu.itesla_project.modules.histo.cache.HistoDbCache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * In the case of an MPI workflow we only want to work with cached data, so
 * we use a dummy http client that throws an exception in case of data not
 * in the cache.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DummyHistoDbHttpClient implements HistoDbHttpClient {

    private final HistoDbCache cache;

    DummyHistoDbHttpClient(HistoDbCache cache) {
        Objects.requireNonNull(cache);
        this.cache = cache;
    }

    @Override
    public HistoDbCache getCache() {
        return cache;
    }

    private InputStream getData(String url) throws IOException {
        InputStream is = cache.getData(url);
        if (is == null) {
            throw new RuntimeException("Query '" + url + "' not cached");
        }
        return is;
    }

    @Override
    public InputStream deleteHttpRequest(HistoDbUrl url) throws IOException {
        return getData(url.format());
    }

    @Override
    public InputStream getHttpRequest(HistoDbUrl url) throws IOException {
        return getData(url.format());
    }

    @Override
    public InputStream postHttpRequest(HistoDbUrl url, byte[] content) throws IOException {
        return getData(url.format());
    }

    @Override
    public void close() throws Exception {
    }

}
