/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.computation;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.limitmodification.result.*;
import com.powsybl.security.limitscaling.result.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * <p>{@link AbstractLimitsScaler} implementation responsible for computing reduced limits of type {@link LoadingLimits}.</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitsScaler extends AbstractLimitsScaler<LoadingLimits> {

    public DefaultLimitsScaler(LoadingLimits originalLimits, String limitsGroupId) {
        super(originalLimits, limitsGroupId);
    }

    @Override
    protected LimitsContainer<LoadingLimits> scale() {
        LoadingLimits originalLimits = getOriginalLimits();
        double reducedPermanentLimit = applyScaling(originalLimits.getPermanentLimit(), getPermanentLimitReduction());
        AbstractScaledLoadingLimits reducedLoadingLimits = init(originalLimits.getLimitType(), reducedPermanentLimit,
                originalLimits.getPermanentLimit(), getPermanentLimitReduction());

        // Compute the temporary limits:
        // A temporary limit L1 should be ignored (not created) if there exists another temporary limit L2
        // such as: acceptableDuration(L2) < acceptableDuration(L1) AND reducedValue(L2) <= reducedValue(L1)
        List<LoadingLimits.TemporaryLimit> temporaryLimits = originalLimits.getTemporaryLimits().stream()
                .sorted(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration)).toList();
        double previousRetainedReducedValue = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order of the durations
            double reduction = getTemporaryLimitReduction(tl.getAcceptableDuration());
            double tlReducedValue = applyScaling(tl.getValue(), reduction);
            if (Double.isNaN(previousRetainedReducedValue) || tlReducedValue < previousRetainedReducedValue) {
                previousRetainedReducedValue = tlReducedValue;
                reducedLoadingLimits.addTemporaryLimit(tl.getName(), tlReducedValue, tl.getAcceptableDuration(),
                        tl.isFictitious(), tl.getValue(), reduction);
            }
        }
        return new DefaultScaledLimitsContainer(reducedLoadingLimits, originalLimits, getLimitsGroupId());
    }

    @Override
    public IntStream getTemporaryLimitsAcceptableDurationStream() {
        return getOriginalLimits().getTemporaryLimits().stream().mapToInt(LoadingLimits.TemporaryLimit::getAcceptableDuration);
    }

    private AbstractScaledLoadingLimits init(LimitType type, double permanentLimit,
                                             double originalPermanentLimit,
                                             double permanentLimitReduction) {
        return switch (type) {
            case ACTIVE_POWER -> new ScaledActivePowerLimits(permanentLimit, originalPermanentLimit, permanentLimitReduction);
            case APPARENT_POWER -> new ScaledApparentPowerLimits(permanentLimit, originalPermanentLimit, permanentLimitReduction);
            case CURRENT -> new ScaledCurrentLimits(permanentLimit, originalPermanentLimit, permanentLimitReduction);
            default -> throw new IllegalArgumentException(
                    String.format("Unsupported limits type for reductions (%s)", type));
        };
    }
}
