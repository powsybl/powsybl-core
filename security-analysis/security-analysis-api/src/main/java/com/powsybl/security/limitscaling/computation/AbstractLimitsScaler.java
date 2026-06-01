/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.computation;

import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.IdenticalLimitsContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * <p>This class is responsible for generating an object (of generic type {@link L}) containing the scaled limits
 * from the original limits and the scaling coefficients to apply for the permanent and each of the temporary limits.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractLimitsScaler<L> {
    private final String limitsGroupId;
    private final L originalLimits;
    private double permanentLimitReduction = 1.0;
    protected final Map<Integer, Double> temporaryLimitReductionByAcceptableDuration = new HashMap<>();

    protected AbstractLimitsScaler(L originalLimits, String limitsGroupId) {
        this.originalLimits = Objects.requireNonNull(originalLimits);
        this.limitsGroupId = Objects.requireNonNull(limitsGroupId);
    }

    /**
     * <p>Generate the scaled limits from the original limits and store them in this object.</p>
     * <p>This method is called when at least one of the scaling factor stored in <code>permanentLimitReduction</code> or
     * <code>temporaryLimitReductionByAcceptableDuration</code> is different from 1. It must return a {@link LimitsContainer}
     * containing the result of {@link #getOriginalLimits()} as original limits and a copy of the original limits on
     * which each limit value is obtained as the original value * the corresponding limit scaling (retrieved from
     * {@link #getPermanentLimitReduction()} or {@link #getTemporaryLimitReduction(int acceptableDuration)}, depending on
     * the type of the limit).</p>
     * @return the scaled limits
     */
    protected abstract LimitsContainer<L> scale();

    /**
     * <p>Return a stream of the temporary limits' acceptable durations.</p>
     * @return the acceptable durations
     */
    public abstract IntStream getTemporaryLimitsAcceptableDurationStream();

    public LimitsContainer<L> getLimits() {
        if (getPermanentLimitReduction() == 1.0
                && (temporaryLimitReductionByAcceptableDuration.isEmpty()
                    || temporaryLimitReductionByAcceptableDuration.values().stream().allMatch(v -> v == 1.0))) {
            // No scaling is applicable
            return new IdenticalLimitsContainer<>(getOriginalLimits(), limitsGroupId);
        }
        return scale();
    }

    public String getLimitsGroupId() {
        return limitsGroupId;
    }

    public L getOriginalLimits() {
        return originalLimits;
    }

    public void setPermanentLimitReduction(double permanentLimitReduction) {
        this.permanentLimitReduction = permanentLimitReduction;
    }

    public double getPermanentLimitReduction() {
        return permanentLimitReduction;
    }

    public void setTemporaryLimitReduction(int acceptableDuration, double limitReduction) {
        temporaryLimitReductionByAcceptableDuration.put(acceptableDuration, limitReduction);
    }

    public double getTemporaryLimitReduction(int acceptableDuration) {
        return temporaryLimitReductionByAcceptableDuration.getOrDefault(acceptableDuration, 1.);
    }

    protected static double applyScaling(double value, double scalingFactor) {
        return value == Double.MAX_VALUE ? Double.MAX_VALUE : value * scalingFactor;
    }
}
