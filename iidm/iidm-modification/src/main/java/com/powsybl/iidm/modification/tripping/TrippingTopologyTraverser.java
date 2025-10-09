/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;

import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
final class TrippingTopologyTraverser {

    private TrippingTopologyTraverser() {
    }

    private static boolean isOpenable(Switch aSwitch) {
        return !aSwitch.isOpen() &&
                !aSwitch.isFictitious() &&
                aSwitch.getKind() == SwitchKind.BREAKER;
    }

    static void traverse(Terminal terminal, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(switchesToOpen);
        Objects.requireNonNull(terminalsToDisconnect);

        terminal.traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
                    // we have no idea what kind of switch it was in the initial node/breaker topology
                    // so to keep things simple we do not propagate the fault
                    if (connected) {
                        terminalsToDisconnect.add(terminal);
                        if (traversedTerminals != null) {
                            traversedTerminals.add(terminal);
                        }
                    }
                    return TraverseResult.TERMINATE_PATH;
                } else if (traversedTerminals != null) {
                    traversedTerminals.add(terminal);
                }
                // in node/breaker topology propagation is decided only based on switch position
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                if (isOpenable(aSwitch)) {
                    // Traverser stops on current path as contingency opens the openable switch
                    switchesToOpen.add(aSwitch);
                    return TraverseResult.TERMINATE_PATH;
                }
                return aSwitch.isOpen() ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE;
            }
        });
    }
}
