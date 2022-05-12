/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsAdderImpl extends AbstractLoadingLimitsAdder<CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    public CurrentLimitsAdderImpl(OperationalLimitsOwner owner) {
        super(owner);
    }

    @Override
    public CurrentLimitsImpl add() {
        checkLoadingLimits();
        CurrentLimitsImpl limits = new CurrentLimitsImpl(owner, id, permanentLimit, temporaryLimits);
        owner.setOperationalLimits(LimitType.CURRENT, limits);
        return limits;
    }

}
