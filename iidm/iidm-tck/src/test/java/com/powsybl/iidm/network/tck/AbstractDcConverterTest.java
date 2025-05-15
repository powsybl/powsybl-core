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
public abstract class AbstractDcConverterTest {

    private Network network;
    private VoltageLevel vla;
    private Bus b1a;
    private Bus b2a;
    private DcNode dcNode1a;
    private DcNode dcNode2a;
    private VoltageLevel vlb;
    private Bus b1b;
    private Bus b2b;
    private DcNode dcNode1b;
    private DcNode dcNode2b;
    private DcConverter<?> dcConverterA;
    private DcConverter<?> dcConverterB;

    @BeforeEach
    void setup() {
        network = Network.create("test", "test");
        Substation sa = network.newSubstation().setId("SA").add();
        vla = sa.newVoltageLevel().setId("VLA").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        b1a = vla.getBusBreakerView().newBus().setId("B1A").add();
        b2a = vla.getBusBreakerView().newBus().setId("B2A").add();
        dcNode1a = network.newDcNode().setId("dcNode1a").setNominalV(1.).add();
        dcNode2a = network.newDcNode().setId("dcNode2a").setNominalV(500.).add();
        Substation sb = network.newSubstation().setId("SB").add();
        vlb = sb.newVoltageLevel().setId("VLB").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        b1b = vlb.getBusBreakerView().newBus().setId("B1B").add();
        b2b = vlb.getBusBreakerView().newBus().setId("B2B").add();
        dcNode1b = network.newDcNode().setId("dcNode1b").setNominalV(1.).add();
        dcNode2b = network.newDcNode().setId("dcNode2b").setNominalV(500.).add();
    }

