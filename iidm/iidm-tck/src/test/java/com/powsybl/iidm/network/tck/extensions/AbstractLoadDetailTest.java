/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractLoadDetailTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B")
                .add();
        vl.newLoad()
                .setId("L")
                .setBus("B")
                .setConnectableBus("B")
                .setP0(100)
                .setQ0(50)
                .add();

        // extends load
        Load load = network.getLoad("L");
        assertNotNull(load);
        load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(40f)
                .withFixedReactivePower(20f)
                .withVariableActivePower(60f)
                .withVariableReactivePower(30f)
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createTestNetwork();

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);
        assertEquals(40f, detail.getFixedActivePower(), 0f);
        assertEquals(20f, detail.getFixedReactivePower(), 0f);
        assertEquals(60f, detail.getVariableActivePower(), 0f);
        assertEquals(30f, detail.getVariableReactivePower(), 0f);

        detail.setFixedActivePower(60f)
                .setFixedReactivePower(30f)
                .setVariableActivePower(40f)
                .setVariableReactivePower(20f);
        assertEquals(60f, detail.getFixedActivePower(), 0f);
        assertEquals(30f, detail.getFixedReactivePower(), 0f);
        assertEquals(40f, detail.getVariableActivePower(), 0f);
        assertEquals(20f, detail.getVariableReactivePower(), 0f);
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        // Creates the extension
        Network network = createTestNetwork();
        LoadDetail ld = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(ld);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(40f, ld.getFixedActivePower(), 0f);
        assertEquals(20f, ld.getFixedReactivePower(), 0f);
        assertEquals(60f, ld.getVariableActivePower(), 0f);
        assertEquals(30f, ld.getVariableReactivePower(), 0f);

        // Testing setting different values in the cloned variant and going back to the initial one
        ld.setFixedActivePower(60f).setFixedReactivePower(30f).setVariableActivePower(40f).setVariableReactivePower(20f);
        assertEquals(60f, ld.getFixedActivePower(), 0f);
        assertEquals(30f, ld.getFixedReactivePower(), 0f);
        assertEquals(40f, ld.getVariableActivePower(), 0f);
        assertEquals(20f, ld.getVariableReactivePower(), 0f);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(40f, ld.getFixedActivePower(), 0f);
        assertEquals(20f, ld.getFixedReactivePower(), 0f);
        assertEquals(60f, ld.getVariableActivePower(), 0f);
        assertEquals(30f, ld.getVariableReactivePower(), 0f);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(40f, ld.getFixedActivePower(), 0f);
        assertEquals(20f, ld.getFixedReactivePower(), 0f);
        assertEquals(60f, ld.getVariableActivePower(), 0f);
        assertEquals(30f, ld.getVariableReactivePower(), 0f);
        variantManager.setWorkingVariant(variant3);
        assertEquals(40f, ld.getFixedActivePower(), 0f);
        assertEquals(20f, ld.getFixedReactivePower(), 0f);
        assertEquals(60f, ld.getVariableActivePower(), 0f);
        assertEquals(30f, ld.getVariableReactivePower(), 0f);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            ld.getFixedActivePower();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }
}
