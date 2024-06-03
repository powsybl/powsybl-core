/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public abstract class AbstractActivePowerControlTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(ActivePowerControlAdder.class)
                .withDroop(4.0)
                .withParticipate(true)
                .withParticipationFactor(1.2)
                .add();
        ActivePowerControl<Battery> activePowerControl = bat.getExtension(ActivePowerControl.class);
        assertEquals("activePowerControl", activePowerControl.getName());
        assertEquals("BAT", activePowerControl.getExtendable().getId());

        checkValues1(activePowerControl);
        activePowerControl.setParticipate(false);
        activePowerControl.setDroop(6.0);
        activePowerControl.setParticipationFactor(3.0);
        checkValues2(activePowerControl);
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(ActivePowerControlAdder.class)
                .withDroop(4.0)
                .withParticipate(true)
                .withParticipationFactor(1.2)
                .add();
        ActivePowerControl<Battery> activePowerControl = bat.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        checkValues1(activePowerControl);

        // Testing setting different values in the cloned variant and going back to the initial one
        activePowerControl.setDroop(6.0);
        activePowerControl.setParticipate(false);
        activePowerControl.setParticipationFactor(3.0);
        checkValues2(activePowerControl);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        checkValues1(activePowerControl);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        checkValues1(activePowerControl);
        variantManager.setWorkingVariant(variant3);
        checkValues1(activePowerControl);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            activePowerControl.getDroop();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }

    @Test
    public void variantsCloneTestWithOverride() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(ActivePowerControlAdder.class)
                .withDroop(4.0)
                .withParticipate(true)
                .withParticipationFactor(1.2)
                .withMinPOverridde(10)
                .withMaxPOverride(100)
                .add();
        ActivePowerControl<Battery> activePowerControl = bat.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        checkValues3(activePowerControl);

        // Testing setting different values in the cloned variant and going back to the initial one
        activePowerControl.setDroop(6.0);
        activePowerControl.setParticipate(false);
        activePowerControl.setParticipationFactor(3.0);
        activePowerControl.setMaxPOverride(110.);
        activePowerControl.setMinPOverride(null);
        checkValues4(activePowerControl);
        activePowerControl.setMinPOverride(Double.NaN);
        checkValues4(activePowerControl);

        activePowerControl.setMinPOverride(11.);
        activePowerControl.setMaxPOverride(null);
        checkValues5(activePowerControl);

        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        checkValues3(activePowerControl);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        checkValues3(activePowerControl);
        variantManager.setWorkingVariant(variant3);
        checkValues3(activePowerControl);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            activePowerControl.getDroop();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }

    private static void checkValues1(ActivePowerControl<Battery> activePowerControl) {
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4.0, activePowerControl.getDroop(), 0.0);
        assertEquals(1.2, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMaxPOverride().isEmpty());
        assertTrue(activePowerControl.getMinPOverride().isEmpty());
    }

    private static void checkValues2(ActivePowerControl<Battery> activePowerControl) {
        assertFalse(activePowerControl.isParticipate());
        assertEquals(6.0, activePowerControl.getDroop(), 0.0);
        assertEquals(3.0, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMaxPOverride().isEmpty());
        assertTrue(activePowerControl.getMinPOverride().isEmpty());
    }

    private static void checkValues3(ActivePowerControl<Battery> activePowerControl) {
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4.0, activePowerControl.getDroop(), 0.0);
        assertEquals(1.2, activePowerControl.getParticipationFactor(), 0.0);
        assertEquals(10, activePowerControl.getMinPOverride().get());
        assertEquals(100, activePowerControl.getMaxPOverride().get());
    }

    private static void checkValues4(ActivePowerControl<Battery> activePowerControl) {
        assertFalse(activePowerControl.isParticipate());
        assertEquals(6.0, activePowerControl.getDroop(), 0.0);
        assertEquals(3.0, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMinPOverride().isEmpty());
        assertEquals(110, activePowerControl.getMaxPOverride().get());
    }

    private static void checkValues5(ActivePowerControl<Battery> activePowerControl) {
        assertFalse(activePowerControl.isParticipate());
        assertEquals(6.0, activePowerControl.getDroop(), 0.0);
        assertEquals(3.0, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMaxPOverride().isEmpty());
        assertEquals(11, activePowerControl.getMinPOverride().get());
    }
}
