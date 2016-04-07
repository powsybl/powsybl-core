/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.modules.mock;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LoadFlowMockFactoryImpl implements LoadFlowFactory {
    @Override
    public LoadFlow create(Network network, ComputationManager computationManager, int priority) {
        return new LoadFlowMock();
    }
}
