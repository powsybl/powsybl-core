package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StandbyAutomatonXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.newExtension(StandbyAutomatonAdder.class)
                .withB0(0.0001)
                .withStandbyStatus(true)
                .withLowVoltageSetpoint(390)
                .withHighVoltageSetpoint(400)
                .withLowVoltageThreshold(385)
                .withHighVoltageThreshold(405)
                .add();
        StandbyAutomaton standbyAutomaton = svc.getExtension(StandbyAutomaton.class);

        Network network2 = roundTripXmlTest(network,
                                            NetworkXml::writeAndValidate,
                                            NetworkXml::read,
                                            "/standbyAutomatonRoundTripRef.xml");

        StaticVarCompensator svc2 = network2.getStaticVarCompensator("SVC2");
        StandbyAutomaton standbyAutomaton2 = svc2.getExtension(StandbyAutomaton.class);
        assertNotNull(standbyAutomaton2);
        assertEquals(standbyAutomaton.getB0(), standbyAutomaton2.getB0(), 0.0);
        assertEquals(standbyAutomaton.isStandby(), standbyAutomaton2.isStandby());
        assertEquals(standbyAutomaton.getLowVoltageSetpoint(), standbyAutomaton2.getLowVoltageSetpoint(), 0.0);
        assertEquals(standbyAutomaton.getHighVoltageSetpoint(), standbyAutomaton2.getHighVoltageSetpoint(), 0.0);
        assertEquals(standbyAutomaton.getLowVoltageThreshold(), standbyAutomaton2.getLowVoltageThreshold(), 0.0);
        assertEquals(standbyAutomaton.getHighVoltageThreshold(), standbyAutomaton2.getHighVoltageThreshold(), 0.0);
    }
}
