/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CoordinatedReactiveControlXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);

        gen.newExtension(CoordinatedReactiveControlAdder.class).withQPercent(100.0).add();

        Network network2 = allFormatsRoundTripTest(network, "/coordinatedReactiveControl.xml", CURRENT_IIDM_VERSION);

        Generator gen2 = network2.getGenerator("GEN");
        assertNotNull(gen2);
        CoordinatedReactiveControl coordinatedReactiveControl2 = gen2.getExtension(CoordinatedReactiveControl.class);
        assertNotNull(coordinatedReactiveControl2);

        assertEquals(100.0, coordinatedReactiveControl2.getQPercent(), 0.0);
        assertEquals("coordinatedReactiveControl", coordinatedReactiveControl2.getName());
    }

}
