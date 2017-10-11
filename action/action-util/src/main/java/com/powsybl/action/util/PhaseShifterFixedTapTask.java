/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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

public class PhaseShifterFixedTapTask implements ModificationTask {

    private final String phaseShifterId;
    private final int tapPosition;

    public PhaseShifterFixedTapTask(String phaseShifterId, int tapPosition) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
        this.tapPosition = tapPosition;
    }

    @Override
    public void modify(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' not found");
        }
        if (phaseShifter.getPhaseTapChanger() == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' is not a phase shifter");
        }
        phaseShifter.getPhaseTapChanger().setTapPosition(tapPosition);
        phaseShifter.getPhaseTapChanger().setRegulating(false);
        phaseShifter.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
    }
}
