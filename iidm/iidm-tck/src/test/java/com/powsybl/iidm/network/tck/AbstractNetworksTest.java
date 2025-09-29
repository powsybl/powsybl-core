/**
 * Copyright (c) 2018, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.setSolvedSectionCount(0);
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        twt.getPhaseTapChanger().setSolvedTapPosition(13);
        twt.getRatioTapChanger().setSolvedTapPosition(2);
        // Modify p and q of load so that it is different to P0 and Q0
        Load load = network.getLoad("LD1");
        load.getTerminal().setP(81).setQ(11);
        // Modify p, q and v of generator so that it is different to the targets
        Generator generator = network.getGenerator("GH1");
        generator.getTerminal().setP(-86).setQ(-512);
        generator.getTerminal().getBusView().getBus().setV(401);

        assertNotEquals(shuntCompensator.getSolvedSectionCount(), shuntCompensator.getSectionCount());
        assertNotEquals(twt.getPhaseTapChanger().getSolvedTapPosition(), twt.getPhaseTapChanger().getTapPosition());
        assertNotEquals(twt.getRatioTapChanger().getSolvedTapPosition(), twt.getRatioTapChanger().getTapPosition());
        assertNotEquals(load.getTerminal().getP(), load.getP0());
        assertNotEquals(load.getTerminal().getQ(), load.getQ0());
        assertNotEquals(-generator.getTerminal().getP(), generator.getTargetP());
        assertNotEquals(-generator.getTerminal().getQ(), generator.getTargetQ());
        assertNotEquals(generator.getTerminal().getBusBreakerView().getBus().getV(), generator.getTargetV());

        Networks.applySolvedValues(network);
        assertEquals(shuntCompensator.getSolvedSectionCount(), shuntCompensator.getSectionCount());
        assertEquals(twt.getPhaseTapChanger().getSolvedTapPosition(), twt.getPhaseTapChanger().getTapPosition());
        assertEquals(twt.getRatioTapChanger().getSolvedTapPosition(), twt.getRatioTapChanger().getTapPosition());
        assertEquals(load.getTerminal().getP(), load.getP0());
        assertEquals(load.getTerminal().getQ(), load.getQ0());
        assertEquals(-generator.getTerminal().getP(), generator.getTargetP());
        assertEquals(-generator.getTerminal().getQ(), generator.getTargetQ());
        assertEquals(generator.getTerminal().getBusBreakerView().getBus().getV(), generator.getTargetV());
    }

    @Test
    public void applySolvedValuesBattery() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        assertNotEquals(battery.getTerminal().getP(), battery.getTargetP());
        assertNotEquals(battery.getTerminal().getQ(), battery.getTargetQ());

        Networks.applySolvedValues(network);
        assertEquals(-battery.getTerminal().getP(), battery.getTargetP());
        assertEquals(-battery.getTerminal().getQ(), battery.getTargetQ());
    }

    @Test
    public void applySolvedValuesDanglingLine() {
        Network network = BoundaryLineNetworkFactory.createWithGeneration();
        BoundaryLine dl = network.getDanglingLine("DL");
        dl.getTerminal().setP(441).setQ(30);
        dl.getTerminal().getBusView().getBus().setV(100);
        assertNotEquals(-dl.getTerminal().getP(), dl.getGeneration().getTargetP());
        assertNotEquals(dl.getTerminal().getBusView().getBus().getV(), dl.getGeneration().getTargetV());

        Networks.applySolvedValues(network);
        assertEquals(-dl.getTerminal().getP(), dl.getGeneration().getTargetP());
        assertEquals(dl.getTerminal().getBusView().getBus().getV(), dl.getGeneration().getTargetV());

        dl.getGeneration().setVoltageRegulationOn(false).setTargetV(Double.NaN).setTargetQ(35);
        assertNotEquals(-dl.getTerminal().getQ(), dl.getGeneration().getTargetQ());

        Networks.applySolvedValues(network);
        assertEquals(-dl.getTerminal().getQ(), dl.getGeneration().getTargetQ());
    }
}
