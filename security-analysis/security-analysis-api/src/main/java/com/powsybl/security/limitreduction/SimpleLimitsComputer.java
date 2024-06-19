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
import com.powsybl.iidm.network.limitmodification.AbstractLimitsComputerWithCache;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.limitreduction.computation.SimpleLimitsReducer;

import java.util.Optional;

/**
 * <p>A limits computer that takes a single limit reduction value for all the identifiers, and applies it on each permanent and temporary limit, for pre- and post-contingency states.</p>
 * <p>Computed values are stored in cache to avoid recomputing them.</p>
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SimpleLimitsComputer extends AbstractLimitsComputerWithCache<Identifiable<?>, LoadingLimits> {

    private final double limitReduction;

    public SimpleLimitsComputer(double limitReduction) {
        this.limitReduction = limitReduction;
    }

    @Override
    protected Optional<LimitsContainer<LoadingLimits>> computeUncachedLimits(Identifiable<?> identifiable, LimitType limitType, ThreeSides side, boolean monitoringOnly) {
        Optional<LoadingLimits> limits = LimitViolationUtils.getLoadingLimits(identifiable, limitType, side);
        return limits.map(limits1 -> new SimpleLimitsReducer(limits1, limitReduction).getLimits());
    }
}
