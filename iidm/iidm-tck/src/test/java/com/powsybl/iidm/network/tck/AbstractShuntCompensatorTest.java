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
        ShuntCompensator shuntCompensator = createLinearShunt(SHUNT, "shuntName", 5.0, 4.0,
                6, 10, terminal, true, 200, 10);

        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getOptionalName().orElse(null));
        assertEquals(SHUNT, shuntCompensator.getId());
        assertEquals(6, shuntCompensator.getCurrentSectionCount());
        assertEquals(10, shuntCompensator.getMaximumSectionCount());
        assertEquals(30.0, shuntCompensator.getCurrentB(), 0.0);
        assertEquals(24.0, shuntCompensator.getCurrentG(), 0.0);
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
        assertEquals(5.0, shuntLinearModel.getbPerSection(), 0.0);
        assertEquals(4.0, shuntLinearModel.getgPerSection(), 0.0);

        // try get incorrect shunt model
        try {
            shuntCompensator.getModel(FooModel.class);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        // currentSectionCount
        try {
            shuntCompensator.setCurrentSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            // max = 10, current could not be 20
            shuntCompensator.setCurrentSectionCount(20);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setCurrentSectionCount(6);
        assertEquals(6, shuntCompensator.getCurrentSectionCount());

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

        // bPerSection
        try {
            shuntLinearModel.setbPerSection(0.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntLinearModel.setbPerSection(-1.0);
        assertEquals(-1.0, shuntLinearModel.getbPerSection(), 0.0);
        assertEquals(-6.0, shuntCompensator.getCurrentB(), 0.0);

        // gPerSection
        shuntLinearModel.setgPerSection(-2.0);
        assertEquals(-2.0, shuntLinearModel.getgPerSection(), 0.0);
        assertEquals(-12.0, shuntCompensator.getCurrentG(), 0.0);

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
    }

    @Test
    public void invalidbPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("section susceptance is invalid");
        createLinearShunt(INVALID, INVALID, Double.NaN, Double.NaN, 5, 10, null, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidZerobPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is equal to zero");
        createLinearShunt(INVALID, INVALID, 0.0, Double.NaN, 5, 10, null, false, Double.NaN, Double.NaN);
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
                .setCurrentSectionCount(1)
                .setRegulatingTerminal(terminal)
                .setVoltageRegulatorOn(true)
                .setTargetV(200)
                .setTargetDeadband(10);
        adder.newNonLinearModel()
                .beginSection()
                    .setSectionIndex(1)
                    .setB(5.0)
                    .setG(2.0)
                .endSection()
                .beginSection()
                    .setSectionIndex(2)
                    .setB(6.0)
                .endSection()
                .add();
        ShuntCompensator shuntCompensator = adder.add();

        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getOptionalName().orElse(null));
        assertEquals("shuntName", shuntCompensator.getNameOrId());
        assertEquals(SHUNT, shuntCompensator.getId());
        assertEquals(1, shuntCompensator.getCurrentSectionCount());
        assertEquals(2, shuntCompensator.getMaximumSectionCount());
        assertEquals(5.0, shuntCompensator.getCurrentB(), 0.0);
        assertEquals(2.0, shuntCompensator.getCurrentG(), 0.0);
        assertEquals(0.0, shuntCompensator.getB(0), 0.0);
        assertEquals(5.0, shuntCompensator.getB(1), 0.0);
        assertEquals(11.0, shuntCompensator.getB(2), 0.0);
        assertEquals(0.0, shuntCompensator.getG(0), 0.0);
        assertEquals(2.0, shuntCompensator.getG(1), 0.0);
        assertEquals(2.0, shuntCompensator.getG(2), 0.0);
        assertSame(terminal, shuntCompensator.getRegulatingTerminal());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());
        assertEquals(200, shuntCompensator.getTargetV(), 0.0);
        assertEquals(10, shuntCompensator.getTargetDeadband(), 0.0);
        assertEquals(ShuntCompensatorModelType.NON_LINEAR, shuntCompensator.getModelType());
        ShuntCompensatorNonLinearModel shuntNonLinearModel = shuntCompensator.getModel(ShuntCompensatorNonLinearModel.class);
        assertEquals(2, shuntNonLinearModel.getSections().size());
        assertFalse(shuntNonLinearModel.getSection(3).isPresent());
        assertFalse(shuntNonLinearModel.getSection(3).isPresent());

        // try get incorrect shunt model
        try {
            shuntCompensator.getModel(FooModel.class);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        // currentSectionCount
        try {
            shuntCompensator.setCurrentSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            // exiting = 0, 1, 2, current could not be 20
            shuntCompensator.setCurrentSectionCount(20);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setCurrentSectionCount(2);
        assertEquals(2, shuntCompensator.getCurrentSectionCount());

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
        // for non linear model

        // getBSection
        try {
            // try to get susceptance of a non-existing section
            shuntNonLinearModel.getBSection(3);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }

        // getGSection
        try {
            // try to get conductance of a non-existing section
            shuntNonLinearModel.getGSection(3);
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }

        // add or replace a section
        try {
            // try to add or replace with negative section number
            shuntNonLinearModel.addOrReplaceSection(-2, 4.0, 1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignored
        }
        try {
            // try to add or replace with a negative susceptance
            shuntNonLinearModel.addOrReplaceSection(3, Double.NaN, 1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignored
        }
        shuntNonLinearModel.addOrReplaceSection(3, 4.0, 1.0); // add a section
        assertEquals(3, shuntNonLinearModel.getSections().size());
        assertEquals(4.0, shuntNonLinearModel.getBSection(3), 0.0);
        assertEquals(1.0, shuntNonLinearModel.getGSection(3), 0.0);
        assertEquals(3, shuntCompensator.getMaximumSectionCount());
        shuntNonLinearModel.addOrReplaceSection(1, -3.0, -1.5); // replace a section
        assertEquals(3, shuntNonLinearModel.getSections().size());
        assertEquals(-3.0, shuntNonLinearModel.getBSection(1), 0.0);
        assertEquals(-1.5, shuntNonLinearModel.getGSection(1), 0.0);

        // remove a section
        try {
            // try to remove a section with negative section number
            shuntNonLinearModel.removeSection(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignored
        }
        try {
            // try to remove a non-existing section
            shuntNonLinearModel.removeSection(4);
            fail();
        } catch (ValidationException ignored) {
            // ignored
        }
        try {
            // try to remove the current section
            shuntNonLinearModel.removeSection(2);
            fail();
        } catch (ValidationException ignored) {
            // ignored
        }
        shuntNonLinearModel.removeSection(3);
        assertEquals(2, shuntNonLinearModel.getSections().size());
        assertFalse(shuntNonLinearModel.getSection(3).isPresent());
        assertEquals(2, shuntCompensator.getMaximumSectionCount());
    }

    @Test
    public void invalidEmptyNonLinearModel() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a shunt compensator must have at least one section");
        ShuntCompensatorAdder adder = createShuntAdder(INVALID, INVALID, 6, terminal, true, 200, 10);
        adder.newNonLinearModel().add();
    }

    @Test
    public void invalidExistingSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a section is already defined at this number");
        ShuntCompensatorAdder adder = createShuntAdder(INVALID, INVALID, 6, terminal, true, 200, 10);
        adder.newNonLinearModel()
                .beginSection()
                    .setSectionIndex(1)
                    .setB(5.0)
                .endSection()
                .beginSection()
                    .setSectionIndex(1)
                    .setB(4.0)
                .endSection();
    }

    @Test
    public void undefinedModel() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("the shunt compensator model has not been defined");
        voltageLevel.newShuntCompensator()
                .setId(INVALID)
                .setName(INVALID)
                .setConnectableBus("busA")
                .setCurrentSectionCount(6)
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
        shuntCompensator.setRegulatingTerminal(null);
        assertSame(shuntCompensator.getTerminal(), shuntCompensator.getRegulatingTerminal());

        // voltageRegulatorOn
        shuntCompensator.setVoltageRegulatorOn(false);
        assertFalse(shuntCompensator.isVoltageRegulatorOn());

        // targetV
        try {
            shuntCompensator.setVoltageRegulatorOn(true);
            shuntCompensator.setTargetV(Double.NaN);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            shuntCompensator.setVoltageRegulatorOn(false);
            shuntCompensator.setTargetV(Double.NaN);
            shuntCompensator.setVoltageRegulatorOn(true);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setTargetV(400);
        assertEquals(400, shuntCompensator.getTargetV(), 0.0);

        // targetDeadband
        try {
            shuntCompensator.setTargetDeadband(-1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            shuntCompensator.setVoltageRegulatorOn(false);
            shuntCompensator.setTargetDeadband(Double.NaN);
            shuntCompensator.setVoltageRegulatorOn(true);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setTargetDeadband(5.0);
        assertEquals(5.0, shuntCompensator.getTargetDeadband(), 0.0);
    }

    @Test
    public void invalidRegulatingTerminal() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("regulating terminal is not part of the network");
        Network tmp = EurostagTutorialExample1Factory.create();
        Terminal tmpTerminal = tmp.getGenerator("GEN").getTerminal();
        createLinearShunt(INVALID, INVALID, 2.0, 1.0, 0, 10, tmpTerminal, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (-10.0) for voltage setpoint");
        createLinearShunt(INVALID, INVALID, 2.0, 1.0, 0, 10, null, true, -10, 0);
    }

    @Test
    public void invalidNanTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (NaN) for voltage setpoint (voltage regulator is on)");
        createLinearShunt(INVALID, INVALID, 5.0, 1.0, 6, 10, null, true, Double.NaN, 0);
    }

    @Test
    public void invalidTargetDeadband() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Unexpected value for target deadband of shunt compensator: -10.0");
        createLinearShunt(INVALID, INVALID, 2.0, 1.0, 0, 10, null, false, Double.NaN, -10);
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
        assertEquals(5, shunt.getCurrentSectionCount());
        assertEquals(10.0, shunt.getCurrentB(), 0.0); // 2*5
        assertEquals(5.0, shunt.getCurrentG(), 0.0); // 1*5
        // change values in s4
        shunt.setCurrentSectionCount(4)
                .setVoltageRegulatorOn(false)
                .setTargetV(220)
                .setTargetDeadband(5.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(4, shunt.getCurrentSectionCount());
        assertEquals(8.0, shunt.getCurrentB(), 0.0); // 2*4
        assertEquals(4.0, shunt.getCurrentG(), 0.0); // 1*4
        assertFalse(shunt.isVoltageRegulatorOn());
        assertEquals(220, shunt.getTargetV(), 0.0);
        assertEquals(5.0, shunt.getTargetDeadband(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(5, shunt.getCurrentSectionCount());
        assertEquals(10.0, shunt.getCurrentB(), 0.0); // 2*5
        assertEquals(5.0, shunt.getCurrentG(), 0.0); // 1*5
        assertTrue(shunt.isVoltageRegulatorOn());
        assertEquals(200, shunt.getTargetV(), 0.0);
        assertEquals(10, shunt.getTargetDeadband(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            shunt.getCurrentSectionCount();
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

    private ShuntCompensator createLinearShunt(String id, String name, double bPerSection, double gPerSection, int currentSectionCount, int maxSectionCount, Terminal regulatingTerminal, boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        return createShuntAdder(id, name, currentSectionCount, regulatingTerminal, voltageRegulatorOn, targetV, targetDeadband)
                .newLinearModel()
                    .setbPerSection(bPerSection)
                    .setgPerSection(gPerSection)
                    .setMaximumSectionCount(maxSectionCount)
                    .add()
                .add();
    }

    private ShuntCompensatorAdder createShuntAdder(String id, String name, int currentSectionCount, Terminal regulatingTerminal, boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        return voltageLevel.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setConnectableBus("busA")
                .setCurrentSectionCount(currentSectionCount)
                .setRegulatingTerminal(regulatingTerminal)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetV(targetV)
                .setTargetDeadband(targetDeadband);
    }
}
