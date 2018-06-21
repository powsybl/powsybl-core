/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GeneratorTest {

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
    public void invalidSource() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("energy source is not set");
        createGenerator("invalid", null, 20.0, 10.0, 20.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createGenerator("invalid", EnergySource.HYDRO, Double.NaN, 10.0, 20.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidMinP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for minimum P");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, Double.NaN, 20.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidLimitsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid active limits");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, 21., 20.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidRatedS() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Invalid value of rated S");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, 11., 0.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidRatedS2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Invalid value of rated S");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, 11., -1.0,
                30.0, 40.0, false, 20.0);
    }

    @Test
    public void invalidActiveP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for active power setpoint");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, 11., 2.0,
                Double.NaN, 10.0, false, 10.0);
    }

    @Test
    public void invalidReactiveQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for reactive power setpoint");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, Double.NaN, false, 10.0);
    }

    @Test
    public void invalidVoltageSetpoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for voltage setpoint");
        createGenerator("invalid", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 0.0);
    }

    @Test
    public void duplicateEquipment() {
        createGenerator("duplicate", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 2.0);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("contains an object 'GeneratorImpl' with the id 'duplicate'");
        createGenerator("duplicate", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 2.0);
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
                        .setId("gen_id")
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
        Generator generator = network.getGenerator("gen_id");
        assertNotNull(generator);
        assertEquals("gen_id", generator.getId());
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
        createGenerator("toRemove", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 2.0);
        int count = network.getGeneratorCount();
        Generator generator = network.getGenerator("toRemove");
        assertNotNull(generator);
        generator.remove();
        assertNotNull(generator);
        assertEquals(count - 1, network.getGeneratorCount());
        assertNull(network.getGenerator("toRemove"));
    }

    @Test
    public void testSetterGetterInMultiStates() {
        StateManager stateManager = network.getStateManager();
        createGenerator("testMultiState", EnergySource.HYDRO, 20.0, 11., 2.0,
                30.0, 40.0, true, 2.0);
        Generator generator = network.getGenerator("testMultiState");
        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, statesToAdd);

        stateManager.setWorkingState("s4");
        // check values cloned by extend
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(30.0, generator.getTargetP(), 0.0);
        assertEquals(40.0, generator.getTargetQ(), 0.0);
        assertEquals(2.0, generator.getTargetV(), 0.0);
        // change values in s4
        generator.setVoltageRegulatorOn(false);
        generator.setTargetP(9.1);
        generator.setTargetQ(9.2);
        generator.setTargetV(9.3);

        // remove s2
        stateManager.removeState("s2");

        stateManager.cloneState("s4", "s2b");
        stateManager.setWorkingState("s2b");
        // check values cloned by allocate
        assertFalse(generator.isVoltageRegulatorOn());
        assertEquals(9.1, generator.getTargetP(), 0.0);
        assertEquals(9.2, generator.getTargetQ(), 0.0);
        assertEquals(9.3, generator.getTargetV(), 0.0);

        // recheck initial state value
        stateManager.setWorkingState(StateManagerConstants.INITIAL_STATE_ID);
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(30.0, generator.getTargetP(), 0.0);
        assertEquals(40.0, generator.getTargetQ(), 0.0);
        assertEquals(2.0, generator.getTargetV(), 0.0);

        // remove working state s4
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        try {
            generator.getTargetP();
            fail();
        } catch (Exception ignored) {
        }
    }

    private void createGenerator(String id, EnergySource source, double maxP, double minP, double ratedS,
                                 double activePowerSetpoint, double reactivePowerSetpoint, boolean regulatorOn, double voltageSetpoint) {
        voltageLevel.newGenerator()
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
