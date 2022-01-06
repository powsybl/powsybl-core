/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

/**
 * @author Hamou AMROUN <hamou.amroun at rte-france.com>
 */
public class PhaseShifterTapTask implements ModificationTask {

    private final String phaseShifterId;
    private final int tapDelta;

    public PhaseShifterTapTask(String phaseShifterId, int tapDelta) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
        this.tapDelta = tapDelta;
    }

    public int getTapDelta() {
        return tapDelta;
    }

    @Override
    public void modify(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' not found");
        }
        PhaseTapChanger phaseTapChanger = phaseShifter.getPhaseTapChanger();
        if (phaseTapChanger == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' is not a phase shifter");
        }
        adjustTapPosition(phaseTapChanger);
        phaseTapChanger.setRegulating(false);
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
    }

    private void adjustTapPosition(PhaseTapChanger phaseTapChanger) {
        phaseTapChanger.setTapPosition(Math.min(Math.max(phaseTapChanger.getTapPosition() + tapDelta,
                phaseTapChanger.getLowTapPosition()), phaseTapChanger.getHighTapPosition()));
    }
}
