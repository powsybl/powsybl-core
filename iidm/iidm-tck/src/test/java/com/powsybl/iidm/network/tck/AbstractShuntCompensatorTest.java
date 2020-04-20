/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

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
    public void baseTest() {
        // adder
        ShuntCompensator shuntCompensator = voltageLevel.newShuntCompensator()
                .setId(SHUNT)
                .setName("shuntName")
                .setConnectableBus("busA")
                .setbPerSection(5.0)
                .setCurrentSectionCount(6)
                .setMaximumSectionCount(10)
                .setRegulatingTerminal(terminal)
                .setVoltageRegulatorOn(true)
                .setTargetV(200)
                .setTargetDeadband(10)
                .add();
        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getName());
        assertEquals(SHUNT, shuntCompensator.getId());
        assertEquals(5.0, shuntCompensator.getbPerSection(), 0.0);
        assertEquals(6, shuntCompensator.getCurrentSectionCount());
        assertEquals(10, shuntCompensator.getMaximumSectionCount());
        assertSame(terminal, shuntCompensator.getRegulatingTerminal());
        assertTrue(shuntCompensator.isVoltageRegulatorOn());
        assertEquals(200, shuntCompensator.getTargetV(), 0.0);
        assertEquals(10, shuntCompensator.getTargetDeadband(), 0.0);

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

        try {
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
        shuntCompensator.setVoltageRegulatorOn(false);
        shuntCompensator.setTargetV(400);
        assertFalse(shuntCompensator.isVoltageRegulatorOn());
        assertEquals(400, shuntCompensator.getTargetV(), 0.0);

        try {
            shuntCompensator.setTargetDeadband(-1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        shuntCompensator.setTargetDeadband(5.0);
        assertEquals(5.0, shuntCompensator.getTargetDeadband(), 0.0);

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
        createShunt(INVALID, INVALID, Double.NaN, 5, 10, null, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidZerobPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is equal to zero");
        createShunt(INVALID, INVALID, 0.0, 5, 10, null, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidNegativeMaxPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should be greater than 0");
        createShunt(INVALID, INVALID, 2.0, 0, -1, null, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidRegulatingTerminal() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("regulating terminal is not part of the network");
        Network tmp = EurostagTutorialExample1Factory.create();
        Terminal tmpTerminal = tmp.getGenerator("GEN").getTerminal();
        createShunt(INVALID, INVALID, 2.0, 0, 10, tmpTerminal, false, Double.NaN, Double.NaN);
    }

    @Test
    public void invalidTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid value (-10.0) for voltage setpoint");
        createShunt(INVALID, INVALID, 2.0, 0, 10, null, true, -10, Double.NaN);
    }

    @Test
    public void invalidTargetDeadband() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Unexpected value for target deadband of shunt compensator: -10.0");
        createShunt(INVALID, INVALID, 2.0, 0, 10, null, false, Double.NaN, -10);
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createShunt(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 2.0, 5, 10, terminal, true, 200, 10);
        ShuntCompensator shunt = network.getShuntCompensator(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(5, shunt.getCurrentSectionCount());
        assertEquals(10.0, shunt.getCurrentB(), 0.0); // 2*5
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
        assertFalse(shunt.isVoltageRegulatorOn());
        assertEquals(220, shunt.getTargetV(), 0.0);
        assertEquals(5.0, shunt.getTargetDeadband(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(5, shunt.getCurrentSectionCount());
        assertEquals(10.0, shunt.getCurrentB(), 0.0); // 2*5
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

    private void createShunt(String id, String name, double bPerSection, int currentSectionCount, int maxSectionCount, Terminal regulatingTerminal, boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        voltageLevel.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setConnectableBus("busA")
                .setbPerSection(bPerSection)
                .setCurrentSectionCount(currentSectionCount)
                .setMaximumSectionCount(maxSectionCount)
                .setRegulatingTerminal(regulatingTerminal)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .add();
    }
}
