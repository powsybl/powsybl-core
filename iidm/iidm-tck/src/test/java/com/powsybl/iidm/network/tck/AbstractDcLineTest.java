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
public abstract class AbstractDcLineTest {

    private Network network;
    private DcNode dcNode1;
    private DcNode dcNode2;

    @BeforeEach
    void setup() {
        network = Network.create("test", "test");
        dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
    }

    @Test
    public void testBase() {
        String dcLine1Id = "dcLine1";
        DcLine dcLine1 = network.newDcLine()
                .setId(dcLine1Id)
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(true)
                .setR(1.1)
                .add();
        assertSame(IdentifiableType.DC_LINE, dcLine1.getType());
        assertEquals(dcLine1Id, dcLine1.getId());
        assertTrue(dcLine1.getDcTerminal1().isConnected());
        assertTrue(dcLine1.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcLine1.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcLine1.getDcTerminal2().getDcNode());
        assertSame(dcLine1, dcLine1.getDcTerminal1().getDcConnectable());
        assertSame(dcLine1, dcLine1.getDcTerminal2().getDcConnectable());
        assertEquals(1, network.getDcLineCount());
        assertEquals(1.1, dcLine1.getR());

        String dcLine2Id = "dcLine2";
        DcLine dcLine2 = network.newDcLine()
                .setId(dcLine2Id)
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(false)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(false)
                .setR(1.2)
                .add();
        assertEquals(dcLine2Id, dcLine2.getId());
        assertFalse(dcLine2.getDcTerminal1().isConnected());
        assertFalse(dcLine2.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcLine2.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcLine2.getDcTerminal2().getDcNode());
        assertEquals(1.2, dcLine2.getR());

        List<DcLine> dcLineList = List.of(dcLine1, dcLine2);

        assertEquals(2, ((Collection<?>) network.getDcLines()).size());
        network.getDcLines().forEach(dcLine -> assertTrue(dcLineList.contains(dcLine)));
        network.getDcLineStream().forEach(dcLine -> assertTrue(dcLineList.contains(dcLine)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.DC_LINE).count());
        network.getIdentifiableStream(IdentifiableType.DC_LINE).forEach(dcLine -> assertTrue(dcLineList.contains((DcLine) dcLine)));
        assertEquals(2, network.getDcLineCount());
    }

