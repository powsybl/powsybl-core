/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.rules.RulesDbClientFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RulesDbClientFactoryImpl implements RulesDbClientFactory {

    @Override
    public RulesDbClient create(String rulesDbName) {
        return new RulesDbClientImpl(HistoDbConfig.load("ruledb"));
    }

}
