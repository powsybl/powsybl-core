/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PhaseShifterOptimizeTap extends AbstractPhaseShifterModification {

    public PhaseShifterOptimizeTap(String phaseShifterId) {
        super(phaseShifterId);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        new LoadFlowBasedPhaseShifterOptimizer(computationManager)
                .findMaximalFlowTap(network, phaseShifterId);
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        // TODO: should we run the loadflow or not? If not, delete this method
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "AbstractPhaseShifterModification",
                String.format("Transformer %s not found", phaseShifterId));
        } else if (!phaseShifter.hasPhaseTapChanger()) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "AbstractPhaseShifterModification",
                String.format("Transformer %s is not a phase shifter", phaseShifterId));
        }
        return dryRunConclusive;
    }
}
