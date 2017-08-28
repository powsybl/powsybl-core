/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        Float minP = 10.0f;
        generator.setMinP(minP);
        assertEquals(minP, generator.getMinP(), 0.0f);
        Float maxP = 20.0f;
        generator.setMaxP(maxP);
        assertEquals(maxP, generator.getMaxP(), 0.0f);

        Float activePowerSetpoint = 11.0f;
        Float reactivePowerSetpoint = 21.0f;
        Float voltageSetpoint = 31.0f;
        Float ratedS = 41.0f;
        generator.setTargetP(activePowerSetpoint);
        generator.setTargetQ(reactivePowerSetpoint);
        generator.setTargetV(voltageSetpoint);
        generator.setRatedS(ratedS);
        assertEquals(activePowerSetpoint, generator.getTargetP(), 0.0f);
        assertEquals(reactivePowerSetpoint, generator.getTargetQ(), 0.0f);
        assertEquals(voltageSetpoint, generator.getTargetV(), 0.0f);
        assertEquals(ratedS, generator.getRatedS(), 0.0f);

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
        createGenerator("invalid", null, 20.0f, 10.0f, 20.0f,
                30.0f, 40.0f, false, 20.0f);
    }

    @Test
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createGenerator("invalid", EnergySource.HYDRO, Float.NaN, 10.0f, 20.0f,
                30.0f, 40.0f, false, 20.0f);
    }

    @Test
    public void invalidMinP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for minimum P");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, Float.NaN, 20.0f,
                30.0f, 40.0f, false, 20.0f);
    }

    @Test
    public void invalidLimitsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid active limits");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, 21.f, 20.0f,
                30.0f, 40.0f, false, 20.0f);
    }

    @Test
    public void invalidRatedS() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Invalid value of rated S");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, 11.f, 0.0f,
                30.0f, 40.0f, false, 20.0f);
    }

    @Test
    public void invalidRatedS2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Invalid value of rated S");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, 11.f, -1.0f,
                30.0f, 40.0f, false, 20.0f);
    }

    @Test
    public void invalidActiveP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for active power setpoint");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                Float.NaN, 10.0f, false, 10.0f);
    }

    @Test
    public void invalidReactiveQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for reactive power setpoint");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                30.0f, Float.NaN, false, 10.0f);
    }

    @Test
    public void invalidVoltageSetpoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for voltage setpoint");
        createGenerator("invalid", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                30.0f, 40.0f, true, 0.0f);
    }

    @Test
    public void duplicateEquipment() {
        createGenerator("duplicate", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                30.0f, 40.0f, true, 2.0f);
        thrown.expect(ITeslaException.class);
        thrown.expectMessage("contains an object 'GeneratorImpl' with the id 'duplicate'");
        createGenerator("duplicate", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                30.0f, 40.0f, true, 2.0f);
    }

    @Test
    public void duplicateId() {
        thrown.expect(ITeslaException.class);
        thrown.expectMessage("with the id 'A'");
        createGenerator("A", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                30.0f, 40.0f, true, 2.0f);
    }

    @Test
    public void testAdder() {
        voltageLevel.newGenerator()
                        .setId("gen_id")
                        .setVoltageRegulatorOn(true)
                        .setEnergySource(EnergySource.NUCLEAR)
                        .setMaxP(100.0f)
                        .setMinP(10.0f)
                        .setRatedS(2.0f)
                        .setTargetP(30.0f)
                        .setTargetQ(20.0f)
                        .setNode(1)
                        .setTargetV(31.0f)
                    .add();
        Generator generator = network.getGenerator("gen_id");
        assertNotNull(generator);
        assertEquals("gen_id", generator.getId());
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(EnergySource.NUCLEAR, generator.getEnergySource());
        assertEquals(100.0f, generator.getMaxP(), 0.0f);
        assertEquals(10.0f, generator.getMinP(), 0.0f);
        assertEquals(2.0f, generator.getRatedS(), 0.0f);
        assertEquals(30.0f, generator.getTargetP(), 0.0f);
        assertEquals(20.0f, generator.getTargetQ(), 0.0f);
        assertEquals(31.0f, generator.getTargetV(), 0.0f);
    }

    @Test
    public void testRemove() {
        createGenerator("toRemove", EnergySource.HYDRO, 20.0f, 11.f, 2.0f,
                30.0f, 40.0f, true, 2.0f);
        int count = network.getGeneratorCount();
        Generator generator = network.getGenerator("toRemove");
        assertNotNull(generator);
        generator.remove();
        assertNotNull(generator);
        assertEquals(count - 1, network.getGeneratorCount());
        assertNull(network.getGenerator("toRemove"));
    }

    private void createGenerator(String id, EnergySource source, float maxP, float minP, float ratedS,
                                 float activePowerSetpoint, float reactivePowerSetpoint, boolean regulatorOn, float voltageSetpoint) {
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
