/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.LimitType;
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
public abstract class AbstractReducedLimitsComputer<P, L> implements ReducedLimitsComputer<P, L> {
    private final Map<CacheKey<P>, LimitsContainer<L>> reducedLimitsCache;

    protected AbstractReducedLimitsComputer() {
        this.reducedLimitsCache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     * <p>Resulting limits are stored in an internal cache, by (identifier's ID, limit type, side), in order to avoid
     * unnecessary re-computation. If needed, this cache can be cleared using {@link #clearCache()}.</p>
     *
     * @param processable The network element for which the reduced limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>processable</code> on which the limits should be retrieved
     * @return an object containing the original limits and the altered ones
     */
    @Override
    public Optional<LimitsContainer<L>> getLimitsWithAppliedReduction(P processable, LimitType limitType, ThreeSides side) {
        Objects.requireNonNull(processable);
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey<P> cacheKey = new CacheKey<>(processable, limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of(reducedLimitsCache.get(cacheKey));
        }

        return computeLimitsWithAppliedReduction(processable, limitType, side);
    }

    protected abstract Optional<LimitsContainer<L>> computeLimitsWithAppliedReduction(P processable, LimitType limitType, ThreeSides side);

    protected void putInCache(P processable, LimitType limitType, ThreeSides side,
                              LimitsContainer<L> limitsContainer) {
        reducedLimitsCache.put(new CacheKey<>(processable, limitType, side), limitsContainer);
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
     * @param processable the processable object
     * @param type the limits type corresponding to the limits to cache
     * @param side the side corresponding to the limits to cache
     */
    private record CacheKey<P>(P processable, LimitType type, ThreeSides side) {
    }
}
