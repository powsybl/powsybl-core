/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusBreakerView;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class DanglingLineTestData {

    public static double r = 0.05;
    public static double x = 0.2;
    public static double G = 0.0;
    public static double B = 0.000001;

    public static double U = 406.62;
    public static double ANGLE = -8.60;

    public static double P0 = -367.40;
    public static double Q0 = 63.73;

    private Bus bus;
    private BusView busView;
    private BusBreakerView busBreakerView;
    private Terminal terminal;
    private DanglingLine danglingLine;
    private DanglingLine.Generation generation;
    private ReactiveLimits reactiveLimits;

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
        Mockito.when(terminal.getP()).thenReturn(Double.NaN);
        Mockito.when(terminal.getQ()).thenReturn(Double.NaN);

        generation = Mockito.mock(DanglingLine.Generation.class);
        Mockito.when(danglingLine.getGeneration()).thenReturn(generation);
        Mockito.when(generation.getTargetP()).thenReturn(0.0);
        Mockito.when(generation.getTargetQ()).thenReturn(0.0);

        Mockito.when(danglingLine.getR()).thenReturn(r);
        Mockito.when(danglingLine.getX()).thenReturn(x);
        Mockito.when(danglingLine.getG()).thenReturn(G);
        Mockito.when(danglingLine.getB()).thenReturn(B);
        Mockito.when(danglingLine.getP0()).thenReturn(P0);
        Mockito.when(danglingLine.getQ0()).thenReturn(Q0);
    }

    public DanglingLine getDanglingLine() {
        return danglingLine;
    }

    public void setP0Zero() {
        Mockito.when(danglingLine.getP0()).thenReturn(0.0);
    }

    public void setQ0Zero() {
        Mockito.when(danglingLine.getQ0()).thenReturn(0.0);
    }

    public void setTargetPHalfP0() {
        Mockito.when(danglingLine.getP0()).thenReturn(P0 * 0.5);
        Mockito.when(generation.getTargetP()).thenReturn(-P0 * 0.5);
    }

    public void setTargetQHalfQ0() {
        Mockito.when(danglingLine.getQ0()).thenReturn(Q0 * 0.5);
        Mockito.when(generation.getTargetQ()).thenReturn(-Q0 * 0.5);
    }

    public void setZ0() {
        Mockito.when(danglingLine.getR()).thenReturn(0.0);
        Mockito.when(danglingLine.getX()).thenReturn(0.0);
        Mockito.when(danglingLine.getG()).thenReturn(0.0);
        Mockito.when(danglingLine.getB()).thenReturn(0.0);
    }

    public void setNetworkFlow() {
        Mockito.when(terminal.getP()).thenReturn(-367.35795801563415);
        Mockito.when(terminal.getQ()).thenReturn(63.73282249057797);
    }

    public void setGenerationControl() {
        Mockito.when(generation.isVoltageRegulationOn()).thenReturn(true);
        Mockito.when(generation.getTargetP()).thenReturn(-P0 * 0.5);
        Mockito.when(generation.getTargetV()).thenReturn(406.60);

        reactiveLimits = Mockito.mock(ReactiveLimits.class);
        Mockito.when(generation.getReactiveLimits()).thenReturn(reactiveLimits);

        Mockito.when(reactiveLimits.getMinQ(-P0 * 0.5)).thenReturn(-200.0);
        Mockito.when(reactiveLimits.getMaxQ(-P0 * 0.5)).thenReturn(200.0);
    }

    public void setGenerationControlSaturatedAtQmin() {
        Mockito.when(generation.isVoltageRegulationOn()).thenReturn(true);
        Mockito.when(generation.getTargetP()).thenReturn(-P0 * 0.5);
        Mockito.when(generation.getTargetV()).thenReturn(406.60);

        reactiveLimits = Mockito.mock(ReactiveLimits.class);
        Mockito.when(generation.getReactiveLimits()).thenReturn(reactiveLimits);

        Mockito.when(reactiveLimits.getMinQ(-P0 * 0.5)).thenReturn(-10.0);
        Mockito.when(reactiveLimits.getMaxQ(-P0 * 0.5)).thenReturn(200.0);
    }

    public void setGenerationControlSaturatedAtQmax() {
        Mockito.when(generation.isVoltageRegulationOn()).thenReturn(true);
        Mockito.when(generation.getTargetP()).thenReturn(-P0 * 0.5);
        Mockito.when(generation.getTargetV()).thenReturn(406.80);

        reactiveLimits = Mockito.mock(ReactiveLimits.class);
        Mockito.when(generation.getReactiveLimits()).thenReturn(reactiveLimits);

        Mockito.when(reactiveLimits.getMinQ(-P0 * 0.5)).thenReturn(-10.0);
        Mockito.when(reactiveLimits.getMaxQ(-P0 * 0.5)).thenReturn(100.0);
    }
}
