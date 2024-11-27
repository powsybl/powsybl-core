/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

        // Before applying the network modification,
        // gen1 regulates remotely at 1.05 pu (420 kV) and gen2 regulates locally at 1.05 pu (21 kV).
        assertNotEquals(gen1.getId(), gen1.getRegulatingTerminal().getConnectable().getId());
        assertEquals(420.0, gen1.getTargetV());
        assertEquals(gen2.getId(), gen2.getRegulatingTerminal().getConnectable().getId());
        assertEquals(21.0, gen2.getTargetV());

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("rootReportNode", "Set generators to local regulation").build();
        new SetGeneratorToLocalRegulation("GEN1").apply(network, reportNode);
        new SetGeneratorToLocalRegulation("GEN2").apply(network, reportNode);
        SetGeneratorToLocalRegulation modification = new SetGeneratorToLocalRegulation("WRONG_ID");
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Generator 'WRONG_ID' not found", e.getMessage());

        // After applying the network modification, both generators regulate locally at 1.05 pu (21 kV).
        assertEquals(gen1.getId(), gen1.getRegulatingTerminal().getConnectable().getId());
        assertEquals(21.0, gen1.getTargetV());
        assertEquals(gen2.getId(), gen2.getRegulatingTerminal().getConnectable().getId());
        assertEquals(21.0, gen2.getTargetV());

        // Report node has been updated with the change for gen1 (no impact for gen2).
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals("""
                   + Set generators to local regulation
                      Changed regulation for generator GEN1 to local instead of remote
                     """, TestUtil.normalizeLineSeparator(sw.toString()));
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
                .setTargetV(21)
                // No regulatingTerminal set == use its own terminal for regulation
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

        vl400.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(1);
        vl20.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(3);
        vl20.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(4);

        return n;
    }
}
