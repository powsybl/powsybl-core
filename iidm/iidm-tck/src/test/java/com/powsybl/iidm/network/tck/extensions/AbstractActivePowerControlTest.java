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
import java.util.OptionalDouble;

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

        // test limitControl
        double minP = bat.getMinP();
        double maxP = bat.getMaxP();
        assertThrows(PowsyblException.class, () -> activePowerControl.setMaxTargetP(maxP + 1));
        assertThrows(PowsyblException.class, () -> activePowerControl.setMaxTargetP(minP - 1));
        assertThrows(PowsyblException.class, () -> activePowerControl.setMinTargetP(maxP + 1));
        assertThrows(PowsyblException.class, () -> activePowerControl.setMinTargetP(minP - 1));

        activePowerControl.setMaxTargetP(200);
        activePowerControl.setMinTargetP(100);
        assertThrows(PowsyblException.class, () -> activePowerControl.setMinTargetP(201));
        assertThrows(PowsyblException.class, () -> activePowerControl.setMaxTargetP(99));

        // try to fool the extension
        bat.setMaxP(190);
        bat.setMinP(110);
        assertEquals(OptionalDouble.of(190), activePowerControl.getMaxTargetP());
        assertEquals(OptionalDouble.of(110), activePowerControl.getMinTargetP());

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
                .withMinTargetP(10)
                .withMaxTargetP(100)
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
        activePowerControl.setMaxTargetP(110.);
        activePowerControl.setMinTargetP(Double.NaN);
        checkValues4(activePowerControl);

        activePowerControl.setMinTargetP(11.);
        activePowerControl.setMaxTargetP(Double.NaN);
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
        assertTrue(activePowerControl.getMaxTargetP().isEmpty());
        assertTrue(activePowerControl.getMinTargetP().isEmpty());
    }

    private static void checkValues2(ActivePowerControl<Battery> activePowerControl) {
        assertFalse(activePowerControl.isParticipate());
        assertEquals(6.0, activePowerControl.getDroop(), 0.0);
        assertEquals(3.0, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMaxTargetP().isEmpty());
        assertTrue(activePowerControl.getMinTargetP().isEmpty());
    }

    private static void checkValues3(ActivePowerControl<Battery> activePowerControl) {
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4.0, activePowerControl.getDroop(), 0.0);
        assertEquals(1.2, activePowerControl.getParticipationFactor(), 0.0);
        assertEquals(10, activePowerControl.getMinTargetP().getAsDouble());
        assertEquals(100, activePowerControl.getMaxTargetP().getAsDouble());
    }

    private static void checkValues4(ActivePowerControl<Battery> activePowerControl) {
        assertFalse(activePowerControl.isParticipate());
        assertEquals(6.0, activePowerControl.getDroop(), 0.0);
        assertEquals(3.0, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMinTargetP().isEmpty());
        assertEquals(110, activePowerControl.getMaxTargetP().getAsDouble());
    }

    private static void checkValues5(ActivePowerControl<Battery> activePowerControl) {
        assertFalse(activePowerControl.isParticipate());
        assertEquals(6.0, activePowerControl.getDroop(), 0.0);
        assertEquals(3.0, activePowerControl.getParticipationFactor(), 0.0);
        assertTrue(activePowerControl.getMaxTargetP().isEmpty());
        assertEquals(11, activePowerControl.getMinTargetP().getAsDouble());
    }

}
