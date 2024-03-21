/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ActivePowerLimitsAdderImpl extends AbstractLoadingLimitsAdder<ActivePowerLimits, ActivePowerLimitsAdder> implements ActivePowerLimitsAdder {
    Supplier<OperationalLimitsGroupImpl> groupSupplier;

    private final NetworkImpl network;

    public ActivePowerLimitsAdderImpl(Supplier<OperationalLimitsGroupImpl> groupSupplier, Validable validable, String ownerId, NetworkImpl network) {
        super(validable, ownerId);
        this.groupSupplier = groupSupplier;
        this.network = network;
    }

    @Override
    public ActivePowerLimits add() {
        checkAndUpdateValidationLevel(network);
        OperationalLimitsGroupImpl group = groupSupplier.get();
        if (group == null) {
            throw new PowsyblException(String.format("Error adding ActivePowerLimits on %s: error getting or creating the group", getOwnerId()));
        }
        ActivePowerLimits limits = new ActivePowerLimitsImpl(group, permanentLimit, temporaryLimits);
        group.setActivePowerLimits(limits);
        return limits;
    }
}
