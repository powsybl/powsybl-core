/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.*;
import com.powsybl.security.limitreduction.criteria.translation.DefaultNetworkElementWithLimitsAdapter;

import java.util.Optional;

/**
 * Interface for classes responsible for computing reduced limits.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface ReducedLimitsComputer<F extends ReducedLimitsComputer.Filterable> {
    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    ReducedLimitsComputer<Filterable> NO_REDUCTIONS = new ReducedLimitsComputer.NoReductionsImpl();
    IdentifiableLimitsGetter IDENTIFIABLE_LIMITS_GETTER = new IdentifiableLimitsGetter();

    Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side);

    <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(F filterable, LimitType limitType, ThreeSides side,
                                                                   OriginalLimitsGetter<F, T> originalLimitsGetter,
                                                                   AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    class NoReductionsImpl extends AbstractReducedLimitsComputer<Filterable> {
        @Override
        public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
            Optional<LoadingLimits> limits = IDENTIFIABLE_LIMITS_GETTER.getLimits(new DefaultNetworkElementWithLimitsAdapter(identifiable), limitType, side);
            return limits.map(l -> new LimitsContainer<>(l, l));
        }

        @Override
        public <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(Filterable filterable, LimitType limitType, ThreeSides side,
                                                                              OriginalLimitsGetter<Filterable, T> originalLimitsGetter,
                                                                              AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
            Optional<T> limits = originalLimitsGetter.getLimits(filterable, limitType, side);
            return limits.map(l -> new LimitsContainer<>(l, l));
        }

        @Override
        protected <T> Optional<LimitsContainer<T>> computeLimitsWithAppliedReduction(Filterable filterable, LimitType limitType, ThreeSides side, OriginalLimitsGetter<Filterable, T> originalLimitsGetter, AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
            return Optional.empty(); // Not used
        }

        @Override
        protected Filterable getAdapter(Identifiable<?> identifiable) {
            return null; // Not used
        }
    }

    interface Filterable {
        String getId();
    }

    interface OriginalLimitsGetter<H extends Filterable, T> {
        Optional<T> getLimits(H e, LimitType limitType, ThreeSides side);
    }

    class IdentifiableLimitsGetter implements OriginalLimitsGetter<DefaultNetworkElementWithLimitsAdapter, LoadingLimits> {
        @Override
        public Optional<LoadingLimits> getLimits(DefaultNetworkElementWithLimitsAdapter filterable, LimitType limitType, ThreeSides side) {
            Identifiable<?> identifiable = filterable.getIdentifiable();
            return switch (identifiable.getType()) {
                case LINE -> (Optional<LoadingLimits>) ((Line) identifiable).getLimits(limitType, side.toTwoSides());
                case TIE_LINE -> (Optional<LoadingLimits>) ((TieLine) identifiable).getLimits(limitType, side.toTwoSides());
                case TWO_WINDINGS_TRANSFORMER -> (Optional<LoadingLimits>) ((TwoWindingsTransformer) identifiable).getLimits(limitType, side.toTwoSides());
                case THREE_WINDINGS_TRANSFORMER -> (Optional<LoadingLimits>) ((ThreeWindingsTransformer) identifiable).getLeg(side).getLimits(limitType);
                default -> Optional.empty();
            };
        }
    }
}
