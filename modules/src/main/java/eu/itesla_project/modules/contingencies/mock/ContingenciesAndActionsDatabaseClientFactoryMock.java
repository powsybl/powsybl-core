/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.mock;

import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ContingenciesAndActionsDatabaseClientFactoryMock implements ContingenciesAndActionsDatabaseClientFactory {
    @Override
    public ContingenciesAndActionsDatabaseClient create() {
        return new ContingenciesAndActionsDatabaseClientMock();
    }
}
