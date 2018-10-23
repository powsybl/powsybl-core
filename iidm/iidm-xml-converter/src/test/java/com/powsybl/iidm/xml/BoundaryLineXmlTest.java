/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class BoundaryLineXmlTest extends AbstractConverterTest {

    @Test
    public void testReadV10() throws IOException {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/refs_V1_0/danglingLineRef.xml"));
        BoundaryLine dl = network.getBoundaryLine("DL");
        assertNotNull(dl);
        assertEquals(6.0, dl.getR(), 0.0);
    }

    @Test
    public void boundaryLine() throws IOException {
        roundTripXmlTest(NoEquipmentNetworkFactory.createWithBoundaryLine(),
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/boundaryLineRef.xml");
    }
}
