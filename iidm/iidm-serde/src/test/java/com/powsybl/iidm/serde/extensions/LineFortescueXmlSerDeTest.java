/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LineFortescueXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testXmlSerializer() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        Line l = network.getLine("NHV1_NHV2_1");
        assertNotNull(l);
        LineFortescue fortescue = l.newExtension(LineFortescueAdder.class)
                .withRz(0.1d)
                .withXz(2d)
                .withOpenPhaseA(true)
                .withOpenPhaseC(true)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/fortescue/lineFortescueRef.xml");

        Line l2 = network2.getLine("NHV1_NHV2_1");
        assertNotNull(l2);
        LineFortescue fortescue2 = l2.getExtension(LineFortescue.class);
        assertNotNull(fortescue2);

        assertEquals(fortescue.getRz(), fortescue2.getRz(), 0);
        assertEquals(fortescue.getXz(), fortescue2.getXz(), 0);
        assertTrue(fortescue2.isOpenPhaseA());
        assertFalse(fortescue2.isOpenPhaseB());
        assertTrue(fortescue2.isOpenPhaseC());
    }
}
