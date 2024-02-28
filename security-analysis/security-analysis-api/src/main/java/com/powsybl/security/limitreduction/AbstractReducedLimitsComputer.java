/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.limitreduction.criteria.translation.DefaultNetworkElementWithLimitsAdapter;
import com.powsybl.security.limitreduction.criteria.translation.NetworkElementWithLimits;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Abstract class responsible for computing reduced limits.</p>
 * <p>Already computed reduced limits are stored in an internal cache to avoid unnecessary computations.
 * This cache should be cleared with {@link #clearCache()} when the reductions to apply are changed.</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractReducedLimitsComputer implements ReducedLimitsComputer {
    protected final Map<CacheKey, Object> reducedLimitsCache;

    protected AbstractReducedLimitsComputer() {
        this.reducedLimitsCache = new HashMap<>();
    }

    @Override
    public Optional<LoadingLimits> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(identifiable.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LoadingLimits) reducedLimitsCache.get(cacheKey));
        }
        return computeLimitsWithAppliedReduction(new DefaultNetworkElementWithLimitsAdapter(identifiable), limitType, side,
                (id, originalLimits) -> new DefaultLimitsReducer(originalLimits));
    }

    @Override
    public <T> Optional<T> getLimitsWithAppliedReduction(NetworkElementWithLimits<T> networkElement, LimitType limitType, ThreeSides side,
                                                         AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(networkElement.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((T) reducedLimitsCache.get(cacheKey));
        }
        return computeLimitsWithAppliedReduction(networkElement, limitType, side, limitsReducerCreator);
    }

    protected abstract <T> Optional<T> computeLimitsWithAppliedReduction(NetworkElementWithLimits<T> networkElement, LimitType limitType, ThreeSides side,
                                                                         AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    protected <T> void putInCache(NetworkElementWithLimits<T> networkElement, LimitType limitType, ThreeSides side, T reducedLimits) {
        reducedLimitsCache.put(new CacheKey(networkElement.getId(), limitType, side), reducedLimits);
    }

    protected void clearCache() {
        reducedLimitsCache.clear();
    }

    protected record CacheKey(String networkElementId, LimitType type, ThreeSides side) {
    }
}
