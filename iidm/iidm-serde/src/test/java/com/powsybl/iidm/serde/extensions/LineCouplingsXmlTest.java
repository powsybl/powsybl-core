/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplings;
import com.powsybl.iidm.network.extensions.LineCouplingsAdder;
import com.powsybl.iidm.network.extensions.MutualCoupling;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class LineCouplingsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        network.newExtension(LineCouplingsAdder.class).add();
        network.getExtension(LineCouplings.class)
                .newMutualCoupling()
                .withLine1(network.getLine("NHV1_NHV2_1"))
                .withLine2(network.getLine("NHV1_NHV2_2"))
                .withR(0.5)
                .withX(1.0)
                .add();
        Network network2 = allFormatsRoundTripTest(network, "/lineCouplingsRef_V1_0.xml");

        LineCouplings mutualCouplings = network2.getExtension(LineCouplings.class);
        assertEquals(1, mutualCouplings.getMutualCouplings().size());
        MutualCoupling mutualCoupling = mutualCouplings.getMutualCouplings().getFirst();
        assertEquals("NHV1_NHV2_1", mutualCoupling.getLine1().getId());
        assertEquals("NHV1_NHV2_2", mutualCoupling.getLine2().getId());
        assertEquals(0.5, mutualCoupling.getR());
        assertEquals(1, mutualCoupling.getX());
        assertEquals(0, mutualCoupling.getLine1Start());
        assertEquals(0, mutualCoupling.getLine2Start());
        assertEquals(1, mutualCoupling.getLine1End());
        assertEquals(1, mutualCoupling.getLine2End());
    }
}
