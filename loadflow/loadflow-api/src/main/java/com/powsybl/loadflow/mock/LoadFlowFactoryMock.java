/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.mock;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;

/**
 * @author Quinary <itesla@quinary.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LoadFlowFactoryMock implements LoadFlowFactory {
    @Override
    public LoadFlow create(Network network, ComputationManager computationManager, int priority) {
        return new LoadFlowMock();
    }
}
