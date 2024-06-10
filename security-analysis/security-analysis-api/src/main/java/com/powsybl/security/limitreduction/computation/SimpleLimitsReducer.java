/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.computation;

import com.powsybl.iidm.network.LoadingLimits;

/**
 * Limits reducer applying the same reduction value for the permanent and every temporary limits.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SimpleLimitsReducer extends DefaultLimitsReducer {

    private final double limitReduction;

    public SimpleLimitsReducer(LoadingLimits originalLimits, double limitReduction) {
        super(originalLimits);
        this.limitReduction = limitReduction;
    }

    @Override
    public double getPermanentLimitReduction() {
        return limitReduction;
    }

    @Override
    public double getTemporaryLimitReduction(int acceptableDuration) {
        return limitReduction;
    }
}
