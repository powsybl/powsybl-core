/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.List;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;

public class BusZ0Calculator {

    private Bus   bus;
    private Z0Bus z0Bus;

    public BusZ0Calculator(Bus bus) {
        this.bus = bus;

        z0Bus = new Z0Bus();
        z0Bus.addBus(bus);
    }

    public void calc(List<Z0Bus> z0Buses) {
        if (!busInZ0Buses(z0Buses, bus)) {
            calcZ0Bus();
            if (z0Bus.size() > 1) {
                z0Buses.add(z0Bus);
            }
        }
    }

    private boolean busInZ0Buses(List<Z0Bus> z0Buses, Bus bus) {
        for (Z0Bus z0Bus : z0Buses) {
            if (z0Bus.busInZ0Bus(bus)) {
                return true;
            }
        }
        return false;
    }

    private void calcZ0Bus() {

        int pos = 0;
        while (pos < z0Bus.size()) {
            Bus b = z0Bus.getBus(pos);
            b.getLines().forEach(line -> {
                Side side = Side.ONE;
                if (line.getTerminal2().getBusView().getBus().equals(bus))
                    side = Side.TWO;
                visitLine(line, side);
            });
            pos++;
        }
    }

    private void visitLine(Line line, Side side) {

        Side otherSide = side.equals(Side.ONE) ? Side.TWO : Side.ONE;
        Bus other = line.getTerminal(otherSide).getBusView().getBus();

        if (bus.getV() == other.getV() && bus.getAngle() == other.getAngle()) {
            z0Bus.addBus(other);
        }
    }
}
