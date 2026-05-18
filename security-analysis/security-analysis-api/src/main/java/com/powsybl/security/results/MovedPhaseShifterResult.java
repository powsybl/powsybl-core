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
 * @author Riad BENRADI {@literal <riad.benradi_externe@ at rte-france.com>}
 */
public class MovedPhaseShifterResult {

    private final String transformerId;

    private final int initialTap;

    private final int newTap;

    public MovedPhaseShifterResult(String transformerId, int initialTap, int newTap) {
        this.transformerId = Objects.requireNonNull(transformerId, "Transformer ID cannot be null");
        this.initialTap = initialTap;
        this.newTap = newTap;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public int getInitialTap() {
        return initialTap;
    }

    public int getNewTap() {
        return newTap;
    }

    @Override
    public String toString() {
        return "MovedPhaseShifterResult{" +
                "transformerId='" + transformerId + '\'' +
                ", initialTap=" + initialTap +
                ", newTap=" + newTap +
                '}';
    }
}
