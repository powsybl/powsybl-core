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
public abstract class AbstractDcSwitchTest {

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
        String dcSwitch1Id = "dcSwitch1";
        DcSwitch dcSwitch1 = network.newDcSwitch()
                .setId(dcSwitch1Id)
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(false)
                .setRetained(false)
                .add();
        assertSame(IdentifiableType.DC_SWITCH, dcSwitch1.getType());
        assertEquals(dcSwitch1Id, dcSwitch1.getId());
        assertFalse(dcSwitch1.isOpen());
        assertFalse(dcSwitch1.isRetained());
        assertSame(dcNode1, dcSwitch1.getDcNode1());
        assertSame(dcNode2, dcSwitch1.getDcNode2());
        assertEquals(1, network.getDcSwitchCount());

        String dcSwitch2Id = "dcSwitch2";
        DcSwitch dcSwitch2 = network.newDcSwitch()
                .setId(dcSwitch2Id)
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(true)
                .setRetained(true)
                .add();
        assertEquals(dcSwitch2Id, dcSwitch2.getId());
        assertTrue(dcSwitch2.isOpen());
        assertTrue(dcSwitch2.isRetained());
        assertSame(dcNode1, dcSwitch2.getDcNode1());
        assertSame(dcNode2, dcSwitch2.getDcNode2());

        List<DcSwitch> dcSwitchList = List.of(dcSwitch1, dcSwitch2);

