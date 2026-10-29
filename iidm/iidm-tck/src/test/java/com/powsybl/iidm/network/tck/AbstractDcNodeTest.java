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
import org.mockito.Mockito;

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

        DcNode bothLimits = network.newDcNode().setId("bothLimits").setNominalV(500.)
                .setLowVoltageLimit(480.).setHighVoltageLimit(520.).add();
        assertEquals(480., bothLimits.getLowVoltageLimit());
        assertEquals(520., bothLimits.getHighVoltageLimit());

        // an undefined limit leaves the other one unconstrained
        DcNode highLimitOnly = network.newDcNode().setId("highLimitOnly").setNominalV(500.)
                .setHighVoltageLimit(520.).add();
        assertTrue(Double.isNaN(highLimitOnly.getLowVoltageLimit()));
        assertEquals(520., highLimitOnly.getHighVoltageLimit());

        // zero is not negative, and equal limits are a consistent range
        DcNode zeroAndEqualLimits = network.newDcNode().setId("zeroAndEqualLimits").setNominalV(500.)
                .setLowVoltageLimit(0.).setHighVoltageLimit(0.).add();
        assertEquals(0., zeroAndEqualLimits.getLowVoltageLimit());
        assertEquals(0., zeroAndEqualLimits.getHighVoltageLimit());

        assertSame(bothLimits, bothLimits.setLowVoltageLimit(490.));
        assertEquals(490., bothLimits.getLowVoltageLimit());
        assertSame(bothLimits, bothLimits.setHighVoltageLimit(510.));
        assertEquals(510., bothLimits.getHighVoltageLimit());
        bothLimits.setLowVoltageLimit(Double.NaN);
        assertTrue(Double.isNaN(bothLimits.getLowVoltageLimit()));
        bothLimits.setHighVoltageLimit(Double.NaN);
        assertTrue(Double.isNaN(bothLimits.getHighVoltageLimit()));
    }

    @Test
    public void testVoltageLimitsAdderError() {
        Network network = Network.create("test", "test");

        // the limits bound the voltage magnitude, so a negative limit is invalid even on a negative pole
        DcNodeAdder negativeLow = network.newDcNode().setId("negativeLow").setNominalV(500.)
                .setLowVoltageLimit(-520.).setHighVoltageLimit(520.);
        ValidationException e1 = assertThrows(ValidationException.class, negativeLow::add);
        assertEquals("DC Node 'negativeLow': low voltage limit is < 0", e1.getMessage());
        assertNull(network.getDcNode("negativeLow"));

        DcNodeAdder negativeHigh = network.newDcNode().setId("negativeHigh").setNominalV(500.)
                .setHighVoltageLimit(-480.);
        ValidationException e2 = assertThrows(ValidationException.class, negativeHigh::add);
        assertEquals("DC Node 'negativeHigh': high voltage limit is < 0", e2.getMessage());
        assertNull(network.getDcNode("negativeHigh"));

        DcNodeAdder inconsistent = network.newDcNode().setId("inconsistent").setNominalV(500.)
                .setLowVoltageLimit(520.).setHighVoltageLimit(480.);
        ValidationException e3 = assertThrows(ValidationException.class, inconsistent::add);
        assertEquals("DC Node 'inconsistent': Inconsistent voltage limit range [520.0, 480.0]", e3.getMessage());
        assertNull(network.getDcNode("inconsistent"));
    }

    @Test
    public void testVoltageLimitsSetterError() {
        Network network = Network.create("test", "test");
        DcNode dcNode = network.newDcNode().setId("dcNode").setNominalV(500.)
                .setLowVoltageLimit(480.).setHighVoltageLimit(520.).add();

        ValidationException e1 = assertThrows(ValidationException.class, () -> dcNode.setLowVoltageLimit(-1.));
        assertEquals("DC Node 'dcNode': low voltage limit is < 0", e1.getMessage());
        ValidationException e2 = assertThrows(ValidationException.class, () -> dcNode.setHighVoltageLimit(-1.));
        assertEquals("DC Node 'dcNode': high voltage limit is < 0", e2.getMessage());
        ValidationException e3 = assertThrows(ValidationException.class, () -> dcNode.setLowVoltageLimit(530.));
        assertEquals("DC Node 'dcNode': Inconsistent voltage limit range [530.0, 520.0]", e3.getMessage());
        ValidationException e4 = assertThrows(ValidationException.class, () -> dcNode.setHighVoltageLimit(470.));
        assertEquals("DC Node 'dcNode': Inconsistent voltage limit range [480.0, 470.0]", e4.getMessage());

        // a rejected value leaves the DC node unchanged
        assertEquals(480., dcNode.getLowVoltageLimit());
        assertEquals(520., dcNode.getHighVoltageLimit());

        // with only one limit defined, a negative value of the other one is still rejected
        DcNode lowLimitOnly = network.newDcNode().setId("lowLimitOnly").setNominalV(500.)
                .setLowVoltageLimit(480.).add();
        assertThrows(ValidationException.class, () -> lowLimitOnly.setHighVoltageLimit(-520.));
        assertTrue(Double.isNaN(lowLimitOnly.getHighVoltageLimit()));
    }

    @Test
    public void testVoltageOutsideLimitsIsAccepted() {
        Network network = Network.create("test", "test");
        DcNode dcNode = network.newDcNode().setId("dcNode").setNominalV(500.)
                .setLowVoltageLimit(480.).setHighVoltageLimit(520.).add();

        // a negative pole within its limits, then voltages violating them in magnitude: none is rejected
        dcNode.setV(-500.);
        assertEquals(-500., dcNode.getV());
        dcNode.setV(-600.);
        assertEquals(-600., dcNode.getV());
        dcNode.setV(600.);
        assertEquals(600., dcNode.getV());
        dcNode.setV(10.);
        assertEquals(10., dcNode.getV());
    }

    @Test
    public void testVoltageLimitsNotifyUpdate() {
        Network network = Network.create("test", "test");
        DcNode dcNode = network.newDcNode().setId("dcNode").setNominalV(500.).add();
        NetworkListener mockedListener = Mockito.mock(NetworkListener.class);
        network.addListener(mockedListener);

        dcNode.setLowVoltageLimit(480.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(dcNode, "lowVoltageLimit", null, Double.NaN, 480.);
        dcNode.setHighVoltageLimit(520.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(dcNode, "highVoltageLimit", null, Double.NaN, 520.);
        dcNode.setLowVoltageLimit(490.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(dcNode, "lowVoltageLimit", null, 480., 490.);
        dcNode.setHighVoltageLimit(510.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(dcNode, "highVoltageLimit", null, 520., 510.);

        // a rejected value is not notified
        assertThrows(ValidationException.class, () -> dcNode.setLowVoltageLimit(-1.));
        assertThrows(ValidationException.class, () -> dcNode.setHighVoltageLimit(-1.));
        Mockito.verifyNoMoreInteractions(mockedListener);
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
