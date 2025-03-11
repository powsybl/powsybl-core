/**
 * Copyright (c) 2024-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class SetGeneratorToLocalRegulationTest {

    Network network;

    @BeforeEach
    void setUp() {
        network = createTestNetwork();
    }

    @Test
    void setLocalRegulationTest() throws IOException {
        assertNotNull(network);
        Generator gen1 = network.getGenerator("GEN1");
        Generator gen2 = network.getGenerator("GEN2");
        Generator gen3 = network.getGenerator("GEN3");
        Generator gen4 = network.getGenerator("GEN4");

        // Before applying the network modification,
        // gen1 regulates remotely at 1.05 pu (420 kV) and gen2 regulates locally at 1.05 pu (21 kV).
        assertNotEquals(gen1.getId(), gen1.getRegulatingTerminal().getConnectable().getId());
        assertEquals(420.0, gen1.getTargetV());
        assertEquals(gen2.getId(), gen2.getRegulatingTerminal().getConnectable().getId());
        assertEquals(25.0, gen2.getTargetV());
        assertEquals(gen3.getId(), gen3.getRegulatingTerminal().getConnectable().getId());
        assertEquals(22.0, gen3.getTargetV());
        assertNotEquals(gen4.getId(), gen4.getRegulatingTerminal().getConnectable().getId());
        assertEquals(21.0, gen4.getTargetV());

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("rootReportNode")
                .build();
        new SetGeneratorToLocalRegulation("GEN1").apply(network, reportNode);
        new SetGeneratorToLocalRegulation("GEN2").apply(network, reportNode);
        SetGeneratorToLocalRegulation modification = new SetGeneratorToLocalRegulation("WRONG_ID");
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Generator 'WRONG_ID' not found", e.getMessage());

        // After applying the network modification, GEN1 generator regulates locally at same targetV of GEN3 (closest to nominal V).
        assertEquals(gen1.getId(), gen1.getRegulatingTerminal().getConnectable().getId());
        assertEquals(22.0, gen1.getTargetV());
        assertEquals(gen2.getId(), gen2.getRegulatingTerminal().getConnectable().getId());
        assertEquals(25.0, gen2.getTargetV());
        assertEquals(gen3.getId(), gen3.getRegulatingTerminal().getConnectable().getId());
        assertEquals(22.0, gen3.getTargetV());

        // Report node has been updated with the change for gen1.
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals("""
                   + Set generators to local regulation
                      Changed regulation for generator GEN1 to local instead of remote
                     """, TestUtil.normalizeLineSeparator(sw.toString()));

        new SetGeneratorToLocalRegulation("GEN4").apply(network, reportNode);
        // After applying the network modification, GEN4 generator regulates locally at voltage level nominal V
        assertEquals(gen4.getId(), gen4.getRegulatingTerminal().getConnectable().getId());
        assertEquals(420.0, gen4.getTargetV());
    }

    @Test
    void hasImpactTest() {
        SetGeneratorToLocalRegulation modification;

        modification = new SetGeneratorToLocalRegulation("WRONG_ID");
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification.hasImpactOnNetwork(network));

        modification = new SetGeneratorToLocalRegulation("GEN1");
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        modification = new SetGeneratorToLocalRegulation("GEN2");
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }

    @Test
    void getNameTest() {
        SetGeneratorToLocalRegulation modification = new SetGeneratorToLocalRegulation("GEN1");
        assertEquals("SetGeneratorToLocalRegulation", modification.getName());
    }

    private Network createTestNetwork() {
        Network n = Network.create("test_network", "test");
        n.setCaseDate(ZonedDateTime.parse("2021-12-07T18:45:00.000+02:00"));
        Substation st = n.newSubstation()
                .setId("ST")
                .setCountry(Country.FR)
                .add();

        VoltageLevel vl400 = st.newVoltageLevel()
                .setId("VL400")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl400.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();

        VoltageLevel vl20 = st.newVoltageLevel()
                .setId("VL20")
                .setNominalV(20)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl20.getNodeBreakerView().newBusbarSection()
                .setId("BBS20")
                .setNode(0)
                .add();
        vl20.newGenerator()
                .setId("GEN1")
                .setNode(3)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(100)
                .setMaxP(200)
                .setTargetP(200)
                .setVoltageRegulatorOn(true)
                .setTargetV(420)
                .setRegulatingTerminal(n.getBusbarSection("BBS").getTerminal())
                .add();

        vl20.newGenerator()
                .setId("GEN2")
                .setNode(4)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(100)
                .setMaxP(200)
                .setTargetP(200)
                .setVoltageRegulatorOn(true)
                .setTargetV(25)
                // No regulatingTerminal set == use its own terminal for regulation
                .add();

        vl20.newGenerator()
                .setId("GEN3")
                .setNode(6)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(100)
                .setMaxP(200)
                .setTargetP(200)
                .setVoltageRegulatorOn(true)
                .setTargetV(22)
                // No regulatingTerminal set == use its own terminal for regulation
                .add();

        vl400.newGenerator()
                .setId("GEN4")
                .setNode(7)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(100)
                .setMaxP(200)
                .setTargetP(200)
                .setVoltageRegulatorOn(true)
                .setTargetV(21)
                .setRegulatingTerminal(n.getBusbarSection("BBS20").getTerminal())
                .add();

        st.newTwoWindingsTransformer()
                .setId("T2W")
                .setName("T2W")
                .setR(1.0)
                .setX(10.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(20.0)
                .setRatedU2(400.0)
                .setRatedS(250.0)
                .setVoltageLevel1("VL400")
                .setVoltageLevel2("VL20")
                .setNode1(1)
                .setNode2(2)
                .add();

        createSwitch(vl20, "BBS20_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(vl20, "BBS20_BREAKER_1_2", SwitchKind.BREAKER, false, 1, 2);
        createSwitch(vl20, "BBS20_BREAKER_2_3", SwitchKind.BREAKER, false, 2, 3);
        createSwitch(vl20, "BBS20_BREAKER_3_4", SwitchKind.BREAKER, false, 3, 4);
        createSwitch(vl20, "BBS20_BREAKER_1_4", SwitchKind.BREAKER, false, 1, 4);
        createSwitch(vl20, "BBS20_BREAKER_2_6", SwitchKind.BREAKER, false, 2, 6);

        Load load1 = vl20.newLoad()
                .setId("LD1")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(80)
                .setQ0(10)
                .setNode(5)
                .add();
        load1.getTerminal().setP(80.0).setQ(10.0);

        vl20.getNodeBreakerView().getBusbarSection("BBS20").getTerminal().getBusView().getBus()
                .setV(224.6139)
                .setAngle(2.2822);

        return n;
    }

    private void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(id)
                .setKind(kind)
                .setRetained(kind.equals(SwitchKind.BREAKER))
                .setOpen(open)
                .setFictitious(false)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }
}
