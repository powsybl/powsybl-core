/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitsReducer extends AbstractLimitsReducer<LoadingLimits> {

    DefaultLimitsReducer(LoadingLimits originalLimits) {
        super(originalLimits);
    }

    @Override
    protected LoadingLimits generateReducedLimits() {
        LoadingLimits originalLimits = getOriginalLimits();
        double reducedPermanentLimit = applyReduction(originalLimits.getPermanentLimit(), getPermanentLimitReduction());
        AbstractReducedLoadingLimits reducedLoadingLimits = initReducedLoadingLimits(originalLimits.getLimitType(), reducedPermanentLimit);

        // Compute the temporary limits:
        // A temporary limit L1 should be ignored (not created) if there exists another temporary limit L2
        // such as: acceptableDuration(L2) < acceptableDuration(L1) AND reducedValue(L2) <= reducedValue(L1)
        List<LoadingLimits.TemporaryLimit> temporaryLimits = originalLimits.getTemporaryLimits().stream()
                .sorted(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration)).toList();
        double previousRetainedReducedValue = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order of the durations
            double tlReducedValue = applyReduction(tl.getValue(), getTemporaryLimitReduction(tl.getAcceptableDuration()));
            if (Double.isNaN(previousRetainedReducedValue) || tlReducedValue < previousRetainedReducedValue) {
                previousRetainedReducedValue = tlReducedValue;
                reducedLoadingLimits.addTemporaryLimit(tl.getName(), tlReducedValue, tl.getAcceptableDuration(), tl.isFictitious());
            }
        }
        return reducedLoadingLimits;
    }

    @Override
    protected IntStream getTemporaryLimitsAcceptableDurationStream() {
        return getOriginalLimits().getTemporaryLimits().stream().mapToInt(LoadingLimits.TemporaryLimit::getAcceptableDuration);
    }

    private AbstractReducedLoadingLimits initReducedLoadingLimits(LimitType type, double permanentLimit) {
        return switch (type) {
            case ACTIVE_POWER -> new ReducedActivePowerLimits(permanentLimit);
            case APPARENT_POWER -> new ReducedApparentPowerLimits(permanentLimit);
            case CURRENT -> new ReducedCurrentLimits(permanentLimit);
            default -> throw new IllegalArgumentException(
                    String.format("Unsupported limits type for reductions (%s)", type));
        };
    }
}
