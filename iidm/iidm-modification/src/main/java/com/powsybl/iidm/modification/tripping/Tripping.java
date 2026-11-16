/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;

import java.util.Set;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface Tripping extends NetworkModification {

    default void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
    }

    default void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        traverse(network, switchesToOpen, terminalsToDisconnect, null);
    }

    default void traverseDc(Network network, Set<DcSwitch> dcSwitchesToOpen, Set<DcTerminal> dcTerminalsToDisconnect, Set<DcTerminal> traversedDcTerminals) {
    }

    default void traverseDc(Network network, Set<DcSwitch> dcSwitchesToOpen, Set<DcTerminal> dcTerminalsToDisconnect) {
        traverseDc(network, dcSwitchesToOpen, dcTerminalsToDisconnect, null);
    }
}
