/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ActivePowerLimits;

import java.util.TreeMap;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ActivePowerLimitsImpl extends AbstractLoadingLimits<ActivePowerLimitsImpl> implements ActivePowerLimits {

    ActivePowerLimitsImpl(OperationalLimitsGroupImpl group, double permanentLimit, String permanentLimitName, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, permanentLimit, permanentLimitName, temporaryLimits);
    }

    /**
     * Create an {@link ActivePowerLimits} with a permanent limit and {@link com.powsybl.iidm.network.DetectionKind#HIGH}.
     */
    ActivePowerLimitsImpl(OperationalLimitsGroupImpl group, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, permanentLimit, temporaryLimits);
    }

    /**
     * Create an {@link ActivePowerLimits} with no permanent limit and {@link com.powsybl.iidm.network.DetectionKind#LOW}.
     */
    ActivePowerLimitsImpl(OperationalLimitsGroupImpl group, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, temporaryLimits);
    }

    @Override
    public void remove() {
        group.removeActivePowerLimits();
    }

}
