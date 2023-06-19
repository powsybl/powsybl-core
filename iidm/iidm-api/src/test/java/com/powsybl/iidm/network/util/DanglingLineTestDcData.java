/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.Terminal.BusBreakerView;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class DanglingLineTestDcData {

    static double r = 0.011;
    static double x = 2.0;
    static double G = 0.0;
    static double B = 0.0;

    static double ANGLE = -0.26030675136807774;

    static double P0 = 70.0;
    static double P = 69.99999999999999;
    static double VN = 225.0;

    private Bus bus;
    private BusView busView;
    private BusBreakerView busBreakerView;
    private Terminal terminal;
    private static VoltageLevel voltageLevel;

    private DanglingLine danglingLine;

    DanglingLineTestDcData() {
        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getV()).thenReturn(Double.NaN);
        Mockito.when(bus.getAngle()).thenReturn(ANGLE);

        busView = Mockito.mock(BusView.class);
        Mockito.when(busView.getBus()).thenReturn(bus);
        busBreakerView = Mockito.mock(BusBreakerView.class);
        Mockito.when(busBreakerView.getBus()).thenReturn(bus);

        danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getId()).thenReturn("DanglingLineDcTest");
        terminal = Mockito.mock(Terminal.class);
        voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(terminal.getBusView()).thenReturn(busView);

        Mockito.when(terminal.getBusBreakerView()).thenReturn(busBreakerView);
        Mockito.when(terminal.getP()).thenReturn(P);
        Mockito.when(terminal.getQ()).thenReturn(Double.NaN);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(VN);

        Mockito.when(danglingLine.getR()).thenReturn(r);
        Mockito.when(danglingLine.getX()).thenReturn(x);
        Mockito.when(danglingLine.getG()).thenReturn(G);
        Mockito.when(danglingLine.getB()).thenReturn(B);
        Mockito.when(danglingLine.getP0()).thenReturn(P0);
        Mockito.when(danglingLine.getQ0()).thenReturn(Double.NaN);
    }

    DanglingLine getDanglingLine() {
        return danglingLine;
    }

    void setInvalidNetworkFlow() {
        Mockito.when(terminal.getP()).thenReturn(Double.NaN);
    }

    void setZ0() {
        Mockito.when(danglingLine.getR()).thenReturn(0.0);
        Mockito.when(danglingLine.getX()).thenReturn(0.0);
        Mockito.when(danglingLine.getG()).thenReturn(0.0);
        Mockito.when(danglingLine.getB()).thenReturn(0.0);
    }
}
