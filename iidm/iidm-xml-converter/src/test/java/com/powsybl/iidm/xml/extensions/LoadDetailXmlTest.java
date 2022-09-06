/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.commons.ComparisonUtils.compareXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadDetailXmlTest extends AbstractXmlConverterTest {

    private static Network createTestNetwork() {
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
                .setId("B")
                .add();
        vl.newLoad()
                .setId("L")
                .setBus("B")
                .setConnectableBus("B")
                .setP0(100)
                .setQ0(50)
                .add();

        // extends load
        Load load = network.getLoad("L");
        assertNotNull(load);
        load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(40f)
                .withFixedReactivePower(20f)
                .withVariableActivePower(60f)
                .withVariableReactivePower(30f)
                .add();
        return network;
    }

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(IidmXmlConstants.CURRENT_IIDM_XML_VERSION) + "loadDetailRef.xml");

        Load load2 = network2.getLoad("L");
        assertNotNull(load2);
        LoadDetail detail2 = load2.getExtension(LoadDetail.class);
        assertNotNull(detail2);
        assertEquals(detail.getFixedActivePower(), detail2.getFixedActivePower(), 0f);
        assertEquals(detail.getFixedReactivePower(), detail2.getFixedReactivePower(), 0f);
        assertEquals(detail.getVariableActivePower(), detail2.getVariableActivePower(), 0f);
        assertEquals(detail.getVariableReactivePower(), detail2.getVariableReactivePower(), 0f);
    }

    @Test
    public void testOld() throws IOException {
        Network network = NetworkXml.read(getVersionedNetworkAsStream("loadDetailRef.xml", IidmXmlVersion.V_1_2));

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);

        Path tmp = tmpDir.resolve("data");
        NetworkXml.writeAndValidate(network, tmp);

        try (InputStream is = Files.newInputStream(tmp)) {
            compareXml(getVersionedNetworkAsStream("loadDetailRef.xml", IidmXmlConstants.CURRENT_IIDM_XML_VERSION), is);
        }
    }
}
