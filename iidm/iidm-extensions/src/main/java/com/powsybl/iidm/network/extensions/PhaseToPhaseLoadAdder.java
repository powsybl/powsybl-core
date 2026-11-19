/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;

/**
 * Adder of a {@link PhaseToPhaseLoad} to a {@link PhaseToPhaseLoads} extension.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface PhaseToPhaseLoadAdder {

    /**
     * Sets the first phase.
     */
    PhaseToPhaseLoadAdder withPhase1(Phase phase1);

    /**
     * Sets the second phase.
     */
    PhaseToPhaseLoadAdder withPhase2(Phase phase2);

    /**
     * Sets the active power in MW.
     */
    PhaseToPhaseLoadAdder withP(double p);

    /**
     * Sets the reactive power in MVar.
     */
    PhaseToPhaseLoadAdder withQ(double q);

    /**
     * Creates the phase to phase load and adds it to the extension.
     * @return the created phase to phase load
     * @throws PowsyblException if a phase is missing, if both phases are identical, if p or q is NaN
     * or if a load already exists between the same two phases (whatever their order)
     */
    PhaseToPhaseLoad add();
}
