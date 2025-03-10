/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractStandbyAutomatonTest {

    @Test
    public void test() {
        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.newExtension(StandbyAutomatonAdder.class)
                .withB0(0.0001f)
                .withStandbyStatus(true)
                .withLowVoltageSetpoint(390f)
                .withHighVoltageSetpoint(400f)
                .withLowVoltageThreshold(385f)
                .withHighVoltageThreshold(405f)
                .add();
        StandbyAutomaton standbyAutomaton = svc.getExtension(StandbyAutomaton.class);
        assertEquals(0.0001f, standbyAutomaton.getB0(), 0.0);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(390, standbyAutomaton.getLowVoltageSetpoint(), 0.0);
        assertEquals(400, standbyAutomaton.getHighVoltageSetpoint(), 0.0);
        assertEquals(385, standbyAutomaton.getLowVoltageThreshold(), 0.0);
        assertEquals(405, standbyAutomaton.getHighVoltageThreshold(), 0.0);
        standbyAutomaton.setB0(0.0002f);
        assertEquals(0.0002f, standbyAutomaton.getB0(), 0.0);
        standbyAutomaton.setLowVoltageSetpoint(391f);
        assertEquals(391, standbyAutomaton.getLowVoltageSetpoint(), 0.0);
        standbyAutomaton.setHighVoltageSetpoint(401f);
        assertEquals(401, standbyAutomaton.getHighVoltageSetpoint(), 0.0);
        standbyAutomaton.setLowVoltageThreshold(386f);
        assertEquals(386, standbyAutomaton.getLowVoltageThreshold(), 0.0);
        standbyAutomaton.setHighVoltageThreshold(406f);
        assertEquals(406, standbyAutomaton.getHighVoltageThreshold(), 0.0);
        try {
            standbyAutomaton.setB0(Float.NaN);
            fail();
        } catch (Exception ignored) {
        }
        try {
            standbyAutomaton.setHighVoltageThreshold(200f);
            fail();
        } catch (Exception ignored) {
        }

        // when standby is false do not throw error on inconsistent low and high voltage thresholds use case
        standbyAutomaton.setStandby(false);
        assertFalse(standbyAutomaton.isStandby());
        standbyAutomaton.setHighVoltageThreshold(200f);
        assertEquals(200f, standbyAutomaton.getHighVoltageThreshold(), 0.0);

        // but an exception is thrown when the standby mode is latter used
        assertThrows(IllegalArgumentException.class, () -> standbyAutomaton.setStandby(true));
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        svc.newExtension(StandbyAutomatonAdder.class)
                .withB0(0.0001f)
                .withStandbyStatus(true)
                .withLowVoltageSetpoint(390f)
                .withHighVoltageSetpoint(400f)
                .withLowVoltageThreshold(385f)
                .withHighVoltageThreshold(405f)
                .add();
        StandbyAutomaton standbyAutomaton = svc.getExtension(StandbyAutomaton.class);
        assertNotNull(standbyAutomaton);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0001f, standbyAutomaton.getB0(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // Testing setting different values in the cloned variant and going back to the initial one
        standbyAutomaton.setB0(0.0004f)
                .setStandby(false)
                .setLowVoltageSetpoint(392f)
                .setHighVoltageSetpoint(403f)
                .setLowVoltageThreshold(390f)
                .setHighVoltageThreshold(410f);

        assertFalse(standbyAutomaton.isStandby());
        assertEquals(0.0004f, standbyAutomaton.getB0(), 0f);
        assertEquals(392f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(403f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(410f, standbyAutomaton.getHighVoltageThreshold(), 0f);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);

        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0004f, standbyAutomaton.getB0(), 0f); // not modify by variant change
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0004f, standbyAutomaton.getB0(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);
        variantManager.setWorkingVariant(variant3);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0004f, standbyAutomaton.getB0(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            standbyAutomaton.getLowVoltageSetpoint();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }
}
