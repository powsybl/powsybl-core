/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorStartupXmlTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        // extends generator
        Generator generator = network.getGenerator("G");
        assertNotNull(generator);
        generator.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(90)
                .withStartupCost(5)
                .withMarginalCost(10)
                .withPlannedOutageRate(0.8)
                .withForcedOutageRate(0.7)
                .add();
        GeneratorStartup startup = generator.getExtension(GeneratorStartup.class);
        generator.addExtension(GeneratorStartup.class, startup);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("generatorStartupRef.xml", IidmXmlConstants.CURRENT_IIDM_XML_VERSION));

        Generator generator2 = network2.getGenerator("G");
        assertNotNull(generator2);
        GeneratorStartup startup2 = generator2.getExtension(GeneratorStartup.class);
        assertNotNull(startup2);
        assertEquals(startup.getPlannedActivePowerSetpoint(), startup2.getPlannedActivePowerSetpoint(), 0);
        assertEquals(startup.getStartupCost(), startup2.getStartupCost(), 0);
        assertEquals(startup.getMarginalCost(), startup2.getMarginalCost(), 0);
        assertEquals(startup.getPlannedOutageRate(), startup2.getPlannedOutageRate(), 0);
        assertEquals(startup.getForcedOutageRate(), startup2.getForcedOutageRate(), 0);

        // backward compatibility
        roundTripXmlTest(network, (n, path) -> NetworkXml.writeAndValidate(n,
                        new ExportOptions().addExtensionVersion(GeneratorStartup.NAME, "1.0"), path),
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("generatorStartupRef-1.0.xml", IidmXmlConstants.CURRENT_IIDM_XML_VERSION));
        roundTripXmlTest(network, (n, path) -> NetworkXml.writeAndValidate(n,
                        new ExportOptions().addExtensionVersion(GeneratorStartup.NAME, "1.0-itesla"), path),
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("generatorStartupRef-1.0-itesla.xml", IidmXmlConstants.CURRENT_IIDM_XML_VERSION));
    }

    private static Network createTestNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");
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
                .setId("B")
                .add();
        vl.newGenerator()
                .setId("G")
                .setBus("B")
                .setConnectableBus("B")
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        return network;
    }
}
