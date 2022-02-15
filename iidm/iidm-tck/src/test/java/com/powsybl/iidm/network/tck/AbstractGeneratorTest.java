/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public abstract class AbstractGeneratorTest {

    private static final String GEN_ID = "gen_id";

    private static final String INVALID = "invalid";

    private static final String TO_REMOVE = "toRemove";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
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
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createGenerator(INVALID, EnergySource.HYDRO, Double.NaN, 10.0, 20.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidMinP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for minimum P");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, Double.NaN, 20.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidLimitsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid active limits");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, 21., 20.0,
                30.0, 40.0, false, 20.0);
    }

    /**
     * This test goal is to check if targetP is allowed to be freely set outside of the bounds defined by minP and maxP.
     * <p>
     * For a Battery it is expected that the current power is between this bounds but it is not mandatory for a Generator
     */
    @Test
    public void invalidPowerBounds() {
        // targetP < minP
        Generator invalidMinGenerator = createGenerator("invalid_min", EnergySource.HYDRO, 20.0, 10.0, 20.0,
                0.0, 40.0, false, 20.0);
        invalidMinGenerator.remove();

        // targetP > maxP
        createGenerator("invalid_max", EnergySource.HYDRO, 20.0, 10.0, 20.0,
                30.0, 40.0, false, 20.0);

    }

    @Test
    public void invalidRatedS() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Invalid value of rated S");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 0.0,
                15.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidRatedS2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Invalid value of rated S");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., -1.0,
                15.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidActiveP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for active power setpoint");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 2.0,
                Double.NaN, 10.0, false, 10.0);
    }

    @Test
    public void invalidReactiveQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for reactive power setpoint");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, Double.NaN, false, 10.0);
    }

    @Test
    public void invalidVoltageSetpoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for voltage setpoint");
        createGenerator(INVALID, EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 0.0);
    }

    @Test
    public void duplicateEquipment() {
        createGenerator("duplicate", EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("contains an object 'GeneratorImpl' with the id 'duplicate'");
        createGenerator("duplicate", EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);
    }

    @Test
    public void duplicateId() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("with the id 'A'");
        createGenerator("A", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 2.0);
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
        String unmodifiableRemovedEqMessage = "Can not modify removed equipment";
        createGenerator(TO_REMOVE, EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);
        int count = network.getGeneratorCount();
        Generator generator = network.getGenerator(TO_REMOVE);
        assertNotNull(generator);
        generator.remove();
        assertNotNull(generator);
        Terminal terminal = generator.getTerminal();
        assertNotNull(terminal);
        assertFalse(terminal.isConnected());
        assertEquals(-1, terminal.getNodeBreakerView().getNode());
        assertNull(terminal.getBusBreakerView().getBus());
        assertNull(terminal.getBusBreakerView().getConnectableBus());
        assertNull(terminal.getBusView().getBus());
        assertNull(terminal.getBusView().getConnectableBus());
        assertNull(terminal.getVoltageLevel());
        try {
            terminal.traverse(Mockito.mock(Terminal.TopologyTraverser.class));
            fail();
        } catch (PowsyblException e) {
            assertEquals("Associated equipment is removed", e.getMessage());
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
        assertNull(generator.getNetwork());
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
