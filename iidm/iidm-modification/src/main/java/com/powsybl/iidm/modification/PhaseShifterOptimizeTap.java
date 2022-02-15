/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseShifterOptimizeTap implements NetworkModification {

    private final String phaseShifterId;

    public PhaseShifterOptimizeTap(String phaseShifterId) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        new LoadFlowBasedPhaseShifterOptimizer(computationManager)
                .findMaximalFlowTap(network, phaseShifterId);
    }
}
