/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util.immutable;

import com.powsybl.entsoe.util.*;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.immutable.ImmutableNetwork;
import org.junit.Test;

import static com.powsybl.commons.TestHelper.assertInvalidInvocation;
import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableEntsoeTest {

    private static final String EXPECTED_MSG = "Unmodifiable extension";

    @Test
    public void testXnode() {
        Network network = XnodeXmlSerializerTest.createTestNetwork();
        // extends dangling line
        DanglingLine dl = network.getDanglingLine("DL");
        Xnode xnode = new Xnode(dl, "XXXXXX11");
        dl.addExtension(Xnode.class, xnode);

        Network network2 = ImmutableNetwork.of(network);

        DanglingLine dl2 = network2.getDanglingLine("DL");
        Xnode xnode2 = dl2.getExtension(Xnode.class);
        assertNotNull(xnode2);
        assertEquals(xnode.getCode(), xnode2.getCode());

        assertSame(dl2, xnode2.getExtendable());
        assertInvalidInvocation(() -> xnode2.setCode("code"), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setExtendable(dl), EXPECTED_MSG);
    }

    @Test
    public void testMergedXnode() {
        Network network = MergedXnodeXmlSerializerTest.createTestNetwork();
        // extends line
        Line line = network.getLine("L");
        MergedXnode xnode = new MergedXnode(line, 0.5f, 0.5f, 1.0, 2.0, 3.0, 4.0, "XXXXXX11");
        line.addExtension(MergedXnode.class, xnode);

        Network network2 = ImmutableNetwork.of(network);

        Line line2 = network2.getLine("L");
        MergedXnode xnode2 = line2.getExtension(MergedXnode.class);
        assertNotNull(xnode2);
        assertEquals(xnode.getRdp(), xnode2.getRdp(), 0f);
        assertEquals(xnode.getXdp(), xnode2.getXdp(), 0f);
        assertEquals(xnode.getXnodeP1(), xnode2.getXnodeP1(), 0.0);
        assertEquals(xnode.getXnodeQ1(), xnode2.getXnodeQ1(), 0.0);
        assertEquals(xnode.getXnodeP2(), xnode2.getXnodeP2(), 0.0);
        assertEquals(xnode.getXnodeQ2(), xnode2.getXnodeQ2(), 0.0);
        assertEquals(xnode.getCode(), xnode2.getCode());
        assertSame(line2, xnode2.getExtendable());

        assertInvalidInvocation(() -> xnode2.setExtendable(line), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setCode("cd"), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setRdp(1.0f), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setXdp(1.0f), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setXnodeP1(1.0f), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setXnodeP2(1.0f), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setXnodeQ1(1.0f), EXPECTED_MSG);
        assertInvalidInvocation(() -> xnode2.setXnodeQ2(1.0f), EXPECTED_MSG);
    }

    @Test
    public void testEntsoeArea() {
        Network network = EntsoeAreaXmlSerializerTest.createTestNetwork();
        Substation s = network.getSubstation("S");
        EntsoeArea country = new EntsoeArea(s, EntsoeGeographicalCode.BE);
        s.addExtension(EntsoeArea.class, country);

        Network network2 = ImmutableNetwork.of(network);

        Substation s2 = network2.getSubstation("S");
        EntsoeArea country2 = s2.getExtension(EntsoeArea.class);
        assertNotNull(country2);
        assertEquals(country.getCode(), country2.getCode());
        assertSame(s2, country2.getExtendable());

        assertInvalidInvocation(() -> country2.setExtendable(s), EXPECTED_MSG);
        assertInvalidInvocation(() -> country2.setCode(EntsoeGeographicalCode.D1), EXPECTED_MSG);
    }
}
