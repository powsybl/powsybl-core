/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Validable;

import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class CurrentLimitsAdderImpl extends AbstractLoadingLimitsAdder<CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    Supplier<OperationalLimitsGroupImpl> groupSupplier;

    private final NetworkImpl network;

    public CurrentLimitsAdderImpl(Supplier<OperationalLimitsGroupImpl> groupSupplier, Validable validable, String ownerId, NetworkImpl network) {
        super(validable, ownerId);
        this.groupSupplier = groupSupplier;
        this.network = network;
    }

    @Override
    public CurrentLimits add() {
        checkAndUpdateValidationLevel(network);
        OperationalLimitsGroupImpl group = groupSupplier.get();
        if (group == null) {
            throw new PowsyblException(String.format("Error adding CurrentLimits on %s: error getting or creating the group", getOwnerId()));
        }
        CurrentLimitsImpl limits = new CurrentLimitsImpl(validable, group, permanentLimit, temporaryLimits);
        group.setCurrentLimits(limits);
        return limits;
    }

}
