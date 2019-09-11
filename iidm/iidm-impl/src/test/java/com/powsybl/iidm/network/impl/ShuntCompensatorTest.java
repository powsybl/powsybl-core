/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.ShuntCompensatorModelType.LINEAR;
import static com.powsybl.iidm.network.ShuntCompensatorModelType.NON_LINEAR;
import static org.junit.Assert.*;

public class ShuntCompensatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;
    private Terminal otherTerminal;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("vl1");
        otherTerminal = EurostagTutorialExample1Factory.create().getGenerator("GEN").getTerminal();
    }

    @Test
    public void baseTest() {
        // adder
        ShuntCompensator linearShunt = voltageLevel
                                        .newShuntCompensator()
                                            .setId("linearShunt")
                                            .setName("linearShuntName")
                                            .setConnectableBus("busA")
                                            .newLinearModel()
                                                .setbPerSection(5.0)
                                                .setMaximumSectionCount(10)
                                            .add()
                                            .setCurrentSectionCount(6)
                                        .add();
        ShuntCompensator nonLinearShunt = network.getVoltageLevel("vl2")
                                        .newShuntCompensator()
                                            .setId("nonLinearShunt")
                                            .setName("nonLinearShuntName")
                                            .setConnectableBus("busB")
                                            .newNonLinearModel()
                                                .beginSection()
                                                    .setSectionNumber(1)
                                                    .setB(4.0)
                                                .endSection()
                                                .beginSection()
                                                    .setSectionNumber(2)
                                                    .setB(3.0)
                                                .endSection()
                                            .add()
                                            .setCurrentSectionCount(2)
                                            .setRegulating(true)
                                            .setRegulatingTerminal(linearShunt.getTerminal())
                                            .setTargetV(400.0)
                                            .setTargetDeadband(10)
                                            .add();

        assertEquals(ConnectableType.SHUNT_COMPENSATOR, linearShunt.getType());
        assertEquals("linearShuntName", linearShunt.getName());
        assertEquals("linearShunt", linearShunt.getId());
        assertEquals(LINEAR, linearShunt.getModelType());
        assertEquals(5.0, linearShunt.getModel(ShuntCompensatorLinearModel.class).getbPerSection(), 0.0);
        assertEquals(6, linearShunt.getCurrentSectionCount());
        assertEquals(10, linearShunt.getModel(ShuntCompensatorLinearModel.class).getMaximumSectionCount());
        assertEquals(6 * 5.0, linearShunt.getCurrentB(), 0.0);
        assertEquals(10 * 5.0, linearShunt.getMaximumB(), 0.0);
        assertFalse(linearShunt.isRegulating());
        assertSame(linearShunt.getTerminal(), linearShunt.getRegulatingTerminal());
        assertTrue(Double.isNaN(linearShunt.getTargetV()));
        assertEquals(0.0, linearShunt.getTargetDeadband(), 0.0);

        assertEquals(ConnectableType.SHUNT_COMPENSATOR, nonLinearShunt.getType());
        assertEquals("nonLinearShuntName", nonLinearShunt.getName());
        assertEquals("nonLinearShunt", nonLinearShunt.getId());
        assertEquals(NON_LINEAR, nonLinearShunt.getModelType());
        assertEquals(2, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getSections().size());
        assertEquals(4.0, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getB(1), 0.0);
        assertEquals(3.0, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getB(2), 0.0);
        assertEquals(4.0, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getSection(1).getB(), 0.0);
        assertEquals(3.0, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getSection(2).getB(), 0.0);
        assertEquals(2, nonLinearShunt.getCurrentSectionCount());
        assertEquals(3.0, nonLinearShunt.getCurrentB(), 0.0);
        assertEquals(4.0, nonLinearShunt.getMaximumB(), 0.0);
        assertTrue(nonLinearShunt.isRegulating());
        assertSame(linearShunt.getTerminal(), nonLinearShunt.getRegulatingTerminal());
        assertEquals(400.0, nonLinearShunt.getTargetV(), 0.0);
        assertEquals(10, nonLinearShunt.getTargetDeadband(), 0.0);

        // get wrong model
        try {
            linearShunt.getModel(ShuntCompensatorNonLinearModel.class);
            fail();
        } catch (ValidationException ignored) {
        }

        // linear model
        try {
            linearShunt.getModel(ShuntCompensatorLinearModel.class).setbPerSection(0.0);
            fail();
        } catch (ValidationException ignored) {
        }
        linearShunt.getModel(ShuntCompensatorLinearModel.class).setbPerSection(1.0);
        assertEquals(1.0, linearShunt.getModel(ShuntCompensatorLinearModel.class).getbPerSection(), 0.0);

        try {
            linearShunt.getModel(ShuntCompensatorLinearModel.class).setMaximumSectionCount(1);
            fail();
        } catch (ValidationException ignored) {
        }
        linearShunt.getModel(ShuntCompensatorLinearModel.class).setMaximumSectionCount(20);
        assertEquals(20, linearShunt.getModel(ShuntCompensatorLinearModel.class).getMaximumSectionCount());

        // non linear model
        try {
            nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).addOrReplaceSection(-1, 2.0);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).addOrReplaceSection(3, 0.0);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getB(3);
            fail();
        } catch (PowsyblException ignored) {
        }
        nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).addOrReplaceSection(3, 2.0);
        assertEquals(2.0, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getB(3), 0.0);
        nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).addOrReplaceSection(1, 5.0);
        assertEquals(5.0, nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getB(1), 0.0);

        try {
            nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).removeSection(2);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).removeSection(-1);
            fail();
        } catch (ValidationException ignored) {
        }
        nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).removeSection(1);
        try {
            nonLinearShunt.getModel(ShuntCompensatorNonLinearModel.class).getB(1);
            fail();
        } catch (PowsyblException ignored) {
        }

        // setter getter shunt
        try {
            linearShunt.setCurrentSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            // max = 20 , current could not be 30
            linearShunt.setCurrentSectionCount(30);
            fail();
        } catch (ValidationException ignored) {
        }
        linearShunt.setCurrentSectionCount(6);
        assertEquals(6, linearShunt.getCurrentSectionCount());

        try {
            linearShunt.setRegulating(true);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            linearShunt.setTargetV(-1);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            linearShunt.setRegulatingTerminal(otherTerminal);
            fail();
        } catch (ValidationException ignored) {
        }

        linearShunt.setTargetDeadband(5.0);
        linearShunt.setTargetV(225.0);
        linearShunt.setRegulatingTerminal(nonLinearShunt.getTerminal());
        linearShunt.setRegulating(true);
        assertTrue(linearShunt.isRegulating());
        assertSame(nonLinearShunt.getTerminal(), linearShunt.getRegulatingTerminal());
        assertEquals(5.0, linearShunt.getTargetDeadband(), 0.0);
        assertEquals(225.0, linearShunt.getTargetV(), 0.0);

        nonLinearShunt.setTargetDeadband(Double.NaN);
        assertEquals(0.0, nonLinearShunt.getTargetDeadband(), 0.0);

        // remove
        int count = network.getShuntCompensatorCount();
        linearShunt.remove();
        assertNull(network.getShuntCompensator("shunt"));
        assertNotNull(linearShunt);
        assertEquals(count - 1, network.getShuntCompensatorCount());
    }

    @Test
    public void invalidbPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is invalid");
        createLinearShunt("invalid", "invalid", Double.NaN, 5, 10);
    }

    @Test
    public void invalidZerobPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is equal to zero");
        createLinearShunt("invalid", "invalid", 0.0, 5, 10);
    }

    @Test
    public void invalidNegativeMaxPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should be greater than 0");
        createLinearShunt("invalid", "invalid", 2.0, 0, -1);
    }

    @Test
    public void invalidCurrentSectionCount() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("the current number (40) of section should be lesser than the maximum number of section (10)");
        createLinearShunt("invalid", "invalid", 2.0, 40, 10);
    }

    @Test
    public void invalidTargetDeadband() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (-1.0) for target deadband (target deadband is strictly negative)");
        createLinearShunt("invalid", "invalid", 2.0, 10, 20, true, 400, -1, null);
    }

    @Test
    public void invalidTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (-1.0) for targetV (targetV is strictly negative)");
        createLinearShunt("invalid", "invalid", 2.0, 10, 20, false, -1, 10, null);
    }

    @Test
    public void invalidRegulatingTerminal() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("regulating terminal is not part of the network");
        createLinearShunt("invalid", "invalid", 2.0, 10, 20, false, Double.NaN, 0.0, otherTerminal);
    }

    @Test
    public void invalidRegulating() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (NaN) for targetV (the shunt is regulating)");
        createLinearShunt("invalid", "invalid", 2.0, 10, 20, true, Double.NaN, 0.0, null);
    }

    @Test
    public void invalidEmptyNonLinearShunt() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("shunt compensator must have at least one section");
        voltageLevel
                .newShuntCompensator()
                    .setId("linearShunt")
                    .setName("linearShuntName")
                    .setConnectableBus("busA")
                    .newNonLinearModel()
                    .add()
                    .setCurrentSectionCount(6)
                .add();
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createLinearShunt("testMultiVariant", "testMultiVariant", 2.0, 5, 10);
        ShuntCompensator shunt = network.getShuntCompensator("testMultiVariant");
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
        }
    }

    private void createLinearShunt(String id, String name, double bPerSection, int currentSectionCount, int maxSectionCount) {
        createLinearShunt(id, name, bPerSection, currentSectionCount, maxSectionCount, false, Double.NaN, 0.0, null);
    }

    private void createLinearShunt(String id, String name, double bPerSection, int currentSectionCount, int maxSectionCount,
                                   boolean regulating, double targetV, double targetDeadband, Terminal regulatingTerminal) {
        voltageLevel.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setConnectableBus("busA")
                .newLinearModel()
                    .setbPerSection(bPerSection)
                    .setMaximumSectionCount(maxSectionCount)
                .add()
                .setCurrentSectionCount(currentSectionCount)
                .setRegulating(regulating)
                .setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .setRegulatingTerminal(regulatingTerminal)
                .add();
    }
}
