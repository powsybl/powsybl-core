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
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2Id(dcNode2.getId())
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
        Bus b2 = vl.getBusBreakerView().newBus().setId("B2").add();
        DcNode dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        DcNode dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
        DcLineCommutatedConverter converter = vl.newDcLineCommutatedConverter()
                .setId("dcConverter")
                .setBus1(b1.getId())
                .setBus2(b2.getId())
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setControlMode(DcConverter.ControlMode.P_PCC)
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
}
