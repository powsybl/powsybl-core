/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Riad BENRADI {@literal <riad.benradi_externe@ at rte-france.com>}
 */
public class PhaseShifterResultsExtension extends AbstractExtension<PostContingencyResult> implements Extension<PostContingencyResult> {

    // Conventionnellement, les noms d'extension sont en minuscules et séparés par des tirets
    public static final String NAME = "phase-shifter-results";

    private final List<String> movedPhaseShifterIds;

    public PhaseShifterResultsExtension() {
        this(new ArrayList<>());
    }

    public PhaseShifterResultsExtension(List<String> movedPhaseShifterIds) {
        this.movedPhaseShifterIds = Objects.requireNonNull(movedPhaseShifterIds, "Moved phase shifter IDs cannot be null");
    }

    public List<String> getMovedPhaseShifterIds() {
        return movedPhaseShifterIds;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return "PhaseShifterResultsExtension{" +
               "movedPhaseShifterIds=" + movedPhaseShifterIds +
               '}';
    }
}