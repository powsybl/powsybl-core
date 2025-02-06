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
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

public class PhaseShifterSetAsFixedTap extends AbstractNetworkModification {

    private final String phaseShifterId;
    private final int tapPosition;

    public PhaseShifterSetAsFixedTap(String phaseShifterId, int tapPosition) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
        this.tapPosition = tapPosition;
    }

    @Override
    public String getName() {
        return "PhaseShifterSetAsFixedTap";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Objects.requireNonNull(network);
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            ModificationLogs.logOrThrow(throwException, "Transformer '" + phaseShifterId + "' not found");
            return;
        }
        if (!phaseShifter.hasPhaseTapChanger()) {
            ModificationLogs.logOrThrow(throwException, "Transformer '" + phaseShifterId + "' is not a phase shifter");
            return;
        }
        phaseShifter.getPhaseTapChanger().setTapPosition(tapPosition);
        phaseShifter.getPhaseTapChanger().setRegulating(false);
        phaseShifter.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null || !phaseShifter.hasPhaseTapChanger()
            || tapPosition > phaseShifter.getPhaseTapChanger().getHighTapPosition()
            || tapPosition < phaseShifter.getPhaseTapChanger().getLowTapPosition()) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (areValuesEqual(tapPosition, phaseShifter.getPhaseTapChanger().getTapPosition(), false)
            && !phaseShifter.getPhaseTapChanger().isRegulating()
            && phaseShifter.getPhaseTapChanger().getRegulationMode() == PhaseTapChanger.RegulationMode.FIXED_TAP) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }
}
