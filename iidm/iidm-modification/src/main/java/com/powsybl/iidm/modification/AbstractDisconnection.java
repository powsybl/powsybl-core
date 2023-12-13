/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.util.SwitchPredicates;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractDisconnection extends AbstractNetworkModification {
    final String connectableId;
    Predicate<Switch> openableSwitches;

    AbstractDisconnection(String connectableId) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.openableSwitches = SwitchPredicates.IS_OPEN.negate();
    }
}
