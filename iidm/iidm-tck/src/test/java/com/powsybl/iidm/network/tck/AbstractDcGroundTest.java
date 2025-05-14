/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractDcGroundTest {

    private Network network;
    private DcNode dcNode;

    @BeforeEach
    void setup() {
        network = Network.create("test", "test");
        dcNode = network.newDcNode().setId("dcNode").setNominalV(1.).add();
    }

    @Test
    public void testBase() {
        String dcGround1Id = "dcGround1";
        DcGround dcGround1 = network.newDcGround()
                .setId(dcGround1Id)
                .setDcNodeId(dcNode.getId())
                .setConnected(true)
                .setR(0.1)
                .add();
        assertSame(IdentifiableType.DC_GROUND, dcGround1.getType());
        assertEquals(dcGround1Id, dcGround1.getId());
        assertTrue(dcGround1.getDcTerminal().isConnected());
        assertEquals(1, dcGround1.getDcTerminals().size());
        assertSame(dcGround1.getDcTerminals().get(0), dcGround1.getDcTerminal());
        assertSame(dcGround1, dcGround1.getDcTerminal().getDcConnectable());
        assertSame(dcNode, dcGround1.getDcTerminal().getDcNode());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(0.1, dcGround1.getR());

        String dcGround2Id = "dcGround2";
        DcGround dcGround2 = network.newDcGround()
                .setId(dcGround2Id)
                .setDcNodeId(dcNode.getId())
                .setConnected(false)
                .setR(0.2)
                .add();
        assertEquals(dcGround2Id, dcGround2.getId());
        assertEquals(0.2, dcGround2.getR());
        assertFalse(dcGround2.getDcTerminal().isConnected());
        assertSame(dcNode, dcGround2.getDcTerminal().getDcNode());

        List<DcGround> dcGroundList = List.of(dcGround1, dcGround2);

        assertEquals(2, ((Collection<?>) network.getDcGrounds()).size());
        network.getDcGrounds().forEach(dcGround -> assertTrue(dcGroundList.contains(dcGround)));
        network.getDcGroundStream().forEach(dcGround -> assertTrue(dcGroundList.contains(dcGround)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.DC_GROUND).count());
        network.getIdentifiableStream(IdentifiableType.DC_GROUND).forEach(dcGround -> assertTrue(dcGroundList.contains((DcGround) dcGround)));
        assertEquals(2, network.getDcGroundCount());
    }

    @Test
    public void testGetterSetter() {
        DcGround dcGround = network.newDcGround()
                .setId("dcGround")
                .setDcNodeId(dcNode.getId())
                .setConnected(true)
                .setR(0.1)
                .add();
        assertEquals(0.1, dcGround.getR());
        dcGround.setR(0.2);
        assertEquals(0.2, dcGround.getR());
        dcGround.setR(0.0);
        assertEquals(0.0, dcGround.getR());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcGround.setR(Double.NaN));
        assertEquals("DC Ground 'dcGround': r is invalid", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> dcGround.setR(-1.0));
        assertEquals("DC Ground 'dcGround': r is invalid", e2.getMessage());
    }

    @Test
    public void testCreateDuplicate() {
        network.newDcGround()
                .setId("dcGround")
                .setDcNodeId(dcNode.getId())
                .setConnected(true)
                .setR(0.1)
                .add();
        DcGroundAdder dcGroundDuplicateAdder = network.newDcGround()
                .setId("dcGround")
                .setDcNodeId(dcNode.getId())
                .setConnected(true)
                .setR(0.1);
        PowsyblException exception = assertThrows(PowsyblException.class, dcGroundDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcGround'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testRemove() {
        String dcGround1Id = "dcGround1";
        DcGround dcGround1 = network.newDcGround()
                .setId(dcGround1Id)
                .setDcNodeId(dcNode.getId())
                .setR(1.1)
                .add();
        DcTerminal t = dcGround1.getDcTerminal();
        String dcGround2Id = "dcGround2";
        DcGround dcGround2 = network.newDcGround()
                .setId(dcGround2Id)
                .setDcNodeId(dcNode.getId())
                .setR(1.2)
                .add();
        assertEquals(2, network.getDcGroundCount());
        dcGround1.remove();
        assertNull(network.getDcGround(dcGround1Id));
        assertEquals(1, network.getDcGroundCount());
        dcGround2.remove();
        assertNull(network.getDcGround(dcGround2Id));
        assertEquals(0, network.getDcGroundCount());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcGround1.setR(2.));
        assertEquals("Cannot modify r of removed equipment dcGround1", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, dcGround1::getR);
        assertEquals("Cannot access r of removed equipment dcGround1", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, t::isConnected);
        assertEquals("Cannot access removed equipment dcGround1", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, () -> t.setConnected(false));
        assertEquals("Cannot modify removed equipment dcGround1", e4.getMessage());
    }

    @Test
    public void testCreationError() {
        DcGroundAdder adder = network.newDcGround();

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Ground id is not set", e1.getMessage());

        adder.setId("dcGround");
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Ground 'dcGround': dcNodeId is not set", e2.getMessage());

        adder.setDcNodeId("notExists");
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Ground 'dcGround': DcNode 'notExists' not found", e3.getMessage());

        adder.setDcNodeId(dcNode.getId());
        adder.setR(Double.NaN);
        PowsyblException e4 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Ground 'dcGround': r is invalid", e4.getMessage());

        adder.setR(-1.0);
        PowsyblException e5 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Ground 'dcGround': r is invalid", e5.getMessage());
    }

    @Test
    public void testOnSubnetwork() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnetwork1").setNominalV(1.).add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnetwork2").setNominalV(1.).add();

        DcGround dcGround1Subnet1 = subnetwork1.newDcGround().setId("dcGround1Subnetwork1")
                .setDcNodeId(dcNode1Subnet1.getId())
                .add();
        DcGround dcGround2Subnet1 = subnetwork1.newDcGround().setId("dcGround2Subnetwork1")
                .setDcNodeId(dcNode1Subnet1.getId())
                .add();
        DcGround dcGround1Subnet2 = subnetwork2.newDcGround().setId("dcGround1Subnetwork2")
                .setDcNodeId(dcNode1Subnet2.getId())
                .add();

        List<DcGround> dcGroundList = List.of(dcGround1Subnet1, dcGround2Subnet1, dcGround1Subnet2);

        // network content
        assertEquals(3, ((Collection<?>) netWithSubnet.getDcGrounds()).size());
        netWithSubnet.getDcGrounds().forEach(dcGround -> assertTrue(dcGroundList.contains(dcGround)));
        netWithSubnet.getDcGroundStream().forEach(dcGround -> assertTrue(dcGroundList.contains(dcGround)));
        assertEquals(3, netWithSubnet.getIdentifiableStream(IdentifiableType.DC_GROUND).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.DC_GROUND).forEach(dcGround -> assertTrue(dcGroundList.contains((DcGround) dcGround)));
        assertEquals(3, netWithSubnet.getDcGroundCount());
        assertSame(dcGround1Subnet1, netWithSubnet.getDcGround(dcGround1Subnet1.getId()));
        assertSame(dcGround2Subnet1, netWithSubnet.getDcGround(dcGround2Subnet1.getId()));
        assertSame(dcGround1Subnet2, netWithSubnet.getDcGround(dcGround1Subnet2.getId()));

        // subnetwork1 content
        List<DcGround> dcGroundListSubnet1 = List.of(dcGround1Subnet1, dcGround2Subnet1);
        assertEquals(2, ((Collection<?>) subnetwork1.getDcGrounds()).size());
        subnetwork1.getDcGrounds().forEach(dcGround -> assertTrue(dcGroundListSubnet1.contains(dcGround)));
        subnetwork1.getDcGroundStream().forEach(dcGround -> assertTrue(dcGroundListSubnet1.contains(dcGround)));
        assertEquals(2, subnetwork1.getIdentifiableStream(IdentifiableType.DC_GROUND).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_GROUND).forEach(dcGround -> assertTrue(dcGroundListSubnet1.contains((DcGround) dcGround)));
        assertEquals(2, subnetwork1.getDcGroundCount());
        assertSame(dcGround1Subnet1, subnetwork1.getDcGround(dcGround1Subnet1.getId()));
        assertSame(dcGround2Subnet1, subnetwork1.getDcGround(dcGround2Subnet1.getId()));
        assertNull(subnetwork1.getDcGround(dcGround1Subnet2.getId()));

        // subnetwork2 content
        List<DcGround> dcGroundListSubnet2 = List.of(dcGround1Subnet2);
        assertEquals(1, ((Collection<?>) subnetwork2.getDcGrounds()).size());
        subnetwork2.getDcGrounds().forEach(dcGround -> assertTrue(dcGroundListSubnet2.contains(dcGround)));
        subnetwork2.getDcGroundStream().forEach(dcGround -> assertTrue(dcGroundListSubnet2.contains(dcGround)));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.DC_GROUND).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.DC_GROUND).forEach(dcGround -> assertTrue(dcGroundListSubnet2.contains((DcGround) dcGround)));
        assertEquals(1, subnetwork2.getDcGroundCount());
        assertNull(subnetwork2.getDcGround(dcGround1Subnet1.getId()));
        assertNull(subnetwork2.getDcGround(dcGround2Subnet1.getId()));
        assertSame(dcGround1Subnet2, subnetwork2.getDcGround(dcGround1Subnet2.getId()));
    }

    @Test
    public void testNotAcrossNetworkSubnetworks() {
        Network netWithSubnet = Network.create("test", "test");
        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnetwork1").setNominalV(1.).add();
        DcNode dcNodeRootNetwork = netWithSubnet.newDcNode().setId("dcNodeRootNetwork").setNominalV(1.).add();

        // test cannot create DcGround in netWithSubnet referencing nodes of subnetwork1
        DcGroundAdder adder1 = netWithSubnet.newDcGround().setId("dcGround1")
                .setDcNodeId(dcNode1Subnet1.getId());
        PowsyblException e1 = assertThrows(PowsyblException.class, adder1::add);
        assertEquals("DC Ground 'dcGround1': DC Node 'dcNode1Subnetwork1' is in network 'subnetwork1' but DC Equipment is in 'test'", e1.getMessage());

        // test cannot create DcGround in subnetwork1 referencing nodes of netWithSubnet
        DcGroundAdder adder2 = subnetwork1.newDcGround().setId("dcGround2")
                .setDcNodeId(dcNodeRootNetwork.getId());
        PowsyblException e2 = assertThrows(PowsyblException.class, adder2::add);
        assertEquals("DC Ground 'dcGround2': DC Node 'dcNodeRootNetwork' is in network 'test' but DC Equipment is in 'subnetwork1'", e2.getMessage());
    }

}
