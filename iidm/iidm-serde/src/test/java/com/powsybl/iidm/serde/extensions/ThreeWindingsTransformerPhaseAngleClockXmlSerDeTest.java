/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author José Antonio Marqués <marquesja at aia.es
 */
class ThreeWindingsTransformerPhaseAngleClockXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2018-03-05T13:30:30.486+01:00"));
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");

        transformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(3).withPhaseAngleClockLeg3(1).add();

        Network network2 = allFormatsRoundTripTest(network, "/threeWindingsTransformerPhaseAngleClock.xml", CURRENT_IIDM_VERSION);

        ThreeWindingsTransformerPhaseAngleClock pacXml = network2.getThreeWindingsTransformer("3WT").getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        assertNotNull(pacXml);
        assertEquals(3, pacXml.getPhaseAngleClockLeg2());
        assertEquals(1, pacXml.getPhaseAngleClockLeg3());
    }
}
