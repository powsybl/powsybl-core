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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 *
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BranchTestData {

    private Line line;
    private Terminal terminal1;
    private VoltageLevel voltageLevel1;
    private Terminal terminal2;
    private VoltageLevel voltageLevel2;

    BranchTestData() {
        Bus bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getV()).thenReturn(400.0);
        Mockito.when(bus1.getAngle()).thenReturn(2.0);

        Bus bus2 = Mockito.mock(Bus.class);
        Mockito.when(bus2.getV()).thenReturn(395.0);
        Mockito.when(bus2.getAngle()).thenReturn(3.0);

        BusView busView1 = Mockito.mock(BusView.class);
        Mockito.when(busView1.getBus()).thenReturn(bus1);
        Mockito.when(busView1.getConnectableBus()).thenReturn(null);

        BusView busView2 = Mockito.mock(BusView.class);
        Mockito.when(busView2.getBus()).thenReturn(bus2);
        Mockito.when(busView1.getConnectableBus()).thenReturn(null);

        voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(400.0);
        voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(400.0);

        terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(terminal1.getBusView()).thenReturn(busView1);
        Mockito.when(terminal1.getP()).thenReturn(0.0);
        Mockito.when(terminal1.getQ()).thenReturn(0.0);

        terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(terminal2.getBusView()).thenReturn(busView2);
        Mockito.when(terminal2.getP()).thenReturn(0.0);
        Mockito.when(terminal2.getQ()).thenReturn(0.0);

        line = Mockito.mock(Line.class);
        Mockito.when(line.getId()).thenReturn("LineTest");
        Mockito.when(line.getTerminal1()).thenReturn(terminal1);
        Mockito.when(line.getTerminal2()).thenReturn(terminal2);

        Mockito.when(line.getR()).thenReturn(0.0);
        Mockito.when(line.getX()).thenReturn(12.5);
        Mockito.when(line.getG1()).thenReturn(0.0);
        Mockito.when(line.getB1()).thenReturn(0.0);
        Mockito.when(line.getG2()).thenReturn(0.0);
        Mockito.when(line.getB2()).thenReturn(0.0);

    }

    Line getLine() {
        return line;
    }

    void setTerminal1Null() {
        Mockito.when(line.getTerminal1()).thenReturn(null);
    }

    void setTerminal2Null() {
        Mockito.when(line.getTerminal1()).thenReturn(null);
    }

    void setVoltageLevel1Null() {
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(null);
    }

    void setVoltageLevel2Null() {
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(null);
    }

    void setNominalV1(double nominalV) {
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(nominalV);
    }

    void setNominalV2(double nominalV) {
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(nominalV);
    }
}
