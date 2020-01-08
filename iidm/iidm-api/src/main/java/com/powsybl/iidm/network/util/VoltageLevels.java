/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class VoltageLevels {

    public static Optional<String> findBus(VoltageLevel vl, int node) {
        VoltageLevel.NodeBreakerView nbv = vl.getNodeBreakerView();
        int[] nodes = nbv.getNodes(node);
        for (int i = 0; i < nodes.length; i++) {
            Terminal terminal = nbv.getTerminal(node);
            if (terminal != null) {
                if (terminal.getBusView().getBus() != null) {
                    return Optional.of(terminal.getBusView().getBus().getId());
                }
            }
        }
        return Optional.empty();
    }

    private VoltageLevels() {
    }
}
