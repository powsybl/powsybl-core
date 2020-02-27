/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public abstract class AbstractShuntCompensatorTest {

    private static final String INVALID = "invalid";

    private static final String SHUNT = "shunt";

    private static final String TEST_MULTI_VARIANT = "testMultiVariant";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("vl1");
    }

    @Test
    public void baseTest() {
        // adder
        ShuntCompensator shuntCompensator = voltageLevel
                                        .newShuntCompensator()
                                            .setId(SHUNT)
                                            .setName("shuntName")
                                            .setConnectableBus("busA")
                                            .setbPerSection(5.0)
                                            .setCurrentSectionCount(6)
                                            .setMaximumSectionCount(10)
                                        .add();
        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getName());
        assertEquals(SHUNT, shuntCompensator.getId());
        assertEquals(5.0, shuntCompensator.getbPerSection(), 0.0);
        assertEquals(6, shuntCompensator.getCurrentSectionCount());
        assertEquals(10, shuntCompensator.getMaximumSectionCount());

        // setter getter
        try {
            shuntCompensator.setbPerSection(0.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setbPerSection(1.0);
        assertEquals(1.0, shuntCompensator.getbPerSection(), 0.0);

        try {
            shuntCompensator.setCurrentSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            // max = 10 , current could not be 20
            shuntCompensator.setCurrentSectionCount(20);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setCurrentSectionCount(6);
        assertEquals(6, shuntCompensator.getCurrentSectionCount());

        try {
            shuntCompensator.setMaximumSectionCount(1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setMaximumSectionCount(20);
        assertEquals(20, shuntCompensator.getMaximumSectionCount());

        // remove
        int count = network.getShuntCompensatorCount();
        shuntCompensator.remove();
        assertNull(network.getShuntCompensator(SHUNT));
        assertNotNull(shuntCompensator);
        assertEquals(count - 1L, network.getShuntCompensatorCount());
    }

    @Test
    public void invalidbPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is invalid");
        createShunt(INVALID, INVALID, Double.NaN, 5, 10);
    }

    @Test
    public void invalidZerobPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is equal to zero");
        createShunt(INVALID, INVALID, 0.0, 5, 10);
    }

    @Test
    public void invalidNegativeMaxPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should be greater than 0");
        createShunt(INVALID, INVALID, 2.0, 0, -1);
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createShunt(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 2.0, 5, 10);
        ShuntCompensator shunt = network.getShuntCompensator(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(5, shunt.getCurrentSectionCount());
        assertEquals(10.0, shunt.getCurrentB(), 0.0); // 2*5
        // change values in s4
        shunt.setCurrentSectionCount(4);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(4, shunt.getCurrentSectionCount());
        assertEquals(8.0, shunt.getCurrentB(), 0.0); // 2*4

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(5, shunt.getCurrentSectionCount());
        assertEquals(10.0, shunt.getCurrentB(), 0.0); // 2*5

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            shunt.getCurrentSectionCount();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void createShunt(String id, String name, double bPerSection, int currentSectionCount, int maxSectionCount) {
        voltageLevel.newShuntCompensator()
                    .setId(id)
                    .setName(name)
                    .setConnectableBus("busA")
                    .setbPerSection(bPerSection)
                    .setCurrentSectionCount(currentSectionCount)
                    .setMaximumSectionCount(maxSectionCount)
                .add();
    }
}
