/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

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

import static org.junit.Assert.*;

public abstract class AbstractShuntCompensatorTest {

    interface FooModel extends ShuntCompensatorModel {
    }

    private static final String INVALID = "invalid";

    private static final String SHUNT = "shunt";

    private static final String TEST_MULTI_VARIANT = "testMultiVariant";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;
    private Terminal terminal;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        network.getVoltageLevel("vl2").newLoad()
                .setId("load")
                .setBus("busB")
                .setConnectableBus("busB")
                .setP0(0)
                .setQ0(0)
                .add();
        terminal = network.getLoad("load").getTerminal();
        voltageLevel = network.getVoltageLevel("vl1");
    }

    @Test
    public void baseLinearShuntTest() {
        // adder
        ShuntCompensatorAdder adder = createShuntAdder(SHUNT, "shuntName", 6, terminal, true, 200, 10);
        adder.newLinearModel()
                .setBPerSection(5.0)
                .setGPerSection(4.0)
                .setMaximumSectionCount(10)
                .add();
        ShuntCompensator shuntCompensator = adder.add();

        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getOptionalName().orElse(null));
        assertEquals(SHUNT, shuntCompensator.getId());
        assertEquals(6, shuntCompensator.getSectionCount());
        assertEquals(10, shuntCompensator.getMaximumSectionCount());
        assertEquals(30.0, shuntCompensator.getB(), 0.0);
        assertEquals(24.0, shuntCompensator.getG(), 0.0);
        assertEquals(0.0, shuntCompensator.getB(0), 0.0);
        assertEquals(30.0, shuntCompensator.getB(6), 0.0);
        assertEquals(0.0, shuntCompensator.getG(0), 0.0);
        assertEquals(24.0, shuntCompensator.getG(6), 0.0);
        assertSame(terminal, shuntCompensator.getRegulatingTerminal());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());
        assertEquals(200, shuntCompensator.getTargetV(), 0.0);
        assertEquals(10, shuntCompensator.getTargetDeadband(), 0.0);
        assertEquals(ShuntCompensatorModelType.LINEAR, shuntCompensator.getModelType());
        ShuntCompensatorLinearModel shuntLinearModel = shuntCompensator.getModel(ShuntCompensatorLinearModel.class);
        assertEquals(5.0, shuntLinearModel.getBPerSection(), 0.0);
        assertEquals(4.0, shuntLinearModel.getGPerSection(), 0.0);

        // try get incorrect shunt model
        try {
            shuntCompensator.getModel(FooModel.class);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        // currentSectionCount
        try {
            shuntCompensator.setSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            // max = 10, current could not be 20
            shuntCompensator.setSectionCount(20);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setSectionCount(6);
        assertEquals(6, shuntCompensator.getSectionCount());

        // b
        try {
            shuntCompensator.getB(-1);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }
        try {
            shuntCompensator.getB(1000);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }

        // g
        try {
            shuntCompensator.getG(-1);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }
        try {
            shuntCompensator.getG(1000);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }

        // for linear model
        shuntLinearModel.setBPerSection(-1.0);
        assertEquals(-1.0, shuntLinearModel.getBPerSection(), 0.0);
        assertEquals(-6.0, shuntCompensator.getB(), 0.0);

        // gPerSection
        shuntLinearModel.setGPerSection(-2.0);
        assertEquals(-2.0, shuntLinearModel.getGPerSection(), 0.0);
        assertEquals(-12.0, shuntCompensator.getG(), 0.0);

        // maximumSectionCount
        try {
            shuntLinearModel.setMaximumSectionCount(1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntLinearModel.setMaximumSectionCount(20);
        assertEquals(20, shuntCompensator.getMaximumSectionCount());

        // remove
        int count = network.getShuntCompensatorCount();
        shuntCompensator.remove();
        assertNull(network.getShuntCompensator(SHUNT));
        assertNotNull(shuntCompensator);
        assertEquals(count - 1L, network.getShuntCompensatorCount());

        // Reuse adder tests
        // Create second model from same adder
        adder.setSectionCount(2)
             .newNonLinearModel()
                .beginSection()
                .setB(5.0)
                .setG(2.0)
                .endSection()
                .beginSection()
                .setB(6.0)
                .setG(2.0)
                .endSection()
                .add();
        // Create second ShuntCompensator from same adder
        ShuntCompensator shuntCompensator2 = adder.setId(SHUNT + "_2").add();
        assertNotSame(shuntCompensator.getModel(), shuntCompensator2.getModel());
        assertEquals(ShuntCompensatorModelType.NON_LINEAR, shuntCompensator2.getModelType());
    }

    @Test
    public void invalidbPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("section susceptance is invalid");
        createLinearShunt(INVALID, INVALID, Double.NaN, Double.NaN, 5, 10, null, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidNegativeMaxPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should be greater than 0");
        createLinearShunt(INVALID, INVALID, 2.0, Double.NaN, 0, -1, null, false, Double.NaN, Double.NaN);
    }

    @Test
    public void baseNonLinearShuntTest() {
        // adder
        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
                .setId(SHUNT)
                .setName("shuntName")
                .setConnectableBus("busA")
                .setSectionCount(1)
                .setRegulatingTerminal(terminal)
                .setVoltageRegulatorOn(true)
                .setTargetV(200)
                .setTargetDeadband(10);
        adder.newNonLinearModel()
                .beginSection()
                .setB(5.0)
                .setG(2.0)
                .endSection()
                .beginSection()
                .setB(6.0)
                .setG(2.0)
                .endSection()
                .add();
        ShuntCompensator shuntCompensator = adder.add();

        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getOptionalName().orElse(null));
        assertEquals("shuntName", shuntCompensator.getNameOrId());
        assertEquals(SHUNT, shuntCompensator.getId());
        assertEquals(1, shuntCompensator.getSectionCount());
        assertEquals(2, shuntCompensator.getMaximumSectionCount());
        assertEquals(5.0, shuntCompensator.getB(), 0.0);
        assertEquals(2.0, shuntCompensator.getG(), 0.0);
        assertEquals(0.0, shuntCompensator.getB(0), 0.0);
        assertEquals(5.0, shuntCompensator.getB(1), 0.0);
        assertEquals(6.0, shuntCompensator.getB(2), 0.0);
        assertEquals(0.0, shuntCompensator.getG(0), 0.0);
        assertEquals(2.0, shuntCompensator.getG(1), 0.0);
        assertEquals(2.0, shuntCompensator.getG(2), 0.0);
        assertSame(terminal, shuntCompensator.getRegulatingTerminal());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());
        assertEquals(200, shuntCompensator.getTargetV(), 0.0);
        assertEquals(10, shuntCompensator.getTargetDeadband(), 0.0);
        assertEquals(ShuntCompensatorModelType.NON_LINEAR, shuntCompensator.getModelType());
        ShuntCompensatorNonLinearModel shuntNonLinearModel = shuntCompensator.getModel(ShuntCompensatorNonLinearModel.class);
        assertEquals(2, shuntNonLinearModel.getAllSections().size());

        // try get incorrect shunt model
        try {
            shuntCompensator.getModel(FooModel.class);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        // currentSectionCount
        try {
            shuntCompensator.setSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            // exiting = 0, 1, 2, current could not be 20
            shuntCompensator.setSectionCount(20);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setSectionCount(2);
        assertEquals(2, shuntCompensator.getSectionCount());

        // b
        try {
            shuntCompensator.getB(-1);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }
        try {
            shuntCompensator.getB(1000);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }

        // g
        try {
            shuntCompensator.getG(-1);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }
        try {
            shuntCompensator.getG(1000);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }

        // Reuse adder tests
        ShuntCompensator shuntCompensator2 = adder.setId(SHUNT + "_2").add();
        assertNotSame(shuntCompensator.getModel(), shuntCompensator2.getModel());
    }

    @Test
    public void invalidEmptyNonLinearModel() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a shunt compensator must have at least one section");
        ShuntCompensatorAdder adder = createShuntAdder(INVALID, INVALID, 6, terminal, true, 200, 10);
        adder.newNonLinearModel().add();
    }

    @Test
    public void undefinedModel() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("the shunt compensator model has not been defined");
        voltageLevel.newShuntCompensator()
                .setId(INVALID)
                .setName(INVALID)
                .setConnectableBus("busA")
                .setSectionCount(6)
                .setRegulatingTerminal(terminal)
                .setVoltageRegulatorOn(false)
                .setTargetV(Double.NaN)
                .setTargetDeadband(Double.NaN)
                .add();
    }

    @Test
    public void regulationTest() {
        ShuntCompensator shuntCompensator = createLinearShunt(SHUNT, "shuntName", 5.0, 4.0, 6, 10, terminal, true,
                200, 10);

        // regulating terminal
        try {
            Network tmp = EurostagTutorialExample1Factory.create();
            Terminal tmpTerminal = tmp.getGenerator("GEN").getTerminal();
            shuntCompensator.setRegulatingTerminal(tmpTerminal);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            shuntCompensator.setRegulatingTerminal(null);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setVoltageRegulatorOn(false);
        shuntCompensator.setRegulatingTerminal(null);
        assertSame(null, shuntCompensator.getRegulatingTerminal());
        shuntCompensator.setRegulatingTerminal(terminal);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertSame(terminal, shuntCompensator.getRegulatingTerminal());

        // targetV
        try {
            shuntCompensator.setTargetV(Double.NaN);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            shuntCompensator.setTargetV(0.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setVoltageRegulatorOn(false);
        shuntCompensator.setTargetV(Double.NaN);
        assertEquals(Double.NaN, shuntCompensator.getTargetV(), 0.0);
        shuntCompensator.setTargetV(400);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertEquals(400, shuntCompensator.getTargetV(), 0.0);

        // targetDeadband
        try {
            shuntCompensator.setTargetDeadband(Double.NaN);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            shuntCompensator.setTargetDeadband(-1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setVoltageRegulatorOn(false);
        shuntCompensator.setTargetDeadband(Double.NaN);
        assertEquals(Double.NaN, shuntCompensator.getTargetDeadband(), 0.0);
        shuntCompensator.setTargetDeadband(5.0);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertEquals(5.0, shuntCompensator.getTargetDeadband(), 0.0);

        // voltageRegulatorOn
        shuntCompensator.setVoltageRegulatorOn(false);
        assertFalse(shuntCompensator.isVoltageRegulatorOn());
        shuntCompensator.setVoltageRegulatorOn(true);
        assertTrue(shuntCompensator.isVoltageRegulatorOn());
    }

    @Test
    public void invalidRegulatingTerminal() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Shunt compensator 'invalid': regulating terminal is not defined or is not part of the network");
        Network tmp = EurostagTutorialExample1Factory.create();
        Terminal tmpTerminal = tmp.getGenerator("GEN").getTerminal();
        createLinearShunt(INVALID, INVALID, 2.0, 1.0, 0, 10, tmpTerminal, true, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (-10.0) for voltage setpoint");
        createLinearShuntWithDefaultRegulatingTerminal(INVALID, INVALID, 2.0, 1.0, 0, 10, true, -10, 0);
    }

    @Test
    public void invalidNanTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Shunt compensator 'invalid': invalid value (NaN) for voltage setpoint");
        createLinearShuntWithDefaultRegulatingTerminal(INVALID, INVALID, 5.0, 1.0, 6, 10, true, Double.NaN, 0);
    }

    @Test
    public void invalidTargetDeadband() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Shunt compensator 'invalid': invalid value (-10.0) for target deadband");
        createLinearShuntWithDefaultRegulatingTerminal(INVALID, INVALID, 2.0, 1.0, 0, 10, true, 400.0, -10);
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createLinearShunt(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 2.0, 1.0, 5, 10, terminal, true, 200, 10);
        ShuntCompensator shunt = network.getShuntCompensator(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(5, shunt.getSectionCount());
        assertEquals(10.0, shunt.getB(), 0.0); // 2*5
        assertEquals(5.0, shunt.getG(), 0.0); // 1*5
        // change values in s4
        shunt.setSectionCount(4)
                .setVoltageRegulatorOn(false)
                .setTargetV(220)
                .setTargetDeadband(5.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(4, shunt.getSectionCount());
        assertEquals(8.0, shunt.getB(), 0.0); // 2*4
        assertEquals(4.0, shunt.getG(), 0.0); // 1*4
        assertFalse(shunt.isVoltageRegulatorOn());
        assertEquals(220, shunt.getTargetV(), 0.0);
        assertEquals(5.0, shunt.getTargetDeadband(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(5, shunt.getSectionCount());
        assertEquals(10.0, shunt.getB(), 0.0); // 2*5
        assertEquals(5.0, shunt.getG(), 0.0); // 1*5
        assertTrue(shunt.isVoltageRegulatorOn());
        assertEquals(200, shunt.getTargetV(), 0.0);
        assertEquals(10, shunt.getTargetDeadband(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            shunt.getSectionCount();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
        try {
            shunt.isVoltageRegulatorOn();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
        try {
            shunt.getTargetV();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
        try {
            shunt.getTargetDeadband();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private ShuntCompensator createLinearShuntWithDefaultRegulatingTerminal(String id, String name, double bPerSection, double gPerSection, int sectionCount, int maxSectionCount, boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        ShuntCompensator sc = createLinearShunt(id, name, bPerSection, gPerSection, sectionCount, maxSectionCount, null, false, targetV, targetDeadband);
        sc.setRegulatingTerminal(sc.getTerminal())
            .setVoltageRegulatorOn(voltageRegulatorOn);
        return sc;
    }

    private ShuntCompensator createLinearShunt(String id, String name, double bPerSection, double gPerSection, int sectionCount, int maxSectionCount, Terminal regulatingTerminal, boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        return createShuntAdder(id, name, sectionCount, regulatingTerminal, voltageRegulatorOn, targetV, targetDeadband)
                .newLinearModel()
                .setBPerSection(bPerSection)
                .setGPerSection(gPerSection)
                .setMaximumSectionCount(maxSectionCount)
                .add()
                .add();
    }

    private ShuntCompensatorAdder createShuntAdder(String id, String name, int sectionCount, Terminal regulatingTerminal, boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        return voltageLevel.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setConnectableBus("busA")
                .setSectionCount(sectionCount)
                .setRegulatingTerminal(regulatingTerminal)
                .setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .setVoltageRegulatorOn(voltageRegulatorOn);
    }
}
