/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.extension;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicModelInfoTest {

    private Network network;
    private Generator gen;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
        gen = network.getGenerator("GEN");
    }

    @Test
    void testExtension() {
        gen.newExtension(DynamicModelInfoAdder.class).setModelName("TestModel").add();
        DynamicModelInfo<Generator> dynamicModelInfo = gen.getExtension(DynamicModelInfo.class);
        assertEquals("dynamicModel", dynamicModelInfo.getName());
        assertEquals("GEN", dynamicModelInfo.getExtendable().getId());

        assertEquals("TestModel", dynamicModelInfo.getModelName());
        dynamicModelInfo.setModelName("TestModel2");
        assertEquals("TestModel2", dynamicModelInfo.getModelName());
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        gen.newExtension(DynamicModelInfoAdder.class).setModelName("TestModel").add();
        DynamicModelInfo<Generator> dynamicModelInfo = gen.getExtension(DynamicModelInfo.class);
        assertNotNull(dynamicModelInfo);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals("TestModel", dynamicModelInfo.getModelName());

        // Testing setting different values in the cloned variant and going back to the initial one
        dynamicModelInfo.setModelName("TestModel2");
        assertEquals("TestModel2", dynamicModelInfo.getModelName());
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals("TestModel", dynamicModelInfo.getModelName());

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, List.of(variant1, variant3));
        variantManager.setWorkingVariant(variant1);
        assertEquals("TestModel", dynamicModelInfo.getModelName());
        variantManager.setWorkingVariant(variant3);
        assertEquals("TestModel", dynamicModelInfo.getModelName());

        // Test removing current variant
        variantManager.removeVariant(variant3);
        Exception e = assertThrows(PowsyblException.class, dynamicModelInfo::getModelName);
        assertEquals("Variant index not set", e.getMessage());
    }
}
