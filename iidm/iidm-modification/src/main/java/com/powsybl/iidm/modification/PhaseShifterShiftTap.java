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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;

import java.util.Objects;

/**
 * @author Hamou AMROUN {@literal <hamou.amroun at rte-france.com>}
 */
public class PhaseShifterShiftTap extends AbstractPhaseShifterModification {

    private final int tapDelta;

    public PhaseShifterShiftTap(String phaseShifterId, int tapDelta) {
        super(phaseShifterId);
        this.tapDelta = tapDelta;
    }

    public int getTapDelta() {
        return tapDelta;
    }

    @Override
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, boolean dryRun, ReportNode reportNode) {
        Objects.requireNonNull(network);
        PhaseTapChanger phaseTapChanger = getPhaseTapChanger(network);
        adjustTapPosition(phaseTapChanger, dryRun);
        phaseTapChanger.setRegulating(false, dryRun);
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP, dryRun);
    }

    @Override
    public String getName() {
        return "PhaseShifterShiftTap";
    }

    private void adjustTapPosition(PhaseTapChanger phaseTapChanger, boolean dryRun) {
        phaseTapChanger.setTapPosition(Math.min(Math.max(phaseTapChanger.getTapPosition() + tapDelta,
                phaseTapChanger.getLowTapPosition()), phaseTapChanger.getHighTapPosition()), dryRun);
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
