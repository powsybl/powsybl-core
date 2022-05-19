/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimitsAdder;
import com.powsybl.iidm.network.LimitType;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ApparentPowerLimitsAdderImpl extends AbstractLoadingLimitsAdder<ApparentPowerLimits, ApparentPowerLimitsAdder> implements ApparentPowerLimitsAdder {

    ApparentPowerLimitsAdderImpl(OperationalLimitsOwner owner) {
        super(owner);
    }

    @Override
    public ApparentPowerLimits add() {
        checkLoadingLimits();
        ApparentPowerLimitsImpl limits = new ApparentPowerLimitsImpl(owner, id, permanentLimit, temporaryLimits);
        owner.setOperationalLimits(LimitType.APPARENT_POWER, limits);
        return limits;
    }
}
