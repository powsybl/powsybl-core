/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class LineXmlTest extends AbstractConverterTest {

    private static final String REF_XML = "tieLineRef.xml";

    private static final String REFS_V1_0 = "/refs_V1_0/";

    @Test
    public void readV10TieLineWithoutExt() {
        Network network = NetworkXml.read(getClass().getResourceAsStream(REFS_V1_0 + REF_XML));
        assertEquals(1, network.getLineCount());
        Line testTie = network.getLine("testTie");
        assertFalse(testTie.isTieLine());
    }

    @Test
    public void danglingLine() throws IOException {
        roundTripXmlTest(NoEquipmentNetworkFactory.createWithDanglingLine(),
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/danglingLineRef.xml");
    }
}
