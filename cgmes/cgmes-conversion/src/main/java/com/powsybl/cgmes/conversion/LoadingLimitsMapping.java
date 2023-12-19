/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.LoadingLimitsAdder;

import java.util.*;
import java.util.function.Supplier;

import static java.lang.Integer.MAX_VALUE;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class LoadingLimitsMapping {

    protected final Map<String, LoadingLimitsAdder<?, ?>> adders = new HashMap<>();
    private final Context context;

    LoadingLimitsMapping(Context context) {
        this.context = Objects.requireNonNull(context);
    }

    public LoadingLimitsAdder<?, ?> computeIfAbsentLoadingLimitsAdder(String id, Supplier<LoadingLimitsAdder<?, ?>> supplier) {
        return adders.computeIfAbsent(id, s -> supplier.get());
    }

    void addAll() {
        for (Map.Entry<String, LoadingLimitsAdder<?, ?>> entry : adders.entrySet()) {
            if (!Double.isNaN(entry.getValue().getPermanentLimit()) || entry.getValue().hasTemporaryLimits()) {
                boolean badPermanentLimit = false;
                if (Double.isNaN(entry.getValue().getPermanentLimit())) {
                    entry.getValue().setPermanentLimit(Double.MAX_VALUE); // FIXME
                    badPermanentLimit = true;
                }
                LoadingLimits limits = entry.getValue().add();
                if (badPermanentLimit) {
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
                        context.fixed("Operational Limit Set of " + entry.getKey(),
                                "An operational limit set without permanent limit is considered with permanent limit" +
                                        "equal to lowest TATL value with infinite acceptable duration",
                                Double.NaN, lowestTemporaryLimitWithInfiniteAcceptableDuration);
                        limits.setPermanentLimit(lowestTemporaryLimitWithInfiniteAcceptableDuration);
                    } else {
                        double firstTemporaryLimit = Iterables.get(limits.getTemporaryLimits(), 0).getValue();
                        double fixedPermanentLimit = firstTemporaryLimit * context.config().getMissingPermanentLimitPercentage() / 100;
                        context.fixed("Operational Limit Set of " + entry.getKey(),
                                "An operational limit set without permanent limit is considered with permanent limit" +
                                        "equal to lowest TATL value weighted by a coefficient of " + context.config().getMissingPermanentLimitPercentage() / 100,
                                Double.NaN, fixedPermanentLimit);
                        limits.setPermanentLimit(fixedPermanentLimit);
                    }
                }
            }
        }
        adders.clear();
    }

    private boolean isAcceptableDurationInfinite(LoadingLimits.TemporaryLimit temporaryLimit) {
        return temporaryLimit.getAcceptableDuration() == MAX_VALUE;
    }
}
