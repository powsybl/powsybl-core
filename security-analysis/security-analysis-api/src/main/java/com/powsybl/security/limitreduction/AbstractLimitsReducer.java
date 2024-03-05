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
 * <p>This class is responsible for generating an object (of generic type {@link L}) containing the reduced limits
 * from the original limits and the reduction coefficients to apply for the permanent and each of the temporary limits.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractLimitsReducer<L> {
    private final L originalLimits;
    private double permanentLimitReduction = 1.0;
    private final Map<Integer, Double> temporaryLimitReductionByAcceptableDuration = new HashMap<>();

    protected AbstractLimitsReducer(L originalLimits) {
        this.originalLimits = originalLimits;
    }

    /**
     * <p>Generate the reduced limits (of generic type {@link L}) from the original limits and reductions stored in this object.</p>
     * @return the reduced limits
     */
    protected abstract L generateReducedLimits();

    /**
     * <p>Return a stream of the temporary limits' acceptable durations.</p>
     * @return the acceptable durations
     */
    protected abstract IntStream getTemporaryLimitsAcceptableDurationStream();

    protected L getReducedLimits() {
        if (permanentLimitReduction == 1.0
                && (temporaryLimitReductionByAcceptableDuration.isEmpty()
                    || temporaryLimitReductionByAcceptableDuration.values().stream().allMatch(v -> v == 1.0))) {
            // No reductions are applicable
            return getOriginalLimits();
        }
        return generateReducedLimits();
    }

    public L getOriginalLimits() {
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
