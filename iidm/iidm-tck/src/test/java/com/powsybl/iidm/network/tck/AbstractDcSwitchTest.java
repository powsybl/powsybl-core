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
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(false)
                .add();
        assertSame(IdentifiableType.DC_SWITCH, dcSwitch1.getType());
        assertEquals(dcSwitch1Id, dcSwitch1.getId());
        assertFalse(dcSwitch1.isOpen());
        assertSame(DcSwitchKind.DISCONNECTOR, dcSwitch1.getKind());
        assertSame(dcNode1, dcSwitch1.getDcNode1());
        assertSame(dcNode2, dcSwitch1.getDcNode2());
        assertEquals(1, network.getDcSwitchCount());

        String dcSwitch2Id = "dcSwitch2";
        DcSwitch dcSwitch2 = network.newDcSwitch()
                .setId(dcSwitch2Id)
                .setKind(DcSwitchKind.BREAKER)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(true)
                .add();
        assertEquals(dcSwitch2Id, dcSwitch2.getId());
        assertTrue(dcSwitch2.isOpen());
        assertSame(DcSwitchKind.BREAKER, dcSwitch2.getKind());
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
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(true)
                .add();
        assertTrue(dcSwitch.isOpen());
        dcSwitch.setOpen(false);
        assertFalse(dcSwitch.isOpen());
    }

    @Test
    public void testCreateDuplicate() {
        network.newDcSwitch()
                .setId("dcSwitch1")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(false)
                .add();
        DcSwitchAdder dcSwitchDuplicateAdder = network.newDcSwitch()
                .setId("dcSwitch1")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(false);
        PowsyblException exception = assertThrows(PowsyblException.class, dcSwitchDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcSwitch1'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testRemove() {
        String dcSwitch1Id = "dcSwitch1";
        DcSwitch dcSwitch1 = network.newDcSwitch()
                .setId(dcSwitch1Id)
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(false)
                .add();
        String dcSwitch2Id = "dcSwitch2";
        DcSwitch dcSwitch2 = network.newDcSwitch()
                .setId(dcSwitch2Id)
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(true)
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

        PowsyblException e3 = assertThrows(PowsyblException.class, dcSwitch1::getDcNode1);
        assertEquals("Cannot access dcNode1 of removed equipment dcSwitch1", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, dcSwitch1::getDcNode2);
        assertEquals("Cannot access dcNode2 of removed equipment dcSwitch1", e4.getMessage());
    }

    @Test
    public void testCreationError() {
        DcSwitchAdder adder = network.newDcSwitch();

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch id is not set", e1.getMessage());

        adder.setId("dcSwitch");
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': dcNode1 is not set", e2.getMessage());

        adder.setDcNode1("notExists");
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': DcNode 'notExists' not found", e3.getMessage());

        adder.setDcNode1(dcNode1.getId());
        PowsyblException e4 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': dcNode2 is not set", e4.getMessage());

        adder.setDcNode2("notExists");
        PowsyblException e5 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': DcNode 'notExists' not found", e5.getMessage());

        adder.setDcNode2(dcNode2.getId());
        PowsyblException e6 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitch': kind is not set", e6.getMessage());

        adder.setKind(DcSwitchKind.DISCONNECTOR);
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
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1Subnet1.getId())
                .setDcNode2(dcNode2Subnet1.getId())
                .setOpen(false)
                .add();
        DcSwitch dcSwitch2Subnet1 = subnetwork1.newDcSwitch().setId("dcSwitch2Subnetwork1")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1Subnet1.getId())
                .setDcNode2(dcNode2Subnet1.getId())
                .setOpen(false)
                .add();
        DcSwitch dcSwitch1Subnet2 = subnetwork2.newDcSwitch().setId("dcSwitch1Subnetwork2")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1Subnet2.getId())
                .setDcNode2(dcNode2Subnet2.getId())
                .setOpen(false)
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
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_SWITCH).forEach(dcSwitch -> assertTrue(dcSwitchListSubnet1.contains((DcSwitch) dcSwitch)));
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
                .setDcNode1(dcNode1Subnet1.getId())
                .setDcNode2(dcNode1Subnet2.getId())
                .setOpen(false);

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitchAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNode1Subnetwork2' are in different networks 'subnetwork1' and 'subnetwork2'", e1.getMessage());

        // test cannot create DcSwitch in netWithSubnet referencing nodes of subnetwork1
        adder.setDcNode1(dcNode1Subnet1.getId()).setDcNode2(dcNode2Subnet1.getId());
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitchAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNode2Subnetwork1' are in network 'subnetwork1' but DC Equipment is in 'test'", e2.getMessage());

        // test cannot create DcSwitch across subnetwork1 & netWithSubnet
        adder.setDcNode2(dcNodeRootNetwork.getId());
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Switch 'dcSwitchAcrossSubnets': DC Nodes 'dcNode1Subnetwork1' and 'dcNodeRootNetwork' are in different networks 'subnetwork1' and 'test'", e3.getMessage());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();

        DcSwitch dcSwitch = network.newDcSwitch()
                .setId("dcSwitch")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setOpen(false)
                .add();

        List<String> variantsToAdd = List.of("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertFalse(dcSwitch.isOpen());

        // change values in s4
        dcSwitch.setOpen(true);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertTrue(dcSwitch.isOpen());

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertFalse(dcSwitch.isOpen());

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertThrows(PowsyblException.class, dcSwitch::isOpen, "Variant index not set");

        // check we delete a single variant's values
        variantManager.setWorkingVariant("s3");
        assertFalse(dcSwitch.isOpen());
    }
}