        assertEquals(2, ((Collection<?>) network.getDcSwitches()).size());
        network.getDcSwitches().forEach(dcSwitch -> assertTrue(dcSwitchList.contains(dcSwitch)));
        network.getDcSwitchStream().forEach(dcSwitch -> assertTrue(dcSwitchList.contains(dcSwitch)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.DC_SWITCH).count());
        network.getIdentifiableStream(IdentifiableType.DC_SWITCH).forEach(dcSwitch -> assertTrue(dcSwitchList.contains((DcSwitch) dcSwitch)));
        assertEquals(2, network.getDcSwitchCount());
    }

    @Test
    public void testGetterSetter() {
        DcSwitch dcSwitch = network.newDcSwitch()
                .setId("dcSwitch")
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(true)
                .setRetained(true)
                .add();
        assertTrue(dcSwitch.isOpen());
        assertTrue(dcSwitch.isRetained());
        dcSwitch.setOpen(false);
        assertFalse(dcSwitch.isOpen());
        assertTrue(dcSwitch.isRetained());
        dcSwitch.setRetained(false);
        assertFalse(dcSwitch.isOpen());
        assertFalse(dcSwitch.isRetained());
    }

    @Test
    public void testCreateDuplicate() {
        network.newDcSwitch()
                .setId("dcSwitch1")
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(false)
                .setRetained(false)
                .add();
        DcSwitchAdder dcSwitchDuplicateAdder = network.newDcSwitch()
                .setId("dcSwitch1")
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(false)
                .setRetained(false);
        PowsyblException exception = assertThrows(PowsyblException.class, dcSwitchDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcSwitch1'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testRemove() {
        String dcSwitch1Id = "dcSwitch1";
        DcSwitch dcSwitch1 = network.newDcSwitch()
                .setId(dcSwitch1Id)
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(false)
                .setRetained(false)
                .add();
        String dcSwitch2Id = "dcSwitch2";
        DcSwitch dcSwitch2 = network.newDcSwitch()
                .setId(dcSwitch2Id)
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(true)
                .setRetained(true)
                .add();
        assertEquals(2, network.getDcSwitchCount());
        dcSwitch1.remove();
        assertNull(network.getDcSwitch(dcSwitch1Id));
        assertEquals(1, network.getDcSwitchCount());
        dcSwitch2.remove();
        assertNull(network.getDcSwitch(dcSwitch2Id));
        assertEquals(0, network.getDcSwitchCount());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcSwitch1.setOpen(true));
        assertEquals("Cannot modify open of removed equipment dcSwitch1", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, dcSwitch1::isOpen);
        assertEquals("Cannot access open of removed equipment dcSwitch1", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> dcSwitch1.setRetained(true));
        assertEquals("Cannot modify retained of removed equipment dcSwitch1", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, dcSwitch1::isRetained);
        assertEquals("Cannot access retained of removed equipment dcSwitch1", e4.getMessage());

        PowsyblException e5 = assertThrows(PowsyblException.class, dcSwitch1::getDcNode1);
        assertEquals("Cannot access dcNode1 of removed equipment dcSwitch1", e5.getMessage());

        PowsyblException e6 = assertThrows(PowsyblException.class, dcSwitch1::getDcNode2);
        assertEquals("Cannot access dcNode2 of removed equipment dcSwitch1", e6.getMessage());
    }

    @Test
    public void testCreationError() {
        DcSwitchAdder adder = network.newDcSwitch();

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch id is not set", e1.getMessage());

        adder.setId("dcSwitch");
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': dcNode1Id is not set", e2.getMessage());

        adder.setDcNode1Id("notExists");
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': DcNode 'notExists' not found", e3.getMessage());

        adder.setDcNode1Id(dcNode1.getId());
        PowsyblException e4 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': dcNode2Id is not set", e4.getMessage());

        adder.setDcNode2Id("notExists");
        PowsyblException e5 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': DcNode 'notExists' not found", e5.getMessage());

        adder.setDcNode2Id(dcNode2.getId());
        PowsyblException e6 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': retained is not set", e6.getMessage());

        adder.setRetained(false);
        PowsyblException e7 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': open is not set", e7.getMessage());
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

        DcSwitch dcSwitch1Subnet1 = subnetwork1.newDcSwitch().setId("dcSwitch1Subnetwork1")
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode2Subnet1.getId())
                .setRetained(false).setOpen(false)
                .add();
        DcSwitch dcSwitch2Subnet1 = subnetwork1.newDcSwitch().setId("dcSwitch2Subnetwork1")
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode2Subnet1.getId())
                .setRetained(false).setOpen(false)
                .add();
        DcSwitch dcSwitch1Subnet2 = subnetwork2.newDcSwitch().setId("dcSwitch1Subnetwork2")
                .setDcNode1Id(dcNode1Subnet2.getId())
                .setDcNode2Id(dcNode2Subnet2.getId())
                .setRetained(false).setOpen(false)
                .add();

        List<DcSwitch> dcSwitchList = List.of(dcSwitch1Subnet1, dcSwitch2Subnet1, dcSwitch1Subnet2);

        // network content
        assertEquals(3, ((Collection<?>) netWithSubnet.getDcSwitches()).size());
        netWithSubnet.getDcSwitches().forEach(dcSwitch -> assertTrue(dcSwitchList.contains(dcSwitch)));
        netWithSubnet.getDcSwitchStream().forEach(dcSwitch -> assertTrue(dcSwitchList.contains(dcSwitch)));
        assertEquals(3, netWithSubnet.getIdentifiableStream(IdentifiableType.DC_SWITCH).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.DC_SWITCH).forEach(dcSwitch -> assertTrue(dcSwitchList.contains((DcSwitch) dcSwitch)));
        assertEquals(3, netWithSubnet.getDcSwitchCount());
        assertSame(dcSwitch1Subnet1, netWithSubnet.getDcSwitch(dcSwitch1Subnet1.getId()));
        assertSame(dcSwitch2Subnet1, netWithSubnet.getDcSwitch(dcSwitch2Subnet1.getId()));
        assertSame(dcSwitch1Subnet2, netWithSubnet.getDcSwitch(dcSwitch1Subnet2.getId()));

        // subnetwork1 content
        List<DcSwitch> dcSwitchListSubnet1 = List.of(dcSwitch1Subnet1, dcSwitch2Subnet1);
        assertEquals(2, ((Collection<?>) subnetwork1.getDcSwitches()).size());
        subnetwork1.getDcSwitches().forEach(dcSwitch -> assertTrue(dcSwitchListSubnet1.contains(dcSwitch)));
        subnetwork1.getDcSwitchStream().forEach(dcSwitch -> assertTrue(dcSwitchListSubnet1.contains(dcSwitch)));
        assertEquals(2, subnetwork1.getIdentifiableStream(IdentifiableType.DC_SWITCH).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_SWITCH).forEach(dcNode -> assertTrue(dcSwitchListSubnet1.contains((DcSwitch) dcNode)));
        assertEquals(2, subnetwork1.getDcSwitchCount());
        assertSame(dcSwitch1Subnet1, subnetwork1.getDcSwitch(dcSwitch1Subnet1.getId()));
        assertSame(dcSwitch2Subnet1, subnetwork1.getDcSwitch(dcSwitch2Subnet1.getId()));
        assertNull(subnetwork1.getDcSwitch(dcSwitch1Subnet2.getId()));

        // subnetwork2 content
        List<DcSwitch> dcSwitchListSubnet2 = List.of(dcSwitch1Subnet2);
        assertEquals(1, ((Collection<?>) subnetwork2.getDcSwitches()).size());
        subnetwork2.getDcSwitches().forEach(dcSwitch -> assertTrue(dcSwitchListSubnet2.contains(dcSwitch)));
        subnetwork2.getDcSwitchStream().forEach(dcSwitch -> assertTrue(dcSwitchListSubnet2.contains(dcSwitch)));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.DC_SWITCH).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.DC_SWITCH).forEach(dcSwitch -> assertTrue(dcSwitchListSubnet2.contains((DcSwitch) dcSwitch)));
        assertEquals(1, subnetwork2.getDcSwitchCount());
        assertNull(subnetwork2.getDcSwitch(dcSwitch1Subnet1.getId()));
        assertNull(subnetwork2.getDcSwitch(dcSwitch2Subnet1.getId()));
        assertSame(dcSwitch1Subnet2, subnetwork2.getDcSwitch(dcSwitch1Subnet2.getId()));
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

        // test cannot create DcSwitch across subnetwork1 & subnetwork2
        DcSwitchAdder adder = netWithSubnet.newDcSwitch().setId("dcSwitchAcrossSubnets")
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode1Subnet2.getId())
                .setRetained(false).setOpen(false);
        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitchAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNode1Subnetwork2' are in different networks 'subnetwork1' and 'subnetwork2'", e1.getMessage());

        // test cannot create DcSwitch in netWithSubnet referencing nodes of subnetwork1
        adder.setDcNode1Id(dcNode1Subnet1.getId()).setDcNode2Id(dcNode2Subnet1.getId());
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitchAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNode2Subnetwork1' are in network 'subnetwork1' but DC Equipment is in 'test'", e2.getMessage());

        // test cannot create DcSwitch across subnetwork1 & netWithSubnet
        adder.setDcNode2Id(dcNodeRootNetwork.getId());
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitchAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNodeRootNetwork' are in different networks 'subnetwork1' and 'test'", e3.getMessage());
    }
}
