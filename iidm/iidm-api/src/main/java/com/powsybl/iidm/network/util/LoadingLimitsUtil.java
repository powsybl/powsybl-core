/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.LoadingLimits;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Integer.MAX_VALUE;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LoadingLimitsUtil {

    private LoadingLimitsUtil() {
    }

    /**
     * Interface for objects used to report the performed operation on the permanent limit when fixed by
     * {@link #fixMissingPermanentLimit(LoadingLimits, double, String, PermanentLimitFixLogger)}.
     */
    public interface PermanentLimitFixLogger {
        void log(String what, String reason, double wrongValue, double fixedValue);
    }

    /**
     * <p>Compute a missing permanent accordingly to the temporary limits and to a given percentage.</p>
     * <p>Note that this method doesn't check whether the permanent limit is valid or not.</p>
     * @param limits the LoadingLimits which permanent limit should be fixed
     * @param missingPermanentLimitPercentage The percentage to apply
     */
    public static void fixMissingPermanentLimit(LoadingLimits limits, double missingPermanentLimitPercentage) {
        PermanentLimitFixLogger noOp = (what, reason, wrongValue, fixedValue) -> { };
        fixMissingPermanentLimit(limits, missingPermanentLimitPercentage, "", noOp);
    }

    /**
     * <p>Compute a missing permanent accordingly to the temporary limits and to a given percentage.</p>
     * <p>Note that this method doesn't check whether the permanent limit is valid or not.</p>
     * @param limits the LoadingLimits which permanent limit should be fixed
     * @param missingPermanentLimitPercentage The percentage to apply
     * @param parentId id of the limits' network element. It is only used for reporting purposes.
     * @param permanentLimitFixLogger the object used to report the performed operation on the permanent limit.
     */
    public static void fixMissingPermanentLimit(LoadingLimits limits, double missingPermanentLimitPercentage,
                                                String parentId, PermanentLimitFixLogger permanentLimitFixLogger) {
        Collection<LoadingLimits.TemporaryLimit> temporaryLimitsToRemove = new ArrayList<>();
        double lowestTemporaryLimitWithInfiniteAcceptableDuration = MAX_VALUE;
        boolean hasTemporaryLimitWithInfiniteAcceptableDuration = false;
        for (LoadingLimits.TemporaryLimit temporaryLimit : limits.getTemporaryLimits()) {
            if (isAcceptableDurationInfinite(temporaryLimit)) {
                hasTemporaryLimitWithInfiniteAcceptableDuration = true;
                lowestTemporaryLimitWithInfiniteAcceptableDuration =
                        Math.min(lowestTemporaryLimitWithInfiniteAcceptableDuration, temporaryLimit.getValue());
                temporaryLimitsToRemove.add(temporaryLimit);
            }
        }
        limits.getTemporaryLimits().removeAll(temporaryLimitsToRemove);

        if (hasTemporaryLimitWithInfiniteAcceptableDuration) {
            permanentLimitFixLogger.log("Operational Limit Set of " + parentId,
                    "An operational limit set without permanent limit is considered with permanent limit" +
                            "equal to lowest TATL value with infinite acceptable duration",
                    Double.NaN, lowestTemporaryLimitWithInfiniteAcceptableDuration);
            limits.setPermanentLimit(lowestTemporaryLimitWithInfiniteAcceptableDuration);
        } else {
            double firstTemporaryLimit = Iterables.get(limits.getTemporaryLimits(), 0).getValue();
            double percentage = missingPermanentLimitPercentage / 100.;
            double fixedPermanentLimit = firstTemporaryLimit * percentage;
            permanentLimitFixLogger.log("Operational Limit Set of " + parentId,
                    "An operational limit set without permanent limit is considered with permanent limit" +
                            "equal to lowest TATL value weighted by a coefficient of " + percentage,
                    Double.NaN, fixedPermanentLimit);
            limits.setPermanentLimit(fixedPermanentLimit);
        }
    }

    private static boolean isAcceptableDurationInfinite(LoadingLimits.TemporaryLimit temporaryLimit) {
        return temporaryLimit.getAcceptableDuration() == MAX_VALUE;
    }

}
