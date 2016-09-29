/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class IIDMDynamicDatabaseFactory implements DynamicDatabaseClientFactory {

    @Override
    public DynamicDatabaseClient create(boolean cache) {
        DynamicDatabaseClient client = new DdbDtaImpExp(DdbConfig.load());
        if (cache) {
            client = new DynamicDatabaseCacheClient(client);
        }
        return client;
    }

}