    @Test
    public void testGetterSetter() {
        DcLine dcLine = network.newDcLine()
                .setId("dcLine")
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(true)
                .setR(1.1)
                .add();
        assertEquals(1.1, dcLine.getR());
        dcLine.setR(1.2);
        assertEquals(1.2, dcLine.getR());
        dcLine.setR(0.0);
        assertEquals(0.0, dcLine.getR());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcLine.setR(Double.NaN));
        assertEquals("DC Line 'dcLine': r is invalid", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> dcLine.setR(-1.0));
        assertEquals("DC Line 'dcLine': r is invalid", e2.getMessage());
    }

    @Test
    public void testCreateDuplicate() {
        network.newDcLine()
                .setId("dcLine1")
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(true)
                .setR(1.1)
                .add();
        DcLineAdder dcLineDuplicateAdder = network.newDcLine()
                .setId("dcLine1")
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(false)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(false)
                .setR(1.2);
        PowsyblException exception = assertThrows(PowsyblException.class, dcLineDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcLine1'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testRemove() {
        String dcLine1Id = "dcLine1";
        DcLine dcLine1 = network.newDcLine()
                .setId(dcLine1Id)
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setR(1.1)
                .add();
        DcTerminal t1 = dcLine1.getDcTerminal1();
        DcTerminal t2 = dcLine1.getDcTerminal2();
        String dcLine2Id = "dcLine2";
        DcLine dcLine2 = network.newDcLine()
                .setId(dcLine2Id)
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setR(1.2)
                .add();
        assertEquals(2, network.getDcLineCount());
        dcLine1.remove();
        assertNull(network.getDcLine(dcLine1Id));
        assertEquals(1, network.getDcLineCount());
        dcLine2.remove();
        assertNull(network.getDcLine(dcLine2Id));
        assertEquals(0, network.getDcLineCount());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcLine1.setR(2.));
        assertEquals("Cannot modify r of removed equipment dcLine1", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, dcLine1::getR);
        assertEquals("Cannot access r of removed equipment dcLine1", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, t1::isConnected);
        assertEquals("Cannot access removed equipment dcLine1", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, () -> t2.setConnected(false));
        assertEquals("Cannot modify removed equipment dcLine1", e4.getMessage());
    }

    @Test
    public void testCreationError() {
        DcLineAdder adder = network.newDcLine();

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line id is not set", e1.getMessage());

        adder.setId("dcLine");
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': dcNode1Id is not set", e2.getMessage());

        adder.setDcNode1Id("notExists");
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': DcNode 'notExists' not found", e3.getMessage());

        adder.setDcNode1Id(dcNode1.getId());
        PowsyblException e4 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': dcNode2Id is not set", e4.getMessage());

        adder.setDcNode2Id("notExists");
        PowsyblException e5 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': DcNode 'notExists' not found", e5.getMessage());

        adder.setDcNode2Id(dcNode2.getId());
        PowsyblException e6 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': r is invalid", e6.getMessage());

        adder.setR(Double.NaN);
        PowsyblException e7 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': r is invalid", e7.getMessage());

        adder.setR(-1.0);
        PowsyblException e8 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLine': r is invalid", e8.getMessage());
    }

    @Test
    public void testOnSubnetwork() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnetwork1").setNominalV(500.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnetwork1").setNominalV(500.).add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnetwork2").setNominalV(500.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnetwork2").setNominalV(500.).add();

        DcLine dcLine1Subnet1 = subnetwork1.newDcLine().setId("dcLine1Subnetwork1")
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode2Subnet1.getId())
                .setR(1.0)
                .add();
        DcLine dcLine2Subnet1 = subnetwork1.newDcLine().setId("dcLine2Subnetwork1")
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode2Subnet1.getId())
                .setR(1.0)
                .add();
        DcLine dcLine1Subnet2 = subnetwork2.newDcLine().setId("dcLine1Subnetwork2")
                .setDcNode1Id(dcNode1Subnet2.getId())
                .setDcNode2Id(dcNode2Subnet2.getId())
                .setR(1.0)
                .add();

        List<DcLine> dcLineList = List.of(dcLine1Subnet1, dcLine2Subnet1, dcLine1Subnet2);

        // network content
        assertEquals(3, ((Collection<?>) netWithSubnet.getDcLines()).size());
        netWithSubnet.getDcLines().forEach(dcLine -> assertTrue(dcLineList.contains(dcLine)));
        netWithSubnet.getDcLineStream().forEach(dcLine -> assertTrue(dcLineList.contains(dcLine)));
        assertEquals(3, netWithSubnet.getIdentifiableStream(IdentifiableType.DC_LINE).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.DC_LINE).forEach(dcLine -> assertTrue(dcLineList.contains((DcLine) dcLine)));
        assertEquals(3, netWithSubnet.getDcLineCount());
        assertSame(dcLine1Subnet1, netWithSubnet.getDcLine(dcLine1Subnet1.getId()));
        assertSame(dcLine2Subnet1, netWithSubnet.getDcLine(dcLine2Subnet1.getId()));
        assertSame(dcLine1Subnet2, netWithSubnet.getDcLine(dcLine1Subnet2.getId()));

        // subnetwork1 content
        List<DcLine> dcLineListSubnet1 = List.of(dcLine1Subnet1, dcLine2Subnet1);
        assertEquals(2, ((Collection<?>) subnetwork1.getDcLines()).size());
        subnetwork1.getDcLines().forEach(dcLine -> assertTrue(dcLineListSubnet1.contains(dcLine)));
        subnetwork1.getDcLineStream().forEach(dcLine -> assertTrue(dcLineListSubnet1.contains(dcLine)));
        assertEquals(2, subnetwork1.getIdentifiableStream(IdentifiableType.DC_LINE).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_LINE).forEach(dcNode -> assertTrue(dcLineListSubnet1.contains((DcLine) dcNode)));
        assertEquals(2, subnetwork1.getDcLineCount());
        assertSame(dcLine1Subnet1, subnetwork1.getDcLine(dcLine1Subnet1.getId()));
        assertSame(dcLine2Subnet1, subnetwork1.getDcLine(dcLine2Subnet1.getId()));
        assertNull(subnetwork1.getDcLine(dcLine1Subnet2.getId()));

        // subnetwork2 content
        List<DcLine> dcLineListSubnet2 = List.of(dcLine1Subnet2);
        assertEquals(1, ((Collection<?>) subnetwork2.getDcLines()).size());
        subnetwork2.getDcLines().forEach(dcLine -> assertTrue(dcLineListSubnet2.contains(dcLine)));
        subnetwork2.getDcLineStream().forEach(dcLine -> assertTrue(dcLineListSubnet2.contains(dcLine)));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.DC_LINE).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.DC_LINE).forEach(dcLine -> assertTrue(dcLineListSubnet2.contains((DcLine) dcLine)));
        assertEquals(1, subnetwork2.getDcLineCount());
        assertNull(subnetwork2.getDcLine(dcLine1Subnet1.getId()));
        assertNull(subnetwork2.getDcLine(dcLine2Subnet1.getId()));
        assertSame(dcLine1Subnet2, subnetwork2.getDcLine(dcLine1Subnet2.getId()));
    }

    @Test
    public void testNotAcrossNetworkSubnetworks() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnetwork1").setNominalV(500.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnetwork1").setNominalV(500.).add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnetwork2").setNominalV(500.).add();
        DcNode dcNodeRootNetwork = netWithSubnet.newDcNode().setId("dcNodeRootNetwork").setNominalV(500.).add();

        // test cannot create DcLine across subnetwork1 & subnetwork2
        DcLineAdder adder = netWithSubnet.newDcLine().setId("dcLineAcrossSubnets")
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode1Subnet2.getId())
                .setR(1.0);
        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLineAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNode1Subnetwork2' are in different networks 'subnetwork1' and 'subnetwork2'", e1.getMessage());

        // test cannot create DcLine in netWithSubnet referencing nodes of subnetwork1
        adder.setDcNode1Id(dcNode1Subnet1.getId()).setDcNode2Id(dcNode2Subnet1.getId());
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLineAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNode2Subnetwork1' are in network 'subnetwork1' but DC Line is in 'test'", e2.getMessage());

        // test cannot create DcLine across subnetwork1 & netWithSubnet
        adder.setDcNode2Id(dcNodeRootNetwork.getId());
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Line 'dcLineAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNodeRootNetwork' are in different networks 'subnetwork1' and 'test'", e3.getMessage());
    }
}
