/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Damien Jeandemange <damien.jeandemange at artelys.com>
 */
class RemoteReactivePowerControlXmlTest extends AbstractXmlConverterTest {

    private static Network createTestNetwork() {
        // network for serialization test purposes only, no load-flow would converge on this.
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        var gen = vl.newGenerator()
                .setId("G")
                .setBus("B1")
                .setConnectableBus("B1")
                .setTargetP(100)
                .setTargetQ(100)
                .setTargetV(400)
                .setMinP(0)
                .setMaxP(200)
                .setVoltageRegulatorOn(true)
                .add();
        var line = network.newLine()
                .setId("L12")
                .setVoltageLevel1("VL").setBus1("B1")
                .setVoltageLevel2("VL").setBus2("B2")
                .setR(0).setX(1).setB1(0).setB2(0).setG1(0).setG2(0)
                .add();

        gen.newExtension(RemoteReactivePowerControlAdder.class)
                .withEnabled(true)
                .withTargetQ(123)
                .withRegulatingTerminal(line.getTerminal2())
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();
        network.write("XIIDM", new Properties(), Path.of("toto.xiidm"));

        RemoteReactivePowerControl rrpc = network.getGenerator("G").getExtension(RemoteReactivePowerControl.class);
        assertNotNull(rrpc);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(IidmXmlConstants.CURRENT_IIDM_XML_VERSION) + "remoteReactivePowerControlRef.xml");

        Generator gen2 = network2.getGenerator("G");
        Line line = network.getLine("L12");
        Line line2 = network2.getLine("L12");
        assertNotNull(gen2);
        RemoteReactivePowerControl rrpc2 = gen2.getExtension(RemoteReactivePowerControl.class);
        assertNotNull(rrpc2);
        assertEquals(rrpc.isEnabled(), rrpc2.isEnabled());
        assertEquals(rrpc.getTargetQ(), rrpc2.getTargetQ(), 0f);
        assertEquals(rrpc.getRegulatingTerminal().getConnectable().getId(), rrpc2.getRegulatingTerminal().getConnectable().getId());
        assertEquals(line.getSide(rrpc.getRegulatingTerminal()), line2.getSide(rrpc2.getRegulatingTerminal()));
    }
}
