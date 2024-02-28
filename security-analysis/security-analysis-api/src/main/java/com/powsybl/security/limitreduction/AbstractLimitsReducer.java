/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractLimitsReducer<T> {
    private final T originalLimits;
    private double permanentLimitReduction = 1.0;
    private final Map<Integer, Double> temporaryLimitReductionByAcceptableDuration = new HashMap<>();

    protected AbstractLimitsReducer(T originalLimits) {
        this.originalLimits = originalLimits;
    }

    protected abstract T generateReducedLimits();

    protected abstract IntStream getTemporaryLimitsAcceptableDurationStream();

    protected T getReducedLimits() {
        if (permanentLimitReduction == 1.0
                && (temporaryLimitReductionByAcceptableDuration.isEmpty()
                    || temporaryLimitReductionByAcceptableDuration.values().stream().allMatch(v -> v == 1.0))) {
            // No reductions are applicable
            return getOriginalLimits();
        }
        return generateReducedLimits();
    }

    public T getOriginalLimits() {
        return originalLimits;
    }

    void setPermanentLimitReduction(double permanentLimitReduction) {
        this.permanentLimitReduction = permanentLimitReduction;
    }

    public double getPermanentLimitReduction() {
        return permanentLimitReduction;
    }

    void setTemporaryLimitReduction(int acceptableDuration, double limitReduction) {
        temporaryLimitReductionByAcceptableDuration.put(acceptableDuration, limitReduction);
    }

    public double getTemporaryLimitReduction(int acceptableDuration) {
        return temporaryLimitReductionByAcceptableDuration.getOrDefault(acceptableDuration, 1.);
    }

    protected static double applyReduction(double value, double reduction) {
        return value == Double.MAX_VALUE ? Double.MAX_VALUE : value * reduction;
    }
}
