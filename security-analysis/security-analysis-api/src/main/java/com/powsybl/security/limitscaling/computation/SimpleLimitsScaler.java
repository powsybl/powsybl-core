/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.computation;

import com.powsybl.iidm.network.LoadingLimits;

/**
 * Limits scaler applying the same scaling value for the permanent and every temporary limits.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SimpleLimitsScaler extends DefaultLimitsScaler {

    private final double limitScaling;

    public SimpleLimitsScaler(LoadingLimits originalLimits, String limitsGroupId, double limitScaling) {
        super(originalLimits, limitsGroupId);
        this.limitScaling = limitScaling;
    }

    @Override
    public double getPermanentLimitScaling() {
        return limitScaling;
    }

    @Override
    public double getTemporaryLimitScaling(int acceptableDuration) {
        return limitScaling;
    }
}
