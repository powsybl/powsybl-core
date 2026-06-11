/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PhaseShifterOptimizeTap extends AbstractNetworkModification {

    private final String phaseShifterId;

    public PhaseShifterOptimizeTap(String phaseShifterId) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
    }

    @Override
    public String getName() {
        return "PhaseShifterOptimizeTap";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        try {
            new LoadFlowBasedPhaseShifterOptimizer(computationManager)
                    .findMaximalFlowTap(network, phaseShifterId);
        } catch (PowsyblException powsyblException) {
            logOrThrow(throwException, "Unable to find maximal flow tap");
        }

    }
}
