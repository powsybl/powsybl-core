/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasImpl.ControlArea;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;

import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesSshControlAreasXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2020-09-07T15:44:10.209+02:00"));
        ControlArea controlArea1 = new ControlArea("_a5bed00f-624f-4c92-8bac-5a4d66fc3213", 274.5, 0.5);
        ControlArea controlArea2 = new ControlArea("_5694535c-442f-4b47-97bc-673296d068bb", 43.5, 0.25);

        network.newExtension(CgmesSshControlAreasAdder.class)
            .addControlArea(controlArea1)
            .addControlArea(controlArea2)
            .add();
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead, "/extensions/eurostag_cgmes_ssh_control_areas.xml");
    }
}
