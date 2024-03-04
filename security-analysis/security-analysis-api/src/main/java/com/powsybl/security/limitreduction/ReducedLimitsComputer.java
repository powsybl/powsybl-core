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
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Optional;

/**
 * Interface for classes responsible for computing reduced limits.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface ReducedLimitsComputer<F extends ReducedLimitsComputer.Processable> {
    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    ReducedLimitsComputer<Processable> NO_REDUCTIONS = new ReducedLimitsComputer.NoReductionsImpl();

    /**
     * <p>Retrieve the limits of <code>identifiable</code> corresponding to the given limits type and side, then apply
     * on them the reductions configured in the current {@link ReducedLimitsComputer}.</p>
     * <p>The result of this method contains both originals and altered limits.</p>
     * @param identifiable The identifiable for which the reduced limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>identifiable</code> on which the limits should be retrieved
     * @return an object containing the original limits and the altered ones
     */
    Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side);

    /**
     * <p>Retrieve the limits of <code>filterable</code> corresponding to the given limits type and side
     * (using <code>originalLimitsGetter</code>), then apply
     * on them the reductions configured in the current {@link ReducedLimitsComputer}.</p>
     * <p>The result of this method contains both originals and altered limits.</p>
     * @param <T> the type of the limits
     * @param filterable The object for which the reduced limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>identifiable</code> on which the limits should be retrieved
     * @param originalLimitsGetter the object to use to retrieve the original limits of <code>filterable</code>
     * @param limitsReducerCreator the object to use to create an object of type {@link T} containing the modified limits.
     * @return an object containing the original limits and the altered ones
     */
    <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(F filterable, LimitType limitType, ThreeSides side,
                                                                   OriginalLimitsGetter<F, T> originalLimitsGetter,
                                                                   AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    /**
     * <p>Interface for objects processable by a {@link ReducedLimitsComputer}.</p>
     * <p>Each class implementing this interface must have a corresponding {@link OriginalLimitsGetter} class allowing
     * to obtain limits.</p>
     */
    interface Processable {
        String getId();
    }

    /**
     * Interface for objects allowing to retrieve limits (of generic type <code>T</code>) from an object
     * manageable by the {@link ReducedLimitsComputer} (of generic type <code>H</code>).
     *
     * @param <H> Generic type for the network element for which we want to retrieve the limits
     * @param <T> Generic type for the limits to retrieve
     */
    interface OriginalLimitsGetter<H extends Processable, T> {
        Optional<T> getLimits(H e, LimitType limitType, ThreeSides side);
    }

    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    class NoReductionsImpl extends AbstractReducedLimitsComputer<Processable> {
        private static final String ERROR_MSG = "Not implemented: Should not be called";

        @Override
        public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
            Optional<LoadingLimits> limits = LimitViolationUtils.getLoadingLimits(identifiable, limitType, side);
            return limits.map(l -> new LimitsContainer<>(l, l));
        }

        @Override
        public <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(Processable processable, LimitType limitType, ThreeSides side,
                                                                              OriginalLimitsGetter<Processable, T> originalLimitsGetter,
                                                                              AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
            Optional<T> limits = originalLimitsGetter.getLimits(processable, limitType, side);
            return limits.map(l -> new LimitsContainer<>(l, l));
        }

        @Override
        protected <T> Optional<LimitsContainer<T>> computeLimitsWithAppliedReduction(Processable processable, LimitType limitType, ThreeSides side, OriginalLimitsGetter<Processable, T> originalLimitsGetter, AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
            throw new IllegalStateException(ERROR_MSG); // Not used
        }

        @Override
        protected Processable getAdapter(Identifiable<?> identifiable) {
            throw new IllegalStateException(ERROR_MSG); // Not used
        }

        @Override
        protected OriginalLimitsGetter<Processable, LoadingLimits> getOriginalLimitsGetterForIdentifiables() {
            throw new IllegalStateException(ERROR_MSG); // Not used
        }
    }
}
