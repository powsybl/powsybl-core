/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import com.powsybl.iidm.network.Validable;

import java.util.Optional;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
class CurrentLimitsAndGroupAdderImpl extends AbstractLoadingLimitsAdder<CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    OperationalLimitsGroups owner;

    public CurrentLimitsAndGroupAdderImpl(OperationalLimitsGroups owner, Validable validable) {
        super(validable);
        this.owner = owner;
    }

    @Override
    public CurrentLimits add() {
        checkLoadingLimits();
        OperationalLimitsGroupImpl group;
        Optional<OperationalLimitsGroup> optGroup = owner.getDefaultOperationalLimitsGroup();
        if (optGroup.isEmpty()) {
            // NB. owner.newOperationalLimitsGroup("") erase previous group with id "" if any and create a new one
            group = (OperationalLimitsGroupImpl) owner.newOperationalLimitsGroup(OPERATIONAL_LIMITS_GROUP_DEFAULT_ID);
            owner.setDefaultTo(OPERATIONAL_LIMITS_GROUP_DEFAULT_ID);
        } else {
            group = (OperationalLimitsGroupImpl) optGroup.get();
        }
        CurrentLimitsImpl limits = new CurrentLimitsImpl(group, permanentLimit, temporaryLimits);
        group.setCurrentLimits(limits);
        return limits;
    }

}
