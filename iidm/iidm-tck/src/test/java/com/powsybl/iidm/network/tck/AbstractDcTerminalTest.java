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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractDcTerminalTest {

    @Test
    public void testDcLineDcTerminal() {
        Network network = Network.create("test", "test");
        DcNode dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        DcNode dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
        DcLine dcLine = network.newDcLine()
                .setId("dcLine")
                .setDcNode1(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2(dcNode2.getId())
                .setConnected2(true)
                .setR(1.1)
                .add();
        DcTerminal dcLineTerminal = dcLine.getDcTerminal1();
        assertTrue(Double.isNaN(dcLineTerminal.getP()));
        assertTrue(Double.isNaN(dcLineTerminal.getI()));
        checkDcTerminalInMultiVariant(network, dcLineTerminal);
    }

    @Test
    public void testDcConverterDcTerminal() {
        Network network = Network.create("test", "test");
        Substation sa = network.newSubstation().setId("S").add();
        VoltageLevel vl = sa.newVoltageLevel().setId("VL").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1 = vl.getBusBreakerView().newBus().setId("B1").add();
        DcNode dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        DcNode dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
        LineCommutatedConverter converter = vl.newLineCommutatedConverter()
                .setId("dcConverter")
                .setBus1(b1.getId())
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .add();
        DcTerminal dcConverterTerminal = converter.getDcTerminal1();
        assertTrue(Double.isNaN(dcConverterTerminal.getP()));
        assertTrue(Double.isNaN(dcConverterTerminal.getI()));
        checkDcTerminalInMultiVariant(network, dcConverterTerminal);
    }

    private static void checkDcTerminalInMultiVariant(Network network, DcTerminal dcTerminal) {
        dcTerminal
                .setConnected(true)
                .setP(10.)
                .setI(5.);

        VariantManager variantManager = network.getVariantManager();

        List<String> variantsToAdd = List.of("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertTrue(dcTerminal.isConnected());
        assertEquals(10.0, dcTerminal.getP(), 1e-6);
        assertEquals(5.0, dcTerminal.getI(), 1e-6);

        // change values in s4
        dcTerminal
                .setConnected(false)
                .setP(-20.)
                .setI(-10.);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertFalse(dcTerminal.isConnected());
        assertEquals(-20.0, dcTerminal.getP(), 1e-6);
        assertEquals(-10.0, dcTerminal.getI(), 1e-6);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertTrue(dcTerminal.isConnected());
        assertEquals(10.0, dcTerminal.getP(), 1e-6);
        assertEquals(5.0, dcTerminal.getI(), 1e-6);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertThrows(PowsyblException.class, dcTerminal::isConnected, "Variant index not set");
        assertThrows(PowsyblException.class, dcTerminal::getP, "Variant index not set");
        assertThrows(PowsyblException.class, dcTerminal::getI, "Variant index not set");

        // check we delete a single variant's values
        variantManager.setWorkingVariant("s3");
        assertTrue(dcTerminal.isConnected());
        assertEquals(10.0, dcTerminal.getP(), 1e-6);
        assertEquals(5.0, dcTerminal.getI(), 1e-6);
    }

    @Test
    public void testChangesNotification() {
        Network network = Network.create("test", "test");
        DcNode dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        DcNode dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
        DcNode dcNode3 = network.newDcNode().setId("dcNode3").setNominalV(500.).add();
        DcNode dcNode4 = network.newDcNode().setId("dcNode4").setNominalV(500.).add();
        DcLine dcLine = network.newDcLine()
                .setId("dcLine")
                .setDcNode1(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2(dcNode2.getId())
                .setConnected2(true)
                .setR(1.1)
                .add();
        Substation sa = network.newSubstation().setId("S").add();
        VoltageLevel vl = sa.newVoltageLevel().setId("VL").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(175).add();
        Bus b1 = vl.getBusBreakerView().newBus().setId("B1").add();
        LineCommutatedConverter converter = vl.newLineCommutatedConverter()
                .setId("dcConverter")
                .setBus1(b1.getId())
                .setDcNode1(dcNode3.getId())
                .setDcNode2(dcNode4.getId())
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetP(100.)
                .setTargetVdc(500.)
                .add();

        DcTerminal dcLineDcTerminal1 = dcLine.getDcTerminal1().setP(1.).setI(2.);
        DcTerminal converterDcTerminal1 = converter.getDcTerminal1().setP(3.).setI(4.);
        Terminal converterAcTerminal1 = converter.getTerminal1().setP(5.);

        // Changes listener
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);

        // Change values and check update notification ...

        // ... DC line P1
        dcLineDcTerminal1.setP(11.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(dcLine, "p_dc1", VariantManagerConstants.INITIAL_VARIANT_ID, 1., 11.);

        // ... DC line I1
        dcLineDcTerminal1.setI(22.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(dcLine, "i_dc1", VariantManagerConstants.INITIAL_VARIANT_ID, 2., 22.);

        // ... AC/DC converter P1 // DC
        converterDcTerminal1.setP(33.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(converter, "p_dc1", VariantManagerConstants.INITIAL_VARIANT_ID, 3., 33.);

        // ... AC/DC converter I1 // DC
        converterDcTerminal1.setI(44.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(converter, "i_dc1", VariantManagerConstants.INITIAL_VARIANT_ID, 4., 44.);

        // ... AC/DC converter P1 // AC
        converterAcTerminal1.setP(55.);
        Mockito.verify(mockedListener, Mockito.times(1))
                .onUpdate(converter, "p1", VariantManagerConstants.INITIAL_VARIANT_ID, 5., 55.);

        // After this point, no more changes are taken into account.

        // Case when same value is set
        dcLineDcTerminal1.setP(11.);
        dcLineDcTerminal1.setI(22.);
        converterDcTerminal1.setP(33.);
        converterDcTerminal1.setI(44.);
        converterAcTerminal1.setP(55.);

        // Case when no listener is registered
        network.removeListener(mockedListener);
        dcLineDcTerminal1.setP(111.);
        dcLineDcTerminal1.setI(222.);
        converterDcTerminal1.setP(333.);
        converterDcTerminal1.setI(444.);
        converterAcTerminal1.setP(555.);

        // Check no notification
        Mockito.verifyNoMoreInteractions(mockedListener);
    }
}
