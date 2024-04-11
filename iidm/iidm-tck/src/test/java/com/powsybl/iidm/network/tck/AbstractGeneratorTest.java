/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractGeneratorTest {

    private static final String GEN_ID = "gen_id";

    private static final String INVALID = "invalid";

    private static final String TO_REMOVE = "toRemove";

    private Network network;
    private VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = FictitiousSwitchFactory.create();
        voltageLevel = network.getVoltageLevel("C");
    }

    @Test
    public void testSetterGetter() {
        Generator generator = network.getGenerator("CB");
        assertNotNull(generator);
        assertEquals(EnergySource.HYDRO, generator.getEnergySource());
        generator.setEnergySource(EnergySource.NUCLEAR);
        assertEquals(EnergySource.NUCLEAR, generator.getEnergySource());
        double targetP = 15.0;
        generator.setTargetP(targetP);
        assertEquals(targetP, generator.getTargetP(), 0.0);
        double minP = 10.0;
        generator.setMinP(minP);
        assertEquals(minP, generator.getMinP(), 0.0);
        double maxP = 20.0;
        generator.setMaxP(maxP);
        assertEquals(maxP, generator.getMaxP(), 0.0);

        double activePowerSetpoint = 11.0;
        double reactivePowerSetpoint = 21.0;
        double voltageSetpoint = 31.0;
        double ratedS = 41.0;
        generator.setTargetP(activePowerSetpoint);
        generator.setTargetQ(reactivePowerSetpoint);
        generator.setTargetV(voltageSetpoint);
        generator.setRatedS(ratedS);
        assertEquals(activePowerSetpoint, generator.getTargetP(), 0.0);
        assertEquals(reactivePowerSetpoint, generator.getTargetQ(), 0.0);
        assertEquals(voltageSetpoint, generator.getTargetV(), 0.0);
        assertEquals(ratedS, generator.getRatedS(), 0.0);

        generator.setVoltageRegulatorOn(false);
        assertFalse(generator.isVoltageRegulatorOn());
        generator.setVoltageRegulatorOn(true);
        assertTrue(generator.isVoltageRegulatorOn());

        assertEquals(12, generator.getTerminal().getNodeBreakerView().getNode());
    }

    @Test
    public void undefinedVoltageRegulatorOn() {
        ValidationException e = assertThrows(ValidationException.class, () -> voltageLevel.newGenerator()
                .setId("GEN")
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE)
                .setTargetP(30.0)
                .setNode(1)
                .add());
        assertEquals("Generator 'GEN': voltage regulator status is not set", e.getMessage());
    }

    @Test
    public void invalidMaxP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, Double.NaN, 10.0, 20.0,
                30.0, 40.0, false, 20.0));
        assertTrue(e.getMessage().contains("for maximum P"));
    }

    @Test
    public void invalidMinP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, Double.NaN, 20.0,
                30.0, 40.0, false, 20.0));
        assertTrue(e.getMessage().contains("for minimum P"));
    }

    @Test
    public void invalidLimitsP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, 21., 20.0,
                30.0, 40.0, false, 20.0));
        assertTrue(e.getMessage().contains("invalid active limits"));
    }

    /**
     * This test goal is to check if targetP is allowed to be freely set outside the bounds defined by minP and maxP.
     * <p>
     * For a Battery it is expected that the current power is between these bounds, but it is not mandatory for a Generator</p>
     */
    @Test
    public void invalidPowerBounds() {
        // targetP < minP
        Generator invalidMinGenerator = createGenerator("invalid_min", EnergySource.HYDRO, 20.0, 10.0, 20.0,
                0.0, 40.0, false, 20.0);
        assertEquals(0.0, invalidMinGenerator.getTargetP(), 0.0);
        invalidMinGenerator.remove();

        // targetP > maxP
        Generator invalidMaxGenerator = createGenerator("invalid_max", EnergySource.HYDRO, 20.0, 10.0, 20.0,
                30.0, 40.0, false, 20.0);
        assertEquals(30.0, invalidMaxGenerator.getTargetP(), 0.0);

    }

    @Test
    public void invalidRatedS() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 0.0,
                15.0, 40.0, false, 20.0));
        assertTrue(e.getMessage().contains("Invalid value of rated S"));
    }

    @Test
    public void invalidRatedS2() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., -1.0,
                15.0, 40.0, false, 20.0));
        assertTrue(e.getMessage().contains("Invalid value of rated S"));
    }

    @Test
    public void invalidActiveP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 2.0,
                Double.NaN, 10.0, false, 10.0));
        assertTrue(e.getMessage().contains("for active power setpoint"));
    }

    @Test
    public void invalidReactiveQ() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, Double.NaN, false, 10.0));
        assertTrue(e.getMessage().contains("for reactive power setpoint"));
    }

    @Test
    public void invalidVoltageSetpoint() {
        ValidationException e = assertThrows(ValidationException.class, () -> createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 0.0));
        assertTrue(e.getMessage().contains("for voltage setpoint"));
    }

    @Test
    public void duplicateEquipment() {
        createGenerator("duplicate", EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);
        PowsyblException e = assertThrows(PowsyblException.class, () -> createGenerator("duplicate", EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0));
        assertTrue(e.getMessage().contains("contains an object 'GeneratorImpl' with the id 'duplicate'"));
    }

    @Test
    public void duplicateId() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createGenerator("A", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 2.0));
        assertTrue(e.getMessage().contains("with the id 'A'"));
    }

    @Test
    public void testAdder() {
        voltageLevel.newGenerator()
                .setId(GEN_ID)
                .setVoltageRegulatorOn(true)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMaxP(100.0)
                .setMinP(10.0)
                .setRatedS(2.0)
                .setTargetP(30.0)
                .setTargetQ(20.0)
                .setNode(1)
                .setTargetV(31.0)
                .add();
        Generator generator = network.getGenerator(GEN_ID);
        assertNotNull(generator);
        assertEquals(GEN_ID, generator.getId());
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(EnergySource.NUCLEAR, generator.getEnergySource());
        assertEquals(100.0, generator.getMaxP(), 0.0);
        assertEquals(10.0, generator.getMinP(), 0.0);
        assertEquals(2.0, generator.getRatedS(), 0.0);
        assertEquals(30.0, generator.getTargetP(), 0.0);
        assertEquals(20.0, generator.getTargetQ(), 0.0);
        assertEquals(31.0, generator.getTargetV(), 0.0);
    }

    @Test
    public void testRemove() {
        String unmodifiableRemovedEqMessage = "Cannot modify removed equipment " + TO_REMOVE;
        createGenerator(TO_REMOVE, EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);
        int count = network.getGeneratorCount();
        Generator generator = network.getGenerator(TO_REMOVE);
        assertNotNull(generator);
        generator.remove();
        assertNotNull(generator);
        Terminal terminal = generator.getTerminal();
        assertNotNull(terminal);
        try {
            terminal.isConnected();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access connectivity status of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.getNodeBreakerView().getNode();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access node of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.getBusBreakerView().getBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.getBusBreakerView().getConnectableBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.getBusView().getBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.getBusView().getConnectableBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.getVoltageLevel();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access voltage level of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            terminal.traverse(Mockito.mock(Terminal.TopologyTraverser.class));
            fail();
        } catch (PowsyblException e) {
            assertEquals("Associated equipment toRemove is removed", e.getMessage());
        }
        Terminal.BusBreakerView bbView = terminal.getBusBreakerView();
        assertNotNull(bbView);
        try {
            bbView.moveConnectable("BUS", true);
            fail();
        } catch (PowsyblException e) {
            assertEquals(unmodifiableRemovedEqMessage, e.getMessage());
        }
        Terminal.NodeBreakerView nbView = terminal.getNodeBreakerView();
        try {
            nbView.moveConnectable(0, "VL");
            fail();
        } catch (PowsyblException e) {
            assertEquals(unmodifiableRemovedEqMessage, e.getMessage());
        }
        try {
            terminal.setP(1.0);
            fail();
        } catch (PowsyblException e) {
            assertEquals(unmodifiableRemovedEqMessage, e.getMessage());
        }
        try {
            terminal.setQ(1.0);
            fail();
        } catch (PowsyblException e) {
            assertEquals(unmodifiableRemovedEqMessage, e.getMessage());
        }
        try {
            generator.getNetwork();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access network of removed equipment " + TO_REMOVE, e.getMessage());
        }
        assertEquals(count - 1L, network.getGeneratorCount());
        assertNull(network.getGenerator(TO_REMOVE));
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createGenerator("testMultiVariant", EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);
        Generator generator = network.getGenerator("testMultiVariant");
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(15.0, generator.getTargetP(), 0.0);
        assertEquals(40.0, generator.getTargetQ(), 0.0);
        assertEquals(2.0, generator.getTargetV(), 0.0);
        // change values in s4
        generator.setVoltageRegulatorOn(false);
        generator.setTargetP(12.1);
        generator.setTargetQ(9.2);
        generator.setTargetV(9.3);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertFalse(generator.isVoltageRegulatorOn());
        assertEquals(12.1, generator.getTargetP(), 0.0);
        assertEquals(9.2, generator.getTargetQ(), 0.0);
        assertEquals(9.3, generator.getTargetV(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(15.0, generator.getTargetP(), 0.0);
        assertEquals(40.0, generator.getTargetQ(), 0.0);
        assertEquals(2.0, generator.getTargetV(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            generator.getTargetP();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private Generator createGenerator(String id, EnergySource source, double maxP, double minP, double ratedS,
                                 double activePowerSetpoint, double reactivePowerSetpoint, boolean regulatorOn, double voltageSetpoint) {
        return voltageLevel.newGenerator()
                .setId(id)
                .setVoltageRegulatorOn(regulatorOn)
                .setEnergySource(source)
                .setMaxP(maxP)
                .setMinP(minP)
                .setRatedS(ratedS)
                .setTargetP(activePowerSetpoint)
                .setTargetQ(reactivePowerSetpoint)
                .setNode(1)
                .setTargetV(voltageSetpoint)
                .add();
    }
}
