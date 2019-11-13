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
final class GeneratorUtil {

    private GeneratorUtil() {
    }

    static void connectGenerator(Generator g) {
        Terminal t = g.getTerminal();
        t.connect();
        if (g.isVoltageRegulatorOn()) {
            Bus bus = t.getBusView().getBus();
            if (bus != null) {
                // set voltage setpoint to the same as other generators connected to the bus
                double targetV = bus.getGeneratorStream().findFirst().map(Generator::getTargetV).orElse(Double.NaN);
                // if no other generator connected to the bus, set voltage setpoint to network voltage
                if (Double.isNaN(targetV) && !Double.isNaN(bus.getV())) {
                    g.setTargetV(bus.getV());
                }
            }
        }
    }
}
