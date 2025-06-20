/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Set;

/**
 * Container for elements that can be operated during a connection or disconnection
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public record ConnectionElementsContainer(Set<Switch> switchesToOperate, Set<Terminal> busBreakerTerminalsToOperate) {

    public void addAll(ConnectionElementsContainer other) {
        switchesToOperate.addAll(other.switchesToOperate);
        busBreakerTerminalsToOperate.addAll(other.busBreakerTerminalsToOperate);
    }
}
