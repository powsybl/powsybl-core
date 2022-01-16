/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
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
public class GeneratorEntsoeCategoryXmlTest extends AbstractXmlConverterTest {

    public static Network createTestNetwork() {
        Network network = NetworkFactory.create("test", "test");
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

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        // extends generator
        Generator generator = network.getGenerator("G");
        assertNotNull(generator);
        generator.newExtension(GeneratorEntsoeCategoryAdder.class).withCode(4).add();
        GeneratorEntsoeCategory entsoeCategory = generator.getExtension(GeneratorEntsoeCategory.class);

        Network network2 = roundTripXmlTest(network,
                                            NetworkXml::writeAndValidate,
                                            NetworkXml::read,
                getVersionDir(IidmXmlConstants.CURRENT_IIDM_XML_VERSION) + "/generatorEntsoeCategoryRef.xml");

        Generator generator2 = network2.getGenerator("G");
        assertNotNull(generator2);
        GeneratorEntsoeCategory entsoeCategory2 = generator2.getExtension(GeneratorEntsoeCategory.class);
        assertNotNull(entsoeCategory2);
        assertEquals(entsoeCategory.getCode(), entsoeCategory2.getCode());
    }
}
