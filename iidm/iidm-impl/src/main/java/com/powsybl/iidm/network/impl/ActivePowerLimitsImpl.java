/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.LimitType;

import java.util.TreeMap;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ActivePowerLimitsImpl extends AbstractLoadingLimits<ActivePowerLimitsImpl> implements ActivePowerLimits {

    ActivePowerLimitsImpl(OperationalLimitsOwner owner, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(owner, permanentLimit, temporaryLimits);
    }

    @Override
    public void remove() {
        owner.setOperationalLimits(LimitType.ACTIVE_POWER, null);
    }
}
