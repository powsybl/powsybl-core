/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitmodification;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.IdenticalLimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Optional;

/**
 * Interface for classes responsible for accessing limits of generic type {@link L}
 * from a network element of generic type {@link P}, and potentially creating a modified copy of them.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface LimitsComputer<P, L> {
    /**
     * An implementation of {@link LimitsComputer} only retrieving the limits of the network elements
     * without applying modifications.
     */
    LimitsComputer<Identifiable<?>, LoadingLimits> NO_MODIFICATIONS = new NoModificationsImpl();

    /**
     * <p>Retrieve the limits of <code>processable</code> corresponding to the given limits type and side,
     * then apply on them the modifications configured in the current {@link LimitsComputer}.</p>
     * <p>The result of this method contains both originals and altered limits.</p>
     *
     * @param processable The network element for which the altered limits must be computed
     * @param limitType The type of the limits to process
     * @param side The side of <code>processable</code> on which the limits should be retrieved
     * @param monitoringOnly If <code>true</code>, compute the limits to use for a monitoring only use case.
     *                       If <code>false</code>, compute the limits to use for a monitoring + action use case.
     * @return an object containing the original limits and the altered ones
     */
    Optional<LimitsContainer<L>> computeLimits(P processable, LimitType limitType, ThreeSides side, boolean monitoringOnly);

    /**
     * An implementation of {@link LimitsComputer} only retrieving the limits without applying modifications.
     */
    class NoModificationsImpl extends AbstractLimitsComputerWithCache<Identifiable<?>, LoadingLimits> {
        @Override
        public Optional<LimitsContainer<LoadingLimits>> computeLimits(Identifiable<?> identifiable, LimitType limitType, ThreeSides side, boolean monitoringOnly) {
            Optional<LoadingLimits> limits = LimitViolationUtils.getLoadingLimits(identifiable, limitType, side);
            return limits.map(IdenticalLimitsContainer::new);
        }

        @Override
        protected Optional<LimitsContainer<LoadingLimits>> computeUncachedLimits(Identifiable<?> processable, LimitType limitType, ThreeSides side, boolean monitoringOnly) {
            throw new IllegalStateException("Not implemented: Should not be called"); // Not used
        }
    }
}
