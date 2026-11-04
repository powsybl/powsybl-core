/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedanceAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
class LineNonReciprocalImpedanceXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        network.getLine("NHV1_NHV2_1").newExtension(LineNonReciprocalImpedanceAdder.class)
                .withR21(-1.2)
                .withX21(35.1)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/lineNonReciprocalImpedanceRef.xml");

        Line line2 = network2.getLine("NHV1_NHV2_1");
        LineNonReciprocalImpedance extension = line2.getExtension(LineNonReciprocalImpedance.class);
        assertNotNull(extension);
        assertEquals(-1.2, extension.getR21(), 0);
        assertEquals(35.1, extension.getX21(), 0);
    }
}
