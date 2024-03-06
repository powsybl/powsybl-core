/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.CurrentLimits;

/**
 * <p>Simple implementation of {@link CurrentLimits} not linked to a network element, used to provide
 * reduced current limits without altering the real ones of the network element.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ReducedCurrentLimits extends AbstractReducedLoadingLimits implements CurrentLimits {
    public ReducedCurrentLimits(double permanentLimit, double originalPermanentLimit,
                                double reductionAppliedOnPermanentLimit) {
        super(permanentLimit, originalPermanentLimit, reductionAppliedOnPermanentLimit);
    }
}
