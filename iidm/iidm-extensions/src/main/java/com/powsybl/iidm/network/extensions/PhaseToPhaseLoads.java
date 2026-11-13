/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Load;

import java.util.List;
import java.util.Optional;

/**
 * This extension stores the loads connected between two phases of a {@link Load}.
 * A load can carry several phase to phase components (e.g. AB, BC, CA). There can be at most one
 * component for a given pair of phases: the pair is unordered, so (A, B) and (B, A) designate the same component.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface PhaseToPhaseLoads extends Extension<Load> {

    String NAME = "phaseToPhaseLoads";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Gets all the phase to phase loads in the extension.
     * @return an unmodifiable list of phase to phase loads
     */
    List<PhaseToPhaseLoad> getPhaseToPhaseLoads();

    /**
     * Creates a new phase to phase load adder.
     */
    PhaseToPhaseLoadAdder newPhaseToPhaseLoad();

    /**
     * Finds the phase to phase load between two phases. The order of the phases does not matter.
     * @param phase1 the first phase
     * @param phase2 the second phase
     * @return the phase to phase load if found, or empty if not
     */
    Optional<PhaseToPhaseLoad> findPhaseToPhaseLoad(Phase phase1, Phase phase2);

    /**
     * Removes a phase to phase load.
     * @param phaseToPhaseLoad the phase to phase load to remove
     * @return true if the phase to phase load was removed, false otherwise
     */
    boolean removePhaseToPhaseLoad(PhaseToPhaseLoad phaseToPhaseLoad);

    /**
     * Removes the phase to phase load between two phases. The order of the phases does not matter.
     * @param phase1 the first phase
     * @param phase2 the second phase
     * @return true if the phase to phase load was removed, false otherwise
     */
    boolean removePhaseToPhaseLoad(Phase phase1, Phase phase2);
}
