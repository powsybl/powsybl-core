/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractInjection<I extends Injection<I>> extends AbstractConnectable<I> implements Injection<I> {

    private final OperationalLimitsHolderImpl operationalLimitsHolder;

    AbstractInjection(String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        operationalLimitsHolder = new OperationalLimitsHolderImpl(this, "limits");
    }

    @Override
    public List<OperationalLimits> getOperationalLimits() {
        return operationalLimitsHolder.getOperationalLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return operationalLimitsHolder.newCurrentLimits();
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return operationalLimitsHolder.newApparentPowerLimits();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return operationalLimitsHolder.newActivePowerLimitsAdder();
    }

    @Override
    public VoltageLimits getVoltageLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.VOLTAGE, VoltageLimits.class);
    }

    @Override
    public VoltageLimitsAdder newVoltageLimits() {
        return operationalLimitsHolder.newVoltageLimits();
    }
}
