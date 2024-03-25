/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public abstract class AbstractBatteryTest {

    private static final String INVALID = "invalid";

    private static final String TO_REMOVE = "toRemove";

    private static final String BAT_ID = "bat_id";

    private Network network;
    private VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLBAT");
    }

    @Test
    public void testSetterGetter() {
        Battery battery = network.getBattery("BAT");
        assertNotNull(battery);

        double targetP = 11.0;
        battery.setTargetP(targetP);
        assertEquals(targetP, battery.getTargetP(), 0.0);
        double targetQ = 12.0;
        battery.setTargetQ(targetQ);
        assertEquals(targetQ, battery.getTargetQ(), 0.0);
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
        ValidationException e = assertThrows(ValidationException.class, () -> createBattery(INVALID, Double.NaN, 12.0, 10.0, 20.0));
        assertTrue(e.getMessage().contains("p0 is invalid"));
    }

    @Test
    public void invalidQ0() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBattery(INVALID, 11, Double.NaN, 10.0, 20.0));
        assertTrue(e.getMessage().contains("q0 is invalid"));
    }

    @Test
    public void invalidMinP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBattery(INVALID, 11, 12, Double.NaN, 20.0));
        assertTrue(e.getMessage().contains("for minimum P"));
    }

    @Test
    public void invalidMaxP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBattery(INVALID, 11, 12, 10, Double.NaN));
        assertTrue(e.getMessage().contains("for maximum P"));
    }

    @Test
    public void invalidLimitsP() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBattery(INVALID, 11, 12, 20.0, 11.0));
        assertTrue(e.getMessage().contains("invalid active limits"));
    }

    @Test
    public void duplicateEquipment() {
        createBattery("duplicate", 11.0, 12, 10, 20.0);
        PowsyblException e = assertThrows(PowsyblException.class, () -> createBattery("duplicate", 11.0, 12, 10, 20.0));
        assertTrue(e.getMessage().contains("contains an object 'BatteryImpl' with the id 'duplicate'"));
    }

    @Test
    public void duplicateId() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createBattery("BAT", 11.0, 12, 10, 20.0));
        assertTrue(e.getMessage().contains("with the id 'BAT'"));
    }

    @Test
    public void testAdder() {
        voltageLevel.newBattery()
                .setId(BAT_ID)
                .setMaxP(20.0)
                .setMinP(10.0)
                .setTargetP(15.0)
                .setTargetQ(10.0)
                .setBus("NBAT")
                .add();
        Battery battery = network.getBattery(BAT_ID);
        assertNotNull(battery);
        assertEquals(BAT_ID, battery.getId());
        assertEquals(20.0, battery.getMaxP(), 0.0);
        assertEquals(10.0, battery.getMinP(), 0.0);
        assertEquals(15.0, battery.getTargetP(), 0.0);
        assertEquals(10.0, battery.getTargetQ(), 0.0);
    }

    @Test
    public void testRemove() {
        String unmodifiableRemovedEqMessage = "Cannot modify removed equipment " + TO_REMOVE;
        createBattery(TO_REMOVE, 11.0, 12, 10, 20.0);
        int count = network.getBatteryCount();
        Battery battery = network.getBattery(TO_REMOVE);
        assertNotNull(battery);
        battery.remove();
        assertNotNull(battery);
        Terminal terminal = battery.getTerminal();
        assertNotNull(terminal);
        try {
            terminal.isConnected();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access connectivity status of removed equipment " + TO_REMOVE, e.getMessage());
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
            bbView.setConnectableBus("TEST");
            fail();
        } catch (PowsyblException e) {
            assertEquals(unmodifiableRemovedEqMessage, e.getMessage());
        }
        try {
            battery.getNetwork();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access network of removed equipment toRemove", e.getMessage());
        }
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
        assertEquals(11.0, battery.getTargetP(), 0.0);
        assertEquals(12.0, battery.getTargetQ(), 0.0);
        // change values in s4
        battery.setTargetP(11.1);
        battery.setTargetQ(12.2);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(11.1, battery.getTargetP(), 0.0);
        assertEquals(12.2, battery.getTargetQ(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(11.0, battery.getTargetP(), 0.0);
        assertEquals(12.0, battery.getTargetQ(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            battery.getTargetP();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void createBattery(String id, double targetP, double targetQ, double minP, double maxP) {
        voltageLevel.newBattery()
                .setId(id)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setMinP(minP)
                .setMaxP(maxP)
                .setBus("NBAT")
                .add();
    }
}
