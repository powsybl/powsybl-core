/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractCoordinatedReactiveControlTest {

    private Generator generator;

    @BeforeEach
    public void setUp() {
        Network network = EurostagTutorialExample1Factory.create();
        generator = network.getGenerator("GEN");
    }

    @Test
    public void test() {
        CoordinatedReactiveControl control = generator.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(100.0)
                .add();
        assertEquals(100.0, control.getQPercent(), 0.0);
        control.setQPercent(99.0);
        assertEquals(99.0, control.getQPercent(), 0.0);
        assertEquals("GEN", control.getExtendable().getId());
    }

    @Test
    public void testUndefined() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> generator.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(Double.NaN)
                .add());
        assertTrue(e.getMessage().contains("Undefined value (NaN) for qPercent for generator GEN"));
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        CoordinatedReactiveControl control = generator.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(100.0)
                .add();

        // Testing variant cloning
        VariantManager variantManager = generator.getNetwork().getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(100.0, control.getQPercent(), 0);

        // Testing setting different values in the cloned variant and going back to the initial one
        control.setQPercent(150.0);
        assertEquals(150.0, control.getQPercent(), 0);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(100.0, control.getQPercent(), 0);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(100, control.getQPercent(), 0);
        variantManager.setWorkingVariant(variant3);
        assertEquals(100, control.getQPercent(), 0);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            control.getQPercent();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }
}
