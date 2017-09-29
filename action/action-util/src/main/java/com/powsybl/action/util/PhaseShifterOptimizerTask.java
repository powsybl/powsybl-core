/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.util;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.contingency.tasks.ModificationTask;
import eu.itesla_project.iidm.network.Network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseShifterOptimizerTask implements ModificationTask {

    private final String phaseShifterId;

    public PhaseShifterOptimizerTask(String phaseShifterId) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
    }

    @Override
    public void modify(Network network, ComputationManager computationManager) {
        new LoadFlowBasedPhaseShifterOptimizer(computationManager)
                        .findMaximalFlowTap(network, phaseShifterId);
    }
}
