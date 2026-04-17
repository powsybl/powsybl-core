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
import org.assertj.core.api.Assertions;
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
    public void applySolvedValuesBoundaryLine() {
        Network network = BoundaryLineNetworkFactory.createWithGeneration();
        BoundaryLine dl = network.getBoundaryLine("BL");
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

    @Test
    public void getSingleConnectableReducibleVoltageLevels() {
        Network network = EurostagTutorialExample1Factory.create();
        Substation p1 = network.getSubstation("P1");
        double vb1NomV = 48;
        VoltageLevel vb1 = p1.newVoltageLevel()
            .setId("VB1")
            .setNominalV(vb1NomV)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus vb1Bus = vb1.getBusBreakerView().newBus()
            .setId("VB1_BUS")
            .add();
        vb1.newBattery()
            .setId("Battery 1")
            .setBus(vb1Bus.getId())
            .setConnectableBus(vb1Bus.getId())
            .setTargetP(20)
            .setTargetQ(2)
            .setMinP(3)
            .setMaxP(50)
            .add();
        vb1.newBattery()
            .setId("Battery 2")
            .setBus(vb1Bus.getId())
            .setConnectableBus(vb1Bus.getId())
            .setTargetP(10)
            .setTargetQ(1)
            .setMinP(1.5)
            .setMaxP(25)
            .add();
        String batteryTransformerId = "battery transformer";
        p1.newTwoWindingsTransformer()
            .setId(batteryTransformerId)
            .setVoltageLevel1(vb1.getId())
            .setBus1(vb1Bus.getId())
            .setConnectableBus1(vb1Bus.getId())
            .setRatedU1(vb1NomV)
            .setVoltageLevel2(EurostagTutorialExample1Factory.VLHV1)
            .setBus2(EurostagTutorialExample1Factory.NHV1)
            .setConnectableBus2(EurostagTutorialExample1Factory.NHV1)
            .setRatedU2(380)
            .setR(0.25)
            .setX(11)
            .setG(0)
            .setB(0)
            .add();

        Substation p2 = network.getSubstation("P2");
        double vb2NomV = 90;
        VoltageLevel vb2 = p2.newVoltageLevel()
            .setId("VB2")
            .setNominalV(vb2NomV)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus vb2Bus = vb2.getBusBreakerView().newBus()
            .setId("VB2_BUS")
            .add();
        Bus vb2Bus2 = vb2.getBusBreakerView().newBus()
            .setId("VB2_BUS_2")
            .add();
        vb2.getBusBreakerView().newSwitch()
            .setId("VB2_S")
            .setBus1(vb2Bus.getId())
            .setBus2(vb2Bus2.getId())
            .setOpen(true)
            .add();
        vb2.newBattery()
            .setId("B2-1")
            .setBus(vb2Bus2.getId())
            .setConnectableBus(vb2Bus2.getId())
            .setTargetP(20)
            .setTargetQ(2)
            .setMinP(3)
            .setMaxP(50)
            .add();
        p2.newTwoWindingsTransformer()
            .setId("TB2")
            .setVoltageLevel1(vb2.getId())
            .setBus1(vb2Bus.getId())
            .setConnectableBus1(vb2Bus.getId())
            .setRatedU1(vb2NomV)
            .setVoltageLevel2(EurostagTutorialExample1Factory.VLHV2)
            .setBus2(EurostagTutorialExample1Factory.NHV2)
            .setConnectableBus2(EurostagTutorialExample1Factory.NHV2)
            .setRatedU2(380)
            .setR(0.25)
            .setX(11)
            .setG(0)
            .setB(0)
            .add();

        double vg2NomV = 380;
        VoltageLevel vg2 = p2.newVoltageLevel().setId("VG2")
            .setNominalV(vg2NomV)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus vg2Bus = vg2.getBusBreakerView().newBus()
            .setId("VG2_BUS")
            .add();
        vg2.newLoad()
            .setId("LOAD2-1")
            .setBus(vg2Bus.getId())
            .setConnectableBus(vg2Bus.getId())
            .setP0(300)
            .setQ0(100)
            .add();
        network.newLine()
            .setId("VG2L")
            .setVoltageLevel1(EurostagTutorialExample1Factory.VLHV2)
            .setBus1(EurostagTutorialExample1Factory.NHV2)
            .setConnectableBus1(EurostagTutorialExample1Factory.NHV2)
            .setVoltageLevel2(vg2.getId())
            .setBus2(vg2Bus.getId())
            .setConnectableBus2(vg2Bus.getId())
            .setR(3.0)
            .setX(33.0)
            .setG1(0.0)
            .setB1(386E-6 / 2)
            .setG2(0.0)
            .setB2(386E-6 / 2)
            .add();
        Assertions.assertThat(Networks.getReducibleTransformerDataStream(network))
            .extracting(
                d -> d.transformer().getId()
            ).containsExactlyInAnyOrder(
                batteryTransformerId,
                EurostagTutorialExample1Factory.NHV2_NLOAD,
                EurostagTutorialExample1Factory.NGEN_NHV1,
                "TB2"
            );
    }
}
