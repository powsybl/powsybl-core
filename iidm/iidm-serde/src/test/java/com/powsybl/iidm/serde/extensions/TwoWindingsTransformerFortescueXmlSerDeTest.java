/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TwoWindingsTransformerFortescueXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testXmlSerializer() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        var twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt);
        TwoWindingsTransformerFortescue fortescue = twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withRz(0.1d)
                .withXz(2d)
                .withFreeFluxes(true)
                .withConnectionType1(WindingConnectionType.Y_GROUNDED)
                .withConnectionType2(WindingConnectionType.DELTA)
                .withGroundingR1(0.02d)
                .withGroundingX1(0.3d)
                .withGroundingR2(0.04d)
                .withGroundingX2(0.95d)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/fortescue/twoWindingsTransformerFortescueRef.xml");

        TwoWindingsTransformer twt2 = network2.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt2);
        TwoWindingsTransformerFortescue fortescue2 = twt2.getExtension(TwoWindingsTransformerFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(fortescue.getRz(), fortescue2.getRz(), 0);
        assertEquals(fortescue.getXz(), fortescue2.getXz(), 0);
        assertEquals(fortescue.isFreeFluxes(), fortescue2.isFreeFluxes());
        assertSame(fortescue.getConnectionType1(), fortescue2.getConnectionType1());
        assertSame(fortescue.getConnectionType2(), fortescue2.getConnectionType2());
        assertEquals(fortescue.getGroundingR1(), fortescue2.getGroundingR1(), 0);
        assertEquals(fortescue.getGroundingX1(), fortescue2.getGroundingX1(), 0);
        assertEquals(fortescue.getGroundingR2(), fortescue2.getGroundingR2(), 0);
        assertEquals(fortescue.getGroundingX2(), fortescue2.getGroundingX2(), 0);
    }

    @Test
    void testXmlSerializerWithMagnetizingReactance() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        var twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt);
        TwoWindingsTransformerFortescue fortescue = twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
            .withRz(0.1d)
            .withXz(2d)
            .withFreeFluxes(false)
            .withXm(0.5d)
            .withConnectionType1(WindingConnectionType.Y_GROUNDED)
            .withConnectionType2(WindingConnectionType.DELTA)
            .withGroundingR1(0.02d)
            .withGroundingX1(0.3d)
            .withGroundingR2(0.04d)
            .withGroundingX2(0.95d)
            .add();

        Network network2 = allFormatsRoundTripTest(network, "/fortescue/twoWindingsTransformerFortescueWithMagnetizingReactanceRef.xml");

        TwoWindingsTransformer twt2 = network2.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt2);
        TwoWindingsTransformerFortescue fortescue2 = twt2.getExtension(TwoWindingsTransformerFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(fortescue.isFreeFluxes(), fortescue2.isFreeFluxes());
        assertEquals(fortescue.getXm(), fortescue2.getXm());
    }

    @Test
    void testV10() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        var twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt);
        TwoWindingsTransformerFortescue fortescue = twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
            .withRz(0.1d)
            .withXz(2d)
            .withFreeFluxes(false)
            .withXm(0.5d)
            .withConnectionType1(WindingConnectionType.Y_GROUNDED)
            .withConnectionType2(WindingConnectionType.DELTA)
            .withGroundingR1(0.02d)
            .withGroundingX1(0.3d)
            .withGroundingR2(0.04d)
            .withGroundingX2(0.95d)
            .add();

        Network network2 = allFormatsRoundTripTest(network, "/fortescue/twoWindingsTransformerFortescueRef_V1_0.xml",
            new ExportOptions()
                .addExtensionVersion(TwoWindingsTransformerFortescue.NAME, "1.0")
                .setVersion(IidmVersion.V_1_14.toString(".")));

        TwoWindingsTransformer twt2 = network2.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt2);
        TwoWindingsTransformerFortescue fortescue2 = twt2.getExtension(TwoWindingsTransformerFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(fortescue.isFreeFluxes(), fortescue2.isFreeFluxes());
        assertTrue(Double.isNaN(fortescue2.getXm()));
    }
}
