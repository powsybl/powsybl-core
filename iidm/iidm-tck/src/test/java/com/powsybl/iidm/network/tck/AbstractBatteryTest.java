/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public abstract class AbstractBatteryTest {

    private static final String INVALID = "invalid";

    private static final String TO_REMOVE = "toRemove";

    private static final String BAT_ID = "bat_id";

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

        assertEquals(IdentifiableType.BATTERY, battery.getType());

        assertEquals("NBAT", battery.getTerminal().getBusBreakerView().getBus().getId());
    }

    @Test
    public void invalidP0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("p0 is invalid");
        createBattery(INVALID, Double.NaN, 12.0, 10.0, 20.0);
    }

    @Test
    public void invalidQ0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("q0 is invalid");
        createBattery(INVALID, 11, Double.NaN, 10.0, 20.0);
    }

    @Test
    public void invalidMinP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for minimum P");
        createBattery(INVALID, 11, 12, Double.NaN, 20.0);
    }

    @Test
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createBattery(INVALID, 11, 12, 10, Double.NaN);
    }

    @Test
    public void invalidLimitsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("invalid active limits");
        createBattery(INVALID, 11, 12, 20.0, 11.0);
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

    @Test
    public void testAdder() {
        voltageLevel.newBattery()
                .setId(BAT_ID)
                .setMaxP(20.0)
                .setMinP(10.0)
                .setP0(15.0)
                .setQ0(10.0)
                .setBus("NBAT")
                .add();
        Battery battery = network.getBattery(BAT_ID);
        assertNotNull(battery);
        assertEquals(BAT_ID, battery.getId());
        assertEquals(20.0, battery.getMaxP(), 0.0);
        assertEquals(10.0, battery.getMinP(), 0.0);
        assertEquals(15.0, battery.getP0(), 0.0);
        assertEquals(10.0, battery.getQ0(), 0.0);
    }

    @Test
    public void testRemove() {
        String unmodifiableRemovedEqMessage = "Can not modify removed equipment";
        createBattery(TO_REMOVE, 11.0, 12, 10, 20.0);
        int count = network.getBatteryCount();
        Battery battery = network.getBattery(TO_REMOVE);
        assertNotNull(battery);
        battery.remove();
        assertNotNull(battery);
        Terminal terminal = battery.getTerminal();
        assertNotNull(terminal);
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
        try {
            bbView.setConnectableBus("TEST");
            fail();
        } catch (PowsyblException e) {
            assertEquals(unmodifiableRemovedEqMessage, e.getMessage());
        }
        assertNull(battery.getNetwork());
        assertEquals(count - 1L, network.getBatteryCount());
        assertNull(network.getBattery(TO_REMOVE));
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createBattery("testMultiVariant", 11.0, 12, 10, 20.0);

        Battery battery = network.getBattery("testMultiVariant");
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(11.0, battery.getP0(), 0.0);
        assertEquals(12.0, battery.getQ0(), 0.0);
        // change values in s4
        battery.setP0(11.1);
        battery.setQ0(12.2);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(11.1, battery.getP0(), 0.0);
        assertEquals(12.2, battery.getQ0(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(11.0, battery.getP0(), 0.0);
        assertEquals(12.0, battery.getQ0(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            battery.getP0();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
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
