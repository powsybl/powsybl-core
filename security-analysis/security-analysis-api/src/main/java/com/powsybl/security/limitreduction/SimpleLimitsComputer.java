/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.limitreduction.computation.SimpleLimitsReducer;

import java.util.Optional;

/**
 *
 * Limits computer that takes a single limit reduction values for all the identifiers and applicable in pre and post contingencies
 * it does not store any values in the cache
 * it uses {@SimpleLimitsReducer}
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SimpleLimitsComputer implements LimitsComputer<Identifiable<?>, LoadingLimits> {

    private final double limitReduction;

    public SimpleLimitsComputer(double limitReduction) {
        this.limitReduction = limitReduction;
    }

    @Override
    public Optional<LimitsContainer<LoadingLimits>> computeLimits(Identifiable<?> identifiable, LimitType limitType, ThreeSides side, boolean monitoringOnly) {
        Optional<LoadingLimits> limits = LimitViolationUtils.getLoadingLimits(identifiable, limitType, side);
        return limits.map(limits1 -> new SimpleLimitsReducer(limits1, limitReduction).getLimits());
    }
}
