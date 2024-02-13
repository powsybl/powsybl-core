/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
public abstract class AbstractHvdcAngleDroopActivePowerTest {

    private Network network;

    @BeforeEach
    public void initNetwork() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void test() {
        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcAngleDroopActivePowerControl hadpc = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        assertNull(hadpc);

        hadpc = hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(200.0f)
                .withDroop(0.9f)
                .withEnabled(true)
                .add();
        assertNotNull(hadpc);
        assertEquals(200.0f, hadpc.getP0(), 0f);
        assertEquals(0.9f, hadpc.getDroop(), 0f);
        assertTrue(hadpc.isEnabled());
        assertEquals("hvdcAngleDroopActivePowerControl", hadpc.getName());

        hadpc.setP0(300.0f);
        hadpc.setDroop(0.0f);
        hadpc.setEnabled(false);
        assertEquals(300.0f, hadpc.getP0(), 0f);
        assertEquals(0.0f, hadpc.getDroop(), 0f);
        assertFalse(hadpc.isEnabled());
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcAngleDroopActivePowerControl hadpc = hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(200.0f)
                .withDroop(0.9f)
                .withEnabled(true)
                .add();

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(200.0f, hadpc.getP0(), 0f);
        assertEquals(0.9f, hadpc.getDroop(), 0f);
        assertTrue(hadpc.isEnabled());

        // Testing setting different values in the cloned variant and going back to the initial one
        hadpc.setP0(210.0f);
        hadpc.setDroop(0.8f);
        hadpc.setEnabled(false);
        assertFalse(hadpc.isEnabled());
        assertEquals(210.0f, hadpc.getP0(), 0f);
        assertEquals(0.8f, hadpc.getDroop(), 0f);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(200.0f, hadpc.getP0(), 0f);
        assertEquals(0.9f, hadpc.getDroop(), 0f);
        assertTrue(hadpc.isEnabled());

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(200.0f, hadpc.getP0(), 0f);
        assertEquals(0.9f, hadpc.getDroop(), 0f);
        assertTrue(hadpc.isEnabled());
        variantManager.setWorkingVariant(variant3);
        assertEquals(200.0f, hadpc.getP0(), 0f);
        assertEquals(0.9f, hadpc.getDroop(), 0f);
        assertTrue(hadpc.isEnabled());

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            hadpc.getP0();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }

        // Test illegal arguments in set methods
        variantManager.setWorkingVariant(variant1);
        try {
            hadpc.setP0(Float.NaN);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("p0 value (NaN) is invalid for HVDC line L", e.getMessage());
        }
        try {
            hadpc.setDroop(Float.NaN);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("droop value (NaN) is invalid for HVDC line L", e.getMessage());
        }
    }
}
