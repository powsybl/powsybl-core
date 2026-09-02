/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import java.util.Objects;

/**
 * Represents the result of a phase shifter tap position change during security analysis.
 *
 * @param transformerId The ID of the phase shifter transformer
 * @param initialTap The tap position before optimization
 * @param newTap The tap position after optimization
 *
 * @author Riad BENRADI {@literal <riad.benradi_externe at rte-france.com>}
 */
public record MovedPhaseShifterResult(String transformerId, int initialTap, int newTap) {

    /**
     * Compact constructor for validation.
     */
    public MovedPhaseShifterResult {
        Objects.requireNonNull(transformerId, "Transformer ID cannot be null");
    }
}
