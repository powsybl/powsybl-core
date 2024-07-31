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
import com.powsybl.iidm.network.PhaseTapChanger;

import java.util.Objects;

public class PhaseShifterSetAsFixedTap extends AbstractPhaseShifterModification {

    private final int tapPosition;

    public PhaseShifterSetAsFixedTap(String phaseShifterId, int tapPosition) {
        super(phaseShifterId);
        this.tapPosition = tapPosition;
    }

    @Override
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                        ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        Objects.requireNonNull(network);
        PhaseTapChanger phaseTapChanger = getPhaseTapChanger(network);
        phaseTapChanger.setTapPosition(tapPosition, dryRun);
        phaseTapChanger.setRegulating(false, dryRun);
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP, dryRun);
    }

    @Override
    public String getName() {
        return "PhaseShifterSetAsFixedTap";
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
    }
}
