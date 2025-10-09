/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusBreakerView;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class DanglingLineTestData {

    static double r = 0.05;
    static double x = 0.2;
    static double G = 0.0;
    static double B = 0.000001;

    static double U = 406.62;
    static double ANGLE = -8.60;

    static double P0 = -367.40;
    static double Q0 = 63.73;

    static double BOUNDARY_BUS_U = 406.63;
    static double BOUNDARY_BUS_ANGLE = -8.57;

    private Bus bus;
    private BusView busView;
    private BusBreakerView busBreakerView;
    private Terminal terminal;
    private DanglingLine danglingLine;

    DanglingLineTestData() {
        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getV()).thenReturn(U);
        Mockito.when(bus.getAngle()).thenReturn(ANGLE);

        busView = Mockito.mock(BusView.class);
        Mockito.when(busView.getBus()).thenReturn(bus);
        busBreakerView = Mockito.mock(BusBreakerView.class);
        Mockito.when(busBreakerView.getBus()).thenReturn(bus);

        danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getId()).thenReturn("DanglingLineTest");
        terminal = Mockito.mock(Terminal.class);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(terminal.getBusView()).thenReturn(busView);
        Mockito.when(terminal.getBusBreakerView()).thenReturn(busBreakerView);

        Mockito.when(danglingLine.getR()).thenReturn(r);
        Mockito.when(danglingLine.getX()).thenReturn(x);
        Mockito.when(danglingLine.getG()).thenReturn(G);
        Mockito.when(danglingLine.getB()).thenReturn(B);
        Mockito.when(danglingLine.getP0()).thenReturn(P0);
        Mockito.when(danglingLine.getQ0()).thenReturn(Q0);
    }

    DanglingLine getDanglingLine() {
        return danglingLine;
    }

    void setP0Zero() {
        Mockito.when(danglingLine.getP0()).thenReturn(0.0);
    }

    void setQ0Zero() {
        Mockito.when(danglingLine.getQ0()).thenReturn(0.0);
    }
}
