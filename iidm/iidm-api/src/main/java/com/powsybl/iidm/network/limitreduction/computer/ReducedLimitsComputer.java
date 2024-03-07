/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitreduction.computer;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitreduction.result.LimitsContainer;
import com.powsybl.iidm.network.limitreduction.result.UnalteredLimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Optional;

/**
 * Interface for classes responsible for computing reduced limits of generic type {@link L}
 * from a network element of generic type {@link P}.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface ReducedLimitsComputer<P, L> {
    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    ReducedLimitsComputer<Identifiable<?>, LoadingLimits> NO_REDUCTIONS = new ReducedLimitsComputer.NoReductionsImpl();

    /**
     * <p>Retrieve the limits of <code>processable</code> corresponding to the given limits type and side,
     * then apply on them the reductions configured in the current {@link ReducedLimitsComputer}.</p>
     * <p>The result of this method contains both originals and altered limits.</p>
     *
     * @param processable The network element for which the reduced limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>processable</code> on which the limits should be retrieved
     * @return an object containing the original limits and the altered ones
     */
    Optional<LimitsContainer<L>> getLimitsWithAppliedReduction(P processable, LimitType limitType, ThreeSides side);

    /**
     * An implementation of {@link ReducedLimitsComputer} only retrieving the limits without applying reductions.
     */
    class NoReductionsImpl extends AbstractReducedLimitsComputer<Identifiable<?>, LoadingLimits> {
        @Override
        public Optional<LimitsContainer<LoadingLimits>> getLimitsWithAppliedReduction(Identifiable<?> identifiable, LimitType limitType, ThreeSides side) {
            Optional<LoadingLimits> limits = LimitViolationUtils.getLoadingLimits(identifiable, limitType, side);
            return limits.map(UnalteredLimitsContainer::new);
        }

        @Override
        protected Optional<LimitsContainer<LoadingLimits>> computeLimitsWithAppliedReduction(Identifiable<?> processable, LimitType limitType, ThreeSides side) {
            throw new IllegalStateException("Not implemented: Should not be called"); // Not used
        }
    }
}
