/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackTerminalXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));

        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);

        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);

        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);

        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();

        Network network2 = roundTripTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                AbstractConverterTest::compareTxt,
                "/slackTerminal.xml");

        VoltageLevel vl2 = network2.getVoltageLevel(voltageLevelId);
        assertNotNull(vl2);
        SlackTerminal s = vl2.getExtension(SlackTerminal.class);
        assertNotNull(s);

        assertEquals(s.getTerminal().getBusBreakerView().getBus().getId(), busId);
    }

    @Test
    public void testNoTerminal() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));

        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);

        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);

        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);

        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();

        SlackTerminal st = vl.getExtension(SlackTerminal.class);
        assertNotNull(st);

        // Removing slackTerminal from current variant
        assertTrue(st.setTerminal(null).isEmpty());

        Network network2 = roundTripTest(network,
            NetworkXml::writeAndValidate,
            NetworkXml::read,
            AbstractConverterTest::compareXml,
            getVersionedNetworkPath("eurostag-tutorial-example1.xml", CURRENT_IIDM_XML_VERSION));

        VoltageLevel vl2 = network2.getVoltageLevel(voltageLevelId);
        assertNotNull(vl2);
        SlackTerminal s = vl2.getExtension(SlackTerminal.class);
        assertNull(s);
    }

}
