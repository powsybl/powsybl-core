/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractPhaseShifterModification extends AbstractSingleNetworkModification {

    protected final String phaseShifterId;
    private static final String TRANSFORMER_NOT_FOUND = "Transformer '%s' not found";
    private static final String NOT_A_PHASE_SHIFTER = "Transformer '%s' is not a phase shifter";

    protected AbstractPhaseShifterModification(String phaseShifterId) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
    }

    protected PhaseTapChanger getPhaseTapChanger(Network network) {
        Objects.requireNonNull(network);
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            throw new PowsyblException(String.format(TRANSFORMER_NOT_FOUND, phaseShifterId));
        }
        PhaseTapChanger phaseTapChanger = phaseShifter.getPhaseTapChanger();
        if (phaseTapChanger == null) {
            throw new PowsyblException(String.format(NOT_A_PHASE_SHIFTER, phaseShifterId));
        }
        return phaseTapChanger;
    }
}
