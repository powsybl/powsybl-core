/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLExporter;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

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

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
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

    @Test
    public void testBadVariant() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));

        VoltageLevel vl = network.getVoltageLevel("VLHV2");
        Terminal terminal = network.getLine("NHV1_NHV2_1").getTerminal(Branch.Side.TWO);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();

        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "test");
        network.getVariantManager().setWorkingVariant("test");
        vl.getExtension(SlackTerminal.class).setTerminal(null);

        Network clone = NetworkXml.copy(network);
        VoltageLevel vlClone = clone.getVoltageLevel("VLHV2");
        Assert.assertNull(vlClone.getExtension(SlackTerminal.class));
    }

    @Test
    public void testExtensionFiltering() {
        Network network = EurostagTutorialExample1Factory.create();

        VoltageLevel vl = network.getVoltageLevel("VLHV2");
        Terminal terminal = network.getLine("NHV1_NHV2_1").getTerminal(Branch.Side.TWO);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();

        Properties properties = new Properties();
        properties.put("iidm.export.xml.extensions", "none");

        MemDataSource ds = new MemDataSource();

        XMLExporter exporter = new XMLExporter();
        exporter.export(network, properties, ds);

        String data = new String(ds.getData("", "xiidm"));
        assertFalse(data.contains(new SlackTerminalXmlSerializer().getNamespaceUri()));
    }

}
