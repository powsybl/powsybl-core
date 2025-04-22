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
    private VoltageLevel vl;
    private Bus b1;
    private Bus b2;
    private DcNode dcNode1;
    private DcNode dcNode2;

    @BeforeEach
    void setup() {
        network = Network.create("test", "test");
        Substation s = network.newSubstation().setId("S").add();
        vl = s.newVoltageLevel().setId("VL").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        b1 = vl.getBusBreakerView().newBus().setId("B1").add();
        b2 = vl.getBusBreakerView().newBus().setId("B2").add();
        dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(1.).add();
        dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
    }

    @Test
    public void testLccAddAndRemove() {
        DcLineCommutatedConverter dcConverter1 = createDcLcc();
        assertSame(IdentifiableType.DC_LINE_COMMUTATED_CONVERTER, dcConverter1.getType());
        assertEquals("lcc1", dcConverter1.getId());
        assertSame(b1, dcConverter1.getTerminal1().getBusBreakerView().getBus());
        assertTrue(dcConverter1.getTerminal2().isPresent());
        assertSame(b2, dcConverter1.getTerminal2().orElseThrow().getBusBreakerView().getBus());
        assertTrue(dcConverter1.getDcTerminal1().isConnected());
        assertTrue(dcConverter1.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcConverter1.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcConverter1.getDcTerminal2().getDcNode());
        assertSame(dcConverter1, dcConverter1.getDcTerminal1().getDcConnectable());
        assertSame(dcConverter1, dcConverter1.getDcTerminal2().getDcConnectable());
        assertEquals(0.01, dcConverter1.getIdleLoss());
        assertEquals(0.02, dcConverter1.getSwitchingLoss());
        assertEquals(0.03, dcConverter1.getResistiveLoss());
        assertEquals(1, network.getDcLineCommutatedConverterCount());
        // note that it does not really make sense to have two converters on the same DCNodes,
        // this is just for iIDM test purposes, not for load flow
        DcLineCommutatedConverter dcConverter2 = vl.newDcLineCommutatedConverter()
                .setId("cs2")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcConnected1(false)
                .setDcNode2Id(dcNode2.getId())
                .setDcConnected2(false)
                .add();
        assertEquals("cs2", dcConverter2.getId());
        assertFalse(dcConverter2.getDcTerminal1().isConnected());
        assertFalse(dcConverter2.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcConverter2.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcConverter2.getDcTerminal2().getDcNode());
        List<DcLineCommutatedConverter> dcConverterList = List.of(dcConverter1, dcConverter2);
        assertEquals(2, ((Collection<?>) network.getDcLineCommutatedConverters()).size());
        network.getDcLineCommutatedConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        network.getDcLineCommutatedConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, network.getDcLineCommutatedConverterCount());
        assertEquals(2, ((Collection<?>) vl.getDcLineCommutatedConverters()).size());
        vl.getDcLineCommutatedConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        vl.getDcLineCommutatedConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, vl.getDcLineCommutatedConverterCount());

        DcLineCommutatedConverterAdder dcConverterDuplicateAdder = vl.newDcLineCommutatedConverter()
                .setId("lcc1")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setDcConnected2(true);
        PowsyblException exception = assertThrows(PowsyblException.class, dcConverterDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'lcc1'").matcher(exception.getMessage()).find());
        dcConverter1.remove();
        assertNull(network.getDcVoltageSourceConverter("lcc1"));
        assertEquals(1, network.getDcLineCommutatedConverterCount());
    }

    private DcLineCommutatedConverter createDcLcc() {
        return vl.newDcLineCommutatedConverter()
                .setId("lcc1")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setDcConnected2(true)
                .setIdleLoss(0.01)
                .setSwitchingLoss(0.02)
                .setResistiveLoss(0.03)
                .add();
    }

    @Test
    public void testVscAddAndRemove() {
        DcVoltageSourceConverter dcConverter1 = createDcVsc();
        assertSame(IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER, dcConverter1.getType());
        assertEquals("vsc1", dcConverter1.getId());
        assertSame(b1, dcConverter1.getTerminal1().getBusBreakerView().getBus());
        assertTrue(dcConverter1.getTerminal2().isPresent());
        assertSame(b2, dcConverter1.getTerminal2().orElseThrow().getBusBreakerView().getBus());
        assertTrue(dcConverter1.getDcTerminal1().isConnected());
        assertTrue(dcConverter1.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcConverter1.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcConverter1.getDcTerminal2().getDcNode());
        assertSame(dcConverter1, dcConverter1.getDcTerminal1().getDcConnectable());
        assertSame(dcConverter1, dcConverter1.getDcTerminal2().getDcConnectable());
        assertEquals(0.01, dcConverter1.getIdleLoss());
        assertEquals(0.02, dcConverter1.getSwitchingLoss());
        assertEquals(0.03, dcConverter1.getResistiveLoss());
        assertEquals(1, network.getDcVoltageSourceConverterCount());
        // note that it does not really make sense to have two converters on the same DCNodes,
        // this is just for iIDM test purposes, not for load flow
        DcVoltageSourceConverter dcConverter2 = vl.newDcVoltageSourceConverter()
                .setId("vsc2")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcConnected1(false)
                .setDcNode2Id(dcNode2.getId())
                .setDcConnected2(false)
                .add();
        assertEquals("vsc2", dcConverter2.getId());
        assertFalse(dcConverter2.getDcTerminal1().isConnected());
        assertFalse(dcConverter2.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcConverter2.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcConverter2.getDcTerminal2().getDcNode());
        List<DcVoltageSourceConverter> dcConverterList = List.of(dcConverter1, dcConverter2);
        assertEquals(2, ((Collection<?>) network.getDcVoltageSourceConverters()).size());
        network.getDcVoltageSourceConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        network.getDcVoltageSourceConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, network.getDcVoltageSourceConverterCount());
        assertEquals(2, ((Collection<?>) vl.getDcVoltageSourceConverters()).size());
        vl.getDcVoltageSourceConverters().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        vl.getDcVoltageSourceConverterStream().forEach(dcConverter -> assertTrue(dcConverterList.contains(dcConverter)));
        assertEquals(2, vl.getDcVoltageSourceConverterCount());

        DcVoltageSourceConverterAdder dcConverterDuplicateAdder = vl.newDcVoltageSourceConverter()
                .setId("vsc1")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setDcConnected2(true);
        PowsyblException exception = assertThrows(PowsyblException.class, dcConverterDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'vsc1'").matcher(exception.getMessage()).find());
        dcConverter1.remove();
        assertNull(network.getDcVoltageSourceConverter("vsc1"));
        assertEquals(1, network.getDcVoltageSourceConverterCount());
    }

    private DcVoltageSourceConverter createDcVsc() {
        return vl.newDcVoltageSourceConverter()
                .setId("vsc1")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setDcConnected2(true)
                .setIdleLoss(0.01)
                .setSwitchingLoss(0.02)
                .setResistiveLoss(0.03)
                .add();
    }

    @Test
    public void testLossParametersSettersGetters() {
        DcLineCommutatedConverter dcConverter1 = createDcLcc();
        assertEquals(0.01, dcConverter1.getIdleLoss());
        assertEquals(0.02, dcConverter1.getSwitchingLoss());
        assertEquals(0.03, dcConverter1.getResistiveLoss());
        dcConverter1
                .setIdleLoss(0.04)
                .setSwitchingLoss(0.05)
                .setResistiveLoss(0.06);
        assertEquals(0.04, dcConverter1.getIdleLoss());
        assertEquals(0.05, dcConverter1.getSwitchingLoss());
        assertEquals(0.06, dcConverter1.getResistiveLoss());
    }
}
