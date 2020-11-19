/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TieLineXmlTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        roundTripAllVersionedXmlTest("tieline.xml");
    }

    @Test
    public void ignoreIncorrectBoundaryValues() {
        Network network = NetworkXml.read(getVersionedNetworkAsStream("tielineNullOtherSideValues.xml", IidmXmlConstants.CURRENT_IIDM_XML_VERSION));
        TieLine tl = (TieLine) network.getLine("NHV1_NHV2_1");
        assertNotEquals(0.0, tl.getHalf1().getOtherSide().getP(), 0.0);
        assertNotEquals(0.0, tl.getHalf1().getOtherSide().getQ(), 0.0);
        assertNotEquals(0.0, tl.getHalf1().getOtherSide().getV(), 0.0);
        assertNotEquals(0.0, tl.getHalf1().getOtherSide().getAngle(), 0.0);
        assertNotEquals(0.0, tl.getHalf2().getOtherSide().getP(), 0.0);
        assertNotEquals(0.0, tl.getHalf2().getOtherSide().getQ(), 0.0);
        assertNotEquals(0.0, tl.getHalf2().getOtherSide().getV(), 0.0);
        assertNotEquals(0.0, tl.getHalf2().getOtherSide().getAngle(), 0.0);
    }
}
