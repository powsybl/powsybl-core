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
    private OriginalLimitsGetter<P, ?> lastUsedOriginalLimitsGetter;
    private AbstractLimitsReducerCreator<?, ?> lastUsedLimitsReducerCreator;

    protected AbstractReducedLimitsComputer() {
        this.reducedLimitsCache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     * <p>Resulting limits are stored in an internal cache, by (identifier's ID, limit type, side), in order to avoid
     * unnecessary re-computation. If needed, this cache can be cleared using {@link #clearCache()}.</p>
     *
     * @param identifiable The identifiable for which the reduced limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>identifiable</code> on which the limits should be retrieved
     * @return an object containing the original limits and the altered ones
     */
    @Override
    public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
        Objects.requireNonNull(identifiable);
        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(identifiable.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LimitsContainer<LoadingLimits>) reducedLimitsCache.get(cacheKey));
        }
        OriginalLimitsGetter<P, LoadingLimits> originalLimitsGetter = getOriginalLimitsGetterForIdentifiables();
        AbstractLimitsReducerCreator<LoadingLimits, AbstractLimitsReducer<LoadingLimits>> limitsReducerCreator =
                (id, originalLimits) -> new DefaultLimitsReducer(originalLimits);
        checkCacheImpactingObjects(originalLimitsGetter, limitsReducerCreator);
        return computeLimitsWithAppliedReduction(getAdapter(identifiable), limitType, side,
                originalLimitsGetter,
                limitsReducerCreator);
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

    /**
     * {@inheritDoc}
     * <p>Resulting limits are stored in an internal cache, by (<code>processable.getId()</code>, limit type, side),
     * in order to avoid unnecessary re-computation. If needed, this cache can be cleared using {@link #clearCache()}.</p>
     * <p>Note that the cache is cleared each time <code>originalLimitsGetter</code> or <code>limitsReducerCreator</code>
     * is changed.</p>
     *
     * @param <T> the type of the limits
     * @param processable The object for which the reduced limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>identifiable</code> on which the limits should be retrieved
     * @param originalLimitsGetter the object to use to retrieve the original limits of <code>processable</code>
     * @param limitsReducerCreator the object to use to create an object of type {@link T} containing the modified limits.
     * @return an object containing the original limits and the altered ones
     */
    @Override
    public <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(P processable,
                                                                          LimitType limitType, ThreeSides side,
                                                                          OriginalLimitsGetter<P, T> originalLimitsGetter,
                                                                          AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        checkCacheImpactingObjects(originalLimitsGetter, limitsReducerCreator);

        // Look into the cache to avoid recomputing reduced limits if they were already computed
        // with the same limit reductions
        CacheKey cacheKey = new CacheKey(processable.getId(), limitType, side);
        if (reducedLimitsCache.containsKey(cacheKey)) {
            return Optional.of((LimitsContainer<T>) reducedLimitsCache.get(cacheKey));
        }
        return computeLimitsWithAppliedReduction(processable, limitType, side, originalLimitsGetter, limitsReducerCreator);
    }

    protected abstract <T> Optional<LimitsContainer<T>> computeLimitsWithAppliedReduction(P processable,
                                                                                          LimitType limitType, ThreeSides side,
                                                                                          OriginalLimitsGetter<P, T> originalLimitsGetter,
                                                                                          AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    protected <T> void putInCache(P processable, LimitType limitType, ThreeSides side,
                                  LimitsContainer<T> limitsContainer) {
        reducedLimitsCache.put(new CacheKey(processable.getId(), limitType, side), limitsContainer);
    }

    protected void checkCacheImpactingObjects(OriginalLimitsGetter<P, ?> originalLimitsGetter,
                                             AbstractLimitsReducerCreator<?, ?> limitsReducerCreator) {
        if (originalLimitsGetter != lastUsedOriginalLimitsGetter || limitsReducerCreator != lastUsedLimitsReducerCreator) {
            //TODO add a unit test to check that the cache is cleared
            clearCache();
            lastUsedOriginalLimitsGetter = originalLimitsGetter;
            lastUsedLimitsReducerCreator = limitsReducerCreator;
        }
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
