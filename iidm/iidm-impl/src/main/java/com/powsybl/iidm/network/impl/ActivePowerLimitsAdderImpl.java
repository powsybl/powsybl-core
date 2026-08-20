/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ActivePowerLimitsAdderImpl extends AbstractLoadingLimitsAdder<ActivePowerLimits, ActivePowerLimitsAdder> implements ActivePowerLimitsAdder {

    ActivePowerLimitsAdderImpl(Supplier<OperationalLimitsGroupImpl> groupSupplier, Validable validable, String ownerId, String operationalGroupId, NetworkImpl network) {
        super(groupSupplier, validable, ownerId, operationalGroupId, network);
    }

    @Override
    protected ActivePowerLimits buildLimit(OperationalLimitsGroupImpl group) {
        return detectionKind == DetectionKind.HIGH ?
            new ActivePowerLimitsImpl(group, permanentLimit, permanentLimitName, temporaryLimits)
            : new ActivePowerLimitsImpl(group, temporaryLimits);
    }

    @Override
    protected void setLimitToGroup(ActivePowerLimits limits, OperationalLimitsGroupImpl group) {
        group.setActivePowerLimits(limits);
    }

    @Override
    protected String getLimitTypeName() {
        return "ActivePowerLimits";
    }
}
