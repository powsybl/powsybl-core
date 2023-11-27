/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Optional;

/**
 */
class ActivePowerLimitsAndGroupAdderImpl extends AbstractLoadingLimitsAdder<ActivePowerLimits, ActivePowerLimitsAdder> implements ActivePowerLimitsAdder {

    OperationalLimitsGroups owner;

    public ActivePowerLimitsAndGroupAdderImpl(OperationalLimitsGroups owner, Validable validable) {
        super(validable);
        this.owner = owner;
    }

    @Override
    public ActivePowerLimits add() {
        checkLoadingLimits();
        OperationalLimitsGroupImpl group;
        Optional<OperationalLimitsGroup> optGroup = owner.getDefaultOperationalLimitsGroup();
        if (optGroup.isEmpty()) {
            // NB. owner.newOperationalLimitsGroup("") erase previous group with id "" if any and create a new one
            group = (OperationalLimitsGroupImpl) owner.newOperationalLimitsGroup("");
            owner.setDefaultTo("");
        } else {
            group = (OperationalLimitsGroupImpl) optGroup.get();
        }
        ActivePowerLimitsImpl limits = new ActivePowerLimitsImpl(group, permanentLimit, temporaryLimits);
        group.setActivePowerLimits(limits);
        return limits;
    }
}
