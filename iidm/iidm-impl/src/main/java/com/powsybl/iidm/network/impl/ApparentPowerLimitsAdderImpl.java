/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ApparentPowerLimitsAdderImpl extends AbstractLoadingLimitsAdder<ApparentPowerLimits, ApparentPowerLimitsAdder> implements ApparentPowerLimitsAdder {

    OperationalLimitsGroupImpl group;

    public ApparentPowerLimitsAdderImpl(OperationalLimitsGroupImpl group, Validable validable) {
        super(validable);
        this.group = group;
    }

    @Override
    public ApparentPowerLimits add() {
        checkLoadingLimits();
        ApparentPowerLimitsImpl limits = new ApparentPowerLimitsImpl(group, permanentLimit, temporaryLimits);
        group.setApparentPowerLimits(limits);
        return limits;
    }
}
