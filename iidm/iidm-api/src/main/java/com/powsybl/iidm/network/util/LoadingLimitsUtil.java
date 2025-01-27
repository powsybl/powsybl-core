/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

import static java.lang.Integer.MAX_VALUE;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LoadingLimitsUtil {

    private LoadingLimitsUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadingLimitsUtil.class);

    /**
     * Interface for objects used to report the performed operation on limits when fixed by
     * {@link #fixMissingPermanentLimit(LoadingLimitsAdder, double, String, LimitFixLogger)}.
     */
    public interface LimitFixLogger {
        LimitFixLogger NO_OP = (what, reason, wrongValue, fixedValue) -> {
        };

        void log(String what, String reason, double wrongValue, double fixedValue);
    }

    /**
     * Comparator for temporary limits
     * to return the temporary limits ordered by descending acceptable duration
     */
    public static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuration1, acceptableDuration2) -> acceptableDuration2 - acceptableDuration1;

    /**
     * <p>Compute a missing permanent limit accordingly to the temporary limits and to a given percentage.</p>
     *
     * @param limitsAdder                     the LoadingLimitsAdder which permanent limit should be fixed
     * @param missingPermanentLimitPercentage The percentage to apply
     */
    public static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void fixMissingPermanentLimit(LoadingLimitsAdder<L, A> limitsAdder,
                                                                                                              double missingPermanentLimitPercentage) {
        fixMissingPermanentLimit(limitsAdder, missingPermanentLimitPercentage, "", LimitFixLogger.NO_OP);
    }

    /**
     * <p>Compute a missing permanent limit accordingly to the temporary limits and to a given percentage.</p>
     *
     * @param adder                           the LoadingLimitsAdder which permanent limit should be fixed
     * @param missingPermanentLimitPercentage The percentage to apply
     * @param ownerId                         id of the limits' network element. It is only used for reporting purposes.
     * @param limitFixLogger                  the object used to report the performed operation on the permanent limit.
     */
    public static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void fixMissingPermanentLimit(LoadingLimitsAdder<L, A> adder, double missingPermanentLimitPercentage,
                                                                                                              String ownerId, LimitFixLogger limitFixLogger) {
        if (!Double.isNaN(adder.getPermanentLimit())) {
            return;
        }

        double lowestTemporaryLimitWithInfiniteAcceptableDuration = MAX_VALUE;
        boolean hasTemporaryLimitWithInfiniteAcceptableDuration = false;
        for (String name : adder.getTemporaryLimitNames()) {
            if (adder.getTemporaryLimitAcceptableDuration(name) == MAX_VALUE) {
                hasTemporaryLimitWithInfiniteAcceptableDuration = true;
                lowestTemporaryLimitWithInfiniteAcceptableDuration =
                        Math.min(lowestTemporaryLimitWithInfiniteAcceptableDuration, adder.getTemporaryLimitValue(name));
                adder.removeTemporaryLimit(name);
            }
        }

        if (hasTemporaryLimitWithInfiniteAcceptableDuration) {
            limitFixLogger.log("Operational Limits of " + ownerId,
                    "Operational limits without permanent limit is considered with permanent limit " +
                            "equal to lowest temporary limit value with infinite acceptable duration",
                    Double.NaN, lowestTemporaryLimitWithInfiniteAcceptableDuration);
            adder.setPermanentLimit(lowestTemporaryLimitWithInfiniteAcceptableDuration);
        } else {
            double firstTemporaryLimit = adder.getLowestTemporaryLimitValue();
            double percentage = missingPermanentLimitPercentage / 100.;
            double fixedPermanentLimit = firstTemporaryLimit * percentage;
            limitFixLogger.log("Operational Limits of " + ownerId,
                    "Operational limits without permanent limit is considered with permanent limit " +
                            "equal to lowest temporary limit value weighted by a coefficient of " + percentage + ".",
                    Double.NaN, fixedPermanentLimit);
            adder.setPermanentLimit(fixedPermanentLimit);
        }
    }

    /**
     * <p>Initialize an adder filled with a copy of an existing limits set</p>
     *
     * @param adder  the empty adder in which we initialize the new limits
     * @param limits the limits to copy
     */
    public static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> A initializeFromLoadingLimits(A adder, L limits) {
        if (limits == null) {
            LOGGER.warn("Created adder is empty");
            return adder;
        }
        adder.setPermanentLimit(limits.getPermanentLimit());
        limits.getTemporaryLimits().forEach(limit ->
                adder.beginTemporaryLimit()
                        .setName(limit.getName())
                        .setAcceptableDuration(limit.getAcceptableDuration())
                        .setValue(limit.getValue())
                        .setFictitious(limit.isFictitious())
                        .endTemporaryLimit());
        return adder;
    }

    /**
     * <p>Copies every limit from each operational limits group.</p>
     *
     * @param copiedBranch the copied object
     * @param branch the object on which the copied object attributes are copied
     */
    public static <I extends Branch<I>> Branch<I> copyBranchOperationalLimits(I copiedBranch, I branch) {
        if (copiedBranch != null) {
            copiedBranch.getOperationalLimitsGroups1().forEach(groupToCopy -> {
                OperationalLimitsGroup copy1 = branch.newOperationalLimitsGroup1(groupToCopy.getId());
                groupToCopy.getCurrentLimits().ifPresent(limit -> copy1.newCurrentLimits(limit).add());
                groupToCopy.getActivePowerLimits().ifPresent(limit -> copy1.newActivePowerLimits(limit).add());
                groupToCopy.getApparentPowerLimits().ifPresent(limit -> copy1.newApparentPowerLimits(limit).add());
            });

            copiedBranch.getOperationalLimitsGroups2().forEach(groupToCopy -> {
                OperationalLimitsGroup copy2 = branch.newOperationalLimitsGroup2(groupToCopy.getId());
                groupToCopy.getCurrentLimits().ifPresent(limit -> copy2.newCurrentLimits(limit).add());
                groupToCopy.getActivePowerLimits().ifPresent(limit -> copy2.newActivePowerLimits(limit).add());
                groupToCopy.getApparentPowerLimits().ifPresent(limit -> copy2.newApparentPowerLimits(limit).add());
            });

            copiedBranch.getSelectedOperationalLimitsGroupId1().ifPresent(branch::setSelectedOperationalLimitsGroup1);
            copiedBranch.getSelectedOperationalLimitsGroupId2().ifPresent(branch::setSelectedOperationalLimitsGroup2);

        }
        return copiedBranch;
    }
}
