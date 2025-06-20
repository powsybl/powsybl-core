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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractAcDcConverterTest {

    private Network network;
    private VoltageLevel vla;
    private VoltageLevel vlax;
    private Bus b1a;
    private Bus b2a;
    private Bus b1ax;
    private Bus b2ax;
    private Load lax;
    private Line lineax;
    private DcNode dcNode1a;
    private DcNode dcNode2a;
    private VoltageLevel vlb;
    private VoltageLevel vlbx;
    private Bus b1b;
    private Bus b2b;
    private Bus b1bx;
    private Bus b2bx;
    private Line linebx;
    private DcNode dcNode1b;
    private DcNode dcNode2b;
    private AcDcConverter<?> acDcConverterA;
    private AcDcConverter<?> acDcConverterB;

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

        vlax = sa.newVoltageLevel().setId("VLA400").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(400).add();
        b1ax = vlax.getBusBreakerView().newBus().setId("B1AX").add();
        b2ax = vlax.getBusBreakerView().newBus().setId("B2AX").add();

        lax = vlax.newLoad()
                .setId("LAX")
                .setBus(b1ax.getId())
                .setP0(0.0).setQ0(0.0)
                .add();
        lineax = network.newLine()
                .setId("LINEAX")
                .setVoltageLevel1(vlax.getId())
                .setBus1(b1ax.getId())
                .setConnectableBus1(b1ax.getId())
                .setVoltageLevel2(vlax.getId())
                .setBus2(b2ax.getId())
                .setConnectableBus2(b2ax.getId())
                .setR(0.3)
                .setX(3.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        sa.newTwoWindingsTransformer()
                .setId("TRA1")
                .setVoltageLevel1(vlax.getId())
                .setBus1(b1ax.getId())
                .setConnectableBus1(b1ax.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vla.getId())
                .setBus2(b1a.getId())
                .setConnectableBus2(b1a.getId())
                .setRatedU2(150)
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .add();
        sa.newTwoWindingsTransformer()
                .setId("TRA2")
                .setVoltageLevel1(vlax.getId())
                .setBus1(b1ax.getId())
                .setConnectableBus1(b1ax.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vla.getId())
                .setBus2(b2a.getId())
                .setConnectableBus2(b2a.getId())
                .setRatedU2(150)
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .add();

        vlbx = sb.newVoltageLevel().setId("VLB400").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(400).add();
        b1bx = vlbx.getBusBreakerView().newBus().setId("B1BX").add();
        b2bx = vlbx.getBusBreakerView().newBus().setId("B2BX").add();

        linebx = network.newLine()
                .setId("LINEBX")
                .setVoltageLevel1(vlbx.getId())
                .setBus1(b1bx.getId())
                .setConnectableBus1(b1bx.getId())
                .setVoltageLevel2(vlbx.getId())
                .setBus2(b2bx.getId())
                .setConnectableBus2(b2bx.getId())
                .setR(0.3)
                .setX(3.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        sb.newTwoWindingsTransformer()
                .setId("TRB1")
                .setVoltageLevel1(vlbx.getId())
                .setBus1(b1bx.getId())
                .setConnectableBus1(b1bx.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vlb.getId())
                .setBus2(b1b.getId())
                .setConnectableBus2(b1b.getId())
                .setRatedU2(150)
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .add();
        sb.newTwoWindingsTransformer()
                .setId("TRB2")
                .setVoltageLevel1(vlbx.getId())
                .setBus1(b1bx.getId())
                .setConnectableBus1(b1bx.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vlb.getId())
                .setBus2(b2b.getId())
                .setConnectableBus2(b2b.getId())
                .setRatedU2(150)
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .add();
    }

    @Test
    public void testBaseLcc() {
        acDcConverterA = createLccA(vla);
        assertEquals(1, network.getLineCommutatedConverterCount());
        assertSame(IdentifiableType.LINE_COMMUTATED_CONVERTER, acDcConverterA.getType());
        acDcConverterB = createLccB(vlb);
        assertSame(IdentifiableType.LINE_COMMUTATED_CONVERTER, acDcConverterB.getType());
        assertEquals(2, network.getLineCommutatedConverterCount());

        checkBaseCommonLccVsc();

        // default values
        assertSame(LineCommutatedConverter.ReactiveModel.FIXED_POWER_FACTOR, ((LineCommutatedConverter) acDcConverterA).getReactiveModel());
        assertEquals(0.5, ((LineCommutatedConverter) acDcConverterA).getPowerFactor());
        // explicitly set values
        assertSame(LineCommutatedConverter.ReactiveModel.CALCULATED_POWER_FACTOR, ((LineCommutatedConverter) acDcConverterB).getReactiveModel());
        assertEquals(0.6, ((LineCommutatedConverter) acDcConverterB).getPowerFactor());

        List<LineCommutatedConverter> dcConverterList = List.of((LineCommutatedConverter) acDcConverterA, (LineCommutatedConverter) acDcConverterB);
        assertEquals(2, ((Collection<?>) network.getLineCommutatedConverters()).size());
        network.getLineCommutatedConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        network.getLineCommutatedConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).count());
        network.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).forEach(dcConverter -> assertTrue(dcConverterList.contains((LineCommutatedConverter) dcConverter)));
        assertEquals(1, vla.getLineCommutatedConverterCount());
        assertEquals(1, ((Collection<?>) vla.getLineCommutatedConverters()).size());
        vla.getLineCommutatedConverters().forEach(dcConverter -> assertSame(acDcConverterA, dcConverter));
        vla.getLineCommutatedConverterStream().forEach(dcConverter -> assertSame(acDcConverterA, dcConverter));
        assertEquals(1, vlb.getLineCommutatedConverterCount());
        assertEquals(1, ((Collection<?>) vlb.getLineCommutatedConverters()).size());
        vlb.getLineCommutatedConverters().forEach(dcConverter -> assertSame(acDcConverterB, dcConverter));
        vlb.getLineCommutatedConverterStream().forEach(dcConverter -> assertSame(acDcConverterB, dcConverter));
    }

    @Test
    public void testBaseVsc() {
        acDcConverterA = createVscA(vla);
        assertEquals(1, network.getVoltageSourceConverterCount());
        assertSame(IdentifiableType.VOLTAGE_SOURCE_CONVERTER, acDcConverterA.getType());
        acDcConverterB = createVscB(vlb);
        assertSame(IdentifiableType.VOLTAGE_SOURCE_CONVERTER, acDcConverterB.getType());
        assertEquals(2, network.getVoltageSourceConverterCount());

        checkBaseCommonLccVsc();

        List<VoltageSourceConverter> dcConverterList = List.of((VoltageSourceConverter) acDcConverterA, (VoltageSourceConverter) acDcConverterB);
        assertEquals(2, ((Collection<?>) network.getVoltageSourceConverters()).size());
        network.getVoltageSourceConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        network.getVoltageSourceConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).count());
        network.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).forEach(dcConverter -> assertTrue(dcConverterList.contains((VoltageSourceConverter) dcConverter)));
        assertEquals(1, vla.getVoltageSourceConverterCount());
        assertEquals(1, ((Collection<?>) vla.getVoltageSourceConverters()).size());
        vla.getVoltageSourceConverters().forEach(dcConverter -> assertSame(acDcConverterA, dcConverter));
        vla.getVoltageSourceConverterStream().forEach(dcConverter -> assertSame(acDcConverterA, dcConverter));
        assertEquals(1, vlb.getVoltageSourceConverterCount());
        assertEquals(1, ((Collection<?>) vlb.getVoltageSourceConverters()).size());
        vlb.getVoltageSourceConverters().forEach(dcConverter -> assertSame(acDcConverterB, dcConverter));
        vlb.getVoltageSourceConverterStream().forEach(dcConverter -> assertSame(acDcConverterB, dcConverter));
    }

    private void checkBaseCommonLccVsc() {
        assertEquals("converterA", acDcConverterA.getId());

        assertSame(TwoSides.ONE, acDcConverterA.getDcTerminal1().getSide());
        assertSame(TwoSides.TWO, acDcConverterA.getDcTerminal2().getSide());
        assertSame(TwoSides.ONE, acDcConverterA.getSide(acDcConverterA.getDcTerminal1()));
        assertSame(TwoSides.TWO, acDcConverterA.getSide(acDcConverterA.getDcTerminal2()));
        assertSame(acDcConverterA.getDcTerminal1(), acDcConverterA.getDcTerminal(TwoSides.ONE));
        assertSame(acDcConverterA.getDcTerminal2(), acDcConverterA.getDcTerminal(TwoSides.TWO));

        assertSame(ThreeSides.ONE, acDcConverterA.getTerminal1().getSide());
        assertSame(ThreeSides.TWO, acDcConverterA.getTerminal2().orElseThrow().getSide());
        assertSame(TwoSides.ONE, acDcConverterA.getSide(acDcConverterA.getTerminal1()));
        assertSame(TwoSides.TWO, acDcConverterA.getSide(acDcConverterA.getTerminal2().orElseThrow()));
        assertSame(acDcConverterA.getTerminal1(), acDcConverterA.getTerminal(TwoSides.ONE));
        assertSame(acDcConverterA.getTerminal2().orElseThrow(), acDcConverterA.getTerminal(TwoSides.TWO));

        assertSame(b1a, acDcConverterA.getTerminal1().getBusBreakerView().getBus());
        assertTrue(acDcConverterA.getTerminal2().isPresent());
        assertSame(b2a, acDcConverterA.getTerminal2().orElseThrow().getBusBreakerView().getBus());
        assertTrue(acDcConverterA.getDcTerminal1().isConnected());
        assertTrue(acDcConverterA.getDcTerminal2().isConnected());
        assertEquals(2, acDcConverterA.getDcTerminals().size());
        assertSame(acDcConverterA.getDcTerminals().get(0), acDcConverterA.getDcTerminal1());
        assertSame(acDcConverterA.getDcTerminals().get(1), acDcConverterA.getDcTerminal2());
        assertSame(dcNode1a, acDcConverterA.getDcTerminal1().getDcNode());
        assertSame(dcNode2a, acDcConverterA.getDcTerminal2().getDcNode());
        assertSame(acDcConverterA, acDcConverterA.getDcTerminal1().getDcConnectable());
        assertSame(acDcConverterA, acDcConverterA.getDcTerminal2().getDcConnectable());
        assertEquals(0.01, acDcConverterA.getIdleLoss());
        assertEquals(0.02, acDcConverterA.getSwitchingLoss());
        assertEquals(0.03, acDcConverterA.getResistiveLoss());

        assertEquals("converterB", acDcConverterB.getId());
        assertFalse(acDcConverterB.getDcTerminal1().isConnected());
        assertFalse(acDcConverterB.getDcTerminal2().isConnected());
        assertSame(dcNode1b, acDcConverterB.getDcTerminal1().getDcNode());
        assertSame(dcNode2b, acDcConverterB.getDcTerminal2().getDcNode());
    }

    private LineCommutatedConverterAdder createLccAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newLineCommutatedConverter()
                .setIdleLoss(0.01)
                .setSwitchingLoss(0.02)
                .setResistiveLoss(0.03)
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.);
    }

    private LineCommutatedConverter createLccA(VoltageLevel voltageLevel) {
        return createLccAdder(voltageLevel)
                .setId("converterA")
                .setBus1(b1a.getId())
                .setConnectableBus1(b1a.getId())
                .setBus2(b2a.getId())
                .setConnectableBus2(b2a.getId())
                .setDcNode1(dcNode1a.getId())
                .setDcNode2(dcNode2a.getId())
                .setDcConnected1(true)
                .setDcConnected2(true)
                .setPccTerminal(lineax.getTerminal1())
                .add();
    }

    private LineCommutatedConverter createLccB(VoltageLevel voltageLevel) {
        return createLccAdder(voltageLevel)
                .setId("converterB")
                .setBus1(b1b.getId())
                .setBus2(b2b.getId())
                .setDcNode1(dcNode1b.getId())
                .setDcNode2(dcNode2b.getId())
                .setDcConnected1(false)
                .setDcConnected2(false)
                .setPccTerminal(linebx.getTerminal1())
                .setReactiveModel(LineCommutatedConverter.ReactiveModel.CALCULATED_POWER_FACTOR)
                .setPowerFactor(0.6)
                .add();
    }

    private VoltageSourceConverterAdder createVscAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newVoltageSourceConverter()
                .setIdleLoss(0.01)
                .setSwitchingLoss(0.02)
                .setResistiveLoss(0.03)
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.);
    }

    private VoltageSourceConverter createVscA(VoltageLevel voltageLevel) {
        return createVscAdder(voltageLevel)
                .setId("converterA")
                .setBus1(b1a.getId())
                .setBus2(b2a.getId())
                .setDcNode1(dcNode1a.getId())
                .setDcNode2(dcNode2a.getId())
                .setDcConnected1(true)
                .setDcConnected2(true)
                .setPccTerminal(lineax.getTerminal1())
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .add();
    }

    private VoltageSourceConverter createVscB(VoltageLevel voltageLevel) {
        return createVscAdder(voltageLevel)
                .setId("converterB")
                .setBus1(b1b.getId())
                .setBus2(b2b.getId())
                .setDcNode1(dcNode1b.getId())
                .setDcNode2(dcNode2b.getId())
                .setDcConnected1(false)
                .setDcConnected2(false)
                .setPccTerminal(linebx.getTerminal1())
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .add();
    }

    @Test
    public void testLossParametersGetterSetter() {
        acDcConverterA = createLccA(vla);
        assertEquals(0.01, acDcConverterA.getIdleLoss());
        assertEquals(0.02, acDcConverterA.getSwitchingLoss());
        assertEquals(0.03, acDcConverterA.getResistiveLoss());
        acDcConverterA
                .setIdleLoss(0.04)
                .setSwitchingLoss(0.05)
                .setResistiveLoss(0.06);
        assertEquals(0.04, acDcConverterA.getIdleLoss());
        assertEquals(0.05, acDcConverterA.getSwitchingLoss());
        assertEquals(0.06, acDcConverterA.getResistiveLoss());
        acDcConverterA
                .setIdleLoss(0.)
                .setSwitchingLoss(0.)
                .setResistiveLoss(0.);
        assertEquals(0., acDcConverterA.getIdleLoss());
        assertEquals(0., acDcConverterA.getSwitchingLoss());
        assertEquals(0., acDcConverterA.getResistiveLoss());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> acDcConverterA.setIdleLoss(Double.NaN));
        assertEquals("AC/DC Line Commutated Converter 'converterA': idleLoss is invalid", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> acDcConverterA.setIdleLoss(-1.0));
        assertEquals("AC/DC Line Commutated Converter 'converterA': idleLoss is invalid", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> acDcConverterA.setSwitchingLoss(Double.NaN));
        assertEquals("AC/DC Line Commutated Converter 'converterA': switchingLoss is invalid", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, () -> acDcConverterA.setSwitchingLoss(-1.0));
        assertEquals("AC/DC Line Commutated Converter 'converterA': switchingLoss is invalid", e4.getMessage());

        PowsyblException e5 = assertThrows(PowsyblException.class, () -> acDcConverterA.setResistiveLoss(Double.NaN));
        assertEquals("AC/DC Line Commutated Converter 'converterA': resistiveLoss is invalid", e5.getMessage());

        PowsyblException e6 = assertThrows(PowsyblException.class, () -> acDcConverterA.setResistiveLoss(-1.0));
        assertEquals("AC/DC Line Commutated Converter 'converterA': resistiveLoss is invalid", e6.getMessage());
    }

    @Test
    public void testLccGetterSetter() {
        acDcConverterA = createLccA(vla);
        LineCommutatedConverter lccA = (LineCommutatedConverter) acDcConverterA;

        assertEquals(0.5, lccA.getPowerFactor());
        assertEquals(LineCommutatedConverter.ReactiveModel.FIXED_POWER_FACTOR, lccA.getReactiveModel());

        lccA.setPowerFactor(0.55).setReactiveModel(LineCommutatedConverter.ReactiveModel.CALCULATED_POWER_FACTOR);
        assertEquals(0.55, lccA.getPowerFactor());
        assertEquals(LineCommutatedConverter.ReactiveModel.CALCULATED_POWER_FACTOR, lccA.getReactiveModel());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> lccA.setPowerFactor(-0.1));
        assertEquals("AC/DC Line Commutated Converter 'converterA': power factor is invalid, it must be between 0 and 1", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> lccA.setPowerFactor(1.1));
        assertEquals("AC/DC Line Commutated Converter 'converterA': power factor is invalid, it must be between 0 and 1", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> lccA.setPowerFactor(Double.NaN));
        assertEquals("AC/DC Line Commutated Converter 'converterA': power factor is invalid", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, () -> lccA.setReactiveModel(null));
        assertEquals("AC/DC Line Commutated Converter 'converterA': reactiveModel is not set", e4.getMessage());
    }

    @Test
    public void testCreateDuplicate() {
        acDcConverterA = createLccA(vla);

        PowsyblException exception = assertThrows(PowsyblException.class, () -> createLccA(vla));
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'converterA'").matcher(exception.getMessage()).find());
    }

    @Test
    public void testRemoveLcc() {
        acDcConverterA = createLccA(vla);
        DcTerminal t1 = acDcConverterA.getDcTerminal1();
        DcTerminal t2 = acDcConverterA.getDcTerminal2();
        acDcConverterB = createLccB(vlb);
        assertEquals(2, network.getLineCommutatedConverterCount());
        assertEquals(1, vla.getLineCommutatedConverterCount());
        assertEquals(1, vlb.getLineCommutatedConverterCount());
        acDcConverterA.remove();
        assertNull(network.getLineCommutatedConverter(acDcConverterA.getId()));
        assertEquals(1, network.getLineCommutatedConverterCount());
        assertEquals(0, vla.getLineCommutatedConverterCount());
        assertEquals(0, ((Collection<?>) vla.getLineCommutatedConverters()).size());
        acDcConverterB.remove();
        assertNull(network.getLineCommutatedConverter(acDcConverterB.getId()));
        assertEquals(0, network.getLineCommutatedConverterCount());
        assertEquals(0, vlb.getLineCommutatedConverterCount());
        assertEquals(0, ((Collection<?>) vlb.getLineCommutatedConverters()).size());

        checkCommonRemoveLccVsc(t1, t2);
    }

    @Test
    public void testRemoveVsc() {
        acDcConverterA = createVscA(vla);
        DcTerminal t1 = acDcConverterA.getDcTerminal1();
        DcTerminal t2 = acDcConverterA.getDcTerminal2();
        acDcConverterB = createVscB(vlb);
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(1, vla.getVoltageSourceConverterCount());
        assertEquals(1, vlb.getVoltageSourceConverterCount());
        acDcConverterA.remove();
        assertNull(network.getLineCommutatedConverter(acDcConverterA.getId()));
        assertEquals(1, network.getVoltageSourceConverterCount());
        assertEquals(0, vla.getVoltageSourceConverterCount());
        assertEquals(0, ((Collection<?>) vla.getVoltageSourceConverters()).size());
        acDcConverterB.remove();
        assertNull(network.getLineCommutatedConverter(acDcConverterB.getId()));
        assertEquals(0, network.getVoltageSourceConverterCount());
        assertEquals(0, vlb.getVoltageSourceConverterCount());
        assertEquals(0, ((Collection<?>) vlb.getVoltageSourceConverters()).size());

        checkCommonRemoveLccVsc(t1, t2);
    }

    private void checkCommonRemoveLccVsc(DcTerminal t1, DcTerminal t2) {
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> acDcConverterA.setIdleLoss(2.));
        assertEquals("Cannot modify idleLoss of removed equipment converterA", e1.getMessage());

        PowsyblException e2 = assertThrows(PowsyblException.class, acDcConverterA::getIdleLoss);
        assertEquals("Cannot access idleLoss of removed equipment converterA", e2.getMessage());

        PowsyblException e3 = assertThrows(PowsyblException.class, () -> acDcConverterA.setSwitchingLoss(2.));
        assertEquals("Cannot modify switchingLoss of removed equipment converterA", e3.getMessage());

        PowsyblException e4 = assertThrows(PowsyblException.class, acDcConverterA::getSwitchingLoss);
        assertEquals("Cannot access switchingLoss of removed equipment converterA", e4.getMessage());

        PowsyblException e5 = assertThrows(PowsyblException.class, () -> acDcConverterA.setResistiveLoss(2.));
        assertEquals("Cannot modify resistiveLoss of removed equipment converterA", e5.getMessage());

        PowsyblException e6 = assertThrows(PowsyblException.class, acDcConverterA::getResistiveLoss);
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
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnet1").setNominalV(1.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnet1").setNominalV(500.).add();
        Substation sSubnet2 = subnetwork2.newSubstation().setId("SSubnetwork2").add();
        VoltageLevel vlSubnet2 = sSubnet2.newVoltageLevel().setId("VLSubnet2").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet2 = vlSubnet2.getBusBreakerView().newBus().setId("B1Subnet2").add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnet2").setNominalV(1.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnet2").setNominalV(500.).add();

        LineCommutatedConverter converterSubnet1 = vlSubnet1
                .newLineCommutatedConverter()
                .setId("converterSubnet1")
                .setBus1(b1Subnet1.getId())
                .setDcNode1(dcNode1Subnet1.getId())
                .setDcNode2(dcNode2Subnet1.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .add();
        LineCommutatedConverter converterSubnet2 = vlSubnet2
                .newLineCommutatedConverter()
                .setId("converterSubnet2")
                .setBus1(b1Subnet2.getId())
                .setDcNode1(dcNode1Subnet2.getId())
                .setDcNode2(dcNode2Subnet2.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .add();

        List<LineCommutatedConverter> dcConverterList = List.of(converterSubnet1, converterSubnet2);

        // network content
        assertEquals(2, ((Collection<?>) netWithSubnet.getLineCommutatedConverters()).size());
        netWithSubnet.getLineCommutatedConverters().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        netWithSubnet.getLineCommutatedConverterStream().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        assertEquals(2, netWithSubnet.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).forEach(converter -> assertTrue(dcConverterList.contains((LineCommutatedConverter) converter)));
        assertEquals(2, netWithSubnet.getLineCommutatedConverterCount());
        assertSame(converterSubnet1, netWithSubnet.getLineCommutatedConverter(converterSubnet1.getId()));
        assertSame(converterSubnet2, netWithSubnet.getLineCommutatedConverter(converterSubnet2.getId()));

        // subnetwork1 content
        assertEquals(1, ((Collection<?>) subnetwork1.getLineCommutatedConverters()).size());
        subnetwork1.getLineCommutatedConverters().forEach(converter -> assertSame(converterSubnet1, converter));
        subnetwork1.getLineCommutatedConverterStream().forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getLineCommutatedConverterCount());
        assertSame(converterSubnet1, subnetwork1.getLineCommutatedConverter(converterSubnet1.getId()));
        assertNull(subnetwork1.getLineCommutatedConverter(converterSubnet2.getId()));

        // subnetwork2 content
        assertEquals(1, ((Collection<?>) subnetwork2.getLineCommutatedConverters()).size());
        subnetwork2.getLineCommutatedConverters().forEach(converter -> assertSame(converterSubnet2, converter));
        subnetwork2.getLineCommutatedConverterStream().forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.LINE_COMMUTATED_CONVERTER).forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getLineCommutatedConverterCount());
        assertSame(converterSubnet2, subnetwork2.getLineCommutatedConverter(converterSubnet2.getId()));
        assertNull(subnetwork2.getLineCommutatedConverter(converterSubnet1.getId()));
    }

    @Test
    public void testOnSubnetworkVsc() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        Substation sSubnet1 = subnetwork1.newSubstation().setId("SSubnetwork1").add();
        VoltageLevel vlSubnet1 = sSubnet1.newVoltageLevel().setId("VLSubnet1").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B1Subnet1").add();
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnet1").setNominalV(1.).add();
        DcNode dcNode2Subnet1 = subnetwork1.newDcNode().setId("dcNode2Subnet1").setNominalV(500.).add();
        Substation sSubnet2 = subnetwork2.newSubstation().setId("SSubnetwork2").add();
        VoltageLevel vlSubnet2 = sSubnet2.newVoltageLevel().setId("VLSubnet2").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet2 = vlSubnet2.getBusBreakerView().newBus().setId("B1Subnet2").add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnet2").setNominalV(1.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnet2").setNominalV(500.).add();

        VoltageSourceConverter converterSubnet1 = vlSubnet1
                .newVoltageSourceConverter()
                .setId("converterSubnet1")
                .setBus1(b1Subnet1.getId())
                .setDcNode1(dcNode1Subnet1.getId())
                .setDcNode2(dcNode2Subnet1.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .add();
        VoltageSourceConverter converterSubnet2 = vlSubnet2
                .newVoltageSourceConverter()
                .setId("converterSubnet2")
                .setBus1(b1Subnet2.getId())
                .setDcNode1(dcNode1Subnet2.getId())
                .setDcNode2(dcNode2Subnet2.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .add();

        List<VoltageSourceConverter> dcConverterList = List.of(converterSubnet1, converterSubnet2);

        // network content
        assertEquals(2, ((Collection<?>) netWithSubnet.getVoltageSourceConverters()).size());
        netWithSubnet.getVoltageSourceConverters().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        netWithSubnet.getVoltageSourceConverterStream().forEach(converter -> assertTrue(dcConverterList.contains(converter)));
        assertEquals(2, netWithSubnet.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).count());
        netWithSubnet.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).forEach(converter -> assertTrue(dcConverterList.contains((VoltageSourceConverter) converter)));
        assertEquals(2, netWithSubnet.getVoltageSourceConverterCount());
        assertSame(converterSubnet1, netWithSubnet.getVoltageSourceConverter(converterSubnet1.getId()));
        assertSame(converterSubnet2, netWithSubnet.getVoltageSourceConverter(converterSubnet2.getId()));

        // subnetwork1 content
        assertEquals(1, ((Collection<?>) subnetwork1.getVoltageSourceConverters()).size());
        subnetwork1.getVoltageSourceConverters().forEach(converter -> assertSame(converterSubnet1, converter));
        subnetwork1.getVoltageSourceConverterStream().forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).count());
        subnetwork1.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).forEach(converter -> assertSame(converterSubnet1, converter));
        assertEquals(1, subnetwork1.getVoltageSourceConverterCount());
        assertSame(converterSubnet1, subnetwork1.getVoltageSourceConverter(converterSubnet1.getId()));
        assertNull(subnetwork2.getVoltageSourceConverter(converterSubnet1.getId()));

        // subnetwork2 content
        assertEquals(1, ((Collection<?>) subnetwork2.getVoltageSourceConverters()).size());
        subnetwork2.getVoltageSourceConverters().forEach(converter -> assertSame(converterSubnet2, converter));
        subnetwork2.getVoltageSourceConverterStream().forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).count());
        subnetwork2.getIdentifiableStream(IdentifiableType.VOLTAGE_SOURCE_CONVERTER).forEach(converter -> assertSame(converterSubnet2, converter));
        assertEquals(1, subnetwork2.getVoltageSourceConverterCount());
        assertSame(converterSubnet2, subnetwork2.getVoltageSourceConverter(converterSubnet2.getId()));
        assertNull(subnetwork1.getVoltageSourceConverter(converterSubnet2.getId()));
    }

    @Test
    public void testNotAcrossNetworkSubnetworks() {
        Network netWithSubnet = Network.create("test", "test");

        Network subnetwork1 = netWithSubnet.createSubnetwork("subnetwork1", "subnetwork1", "format1");
        Network subnetwork2 = netWithSubnet.createSubnetwork("subnetwork2", "subnetwork2", "format2");
        Substation sSubnet1 = subnetwork1.newSubstation().setId("SSubnetwork1").add();
        VoltageLevel vlSubnet1 = sSubnet1.newVoltageLevel().setId("VLSubnet1").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1Subnet1 = vlSubnet1.getBusBreakerView().newBus().setId("B1Subnet1").add();
        DcNode dcNode1Root = netWithSubnet.newDcNode().setId("dcNode1Root").setNominalV(1.).add();
        DcNode dcNode1Subnet1 = subnetwork1.newDcNode().setId("dcNode1Subnet1").setNominalV(1.).add();
        DcNode dcNode1Subnet2 = subnetwork2.newDcNode().setId("dcNode1Subnet2").setNominalV(1.).add();
        DcNode dcNode2Subnet2 = subnetwork2.newDcNode().setId("dcNode2Subnet2").setNominalV(500.).add();

        VoltageSourceConverterAdder adder = vlSubnet1
                .newVoltageSourceConverter()
                .setId("converterAcrossSubnets")
                .setBus1(b1Subnet1.getId())
                .setDcNode1(dcNode1Subnet1.getId())
                .setDcNode2(dcNode1Subnet2.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0);

        // test cannot create Converter across subnetwork1 & subnetwork2
        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Voltage Source Converter 'converterAcrossSubnets': DC Nodes 'dcNode1Subnet1' and 'dcNode1Subnet2' are in different networks 'subnetwork1' and 'subnetwork2'", e1.getMessage());

        // test cannot create Converter in subnetwork1 referencing nodes of subnetwork2
        adder.setDcNode1(dcNode1Subnet2.getId()).setDcNode2(dcNode2Subnet2.getId());
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Voltage Source Converter 'converterAcrossSubnets': DC Nodes 'dcNode1Subnet2' and 'dcNode2Subnet2' are in network 'subnetwork2' but DC Equipment is in 'subnetwork1'", e2.getMessage());

        // test cannot create Converter in subnetwork1 referencing nodes of netWithSubnet
        adder.setDcNode1(dcNode1Subnet1.getId()).setDcNode2(dcNode1Root.getId());
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Voltage Source Converter 'converterAcrossSubnets': DC Nodes 'dcNode1Subnet1' and 'dcNode1Root' are in different networks 'subnetwork1' and 'test'", e3.getMessage());
    }

    @Test
    public void testVscReactiveLimits() {
        acDcConverterA = createVscA(vla);
        VoltageSourceConverter vsc = (VoltageSourceConverter) acDcConverterA;

        assertSame(ReactiveLimitsKind.MIN_MAX, vsc.getReactiveLimits().getKind());
        assertEquals(-Double.MAX_VALUE, vsc.getReactiveLimits().getMinQ(0));
        assertEquals(Double.MAX_VALUE, vsc.getReactiveLimits().getMaxQ(0));
        assertSame(vsc.getReactiveLimits(), vsc.getReactiveLimits(MinMaxReactiveLimits.class));

        vsc.newMinMaxReactiveLimits().setMinQ(-100.).setMaxQ(150.).add();
        assertEquals(-100., vsc.getReactiveLimits().getMinQ(0));
        assertEquals(150, vsc.getReactiveLimits().getMaxQ(0));

        vsc.newReactiveCapabilityCurve()
                .beginPoint().setP(-100.).setMinQ(-80).setMaxQ(70.).endPoint()
                .beginPoint().setP(0.).setMinQ(-100).setMaxQ(90.).endPoint()
                .beginPoint().setP(100.).setMinQ(-70).setMaxQ(60.).endPoint()
                .add();
        assertSame(ReactiveLimitsKind.CURVE, vsc.getReactiveLimits().getKind());
        assertEquals(-90., vsc.getReactiveLimits().getMinQ(-50));
        assertEquals(80, vsc.getReactiveLimits().getMaxQ(-50));
        assertSame(vsc.getReactiveLimits(), vsc.getReactiveLimits(ReactiveCapabilityCurve.class));
    }

    @Test
    public void testCreationError() {
        LineCommutatedConverterAdder adder = vla.newLineCommutatedConverter()
                .setId("converterA")
                .setBus1(b1a.getId())
                .setBus2(b2a.getId());

        PowsyblException e1 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': controlMode is not set", e1.getMessage());

        adder.setControlMode(AcDcConverter.ControlMode.V_DC);
        PowsyblException e2 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': targetP is invalid", e2.getMessage());

        adder.setTargetP(200.);
        PowsyblException e3 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': targetVdc is invalid", e3.getMessage());

        adder.setTargetVdc(200.);
        PowsyblException e4 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': converter has two AC terminals and pccTerminal is not set", e4.getMessage());

        adder.setPccTerminal(lax.getTerminal());
        PowsyblException e5 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': converter has two AC terminals and pccTerminal is not a line or transformer terminal", e5.getMessage());

        adder.setBus2(null);
        PowsyblException e6 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': pccTerminal is not a line or transformer or the converter terminal", e6.getMessage());

        adder.setPccTerminal(lineax.getTerminal1());
        PowsyblException e7 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': dcNode1 is not set", e7.getMessage());

        adder.setDcNode1(dcNode1a.getId());
        PowsyblException e8 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': dcNode2 is not set", e8.getMessage());

        adder.setDcNode2(dcNode2a.getId());
        Network subnet = network.createSubnetwork("subNet", "subNetName", "code");
        VoltageLevel subNetVl = subnet.newVoltageLevel().setId("subNetVl").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(400.).add();
        Bus subnetB1 = subNetVl.getBusBreakerView().newBus().setId("subNetB1").add();
        Bus subnetB2 = subNetVl.getBusBreakerView().newBus().setId("subNetB2").add();
        Line subNetLine = subnet.newLine().setId("subNetLine")
                .setBus1(subnetB1.getId())
                .setBus2(subnetB2.getId())
                .setR(0.).setX(1.0).setB1(0.).setB2(0.).setG1(0.).setG2(0.)
                .add();

        adder.setPccTerminal(subNetLine.getTerminal1());
        PowsyblException e9 = assertThrows(PowsyblException.class, adder::add);
        assertEquals("AC/DC Line Commutated Converter 'converterA': pccTerminal is not in the same parent network as the voltage level", e9.getMessage());
    }

    @Test
    public void testSingleAcTerminal() {
        acDcConverterA = vla.newVoltageSourceConverter()
                .setId("converterA")
                .setBus1(b1a.getId())
                .setDcNode1(dcNode1a.getId())
                .setDcNode2(dcNode2a.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .add();
        assertTrue(acDcConverterA.getTerminal2().isEmpty());
        assertSame(acDcConverterA.getPccTerminal(), acDcConverterA.getTerminal1());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> acDcConverterA.getTerminal(TwoSides.TWO));
        assertEquals("This AC/DC converter does not have a second AC Terminal", e.getMessage());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        acDcConverterA = vla.newVoltageSourceConverter()
                .setId("converterA")
                .setBus1(b1a.getId())
                .setDcNode1(dcNode1a.getId())
                .setDcNode2(dcNode2a.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .add();
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(AcDcConverter.ControlMode.P_PCC, acDcConverterA.getControlMode());
        assertEquals(100.0, acDcConverterA.getTargetP(), 0.0);
        assertEquals(500.0, acDcConverterA.getTargetVdc(), 0.0);
        // change values in s4
        acDcConverterA.setControlMode(AcDcConverter.ControlMode.V_DC);
        acDcConverterA.setTargetP(-50.);
        acDcConverterA.setTargetVdc(495.);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(AcDcConverter.ControlMode.V_DC, acDcConverterA.getControlMode());
        assertEquals(-50., acDcConverterA.getTargetP(), 0.0);
        assertEquals(495., acDcConverterA.getTargetVdc(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(AcDcConverter.ControlMode.P_PCC, acDcConverterA.getControlMode());
        assertEquals(100.0, acDcConverterA.getTargetP(), 0.0);
        assertEquals(500.0, acDcConverterA.getTargetVdc(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertThrows(PowsyblException.class, acDcConverterA::getControlMode, "Variant index not set");
        assertThrows(PowsyblException.class, acDcConverterA::getTargetP, "Variant index not set");
        assertThrows(PowsyblException.class, acDcConverterA::getTargetVdc, "Variant index not set");
    }
}
