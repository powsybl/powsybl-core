/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class PropertiesXmlTest extends AbstractConverterTest {

    private Network createNetwork() {
        return NetworkXml.read(getClass().getResourceAsStream("/eurostag-tutorial-example1-properties.xml"));
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(createNetwork(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/eurostag-tutorial-example1-properties.xml");
    }
}
