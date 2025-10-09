/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitmodification;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>Abstract class responsible for computing limits (potentially altered from the ones declared on
 * the network element).</p>
 * <p>Already computed altered limits are stored in an internal cache to avoid unnecessary computations.
 * This cache should be cleared with {@link #clearCache()} when the reductions to apply are changed.</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractLimitsComputerWithCache<P, L> implements LimitsComputer<P, L> {
    private final Map<CacheKey<P>, LimitsContainer<L>> reducedLimitsCache;

    protected AbstractLimitsComputerWithCache() {
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
     * @param monitoringOnly If <code>true</code>, compute the limits to use for a monitoring only use case.
     *                       If <code>false</code>, compute the limits to use for a monitoring + action use case.
     * @return an object containing the original limits and the altered ones
     */
    @Override
    public Optional<LimitsContainer<L>> computeLimits(P processable, LimitType limitType, ThreeSides side, boolean monitoringOnly) {
        Objects.requireNonNull(processable);
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey<P> cacheKey = new CacheKey<>(processable, limitType, side, monitoringOnly);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of(reducedLimitsCache.get(cacheKey));
        }

        return computeUncachedLimits(processable, limitType, side, monitoringOnly);
    }

    /**
     * <p>Retrieve the limits on <code>processable</code> then apply modifications on them.</p>
     * <p>If no modifications applies on the resulting {@link LimitsContainer} must contains the same object for
     * the original and the reduced limits.</p>
     * <p>This function is called when the corresponding limits were not found in the cache.</p>
     * <p>This function is responsible for the addition of the computed limit in the cache
     * (via the {@link #putInCache} method).</p>
     *
     * @param processable the network element for which the limits must be retrieved and modified
     * @param limitType the type of limits to process
     * @param side the side of the network element where to retrieve the original limits
     * @param monitoringOnly If <code>true</code>, compute the limits to use for a monitoring only use case.
     *                       If <code>false</code>, compute the limits to use for a monitoring + action use case.
     * @return an object containing both the original and the modified limits.
     */
    protected abstract Optional<LimitsContainer<L>> computeUncachedLimits(P processable, LimitType limitType, ThreeSides side, boolean monitoringOnly);

    protected void putInCache(P processable, LimitType limitType, ThreeSides side, boolean monitoringOnly,
                              LimitsContainer<L> limitsContainer) {
        reducedLimitsCache.put(new CacheKey<>(processable, limitType, side, monitoringOnly), limitsContainer);
    }

    /**
     * <p>Clear the cache containing the already computed limits.</p>
     * <p>This method must be called when the modifications to apply on the original limits of a network element
     * are changed.</p>
     */
    public void clearCache() {
        reducedLimitsCache.clear();
    }

    /**
     * <p>Key for the cache of already computed limits.</p>
     * @param processable the processable object
     * @param type the limits type corresponding to the limits to cache
     * @param side the side corresponding to the limits to cache
     * @param monitoringOnly If <code>true</code>, the limits are for a monitoring only use case.
     *                       If <code>false</code>, the limits are for a monitoring + action use case.
     */
    private record CacheKey<P>(P processable, LimitType type, ThreeSides side, boolean monitoringOnly) {
    }
}
