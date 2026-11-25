/**
 * Copyright (c) 2026, Elia Group
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that all variant arrays of an HVDC line grow and shrink together.
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class HvdcLineVariantArrayTest {

    @Test
    void variantArraysShrinkWhenTrailingVariantsAreRemoved() throws ReflectiveOperationException {
        Network network = HvdcTestNetwork.createVsc();
        HvdcLine hvdcLine = network.getHvdcLine("L");
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, List.of("v1", "v2", "v3"));
        assertSizes(hvdcLine, 4);

        variantManager.setWorkingVariant("v3");
        hvdcLine.setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER).setActivePowerSetpoint(42);

        // removing the variants at the end of the arrays reduces the arrays
        variantManager.removeVariant("v3");
        variantManager.removeVariant("v2");
        assertSizes(hvdcLine, 2);

        // re-adding variants extends them again from the source variant, without stale values
        variantManager.cloneVariant("v1", List.of("v4", "v5"));
        assertSizes(hvdcLine, 4);
        for (String variantId : List.of("v4", "v5")) {
            variantManager.setWorkingVariant(variantId);
            assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hvdcLine.getConvertersMode());
            assertEquals(280, hvdcLine.getActivePowerSetpoint(), 0);
        }
    }

    private static void assertSizes(HvdcLine hvdcLine, int expectedSize) throws ReflectiveOperationException {
        assertEquals(expectedSize, ((TIntArrayList) field("convertersMode").get(hvdcLine)).size());
        assertEquals(expectedSize, ((TDoubleArrayList) field("activePowerSetpoint").get(hvdcLine)).size());
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = HvdcLineImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
