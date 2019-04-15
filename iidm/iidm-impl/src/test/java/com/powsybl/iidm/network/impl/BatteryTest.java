/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
    public void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLBAT");
    }

    @Test
    public void testSetterGetter() {
        Battery battery = network.getBattery("BAT");
        assertNotNull(battery);

        double p0 = 11.0;
        battery.setP0(p0);
        assertEquals(p0, battery.getP0(), 0.0);
        double q0 = 12.0;
        battery.setQ0(q0);
        assertEquals(q0, battery.getQ0(), 0.0);
        double minP = 10.0;
        battery.setMinP(minP);
        assertEquals(minP, battery.getMinP(), 0.0);
        double maxP = 20.0;
        battery.setMaxP(maxP);
        assertEquals(maxP, battery.getMaxP(), 0.0);

        assertEquals(ConnectableType.BATTERY, battery.getType());

        assertEquals("NBAT", battery.getTerminal().getBusBreakerView().getBus().getId());
    }

    @Test
    public void invalidP0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("p0 is invalid");
        createBattery("invalid", Double.NaN, 12.0, 10.0, 20.0);
    }

    @Test
    public void invalidQ0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("q0 is invalid");
        createBattery("invalid", 11, Double.NaN, 10.0, 20.0);
    }

    @Test
    public void invalidMinP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for minimum P");
        createBattery("invalid", 11, 12, Double.NaN, 20.0);
    }

    @Test
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createBattery("invalid", 11, 12, 10, Double.NaN);
    }

    @Test
    public void invalidLimitsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid active limits");
        createBattery("invalid", 11, 12, 20.0, 11.0);
    }

    @Test
    public void duplicateEquipment() {
        createBattery("duplicate", 11.0, 12, 10, 20.0);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("contains an object 'BatteryImpl' with the id 'duplicate'");
        createBattery("duplicate", 11.0, 12, 10, 20.0);
    }

    @Test
    public void duplicateId() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("with the id 'BAT'");
        createBattery("BAT", 11.0, 12, 10, 20.0);
    }

    private void createBattery(String id, double p0, double q0, double minP, double maxP) {
        voltageLevel.newBattery()
                .setId(id)
                .setP0(p0)
                .setQ0(q0)
                .setMinP(minP)
                .setMaxP(maxP)
                .setBus("NBAT")
                .add();
    }
}
