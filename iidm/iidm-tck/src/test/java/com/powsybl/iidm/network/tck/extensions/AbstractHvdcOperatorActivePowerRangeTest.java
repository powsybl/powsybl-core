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
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.*;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public abstract class AbstractHvdcOperatorActivePowerRangeTest {

    private Network network;

    @Before
    public void initNetwork() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void test() {
        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcOperatorActivePowerRange hopc = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        assertNull(hopc);

        hopc = hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class)
                .withOprFromCS2toCS1(1.0f)
                .withOprFromCS1toCS2(2.0f)
                .add();
        assertNotNull(hopc);
        assertEquals(2.0f, hopc.getOprFromCS1toCS2(), 0f);
        assertEquals(1.0f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals("hvdcOperatorActivePowerRange", hopc.getName());

        hopc.setOprFromCS1toCS2(1.1f);
        hopc.setOprFromCS2toCS1(2.1f);
        assertEquals(1.1f, hopc.getOprFromCS1toCS2(), 0f);
        assertEquals(2.1f, hopc.getOprFromCS2toCS1(), 0f);
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcOperatorActivePowerRange hopc = hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class)
                .withOprFromCS2toCS1(1.0f)
                .withOprFromCS1toCS2(2.0f)
                .add();

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(1.0f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals(2.0f, hopc.getOprFromCS1toCS2(), 0f);

        // Testing setting different values in the cloned variant and going back to the initial one
        hopc.setOprFromCS2toCS1(1.5f);
        hopc.setOprFromCS1toCS2(2.5f);
        assertEquals(1.5f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals(2.5f, hopc.getOprFromCS1toCS2(), 0f);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(1.0f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals(2.0f, hopc.getOprFromCS1toCS2(), 0f);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(1.0f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals(2.0f, hopc.getOprFromCS1toCS2(), 0f);
        variantManager.setWorkingVariant(variant3);
        assertEquals(1.0f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals(2.0f, hopc.getOprFromCS1toCS2(), 0f);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            hopc.getOprFromCS2toCS1();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }

        // Test illegal arguments in set methods
        variantManager.setWorkingVariant(variant1);
        try {
            hopc.setOprFromCS2toCS1(-1.0f);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("OPR from C1 to C2 must be greater than 0 (current value -1.0).", e.getMessage());
        }
    }
}
