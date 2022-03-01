/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.*;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public abstract class AbstractActivePowerControlTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(ActivePowerControlAdder.class)
                .withDroop(4f)
                .withParticipate(true)
                .add();
        ActivePowerControl<Battery> activePowerControl = bat.getExtension(ActivePowerControl.class);
        assertEquals("activePowerControl", activePowerControl.getName());
        assertEquals("BAT", activePowerControl.getExtendable().getId());

        assertTrue(activePowerControl.isParticipate());
        assertEquals(4f, activePowerControl.getDroop(), 0f);
        activePowerControl.setParticipate(false);
        assertFalse(activePowerControl.isParticipate());
        activePowerControl.setDroop(6f);
        assertEquals(6f, activePowerControl.getDroop(), 0f);
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
                .withDroop(4f)
                .withParticipate(true)
                .add();
        ActivePowerControl<Battery> activePowerControl = bat.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4f, activePowerControl.getDroop(), 0f);

        // Testing setting different values in the cloned variant and going back to the initial one
        activePowerControl.setDroop(5f);
        activePowerControl.setParticipate(false);
        assertFalse(activePowerControl.isParticipate());
        assertEquals(5f, activePowerControl.getDroop(), 0f);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4f, activePowerControl.getDroop(), 0f);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4f, activePowerControl.getDroop(), 0f);
        variantManager.setWorkingVariant(variant3);
        assertTrue(activePowerControl.isParticipate());
        assertEquals(4f, activePowerControl.getDroop(), 0f);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            activePowerControl.getDroop();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }
}
