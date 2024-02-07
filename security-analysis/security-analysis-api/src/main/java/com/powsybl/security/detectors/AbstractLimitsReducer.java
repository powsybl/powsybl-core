/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractLimitsReducer<T> {
    private T originalLimits;
    private double permanentLimitReduction = 1.0;
    private final Map<Integer, Double> temporaryLimitReductionByAcceptableDuration = new HashMap<>();

    abstract T generateReducedLimits();

    abstract IntStream getTemporaryLimitsAcceptableDurationStream();

    void initializeLimits(T originalLimits) {
        this.originalLimits = originalLimits;
        permanentLimitReduction = 1.0;
        temporaryLimitReductionByAcceptableDuration.clear();
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

    public Double getTemporaryLimitReduction(int acceptableDuration) {
        return temporaryLimitReductionByAcceptableDuration.getOrDefault(acceptableDuration, 1.);
    }
}
