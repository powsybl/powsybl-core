/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.result;

import com.powsybl.iidm.network.ApparentPowerLimits;

/**
 * <p>Simple implementation of {@link ApparentPowerLimits} not linked to a network element, used to provide
 * scaled apparent power limits without altering the real ones of the network element.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ScaledApparentPowerLimits extends AbstractScaledLoadingLimits implements ApparentPowerLimits {

    /**
     * Create a {@link ScaledApparentPowerLimits} with a permanent limit and {@link com.powsybl.iidm.network.DetectionKind#LOW}
     */
    public ReducedApparentPowerLimits(double permanentLimit, double originalPermanentLimit,
                                      double permanentLimitScaling, String permanentLimitName) {
        super(permanentLimit, originalPermanentLimit, permanentLimitScaling, permanentLimitName);
    }

    /**
     * Create a {@link ScaledApparentPowerLimits} with no permanent limit and {@link com.powsybl.iidm.network.DetectionKind#LOW}
     */
    public ScaledApparentPowerLimits() {
    }
}
