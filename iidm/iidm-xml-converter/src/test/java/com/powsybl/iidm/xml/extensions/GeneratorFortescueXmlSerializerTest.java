/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorFortescueXmlSerializerTest extends AbstractConverterTest {

    @Test
    void testXmlSerializer() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2016-12-07T11:18:52.881+01:00"));
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        GeneratorFortescue fortescue = gen.newExtension(GeneratorFortescueAdder.class)
                .withRo(0.1d)
                .withXo(2d)
                .withRi(0.2d)
                .withXi(2.4d)
                .withGrounded(true)
                .withGroundingR(0.02d)
                .withGroundingX(0.3d)
                .add();

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/fortescue/generatorFortescueRef.xml");

        Generator gen2 = network2.getGenerator("GEN");
        assertNotNull(gen2);
        GeneratorFortescue fortescue2 = gen2.getExtension(GeneratorFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(fortescue.getRo(), fortescue2.getRo(), 0);
        assertEquals(fortescue.getXo(), fortescue2.getXo(), 0);
        assertEquals(fortescue.getRi(), fortescue2.getRi(), 0);
        assertEquals(fortescue.getXi(), fortescue2.getXi(), 0);
        assertEquals(fortescue.isGrounded(), fortescue2.isGrounded());
        assertEquals(fortescue.getGroundingR(), fortescue2.getGroundingR(), 0);
        assertEquals(fortescue.getGroundingX(), fortescue2.getGroundingX(), 0);
    }
}
