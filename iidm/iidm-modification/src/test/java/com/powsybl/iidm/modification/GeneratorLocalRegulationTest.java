package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorLocalRegulationTest {

    @Test
    void localRegulationTest() throws IOException {
        Network network = createTestNetwork();
        assertNotNull(network);
        Generator g = network.getGenerator("GEN");
        assertNotNull(g);

        // Before applying the network modification, the generator regulates remotely at 1.05 pu (420 kV)
        assertNotEquals(g.getRegulatingTerminal(), g.getTerminal());
        assertEquals(420.0, g.getTargetV());

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("rootReportNode", "Generator local regulation").build();
        GeneratorLocalRegulation generatorLocalRegulation = new GeneratorLocalRegulation();
        generatorLocalRegulation.apply(network, reportNode);

        // After applying the network modification, the generator regulates locally at 1.05 pu (21 kV)
        assertNotNull(g);
        assertEquals(g.getRegulatingTerminal(), g.getTerminal());
        assertEquals(21.0, g.getTargetV());

        // Report node has been updated with the change
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals("""
                   + Generator local regulation
                      Changed regulation for generator GEN to local instead of remote
                     """, TestUtil.normalizeLineSeparator(sw.toString()));
    }

    private Network createTestNetwork() {
        Network network = Network.create("test_network", "test");
        network.setCaseDate(ZonedDateTime.parse("2021-12-07T18:45:00.000+02:00"));
        Substation st = network.newSubstation()
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
                .setId("GEN")
                .setNode(3)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(100)
                .setMaxP(200)
                .setTargetP(200)
                .setTargetV(420)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(network.getBusbarSection("BBS").getTerminal())
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

        return network;
    }
}
