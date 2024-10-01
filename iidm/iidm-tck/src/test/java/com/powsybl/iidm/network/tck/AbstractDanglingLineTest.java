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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractDanglingLineTest {

    private static final String DUPLICATE = "duplicate";

    private static final String INVALID = "invalid";

    private static final String TEST_MULTI_VARIANT = "testMultiVariant";

    private static final String TO_REMOVE = "toRemove";

    private static final String BUS_VL_ID = "bus_vl";

    private Network network;
    private VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = Network.create("test", "test");
        Substation substation = network.newSubstation()
                .setId("sub")
                .setCountry(Country.FR)
                .setTso("RTE")
            .add();
        voltageLevel = substation.newVoltageLevel()
                                    .setId("vl")
                                    .setName("vl")
                                    .setNominalV(440.0)
                                    .setHighVoltageLimit(400.0)
                                    .setLowVoltageLimit(200.0)
                                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                                .add();
        voltageLevel.getBusBreakerView().newBus()
                                            .setId(BUS_VL_ID)
                                            .setName(BUS_VL_ID)
                                        .add();
    }

    @Test
    public void baseTests() {
        double r = 10.0;
        double x = 20.0;
        double g = 30.0;
        double b = 40.0;
        double p0 = 50.0;
        double q0 = 60.0;
        String id = "danglingId";
        String name = "danlingName";
        String pairingKey = "code";
        String newPairingKey = "new_code";
        voltageLevel.newDanglingLine()
                        .setId(id)
                        .setName(name)
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setP0(p0)
                        .setQ0(q0)
                        .setPairingKey(pairingKey)
                        .setBus(BUS_VL_ID)
                        .setConnectableBus(BUS_VL_ID)
                        .setHasShuntAdmittanceLineEquivalentModel(true)
                        .add();
        DanglingLine danglingLine = network.getDanglingLine(id);
        // adder
        assertEquals(IdentifiableType.DANGLING_LINE, danglingLine.getType());
        assertEquals(r, danglingLine.getR(), 0.0);
        assertEquals(x, danglingLine.getX(), 0.0);
        assertEquals(g, danglingLine.getG(), 0.0);
        assertEquals(b, danglingLine.getB(), 0.0);
        assertEquals(p0, danglingLine.getP0(), 0.0);
        assertEquals(q0, danglingLine.getQ0(), 0.0);
        assertEquals(id, danglingLine.getId());
        assertEquals(name, danglingLine.getOptionalName().orElse(null));
        assertEquals(name, danglingLine.getNameOrId());
        assertEquals(pairingKey, danglingLine.getPairingKey());
        assertNull(danglingLine.getGeneration());

        // setter getter
        double r2 = 11.0;
        double x2 = 21.0;
        double g2 = 31.0;
        double b2 = 41.0;
        double p02 = 51.0;
        double q02 = 61.0;
        danglingLine.setR(r2);
        assertEquals(r2, danglingLine.getR(), 0.0);
        danglingLine.setX(x2);
        assertEquals(x2, danglingLine.getX(), 0.0);
        danglingLine.setG(g2);
        assertEquals(g2, danglingLine.getG(), 0.0);
        danglingLine.setB(b2);
        assertEquals(b2, danglingLine.getB(), 0.0);
        danglingLine.setP0(p02);
        assertEquals(p02, danglingLine.getP0(), 0.0);
        danglingLine.setQ0(q02);
        assertEquals(q02, danglingLine.getQ0(), 0.0);
        danglingLine.setPairingKey(newPairingKey);
        assertEquals(newPairingKey, danglingLine.getPairingKey());

        danglingLine.newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();
        assertEquals(100.0, danglingLine.getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        danglingLine.newActivePowerLimits()
                .setPermanentLimit(60.0)
                .add();
        assertEquals(60.0, danglingLine.getActivePowerLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        danglingLine.newApparentPowerLimits()
                .setPermanentLimit(132.0)
                .add();
        assertEquals(132.0, danglingLine.getApparentPowerLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        Bus bus = voltageLevel.getBusBreakerView().getBus(BUS_VL_ID);
        Bus terminal = danglingLine.getTerminal().getBusBreakerView().getBus();
        assertSame(bus, terminal);
    }

    @Test
    public void testDefaultValuesDanglingLine() {
        voltageLevel.newDanglingLine()
                .setId("danglingId")
                .setName("DanglingName")
                .setR(10.0)
                .setX(20.0)
                .setP0(30.0)
                .setQ0(40.0)
                .setPairingKey("code")
                .setBus(BUS_VL_ID)
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .add();

        DanglingLine danglingLine = network.getDanglingLine("danglingId");
        assertEquals(0.0, danglingLine.getG(), 0.0);
        assertEquals(0.0, danglingLine.getB(), 0.0);
    }

    @Test
    public void testInvalidR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createDanglingLine(INVALID, INVALID, Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void testInvalidX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createDanglingLine(INVALID, INVALID, 1.0, Double.NaN, 1.0, 1.0, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void testInvalidG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createDanglingLine(INVALID, INVALID, 1.0, 1.0, Double.NaN, 1.0, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("g is invalid"));
    }

    @Test
    public void testInvalidB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createDanglingLine(INVALID, INVALID, 1.0, 1.0, 1.0, Double.NaN, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("b is invalid"));

    }

    @Test
    public void testInvalidP0() {
        ValidationException e = assertThrows(ValidationException.class, () -> createDanglingLine(INVALID, INVALID, 1.0, 1.0, 1.0, 1.0, Double.NaN, 1.0, "code"));
        assertTrue(e.getMessage().contains("p0 is invalid"));
    }

    @Test
    public void testInvalidQ0() {
        ValidationException e = assertThrows(ValidationException.class, () -> createDanglingLine(INVALID, INVALID, 1.0, 1.0, 1.0, 1.0, 1.0, Double.NaN, "code"));
        assertTrue(e.getMessage().contains("q0 is invalid"));
    }

    @Test
    public void duplicateDanglingLine() {
        createDanglingLine(DUPLICATE, DUPLICATE, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
        assertNotNull(network.getDanglingLine(DUPLICATE));
        PowsyblException e = assertThrows(PowsyblException.class, () -> createDanglingLine(DUPLICATE, DUPLICATE, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code"));
    }

    @Test
    public void testRemove() {
        createDanglingLine(TO_REMOVE, TO_REMOVE, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
        DanglingLine danglingLine = network.getDanglingLine(TO_REMOVE);
        int count = network.getDanglingLineCount();
        assertNotNull(danglingLine);
        danglingLine.remove();
        assertEquals(count - 1L, network.getDanglingLineCount());
        assertNull(network.getDanglingLine(TO_REMOVE));
        assertNotNull(danglingLine);
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createDanglingLine(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 1.0, 1.1, 2.2, 1.0, 1.0, 1.2, "code");
        DanglingLine danglingLine = network.getDanglingLine(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(1.0, danglingLine.getP0(), 0.0);
        assertEquals(1.2, danglingLine.getQ0(), 0.0);
        // change values in s4
        danglingLine.setP0(3.0);
        danglingLine.setQ0(2.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(3.0, danglingLine.getP0(), 0.0);
        assertEquals(2.0, danglingLine.getQ0(), 0.0);
        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(1.0, danglingLine.getP0(), 0.0);
        assertEquals(1.2, danglingLine.getQ0(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            danglingLine.getQ0();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Test
    public void withRegulatingCapabilityTests() {
        double r = 10.0;
        double x = 20.0;
        double g = 0.0;
        double b = 0.0;
        double p0 = 0.0;
        double q0 = 0.0;
        String id = "danglingId";
        String name = "danlingName";
        String pairingKey = "code";
        DanglingLineAdder adder = voltageLevel.newDanglingLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setP0(p0)
                .setQ0(q0)
                .setPairingKey(pairingKey)
                .setBus(BUS_VL_ID)
                .setConnectableBus(BUS_VL_ID)
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .newGeneration()
                    .setTargetP(440)
                    .setMaxP(900)
                    .setMinP(0)
                    .setTargetV(400)
                    .setVoltageRegulationOn(true)
                .add();
        DanglingLine dl = adder.add();

        DanglingLine.Generation generation = dl.getGeneration();
        assertNotNull(generation);
        assertEquals(440, generation.getTargetP(), 0.0);
        assertEquals(900, generation.getMaxP(), 0.0);
        assertEquals(0, generation.getMinP(), 0.0);
        assertEquals(400, generation.getTargetV(), 0.0);
        assertTrue(generation.isVoltageRegulationOn());
        generation.newMinMaxReactiveLimits()
                .setMaxQ(500)
                .setMinQ(-500)
                .add();
        assertNotNull(generation.getReactiveLimits());
        assertInstanceOf(MinMaxReactiveLimits.class, generation.getReactiveLimits());

        // Test if new Generation is instantiate at each add
        DanglingLine dl2 = adder.setId(id + "_2").add();
        assertNotSame(dl.getGeneration(), dl2.getGeneration());
    }

    private void createDanglingLine(String id, String name, double r, double x, double g, double b,
                                    double p0, double q0, String ucteCode) {
        voltageLevel.newDanglingLine()
                        .setId(id)
                        .setName(name)
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setP0(p0)
                        .setQ0(q0)
                        .setPairingKey(ucteCode)
                        .setBus(BUS_VL_ID)
                        .setConnectableBus(BUS_VL_ID)
                        .setHasShuntAdmittanceLineEquivalentModel(true)
                        .add();
    }

}
