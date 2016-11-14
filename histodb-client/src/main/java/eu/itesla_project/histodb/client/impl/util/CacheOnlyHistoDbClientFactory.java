/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl.util;

import eu.itesla_project.histodb.client.impl.HistoDbCacheImpl;
import eu.itesla_project.histodb.client.impl.HistoDbClientImpl;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.histo.HistoDbClientFactory;
import eu.itesla_project.histodb.client.impl.HistoDbConfig;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CacheOnlyHistoDbClientFactory implements HistoDbClientFactory {

    @Override
    public HistoDbClient create(boolean cache) {
        return new HistoDbClientImpl(HistoDbConfig.load(), new DummyHistoDbHttpClient(new HistoDbCacheImpl()));
    }

}
