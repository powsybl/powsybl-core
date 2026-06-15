/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.result;

import com.powsybl.iidm.network.ApparentPowerLimits;

/**
 * <p>Simple implementation of {@link ApparentPowerLimits} not linked to a network element, used to provide
 * reduced apparent power limits without altering the real ones of the network element.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ReducedApparentPowerLimits extends AbstractReducedLoadingLimits implements ApparentPowerLimits {

    /**
     * Create a {@link ReducedApparentPowerLimits} with a permanent limit and {@link com.powsybl.iidm.network.DetectionKind#LOW}
     */
    public ReducedApparentPowerLimits(double permanentLimit, double originalPermanentLimit,
                                      double permanentLimitReduction) {
        super(permanentLimit, originalPermanentLimit, permanentLimitReduction);
    }

    /**
     * Create a {@link ReducedApparentPowerLimits} with no permanent limit and {@link com.powsybl.iidm.network.DetectionKind#LOW}
     */
    public ReducedApparentPowerLimits() {
    }
}
