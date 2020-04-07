/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CoordinatedReactiveControlXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);

        gen.newExtension(CoordinatedReactiveControlAdder.class).withQPercent(100.0).add();

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/coordinatedReactiveControl.xml");

        Generator gen2 = network2.getGenerator("GEN");
        assertNotNull(gen2);
        CoordinatedReactiveControl coordinatedReactiveControl2 = gen2.getExtension(CoordinatedReactiveControl.class);
        assertNotNull(coordinatedReactiveControl2);

        assertEquals(100.0, coordinatedReactiveControl2.getQPercent(), 0.0);
        assertEquals("coordinatedReactiveControl", coordinatedReactiveControl2.getName());
    }

}
