/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ThreeWindingsTransformerFortescueXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testXmlSerializer() throws IOException {
        var network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        var twt = network.getThreeWindingsTransformer("3WT");
        assertNotNull(twt);

        var fortescue = twt.newExtension(ThreeWindingsTransformerFortescueAdder.class)
                .leg1()
                    .withRz(0.1d)
                    .withXz(2d)
                    .withFreeFluxes(true)
                    .withConnectionType(WindingConnectionType.Y_GROUNDED)
                    .withGroundingR(0.02d)
                    .withGroundingX(0.3d)
                .leg2()
                    .withRz(0.2d)
                    .withXz(2.1d)
                    .withFreeFluxes(false)
                    .withConnectionType(WindingConnectionType.Y)
                    .withGroundingR(0.12d)
                    .withGroundingX(0.4d)
                .leg3()
                    .withRz(0.3d)
                    .withXz(2.2d)
                    .withFreeFluxes(true)
                    .withConnectionType(WindingConnectionType.DELTA)
                    .withGroundingR(0.22d)
                    .withGroundingX(0.5d)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/fortescue/threeWindingsTransformerFortescueRef.xml");

        var twt2 = network2.getThreeWindingsTransformer("3WT");
        assertNotNull(twt2);
        var fortescue2 = twt2.getExtension(ThreeWindingsTransformerFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(0.1d, fortescue.getLeg1().getRz());
        assertEquals(2d, fortescue.getLeg1().getXz());
        assertTrue(fortescue.getLeg1().isFreeFluxes());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getLeg1().getConnectionType());
        assertEquals(0.02d, fortescue.getLeg1().getGroundingR());
        assertEquals(0.3d, fortescue.getLeg1().getGroundingX());
        assertEquals(0.2d, fortescue.getLeg2().getRz());
        assertEquals(2.1d, fortescue.getLeg2().getXz());
        assertFalse(fortescue.getLeg2().isFreeFluxes());
        assertSame(WindingConnectionType.Y, fortescue.getLeg2().getConnectionType());
        assertEquals(0.12d, fortescue.getLeg2().getGroundingR());
        assertEquals(0.4d, fortescue.getLeg2().getGroundingX());
        assertEquals(0.3d, fortescue.getLeg3().getRz());
        assertEquals(2.2d, fortescue.getLeg3().getXz());
        assertTrue(fortescue.getLeg3().isFreeFluxes());
        assertSame(WindingConnectionType.DELTA, fortescue.getLeg3().getConnectionType());
        assertEquals(0.22d, fortescue.getLeg3().getGroundingR());
        assertEquals(0.5d, fortescue.getLeg3().getGroundingX());
    }
}
