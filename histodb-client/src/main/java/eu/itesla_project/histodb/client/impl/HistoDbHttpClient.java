/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import eu.itesla_project.modules.histo.cache.HistoDbCache;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface HistoDbHttpClient extends AutoCloseable {

    HistoDbCache getCache();

    InputStream getHttpRequest(HistoDbUrl url) throws IOException;

    InputStream postHttpRequest(HistoDbUrl url, byte[] content) throws IOException;

    InputStream deleteHttpRequest(HistoDbUrl url) throws IOException;

}
