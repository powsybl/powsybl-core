/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.computation;

import com.powsybl.iidm.network.limitmodification.result.IdenticalLimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;

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
    private double permanentLimitScaling = 1.0;
    protected final Map<Integer, Double> temporaryLimitScalingByAcceptableDuration = new HashMap<>();

    protected AbstractLimitsScaler(L originalLimits, String limitsGroupId) {
        this.originalLimits = Objects.requireNonNull(originalLimits);
        this.limitsGroupId = Objects.requireNonNull(limitsGroupId);
    }

    /**
     * <p>Generate the scaled limits from the original limits and scalings stored in this object.</p>
     * <p>This method is called when at least one of the scalings store in <code>permanentLimitScaling</code> or
     * <code>temporaryLimitScalingByAcceptableDuration</code> is different to 1. It must return a {@link LimitsContainer}
     * containing the result of {@link #getOriginalLimits()} as original limits and a copy of the original limits on
     * which each limit value is obtained as the original value * the corresponding limit scaling (retrieved from
     * {@link #getPermanentLimitScaling()} or {@link #getTemporaryLimitScaling(int acceptableDuration)} (depending on
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
        if (getPermanentLimitScaling() == 1.0
                && (temporaryLimitScalingByAcceptableDuration.isEmpty()
                    || temporaryLimitScalingByAcceptableDuration.values().stream().allMatch(v -> v == 1.0))) {
            // No scalings are applicable
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

    public void setPermanentLimitScaling(double permanentLimitScaling) {
        this.permanentLimitScaling = permanentLimitScaling;
    }

    public double getPermanentLimitScaling() {
        return permanentLimitScaling;
    }

    public void setTemporaryLimitScaling(int acceptableDuration, double limitScaling) {
        temporaryLimitScalingByAcceptableDuration.put(acceptableDuration, limitScaling);
    }

    public double getTemporaryLimitScaling(int acceptableDuration) {
        return temporaryLimitScalingByAcceptableDuration.getOrDefault(acceptableDuration, 1.);
    }

    protected static double applyScaling(double value, double scaling) {
        return value == Double.MAX_VALUE ? Double.MAX_VALUE : value * scaling;
    }
}
