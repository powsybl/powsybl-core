/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public final class GeneratorUtil {

    private GeneratorUtil() {
    }

    public static void connectGenerator(Generator g) {
        Terminal t = g.getTerminal();
        t.connect();
        if (g.isVoltageRegulatorOn()) {
            Bus bus = t.getBusView().getBus();
            if (bus != null) {
                // set voltage setpoint to the same as other generators connected to the bus
                double targetV = bus.getGeneratorStream()
                        .filter(g2 -> !g2.getId().equals(g.getId()))
                        .findFirst().map(Generator::getTargetV).orElse(Double.NaN);
                if (!Double.isNaN(targetV)) {
                    g.setTargetV(targetV);
                } else if (!Double.isNaN(bus.getV())) {
                    // if no other generator connected to the bus, set voltage setpoint to network voltage
                    g.setTargetV(bus.getV());
                }
            }
        }
    }
}
