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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>Abstract class responsible for computing reduced limits.</p>
 * <p>Already computed reduced limits are stored in an internal cache to avoid unnecessary computations.
 * This cache should be cleared with {@link #clearCache()} when the reductions to apply are changed.</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractReducedLimitsComputer<P extends ReducedLimitsComputer.Processable> implements ReducedLimitsComputer<P> {
    private final Map<CacheKey, LimitsContainer<?>> reducedLimitsCache;

    protected AbstractReducedLimitsComputer() {
        this.reducedLimitsCache = new HashMap<>();
    }

    @Override
    public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        Objects.requireNonNull(identifiable);
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(identifiable.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LimitsContainer<LoadingLimits>) reducedLimitsCache.get(cacheKey));
        }
        return computeLimitsWithAppliedReduction(getAdapter(identifiable), limitType, side,
                getOriginalLimitsGetterForIdentifiables(),
                (id, originalLimits) -> new DefaultLimitsReducer(originalLimits));
    }

    /**
     * Get or create an adapter for {@link P} from an {@link Identifiable}.
     * @param identifiable an identifiable
     * @return a {@link P} object corresponding to the given identifiable.
     */
    protected abstract P getAdapter(Identifiable<?> identifiable);

    /**
     * Return an {@link com.powsybl.security.limitreduction.ReducedLimitsComputer.OriginalLimitsGetter} to retrieve
     * {@link  LoadingLimits} from an identifiable adapter of type {@link P}.
     * @see #getAdapter(Identifiable)
     * @return an original limits getter for identifiables.
     */
    protected abstract OriginalLimitsGetter<P, LoadingLimits> getOriginalLimitsGetterForIdentifiables();

    @Override
    public <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(P filterable,
                                                                          LimitType limitType, ThreeSides side,
                                                                          OriginalLimitsGetter<P, T> originalLimitsGetter,
                                                                          AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(filterable.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LimitsContainer<T>) reducedLimitsCache.get(cacheKey));
        }
        return computeLimitsWithAppliedReduction(filterable, limitType, side, originalLimitsGetter, limitsReducerCreator);
    }

    protected abstract <T> Optional<LimitsContainer<T>> computeLimitsWithAppliedReduction(P processable,
                                                                                          LimitType limitType, ThreeSides side,
                                                                                          OriginalLimitsGetter<P, T> originalLimitsGetter,
                                                                                          AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    protected <T> void putInCache(P processable, LimitType limitType, ThreeSides side,
                                  LimitsContainer<T> limitsContainer) {
        reducedLimitsCache.put(new CacheKey(processable.getId(), limitType, side), limitsContainer);
    }

    /**
     * <p>Clear the cache containing the already computed limits.</p>
     * <p>This method must be called when the reductions to apply are changed.</p>
     */
    public void clearCache() {
        reducedLimitsCache.clear();
    }

    /**
     * <p>Key for the cache of already computed limits.</p>
     * @param processableId the id of the processable object
     * @param type the limits type corresponding to the limits to cache
     * @param side the side corresponding to the limits to cache
     */
    private record CacheKey(String processableId, LimitType type, ThreeSides side) {
    }
}
