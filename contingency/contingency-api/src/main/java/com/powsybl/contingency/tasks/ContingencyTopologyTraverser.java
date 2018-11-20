/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
final class ContingencyTopologyTraverser {

    private ContingencyTopologyTraverser() {
    }

    private static boolean isOpenable(Switch aSwitch) {
        return !aSwitch.isOpen() &&
                !aSwitch.isFictitious() &&
                aSwitch.getKind() == SwitchKind.BREAKER;
    }

    static void traverse(Terminal terminal, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(switchesToOpen);
        Objects.requireNonNull(terminalsToDisconnect);

        terminal.traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                boolean traverse = true;

                if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER && connected) {
                    terminalsToDisconnect.add(terminal);
                    traverse = false;
                }

                return traverse;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                boolean traverse = false;

                if (isOpenable(aSwitch)) {
                    switchesToOpen.add(aSwitch);
                } else if (!aSwitch.isOpen()) {
                    traverse = true;
                }

                return traverse;
            }
        });
    }
}
