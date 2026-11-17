/**
 * Copyright (c) 2018, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
public abstract class AbstractNetworksTest {

    @Test
    public void printBalanceSummaryTest() throws IOException {
        try (Writer writer = new StringWriter()) {
            Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents();
            Networks.printBalanceSummary("", network, writer);
            assertEquals("Active balance at step '':" + System.lineSeparator() +
                            "+-----------------------+--------------------------------+----------------------------------+\n" +
                            "|                       | Main CC connected/disconnected | Others CC connected/disconnected |\n" +
                            "+-----------------------+--------------------------------+----------------------------------+\n" +
                            "| Bus count             |               4                |                3                 |\n" +
                            "| Load count            | 1           | 0                | 1           | 1                  |\n" +
                            "| Load (MW)             | 600.0       | 0.0              | 600.0       | 600.0              |\n" +
                            "| Generator count       | 1           | 0                | 1           | 1                  |\n" +
                            "| Max generation (MW)   | 9999.99     | 0.0              | 9999.99     | 9999.99            |\n" +
                            "| Generation (MW)       | 607.0       | 0.0              | 607.0       | 607.0              |\n" +
                            "| Shunt at nom V (MVar) | 0.0 0.0 (0) | 0.0 0.0 (0)      | 0.0 0.0 (0) | 0.00576 0.0 (1)    |\n" +
                            "+-----------------------+-------------+------------------+-------------+--------------------+" + System.lineSeparator() +
                            "Connected loads in other CC: [LOAD2]" + System.lineSeparator() +
                            "Disconnected loads in other CC: [LOAD3]" + System.lineSeparator() +
                            "Connected generators in other CC: [GEN2]" + System.lineSeparator() +
                            "Disconnected generators in other CC: [GEN3]" + System.lineSeparator() +
                            "Disconnected shunts in other CC: [SHUNT]" + System.lineSeparator(),
                    writer.toString());
        }
    }

    @Test
    public void applySolvedValuesTest() {
        // check that applySolvedValues call each equipment type with solved values
        Network network = mock(Network.class);
        Generator generator = mock(Generator.class);
        Battery battery = mock(Battery.class);
        Load load = mock(Load.class);
        BoundaryLine boundaryLine = mock(BoundaryLine.class);
        TwoWindingsTransformer twt = mock(TwoWindingsTransformer.class);
        ThreeWindingsTransformer twt3 = mock(ThreeWindingsTransformer.class);
        ShuntCompensator shunt = mock(ShuntCompensator.class);
        when(network.getGeneratorStream()).thenReturn(Stream.of(generator));
        when(network.getBatteryStream()).thenReturn(Stream.of(battery));
        when(network.getLoadStream()).thenReturn(Stream.of(load));
        when(network.getBoundaryLineStream()).thenReturn(Stream.of(boundaryLine));
        when(network.getTwoWindingsTransformerStream()).thenReturn(Stream.of(twt));
        when(network.getThreeWindingsTransformerStream()).thenReturn(Stream.of(twt3));
        when(network.getShuntCompensatorStream()).thenReturn(Stream.of(shunt));

        Networks.applySolvedValues(network);

        verify(generator, times(1)).applySolvedValues();
        verify(load, times(1)).applySolvedValues();
        verify(battery, times(1)).applySolvedValues();
        verify(boundaryLine, times(1)).applySolvedValues();
        verify(twt, times(1)).applySolvedValues();
        verify(twt3, times(1)).applySolvedValues();
        verify(shunt, times(1)).applySolvedValues();
    }

    @Test
    public void unsetSolvedValues() {
        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        Networks.unsetSolvedValues(network);

        Line line = network.getLine("NHV1_NHV2_1");
        assertEquals(Double.NaN, line.getTerminal1().getP());
        assertEquals(Double.NaN, line.getTerminal2().getP());
        assertEquals(Double.NaN, line.getTerminal1().getQ());
        assertEquals(Double.NaN, line.getTerminal2().getQ());

        Bus bus = network.getBusView().getBus("VLHV1_0");
        assertEquals(Double.NaN, bus.getV());
        assertEquals(Double.NaN, bus.getAngle());

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        assertNull(twt.getRatioTapChanger().getSolvedTapPosition());
    }

    @Test
    public void unsetDcSolvedValues() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();

        DcLine line = network.getDcLine("dcLine1");
        line.getDcTerminal1().setP(10);
        line.getDcTerminal1().setI(20);
        LineCommutatedConverter lcc = network.getLineCommutatedConverter("LccFr");
        lcc.getDcTerminal1().setP(30);
        lcc.getTerminal1().setP(40);
        network.getDcBus("dcNodeFrPos_dcBus").setV(100);

        Networks.unsetSolvedValues(network);
        assertEquals(Double.NaN, line.getDcTerminal1().getP());
        assertEquals(Double.NaN, line.getDcTerminal1().getI());
        assertEquals(Double.NaN, lcc.getDcTerminal1().getP());
        assertEquals(Double.NaN, lcc.getTerminal1().getP());
        assertEquals(Double.NaN, network.getDcBus("dcNodeFrPos_dcBus").getV());
    }

}