    @Test
    public void testBaseLcc() {
        dcConverterA = createDcLccA(vla);
        assertEquals(1, network.getDcLineCommutatedConverterCount());
        assertSame(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER, dcConverterA.getType());
        dcConverterB = createDcLccB(vlb);
        assertSame(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER, dcConverterB.getType());
        assertEquals(2, network.getDcLineCommutatedConverterCount());

        checkBaseCommonLccVsc();

        List<DcLineCommutatedConverter> dcConverterList = List.of((DcLineCommutatedConverter) dcConverterA, (DcLineCommutatedConverter) dcConverterB);
        assertEquals(2, ((Collection<?>) network.getDcLineCommutatedConverters()).size());
        network.getDcLineCommutatedConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        network.getDcLineCommutatedConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).count());
        network.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).forEach(dcConverter -> assertTrue(dcConverterList.contains((DcLineCommutatedConverter) dcConverter)));
        assertEquals(1, vla.getDcLineCommutatedConverterCount());
        assertEquals(1, ((Collection<?>) vla.getDcLineCommutatedConverters()).size());
        vla.getDcLineCommutatedConverters().forEach(dcConverter -> assertSame(dcConverterA, dcConverter));
        vla.getDcLineCommutatedConverterStream().forEach(dcConverter -> assertSame(dcConverterA, dcConverter));
        assertEquals(1, vlb.getDcLineCommutatedConverterCount());
        assertEquals(1, ((Collection<?>) vlb.getDcLineCommutatedConverters()).size());
        vlb.getDcLineCommutatedConverters().forEach(dcConverter -> assertSame(dcConverterB, dcConverter));
        vlb.getDcLineCommutatedConverterStream().forEach(dcConverter -> assertSame(dcConverterB, dcConverter));
    }

    @Test
    public void testBaseVsc() {
        dcConverterA = createDcVscA(vla);
        assertEquals(1, network.getDcVoltageSourceConverterCount());
        assertSame(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER, dcConverterA.getType());
        dcConverterB = createDcVscB(vlb);
        assertSame(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER, dcConverterB.getType());
        assertEquals(2, network.getDcVoltageSourceConverterCount());

        checkBaseCommonLccVsc();

        List<DcVoltageSourceConverter> dcConverterList = List.of((DcVoltageSourceConverter) dcConverterA, (DcVoltageSourceConverter) dcConverterB);
        assertEquals(2, ((Collection<?>) network.getDcVoltageSourceConverters()).size());
        network.getDcVoltageSourceConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        network.getDcVoltageSourceConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).count());
        network.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).forEach(dcConverter -> assertTrue(dcConverterList.contains((DcVoltageSourceConverter) dcConverter)));
        assertEquals(1, vla.getDcVoltageSourceConverterCount());
        assertEquals(1, ((Collection<?>) vla.getDcVoltageSourceConverters()).size());
        vla.getDcVoltageSourceConverters().forEach(dcConverter -> assertSame(dcConverterA, dcConverter));
        vla.getDcVoltageSourceConverterStream().forEach(dcConverter -> assertSame(dcConverterA, dcConverter));
        assertEquals(1, vlb.getDcVoltageSourceConverterCount());
        assertEquals(1, ((Collection<?>) vlb.getDcVoltageSourceConverters()).size());
        vlb.getDcVoltageSourceConverters().forEach(dcConverter -> assertSame(dcConverterB, dcConverter));
        vlb.getDcVoltageSourceConverterStream().forEach(dcConverter -> assertSame(dcConverterB, dcConverter));
    }

    private void checkBaseCommonLccVsc() {
        assertEquals("converterA", dcConverterA.getId());

        assertSame(TwoSides.ONE, dcConverterA.getDcTerminal1().getSide());
        assertSame(TwoSides.TWO, dcConverterA.getDcTerminal2().getSide());
        assertSame(TwoSides.ONE, dcConverterA.getSide(dcConverterA.getDcTerminal1()));
        assertSame(TwoSides.TWO, dcConverterA.getSide(dcConverterA.getDcTerminal2()));
        assertSame(dcConverterA.getDcTerminal1(), dcConverterA.getDcTerminal(TwoSides.ONE));
        assertSame(dcConverterA.getDcTerminal2(), dcConverterA.getDcTerminal(TwoSides.TWO));

        assertSame(ThreeSides.ONE, dcConverterA.getTerminal1().getSide());
        assertSame(ThreeSides.TWO, dcConverterA.getTerminal2().orElseThrow().getSide());
        assertSame(TwoSides.ONE, dcConverterA.getSide(dcConverterA.getTerminal1()));
        assertSame(TwoSides.TWO, dcConverterA.getSide(dcConverterA.getTerminal2().orElseThrow()));
        assertSame(dcConverterA.getTerminal1(), dcConverterA.getTerminal(TwoSides.ONE));
        assertSame(dcConverterA.getTerminal2().orElseThrow(), dcConverterA.getTerminal(TwoSides.TWO));

        assertSame(b1a, dcConverterA.getTerminal1().getBusBreakerView().getBus());
        assertTrue(dcConverterA.getTerminal2().isPresent());
        assertSame(b2a, dcConverterA.getTerminal2().orElseThrow().getBusBreakerView().getBus());
        assertTrue(dcConverterA.getDcTerminal1().isConnected());
        assertTrue(dcConverterA.getDcTerminal2().isConnected());
        assertEquals(2, dcConverterA.getDcTerminals().size());
        assertSame(dcConverterA.getDcTerminals().get(0), dcConverterA.getDcTerminal1());
        assertSame(dcConverterA.getDcTerminals().get(1), dcConverterA.getDcTerminal2());
        assertSame(dcNode1a, dcConverterA.getDcTerminal1().getDcNode());
        assertSame(dcNode2a, dcConverterA.getDcTerminal2().getDcNode());
        assertSame(dcConverterA, dcConverterA.getDcTerminal1().getDcConnectable());
        assertSame(dcConverterA, dcConverterA.getDcTerminal2().getDcConnectable());
        assertEquals(0.01, dcConverterA.getIdleLoss());
        assertEquals(0.02, dcConverterA.getSwitchingLoss());
        assertEquals(0.03, dcConverterA.getResistiveLoss());

        assertEquals("converterB", dcConverterB.getId());
        assertFalse(dcConverterB.getDcTerminal1().isConnected());
        assertFalse(dcConverterB.getDcTerminal2().isConnected());
        assertSame(dcNode1b, dcConverterB.getDcTerminal1().getDcNode());
        assertSame(dcNode2b, dcConverterB.getDcTerminal2().getDcNode());
    }

    private DcLineCommutatedConverterAdder createDcLccAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newDcLineCommutatedConverter()
                .setIdleLoss(0.01)
                .setSwitchingLoss(0.02)
                .setResistiveLoss(0.03);
    }

    private DcLineCommutatedConverter createDcLccA(VoltageLevel voltageLevel) {
        return createDcLccAdder(voltageLevel)
                .setId("converterA")
                .setBus1(b1a.getId())
                .setConnectableBus1(b1a.getId())
                .setBus2(b2a.getId())
                .setConnectableBus2(b2a.getId())
                .setDcNode1Id(dcNode1a.getId())
                .setDcNode2Id(dcNode2a.getId())
                .setDcConnected1(true)
                .setDcConnected2(true)
                .add();
    }

    private DcLineCommutatedConverter createDcLccB(VoltageLevel voltageLevel) {
        return createDcLccAdder(voltageLevel)
                .setId("converterB")
                .setBus1(b1b.getId())
                .setBus2(b2b.getId())
                .setDcNode1Id(dcNode1b.getId())
                .setDcNode2Id(dcNode2b.getId())
                .setDcConnected1(false)
                .setDcConnected2(false)
                .add();
    }

    private DcVoltageSourceConverterAdder createDcVscAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newDcVoltageSourceConverter()
                .setIdleLoss(0.01)
                .setSwitchingLoss(0.02)
                .setResistiveLoss(0.03);
    }

    private DcVoltageSourceConverter createDcVscA(VoltageLevel voltageLevel) {
        return createDcVscAdder(voltageLevel)
                .setId("converterA")
                .setBus1(b1a.getId())
                .setBus2(b2a.getId())
                .setDcNode1Id(dcNode1a.getId())
                .setDcNode2Id(dcNode2a.getId())
                .setDcConnected1(true)
                .setDcConnected2(true)
                .add();
    }

    private DcVoltageSourceConverter createDcVscB(VoltageLevel voltageLevel) {
        return createDcVscAdder(voltageLevel)
                .setId("converterB")
                .setBus1(b1b.getId())
                .setBus2(b2b.getId())
                .setDcNode1Id(dcNode1b.getId())
                .setDcNode2Id(dcNode2b.getId())
                .setDcConnected1(false)
                .setDcConnected2(false)
                .add();
    }

    @Test
    public void testLossParametersGetterSetter() {
        dcConverterA = createDcLccA(vla);
        assertEquals(0.01, dcConverterA.getIdleLoss());
        assertEquals(0.02, dcConverterA.getSwitchingLoss());
        assertEquals(0.03, dcConverterA.getResistiveLoss());
        dcConverterA
                .setIdleLoss(0.04)
                .setSwitchingLoss(0.05)
                .setResistiveLoss(0.06);
        assertEquals(0.04, dcConverterA.getIdleLoss());
        assertEquals(0.05, dcConverterA.getSwitchingLoss());
        assertEquals(0.06, dcConverterA.getResistiveLoss());
        dcConverterA
                .setIdleLoss(0.)
                .setSwitchingLoss(0.)
                .setResistiveLoss(0.);
        assertEquals(0., dcConverterA.getIdleLoss());
        assertEquals(0., dcConverterA.getSwitchingLoss());
        assertEquals(0., dcConverterA.getResistiveLoss());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcConverterA.setIdleLoss(Double.NaN));
        assertEquals("DC Line Commutated Converter 'converterA': idleLoss is invalid", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> dcConverterA.setIdleLoss(-1.0));
        assertEquals("DC Line Commutated Converter 'converterA': idleLoss is invalid", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> dcConverterA.setSwitchingLoss(Double.NaN));
        assertEquals("DC Line Commutated Converter 'converterA': switchingLoss is invalid", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, () -> dcConverterA.setSwitchingLoss(-1.0));
        assertEquals("DC Line Commutated Converter 'converterA': switchingLoss is invalid", e4.getMessage());

        PowsyblException e5 = assertThrows(PowsyblException.class, () -> dcConverterA.setResistiveLoss(Double.NaN));
        assertEquals("DC Line Commutated Converter 'converterA': resistiveLoss is invalid", e5.getMessage());

        PowsyblException e6 = assertThrows(PowsyblException.class, () -> dcConverterA.setResistiveLoss(-1.0));
        assertEquals("DC Line Commutated Converter 'converterA': resistiveLoss is invalid", e6.getMessage());

    }

    @Test
    public void testCreateDuplicate() {
        dcConverterA = createDcLccA(vla);

        PowsyblException exception = assertThrows(PowsyblException.class, () -> createDcLccA(vla));
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'converterA'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testRemoveLcc() {
        dcConverterA = createDcLccA(vla);
        DcTerminal t1 = dcConverterA.getDcTerminal1();
        DcTerminal t2 = dcConverterA.getDcTerminal2();
        dcConverterB = createDcLccB(vlb);
        assertEquals(2, network.getDcLineCommutatedConverterCount());
        assertEquals(1, vla.getDcLineCommutatedConverterCount());
        assertEquals(1, vlb.getDcLineCommutatedConverterCount());
        dcConverterA.remove();
        assertNull(network.getDcLineCommutatedConverter(dcConverterA.getId()));
        assertEquals(1, network.getDcLineCommutatedConverterCount());
        assertEquals(0, vla.getDcLineCommutatedConverterCount());
        assertEquals(0, ((Collection<?>) vla.getDcLineCommutatedConverters()).size());
        dcConverterB.remove();
        assertNull(network.getDcLineCommutatedConverter(dcConverterB.getId()));
        assertEquals(0, network.getDcLineCommutatedConverterCount());
        assertEquals(0, vlb.getDcLineCommutatedConverterCount());
        assertEquals(0, ((Collection<?>) vlb.getDcLineCommutatedConverters()).size());

        checkCommonRemoveLccVsc(t1, t2);
    }

    @Test
    public void testRemoveVsc() {
        dcConverterA = createDcVscA(vla);
        DcTerminal t1 = dcConverterA.getDcTerminal1();
        DcTerminal t2 = dcConverterA.getDcTerminal2();
        dcConverterB = createDcVscB(vlb);
        assertEquals(2, network.getDcVoltageSourceConverterCount());
        assertEquals(1, vla.getDcVoltageSourceConverterCount());
        assertEquals(1, vlb.getDcVoltageSourceConverterCount());
        dcConverterA.remove();
        assertNull(network.getDcLineCommutatedConverter(dcConverterA.getId()));
        assertEquals(1, network.getDcVoltageSourceConverterCount());
        assertEquals(0, vla.getDcVoltageSourceConverterCount());
        assertEquals(0, ((Collection<?>) vla.getDcVoltageSourceConverters()).size());
        dcConverterB.remove();
        assertNull(network.getDcLineCommutatedConverter(dcConverterB.getId()));
        assertEquals(0, network.getDcVoltageSourceConverterCount());
        assertEquals(0, vlb.getDcVoltageSourceConverterCount());
        assertEquals(0, ((Collection<?>) vlb.getDcVoltageSourceConverters()).size());

        checkCommonRemoveLccVsc(t1, t2);
    }

    private void checkCommonRemoveLccVsc(DcTerminal t1, DcTerminal t2) {
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> dcConverterA.setIdleLoss(2.));
        assertEquals("Cannot modify idleLoss of removed equipment converterA", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, dcConverterA::getIdleLoss);
        assertEquals("Cannot access idleLoss of removed equipment converterA", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> dcConverterA.setSwitchingLoss(2.));
        assertEquals("Cannot modify switchingLoss of removed equipment converterA", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, dcConverterA::getSwitchingLoss);
        assertEquals("Cannot access switchingLoss of removed equipment converterA", e4.getMessage());

        PowsyblException e5 = assertThrows(PowsyblException.class, () -> dcConverterA.setResistiveLoss(2.));
        assertEquals("Cannot modify resistiveLoss of removed equipment converterA", e5.getMessage());

        PowsyblException e6 = assertThrows(PowsyblException.class, dcConverterA::getResistiveLoss);
        assertEquals("Cannot access resistiveLoss of removed equipment converterA", e6.getMessage());

        PowsyblException e7 = assertThrows(PowsyblException.class, t1::isConnected);
        assertEquals("Cannot access removed equipment converterA", e7.getMessage());

        PowsyblException e8 = assertThrows(PowsyblException.class, () -> t2.setConnected(false));
        assertEquals("Cannot modify removed equipment converterA", e8.getMessage());
    }

    @Test
    public void testOnSubnetworkLcc() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        Substation sSubnet1 = subnetwork1.newSubstation().setId("SSubnetwork1").add();
        VoltageLevel vlSubnet1 = sSubnet1.newVoltageLevel().setId("VLSubnet1").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B1Subnet1").add();
        Bus b2Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B2Subnet1").add();
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnet1").setNominalV(1.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnet1").setNominalV(500.).add();
        Substation sSubnet2 = subnetwork2.newSubstation().setId("SSubnetwork2").add();
        VoltageLevel vlSubnet2 = sSubnet2.newVoltageLevel().setId("VLSubnet2").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet2 = vlSubnet2.getBusBreakerView().newBus().setId("B1Subnet2").add();
        Bus b2Subnet2 = vlSubnet2.getBusBreakerView().newBus().setId("B2Subnet2").add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnet2").setNominalV(1.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnet2").setNominalV(500.).add();

        DcLineCommutatedConverter converterSubnet1 = vlSubnet1
                .newDcLineCommutatedConverter()
                .setId("converterSubnet1")
                .setBus1(b1Subnet1.getId())
                .setBus2(b2Subnet1.getId())
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode2Subnet1.getId())
                .add();
        DcLineCommutatedConverter converterSubnet2 = vlSubnet2
                .newDcLineCommutatedConverter()
                .setId("converterSubnet2")
                .setBus1(b1Subnet2.getId())
                .setBus2(b2Subnet2.getId())
                .setDcNode1Id(dcNode1Subnet2.getId())
                .setDcNode2Id(dcNode2Subnet2.getId())
                .add();

        List<DcLineCommutatedConverter> dcConverterList = List.of(converterSubnet1, converterSubnet2);

        // network content
        assertEquals(2, ((Collection<?>) netWithSubnet.getDcLineCommutatedConverters()).size());
        netWithSubnet.getDcLineCommutatedConverters().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        netWithSubnet.getDcLineCommutatedConverterStream().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        assertEquals(2, netWithSubnet.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).forEach(converter -> assertTrue(dcConverterList.contains((DcLineCommutatedConverter) converter)));
        assertEquals(2, netWithSubnet.getDcLineCommutatedConverterCount());
        assertSame(converterSubnet1, netWithSubnet.getDcLineCommutatedConverter(converterSubnet1.getId()));
        assertSame(converterSubnet2, netWithSubnet.getDcLineCommutatedConverter(converterSubnet2.getId()));

        // subnetwork1 content
        assertEquals(1, ((Collection<?>) subnetwork1.getDcLineCommutatedConverters()).size());
        subnetwork1.getDcLineCommutatedConverters().forEach(converter -> assertSame(converterSubnet1, converter));
        subnetwork1.getDcLineCommutatedConverterStream().forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getDcLineCommutatedConverterCount());
        assertSame(converterSubnet1, subnetwork1.getDcLineCommutatedConverter(converterSubnet1.getId()));
        assertNull(subnetwork1.getDcLineCommutatedConverter(converterSubnet2.getId()));

        // subnetwork2 content
        assertEquals(1, ((Collection<?>) subnetwork2.getDcLineCommutatedConverters()).size());
        subnetwork2.getDcLineCommutatedConverters().forEach(converter -> assertSame(converterSubnet2, converter));
        subnetwork2.getDcLineCommutatedConverterStream().forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER).forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getDcLineCommutatedConverterCount());
        assertSame(converterSubnet2, subnetwork2.getDcLineCommutatedConverter(converterSubnet2.getId()));
        assertNull(subnetwork2.getDcLineCommutatedConverter(converterSubnet1.getId()));
    }

    @Test
    public void testOnSubnetworkVsc() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        Substation sSubnet1 = subnetwork1.newSubstation().setId("SSubnetwork1").add();
        VoltageLevel vlSubnet1 = sSubnet1.newVoltageLevel().setId("VLSubnet1").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B1Subnet1").add();
        Bus b2Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B2Subnet1").add();
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnet1").setNominalV(1.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnet1").setNominalV(500.).add();
        Substation sSubnet2 = subnetwork2.newSubstation().setId("SSubnetwork2").add();
        VoltageLevel vlSubnet2 = sSubnet2.newVoltageLevel().setId("VLSubnet2").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet2 = vlSubnet2.getBusBreakerView().newBus().setId("B1Subnet2").add();
        Bus b2Subnet2 = vlSubnet2.getBusBreakerView().newBus().setId("B2Subnet2").add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnet2").setNominalV(1.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnet2").setNominalV(500.).add();

        DcVoltageSourceConverter converterSubnet1 = vlSubnet1
                .newDcVoltageSourceConverter()
                .setId("converterSubnet1")
                .setBus1(b1Subnet1.getId())
                .setBus2(b2Subnet1.getId())
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode2Subnet1.getId())
                .add();
        DcVoltageSourceConverter converterSubnet2 = vlSubnet2
                .newDcVoltageSourceConverter()
                .setId("converterSubnet2")
                .setBus1(b1Subnet2.getId())
                .setBus2(b2Subnet2.getId())
                .setDcNode1Id(dcNode1Subnet2.getId())
                .setDcNode2Id(dcNode2Subnet2.getId())
                .add();

        List<DcVoltageSourceConverter> dcConverterList = List.of(converterSubnet1, converterSubnet2);

        // network content
        assertEquals(2, ((Collection<?>) netWithSubnet.getDcVoltageSourceConverters()).size());
        netWithSubnet.getDcVoltageSourceConverters().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        netWithSubnet.getDcVoltageSourceConverterStream().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        assertEquals(2, netWithSubnet.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).forEach(converter -> assertTrue(dcConverterList.contains((DcVoltageSourceConverter) converter)));
        assertEquals(2, netWithSubnet.getDcVoltageSourceConverterCount());
        assertSame(converterSubnet1, netWithSubnet.getDcVoltageSourceConverter(converterSubnet1.getId()));
        assertSame(converterSubnet2, netWithSubnet.getDcVoltageSourceConverter(converterSubnet2.getId()));

        // subnetwork1 content
        assertEquals(1, ((Collection<?>) subnetwork1.getDcVoltageSourceConverters()).size());
        subnetwork1.getDcVoltageSourceConverters().forEach(converter -> assertSame(converterSubnet1, converter));
        subnetwork1.getDcVoltageSourceConverterStream().forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getDcVoltageSourceConverterCount());
        assertSame(converterSubnet1, subnetwork1.getDcVoltageSourceConverter(converterSubnet1.getId()));
        assertNull(subnetwork2.getDcVoltageSourceConverter(converterSubnet1.getId()));

        // subnetwork2 content
        assertEquals(1, ((Collection<?>) subnetwork2.getDcVoltageSourceConverters()).size());
        subnetwork2.getDcVoltageSourceConverters().forEach(converter -> assertSame(converterSubnet2, converter));
        subnetwork2.getDcVoltageSourceConverterStream().forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER).forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getDcVoltageSourceConverterCount());
        assertSame(converterSubnet2, subnetwork2.getDcVoltageSourceConverter(converterSubnet2.getId()));
        assertNull(subnetwork1.getDcVoltageSourceConverter(converterSubnet2.getId()));
    }

    @Test
    public void testNotAcrossNetworkSubnetworks() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        Substation sSubnet1 = subnetwork1.newSubstation().setId("SSubnetwork1").add();
        VoltageLevel vlSubnet1 = sSubnet1.newVoltageLevel().setId("VLSubnet1").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B1Subnet1").add();
        Bus b2Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B2Subnet1").add();
        DcNode dcNode1Root = netWithSubnet.newDcNode().setId("dcNode1Root").setNominalV(1.).add();
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnet1").setNominalV(1.).add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnet2").setNominalV(1.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnet2").setNominalV(500.).add();

        DcVoltageSourceConverterAdder adder = vlSubnet1
                .newDcVoltageSourceConverter()
                .setId("converterAcrossSubnets")
                .setBus1(b1Subnet1.getId())
                .setBus2(b2Subnet1.getId())
                .setDcNode1Id(dcNode1Subnet1.getId())
                .setDcNode2Id(dcNode1Subnet2.getId());

        // test cannot create Converter across subnetwork1 & subnetwork2
        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Voltage Source Converter 'converterAcrossSubnets': DC Nodes 'dcNode1Subnet1' and 'dcNode1Subnet2' are in different networks 'subnetwork1' and 'subnetwork2'", e1.getMessage());

        // test cannot create Converter in subnetwork1 referencing nodes of subnetwork2
        adder.setDcNode1Id(dcNode1Subnet2.getId()).setDcNode2Id(dcNode2Subnet2.getId());
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Voltage Source Converter 'converterAcrossSubnets': DC Nodes 'dcNode1Subnet2' and 'dcNode2Subnet2' are in network 'subnetwork2' but DC Equipment is in 'subnetwork1'", e2.getMessage());

        // test cannot create Converter in subnetwork1 referencing nodes of netWithSubnet
        adder.setDcNode1Id(dcNode1Subnet1.getId()).setDcNode2Id(dcNode1Root.getId());
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("DC Voltage Source Converter 'converterAcrossSubnets': DC Nodes 'dcNode1Subnet1' and 'dcNode1Root' are in different networks 'subnetwork1' and 'test'", e3.getMessage());
    }
}
