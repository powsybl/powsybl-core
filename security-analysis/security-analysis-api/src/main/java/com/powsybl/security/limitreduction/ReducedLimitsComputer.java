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

import java.util.Optional;

/**
 * Interface for classes responsible for computing reduced limits.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface ReducedLimitsComputer {
    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    ReducedLimitsComputer NO_REDUCTIONS = new ReducedLimitsComputer.NoReductionsImpl();

    Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side);

    <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(NetworkElementWithLimits<T> networkElement, LimitType limitType, ThreeSides side,
                                                                   AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator);

    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    class NoReductionsImpl implements ReducedLimitsComputer {
        @Override
        public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
            Optional<LoadingLimits> limits = (new DefaultNetworkElementWithLimitsAdapter(identifiable)).getLimits(limitType, side);
            return limits.map(l -> new LimitsContainer<>(l, l));
        }

        @Override
        public <T> Optional<LimitsContainer<T>> getLimitsWithAppliedReduction(NetworkElementWithLimits<T> networkElement, LimitType limitType,
                                                             ThreeSides side, AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
            Optional<T> limits = networkElement.getLimits(limitType, side);
            return limits.map(l -> new LimitsContainer<>(l, l));
        }
    }
}
