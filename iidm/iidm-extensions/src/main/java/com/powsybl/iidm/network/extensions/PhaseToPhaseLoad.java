/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

/**
 * A load connected between two phases of a three-phase system.
 * The active and reactive powers are the total powers consumed between the two phases
 * (consumption is positive, same convention as {@code Load.p0/q0}). They are multi-variant.
 * <p>
 * No consistency check is performed between the powers of this extension and the active and reactive power setpoints
 * ({@code p0}/{@code q0}) of the load, nor with the values of the other extensions of the load (e.g. {@code LoadAsymmetrical}).
 * They are independent data: updating one does not update the others, and it is up to the user to keep them consistent if needed.
 * The {@code LoadAsymmetrical} extension should not be used with this extension.
 * <p>
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface PhaseToPhaseLoad {

    /**
     * Get the first phase.
     */
    Phase getPhase1();

    /**
     * Get the second phase.
     */
    Phase getPhase2();

    /**
     * Get the active power consumed between the two phases, in MW (for the current variant).
     */
    double getP();

    /**
     * Set the active power consumed between the two phases, in MW (for the current variant).
     * @param p the active power in MW, must not be NaN
     */
    PhaseToPhaseLoad setP(double p);

    /**
     * Get the reactive power consumed between the two phases, in MVar (for the current variant).
     */
    double getQ();

    /**
     * Set the reactive power consumed between the two phases, in MVar (for the current variant).
     * @param q the reactive power in MVar, must not be NaN
     */
    PhaseToPhaseLoad setQ(double q);
}
