/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class LineXmlTest extends AbstractConverterTest {

    @Test
    public void tieLine() throws IOException {
        roundTripXmlTest(NoEquipmentNetworkFactory.createWithTieLine(),
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/tieLineRef.xml");
    }

    @Test
    public void danglingLine() throws IOException {
        roundTripXmlTest(NoEquipmentNetworkFactory.createWithDanglingLine(),
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/danglingLineRef.xml");
    }
}
