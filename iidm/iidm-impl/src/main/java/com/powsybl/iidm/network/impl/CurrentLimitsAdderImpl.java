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
import com.powsybl.iidm.network.DetectionKind;
import com.powsybl.iidm.network.Validable;

import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class CurrentLimitsAdderImpl extends AbstractLoadingLimitsAdder<CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    CurrentLimitsAdderImpl(Supplier<OperationalLimitsGroupImpl> groupSupplier, Validable validable, String ownerId, String operationalGroupId, NetworkImpl network) {
        super(groupSupplier, validable, ownerId, operationalGroupId, network);
    }

    @Override
    protected CurrentLimits buildLimit(OperationalLimitsGroupImpl group) {
        return detectionKind == DetectionKind.HIGH ?
            new CurrentLimitsImpl(group, permanentLimit, permanentLimitName, temporaryLimits)
            : new CurrentLimitsImpl(group, temporaryLimits);
    }

    @Override
    protected void setLimitToGroup(CurrentLimits limits, OperationalLimitsGroupImpl group) {
        group.setCurrentLimits(limits);
    }

    @Override
    protected String getLimitTypeName() {
        return "CurrentLimits";
    }
}
