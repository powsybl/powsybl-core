/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.BoundaryLine;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusBreakerView;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
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
    private BoundaryLine boundaryLine;

    DanglingLineTestData() {
        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getV()).thenReturn(U);
        Mockito.when(bus.getAngle()).thenReturn(ANGLE);

        busView = Mockito.mock(BusView.class);
        Mockito.when(busView.getBus()).thenReturn(bus);
        busBreakerView = Mockito.mock(BusBreakerView.class);
        Mockito.when(busBreakerView.getBus()).thenReturn(bus);

        boundaryLine = Mockito.mock(BoundaryLine.class);
        Mockito.when(boundaryLine.getId()).thenReturn("DanglingLineTest");
        terminal = Mockito.mock(Terminal.class);
        Mockito.when(boundaryLine.getTerminal()).thenReturn(terminal);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(terminal.getBusView()).thenReturn(busView);
        Mockito.when(terminal.getBusBreakerView()).thenReturn(busBreakerView);

        Mockito.when(boundaryLine.getR()).thenReturn(r);
        Mockito.when(boundaryLine.getX()).thenReturn(x);
        Mockito.when(boundaryLine.getG()).thenReturn(G);
        Mockito.when(boundaryLine.getB()).thenReturn(B);
        Mockito.when(boundaryLine.getP0()).thenReturn(P0);
        Mockito.when(boundaryLine.getQ0()).thenReturn(Q0);
    }

    BoundaryLine getDanglingLine() {
        return boundaryLine;
    }

    void setP0Zero() {
        Mockito.when(boundaryLine.getP0()).thenReturn(0.0);
    }

    void setQ0Zero() {
        Mockito.when(boundaryLine.getQ0()).thenReturn(0.0);
    }
}
