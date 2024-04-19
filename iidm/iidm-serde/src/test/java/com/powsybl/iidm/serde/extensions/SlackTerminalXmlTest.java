/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.serde.XMLExporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class SlackTerminalXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));

        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);

        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);

        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);

        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();

        Network network2 = allFormatsRoundTripTest(network, "/slackTerminal.xml");

        VoltageLevel vl2 = network2.getVoltageLevel(voltageLevelId);
        assertNotNull(vl2);
        SlackTerminal s = vl2.getExtension(SlackTerminal.class);
        assertNotNull(s);

        assertEquals(s.getTerminal().getBusBreakerView().getBus().getId(), busId);
    }

    @Test
    void testNoTerminal() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));

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

        Network network2 = allFormatsRoundTripTest(network, "eurostag-tutorial-example1.xml", CURRENT_IIDM_VERSION);

        VoltageLevel vl2 = network2.getVoltageLevel(voltageLevelId);
        assertNotNull(vl2);
        SlackTerminal s = vl2.getExtension(SlackTerminal.class);
        assertNull(s);
    }

    @Test
    void testBadVariant() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));

        VoltageLevel vl = network.getVoltageLevel("VLHV2");
        Terminal terminal = network.getLine("NHV1_NHV2_1").getTerminal(TwoSides.TWO);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();

        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "test");
        network.getVariantManager().setWorkingVariant("test");
        vl.getExtension(SlackTerminal.class).setTerminal(null);

        Network clone = NetworkSerDe.copy(network);
        VoltageLevel vlClone = clone.getVoltageLevel("VLHV2");
        assertNull(vlClone.getExtension(SlackTerminal.class));
    }

    @Test
    void testExtensionFiltering() {
        Network network = EurostagTutorialExample1Factory.create();

        VoltageLevel vl = network.getVoltageLevel("VLHV2");
        Terminal terminal = network.getLine("NHV1_NHV2_1").getTerminal(TwoSides.TWO);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();

        Properties properties = new Properties();
        properties.put("iidm.export.xml.extensions", "");

        MemDataSource ds = new MemDataSource();

        XMLExporter exporter = new XMLExporter();
        exporter.export(network, properties, ds);

        String data = new String(ds.getData("", "xiidm"));
        assertFalse(data.contains(new SlackTerminalSerDe().getNamespaceUri()));
    }

}
