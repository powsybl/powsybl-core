/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractNetworkModification implements NetworkModification {

    protected Reporter reporter;

    protected AbstractNetworkModification(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void apply(Network network) {
        apply(network, false, LocalComputationManager.getDefault());
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network, false, computationManager);
    }

    @Override
    public void apply(Network network, boolean throwException) {
        apply(network, throwException, LocalComputationManager.getDefault());
    }
}
