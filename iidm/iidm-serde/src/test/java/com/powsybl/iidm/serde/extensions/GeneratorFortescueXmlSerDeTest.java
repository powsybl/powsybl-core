/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorFortescueXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testXmlSerializer() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        GeneratorFortescue fortescue = gen.newExtension(GeneratorFortescueAdder.class)
                .withRz(0.1d)
                .withXz(2d)
                .withRn(0.2d)
                .withXn(2.4d)
                .withGrounded(true)
                .withGroundingR(0.02d)
                .withGroundingX(0.3d)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/fortescue/generatorFortescueRef.xml");

        Generator gen2 = network2.getGenerator("GEN");
        assertNotNull(gen2);
        GeneratorFortescue fortescue2 = gen2.getExtension(GeneratorFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(fortescue.getRz(), fortescue2.getRz(), 0);
        assertEquals(fortescue.getXz(), fortescue2.getXz(), 0);
        assertEquals(fortescue.getRn(), fortescue2.getRn(), 0);
        assertEquals(fortescue.getXn(), fortescue2.getXn(), 0);
        assertEquals(fortescue.isGrounded(), fortescue2.isGrounded());
        assertEquals(fortescue.getGroundingR(), fortescue2.getGroundingR(), 0);
        assertEquals(fortescue.getGroundingX(), fortescue2.getGroundingX(), 0);
    }
}
