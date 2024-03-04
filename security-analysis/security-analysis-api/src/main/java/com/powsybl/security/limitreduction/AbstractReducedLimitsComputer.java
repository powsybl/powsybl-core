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
import com.powsybl.security.limitreduction.criteria.translation.LimitsHolder;

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
public abstract class AbstractReducedLimitsComputer<F> implements ReducedLimitsComputer {
    protected final Map<CacheKey, LimitsContainer<?>> reducedLimitsCache;

    protected AbstractReducedLimitsComputer() {
        this.reducedLimitsCache = new HashMap<>();
    }

    @Override
    public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(identifiable.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LimitsContainer<LoadingLimits>) reducedLimitsCache.get(cacheKey));
        }
        DefaultNetworkElementWithLimitsAdapter adapter = new DefaultNetworkElementWithLimitsAdapter(identifiable);
        return computeLimitsWithAppliedReduction(adapter, (F) adapter, limitType, side,
                (id, originalLimits) -> new DefaultLimitsReducer(originalLimits));
    }

    @Override
    public <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(LimitsHolder<T> limitsHolder,
                                                                          LimitType limitType, ThreeSides side,
                                                         AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(limitsHolder.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LimitsContainer<T>) reducedLimitsCache.get(cacheKey));
        }
        return computeLimitsWithAppliedReduction(limitsHolder, (F) limitsHolder, limitType, side, limitsReducerCreator);
    }

    protected abstract <T> Optional<LimitsContainer<T>> computeLimitsWithAppliedReduction(LimitsHolder<T> limitsHolder,
                                                                                          F filterable,
                                                                                          LimitType limitType, ThreeSides side,
                                                                                          AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    protected <T> void putInCache(LimitsHolder<T> limitsHolder, LimitType limitType, ThreeSides side,
                                  LimitsContainer<T> limitsContainer) {
        reducedLimitsCache.put(new CacheKey(limitsHolder.getId(), limitType, side), limitsContainer);
    }

    protected void clearCache() {
        reducedLimitsCache.clear();
    }

    protected record CacheKey(String limitsHolderId, LimitType type, ThreeSides side) {
    }
}
