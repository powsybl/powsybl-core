/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Validable;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CurrentLimitsAdderImpl extends AbstractLoadingLimitsAdder<CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    OperationalLimitsGroupImpl group;

    public CurrentLimitsAdderImpl(OperationalLimitsGroupImpl group, Validable validable, String ownerId) {
        super(validable, ownerId);
        this.group = group;
    }

    @Override
    public CurrentLimits add() {
        CurrentLimitsImpl limits = new CurrentLimitsImpl(group, permanentLimit, temporaryLimits);
        group.setCurrentLimits(limits);
        return limits;
    }

}
