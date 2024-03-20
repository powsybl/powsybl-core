/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimitsAdder;
import com.powsybl.iidm.network.Validable;

import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ApparentPowerLimitsAdderImpl extends AbstractLoadingLimitsAdder<ApparentPowerLimits, ApparentPowerLimitsAdder> implements ApparentPowerLimitsAdder {

    Supplier<OperationalLimitsGroupImpl> groupSupplier;

    private final NetworkImpl network;

    public ApparentPowerLimitsAdderImpl(Supplier<OperationalLimitsGroupImpl> groupSupplier, Validable validable, String ownerId, NetworkImpl network) {
        super(validable, ownerId);
        this.groupSupplier = groupSupplier;
        this.network = network;
    }

    @Override
    public ApparentPowerLimits add() {
        checkAndUpdateValidationLevel(network);
        OperationalLimitsGroupImpl group = groupSupplier.get();
        if (group == null) {
            throw new PowsyblException(String.format("Error adding ApparentPowerLimits on %s: error getting or creating the group", getOwnerId()));
        }
        ApparentPowerLimits limits = new ApparentPowerLimitsImpl(group, permanentLimit, temporaryLimits, network);
        group.setApparentPowerLimits(limits);
        return limits;
    }
}
