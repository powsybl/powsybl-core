/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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

/**
 * @author Hamou AMROUN {@literal <hamou.amroun at rte-france.com>}
 */
public class PhaseShifterShiftTap extends AbstractNetworkModification {

    private final String phaseShifterId;
    private final int tapDelta;

    public PhaseShifterShiftTap(String phaseShifterId, int tapDelta) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
        this.tapDelta = tapDelta;
    }

    @Override
    public String getName() {
        return "PhaseShifterShiftTap";
    }

    public int getTapDelta() {
        return tapDelta;
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
        PhaseTapChanger phaseTapChanger = phaseShifter.getPhaseTapChanger();
        if (phaseTapChanger == null) {
            ModificationLogs.logOrThrow(throwException, "Transformer '" + phaseShifterId + "' is not a phase shifter");
            return;
        }
        adjustTapPosition(phaseTapChanger);
        phaseTapChanger.setRegulating(false);
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
    }

    private void adjustTapPosition(PhaseTapChanger phaseTapChanger) {
        phaseTapChanger.setTapPosition(Math.min(Math.max(phaseTapChanger.getTapPosition() + tapDelta,
                phaseTapChanger.getLowTapPosition()), phaseTapChanger.getHighTapPosition()));
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null || !phaseShifter.hasPhaseTapChanger()) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else {
            PhaseTapChanger phaseTapChanger = phaseShifter.getPhaseTapChanger();
            int tapPosition = Math.min(Math.max(phaseTapChanger.getTapPosition() + tapDelta,
                phaseTapChanger.getLowTapPosition()), phaseTapChanger.getHighTapPosition());
            if (areValuesEqual(tapPosition, phaseTapChanger.getTapPosition(), false)
                && !phaseTapChanger.isRegulating()
                && phaseTapChanger.getRegulationMode() == PhaseTapChanger.RegulationMode.FIXED_TAP) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        }
        return impact;
    }
}
