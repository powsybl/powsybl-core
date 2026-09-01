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
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractDcNodeTest {

    @Test
    public void testBase() {
        Network network = Network.create("test", "test");

        String dcNode1Id = "dcNode1";
        DcNode dcNode1 = network.newDcNode().setId(dcNode1Id).setNominalV(500.).add();
        assertSame(IdentifiableType.DC_NODE, dcNode1.getType());
        assertEquals(dcNode1.getId(), dcNode1Id);
        assertEquals(500., dcNode1.getNominalV());
        assertSame(network.getDcNode(dcNode1Id), dcNode1);
        assertEquals(1, network.getDcNodeCount());

        String dcNode2Id = "dcNode2";
        DcNode dcNode2 = network.newDcNode().setId(dcNode2Id).setNominalV(510.).add();
        assertEquals(dcNode2.getId(), dcNode2Id);
        assertEquals(510., dcNode2.getNominalV());
        assertSame(network.getDcNode(dcNode2Id), dcNode2);

        List<DcNode> dcNodeList = List.of(dcNode1, dcNode2);

        assertEquals(2, ((Collection<?>) network.getDcNodes()).size());
        network.getDcNodes().forEach(dcNode -> assertTrue(dcNodeList.contains(dcNode)));
        network.getDcNodeStream().forEach(dcNode -> assertTrue(dcNodeList.contains(dcNode)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.DC_NODE).count());
        network.getIdentifiableStream(IdentifiableType.DC_NODE).forEach(dcNode -> assertTrue(dcNodeList.contains((DcNode) dcNode)));
        assertEquals(2, network.getDcNodeCount());
    }

    @Test
    public void testGetterSetter() {
        Network network = Network.create("test", "test");
        DcNode dcNode = network.newDcNode().setId("dcNode").setNominalV(500.).add();
        assertEquals(500., dcNode.getNominalV());
        dcNode.setNominalV(510.);
        assertEquals(510., dcNode.getNominalV());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcNode.setNominalV(Double.NaN));
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> dcNode.setNominalV(0.0));
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> dcNode.setNominalV(-1.0));
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e3.getMessage());
    }

    @Test
    public void testVoltageLimits() {
        Network network = Network.create("test", "test");

        DcNode noLimits = network.newDcNode().setId("noLimits").setNominalV(500.).add();
        assertTrue(Double.isNaN(noLimits.getLowVoltageLimit()));
        assertTrue(Double.isNaN(noLimits.getHighVoltageLimit()));

        DcNode positiveLimits = network.newDcNode().setId("positiveLimits").setNominalV(500.)
                .setLowVoltageLimit(480.).setHighVoltageLimit(520.).add();
        assertEquals(480., positiveLimits.getLowVoltageLimit());
        assertEquals(520., positiveLimits.getHighVoltageLimit());

        DcNode negativeLimits = network.newDcNode().setId("negativeLimits").setNominalV(500.)
                .setLowVoltageLimit(-520.).setHighVoltageLimit(-480.).add();
        assertEquals(-520., negativeLimits.getLowVoltageLimit());
        assertEquals(-480., negativeLimits.getHighVoltageLimit());

        DcNode spanningLimits = network.newDcNode().setId("spanningLimits").setNominalV(500.)
                .setLowVoltageLimit(-520.).setHighVoltageLimit(520.).add();
        assertEquals(-520., spanningLimits.getLowVoltageLimit());
        assertEquals(520., spanningLimits.getHighVoltageLimit());

        // an undefined bound leaves the other one unconstrained, since all comparisons with NaN are false
        DcNode highLimitOnly = network.newDcNode().setId("highLimitOnly").setNominalV(500.)
                .setHighVoltageLimit(520.).add();
        assertTrue(Double.isNaN(highLimitOnly.getLowVoltageLimit()));
        assertEquals(520., highLimitOnly.getHighVoltageLimit());

        // the widest band, so that each new bound stays consistent with the stored value of the other one
        assertSame(spanningLimits, spanningLimits.setLowVoltageLimit(-510.));
        assertEquals(-510., spanningLimits.getLowVoltageLimit());
        assertSame(spanningLimits, spanningLimits.setHighVoltageLimit(510.));
        assertEquals(510., spanningLimits.getHighVoltageLimit());
        spanningLimits.setLowVoltageLimit(Double.NaN);
        assertTrue(Double.isNaN(spanningLimits.getLowVoltageLimit()));
    }

    @Test
    public void testVoltageLimitsError() {
        Network network = Network.create("test", "test");

        DcNodeAdder adder = network.newDcNode().setId("dcNode").setNominalV(500.)
                .setLowVoltageLimit(520.).setHighVoltageLimit(480.);
        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Node 'dcNode': Inconsistent voltage limit range [520.0, 480.0]", e1.getMessage());

        // only the ordering is checked, so a negative pair is rejected on the same rule
        DcNodeAdder negativeAdder = network.newDcNode().setId("negativeDcNode").setNominalV(500.)
                .setLowVoltageLimit(-480.).setHighVoltageLimit(-520.);
        PowsyblException e2 = assertThrows(PowsyblException.class, negativeAdder::add);
        assertEquals("DC Node 'negativeDcNode': Inconsistent voltage limit range [-480.0, -520.0]", e2.getMessage());

        DcNode dcNode = network.newDcNode().setId("dcNode").setNominalV(500.)
                .setLowVoltageLimit(-520.).setHighVoltageLimit(-480.).add();
        PowsyblException e3 = assertThrows(PowsyblException.class, () -> dcNode.setLowVoltageLimit(-470.));
        assertEquals("DC Node 'dcNode': Inconsistent voltage limit range [-470.0, -480.0]", e3.getMessage());
        PowsyblException e4 = assertThrows(PowsyblException.class, () -> dcNode.setHighVoltageLimit(-530.));
        assertEquals("DC Node 'dcNode': Inconsistent voltage limit range [-520.0, -530.0]", e4.getMessage());

        assertEquals(-520., dcNode.getLowVoltageLimit());
        assertEquals(-480., dcNode.getHighVoltageLimit());
    }

    @Test
    public void testRemove() {
        Network network = Network.create("test", "test");
        String dcNode1Id = "dcNode1";
        String dcNode2Id = "dcNode2";
        DcNode dcNode1 = network.newDcNode().setId(dcNode1Id).setNominalV(500.).add();
        DcNode dcNode2 = network.newDcNode().setId(dcNode2Id).setNominalV(510.).add();
        assertEquals(2, network.getDcNodeCount());
        dcNode1.remove();
        assertNull(network.getDcNode(dcNode1Id));
        assertEquals(1, network.getDcNodeCount());
        dcNode2.remove();
        assertNull(network.getDcNode(dcNode2Id));
        assertEquals(0, network.getDcNodeCount());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcNode1.setNominalV(501.));
        assertEquals("Cannot modify nominalV of removed equipment dcNode1", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, dcNode1::getNominalV);
        assertEquals("Cannot access nominalV of removed equipment dcNode1", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> dcNode1.setLowVoltageLimit(480.));
        assertEquals("Cannot modify lowVoltageLimit of removed equipment dcNode1", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, dcNode1::getLowVoltageLimit);
        assertEquals("Cannot access lowVoltageLimit of removed equipment dcNode1", e4.getMessage());

        PowsyblException e5 = assertThrows(PowsyblException.class, () -> dcNode1.setHighVoltageLimit(520.));
        assertEquals("Cannot modify highVoltageLimit of removed equipment dcNode1", e5.getMessage());

        PowsyblException e6 = assertThrows(PowsyblException.class, dcNode1::getHighVoltageLimit);
        assertEquals("Cannot access highVoltageLimit of removed equipment dcNode1", e6.getMessage());
    }

    @Test
    public void testCreateDuplicate() {
        Network network = Network.create("test", "test");
        String dcNode1Id = "dcNode1";
        network.newDcNode().setId(dcNode1Id).setNominalV(500.).add();
        DcNodeAdder dcNodeDuplicateAdder = network.newDcNode().setId(dcNode1Id).setNominalV(500.);
        PowsyblException exception = assertThrows(PowsyblException.class, dcNodeDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcNode1'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testCreationError() {
        Network network = Network.create("test", "test");
        DcNodeAdder adder = network.newDcNode();

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Node id is not set", e1.getMessage());

        adder.setId("dcNode");
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e2.getMessage());

        adder.setNominalV(Double.NaN);
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e3.getMessage());

        adder.setNominalV(0.0);
        PowsyblException e4 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e4.getMessage());

        adder.setNominalV(-1.0);
        PowsyblException e5 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Node 'dcNode': nominal voltage is invalid", e5.getMessage());
    }

    @Test
    public void testOnSubnetwork() {
        Network network = Network.create("test", "test");

        Network subnetwork1 = network.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = network.createSubnetwork("subnetwork2", "subnetwork2", "format2");

        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnetwork1").setNominalV(500.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnetwork1").setNominalV(500.).add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnetwork2").setNominalV(500.).add();

        List<DcNode> dcNodeList = List.of(dcNode1Subnet1, dcNode2Subnet1, dcNode1Subnet2);

        // network content
        assertEquals(3, ((Collection<?>) network.getDcNodes()).size());
        network.getDcNodes().forEach(dcNode -> assertTrue(dcNodeList.contains(dcNode)));
        network.getDcNodeStream().forEach(dcNode -> assertTrue(dcNodeList.contains(dcNode)));
        assertEquals(3, network.getIdentifiableStream(IdentifiableType.DC_NODE).count());
        network.getIdentifiableStream(IdentifiableType.DC_NODE).forEach(dcNode -> assertTrue(dcNodeList.contains((DcNode) dcNode)));
        assertEquals(3, network.getDcNodeCount());
        assertSame(dcNode1Subnet1, network.getDcNode(dcNode1Subnet1.getId()));
        assertSame(dcNode2Subnet1, network.getDcNode(dcNode2Subnet1.getId()));
        assertSame(dcNode1Subnet2, network.getDcNode(dcNode1Subnet2.getId()));

        // subnetwork1 content
        List<DcNode> dcNodeListSubnet1 = List.of(dcNode1Subnet1, dcNode2Subnet1);
        assertEquals(2, ((Collection<?>) subnetwork1.getDcNodes()).size());
        subnetwork1.getDcNodes().forEach(dcNode -> assertTrue(dcNodeListSubnet1.contains(dcNode)));
        subnetwork1.getDcNodeStream().forEach(dcNode -> assertTrue(dcNodeListSubnet1.contains(dcNode)));
        assertEquals(2, subnetwork1.getIdentifiableStream(IdentifiableType.DC_NODE).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_NODE).forEach(dcNode -> assertTrue(dcNodeListSubnet1.contains((DcNode) dcNode)));
        assertEquals(2, subnetwork1.getDcNodeCount());
        assertSame(dcNode1Subnet1, subnetwork1.getDcNode(dcNode1Subnet1.getId()));
        assertSame(dcNode2Subnet1, subnetwork1.getDcNode(dcNode2Subnet1.getId()));
        assertNull(subnetwork1.getDcNode(dcNode1Subnet2.getId()));

        // subnetwork2 content
        List<DcNode> dcNodeListSubnet2 = List.of(dcNode1Subnet2);
        assertEquals(1, ((Collection<?>) subnetwork2.getDcNodes()).size());
        subnetwork2.getDcNodes().forEach(dcNode -> assertTrue(dcNodeListSubnet2.contains(dcNode)));
        subnetwork2.getDcNodeStream().forEach(dcNode -> assertTrue(dcNodeListSubnet2.contains(dcNode)));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.DC_NODE).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.DC_NODE).forEach(dcNode -> assertTrue(dcNodeListSubnet2.contains((DcNode) dcNode)));
        assertEquals(1, subnetwork2.getDcNodeCount());
        assertNull(subnetwork2.getDcNode(dcNode1Subnet1.getId()));
        assertNull(subnetwork2.getDcNode(dcNode2Subnet1.getId()));
        assertSame(dcNode1Subnet2, subnetwork2.getDcNode(dcNode1Subnet2.getId()));
    }

    @Test
    public void testRemoveDcNodeStillReferencedDcConnectable() {
        Network network = Network.create("test", "test");
        DcNode dcNode = network.newDcNode().setId("dcNode").setNominalV(500.).add();
        DcGround dcGround = network.newDcGround().setId("dcGround").setDcNode(dcNode.getId()).add();

        PowsyblException e = assertThrows(PowsyblException.class, dcNode::remove);
        assertEquals("Cannot remove DC node '" + dcNode.getId()
                + "' because DC connectable '" + dcGround.getId() + "' is connected to it", e.getMessage());

        dcGround.remove();
        dcNode.remove();
        assertNull(network.getDcNode("dcNode"));
    }

    @Test
    public void testRemoveDcNodeStillReferencedDcSwitch() {
        Network network = Network.create("test", "test");
        DcNode dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        network.newDcNode().setId("dcNode2").setNominalV(500.).add();
        DcSwitch dcSwitch = network.newDcSwitch().setId("dcSwitch")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode1.getId())
                .setOpen(false)
                .add();

        PowsyblException e = assertThrows(PowsyblException.class, dcNode1::remove);
        assertEquals("Cannot remove DC node '" + dcNode1.getId()
                + "' because DC switch '" + dcSwitch.getId() + "' is connected to it", e.getMessage());

        dcSwitch.remove();
        dcNode1.remove();
        assertNull(network.getDcNode("dcNode1"));
    }
}
