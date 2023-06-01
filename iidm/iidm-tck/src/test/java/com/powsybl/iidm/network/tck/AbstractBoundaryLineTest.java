/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractBoundaryLineTest {

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
        String ucteXnodeCode = "code";
        voltageLevel.newBoundaryLine()
                        .setId(id)
                        .setName(name)
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setP0(p0)
                        .setQ0(q0)
                        .setUcteXnodeCode(ucteXnodeCode)
                        .setBus(BUS_VL_ID)
                        .setConnectableBus(BUS_VL_ID)
                    .add();
        BoundaryLine boundaryLine = network.getBoundaryLine(id);
        // adder
        assertEquals(IdentifiableType.DANGLING_LINE, boundaryLine.getType());
        assertEquals(r, boundaryLine.getR(), 0.0);
        assertEquals(x, boundaryLine.getX(), 0.0);
        assertEquals(g, boundaryLine.getG(), 0.0);
        assertEquals(b, boundaryLine.getB(), 0.0);
        assertEquals(p0, boundaryLine.getP0(), 0.0);
        assertEquals(q0, boundaryLine.getQ0(), 0.0);
        assertEquals(id, boundaryLine.getId());
        assertEquals(name, boundaryLine.getOptionalName().orElse(null));
        assertEquals(name, boundaryLine.getNameOrId());
        assertEquals(ucteXnodeCode, boundaryLine.getUcteXnodeCode());
        assertNull(boundaryLine.getGeneration());

        // setter getter
        double r2 = 11.0;
        double x2 = 21.0;
        double g2 = 31.0;
        double b2 = 41.0;
        double p02 = 51.0;
        double q02 = 61.0;
        boundaryLine.setR(r2);
        assertEquals(r2, boundaryLine.getR(), 0.0);
        boundaryLine.setX(x2);
        assertEquals(x2, boundaryLine.getX(), 0.0);
        boundaryLine.setG(g2);
        assertEquals(g2, boundaryLine.getG(), 0.0);
        boundaryLine.setB(b2);
        assertEquals(b2, boundaryLine.getB(), 0.0);
        boundaryLine.setP0(p02);
        assertEquals(p02, boundaryLine.getP0(), 0.0);
        boundaryLine.setQ0(q02);
        assertEquals(q02, boundaryLine.getQ0(), 0.0);

        boundaryLine.newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();
        assertEquals(100.0, boundaryLine.getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        boundaryLine.newActivePowerLimits()
                .setPermanentLimit(60.0)
                .add();
        assertEquals(60.0, boundaryLine.getActivePowerLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        boundaryLine.newApparentPowerLimits()
                .setPermanentLimit(132.0)
                .add();
        assertEquals(132.0, boundaryLine.getApparentPowerLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        Bus bus = voltageLevel.getBusBreakerView().getBus(BUS_VL_ID);
        Bus terminal = boundaryLine.getTerminal().getBusBreakerView().getBus();
        assertSame(bus, terminal);
    }

    @Test
    public void testDefaultValuesBoundaryLine() {
        voltageLevel.newBoundaryLine()
                .setId("danglingId")
                .setName("DanglingName")
                .setR(10.0)
                .setX(20.0)
                .setP0(30.0)
                .setQ0(40.0)
                .setUcteXnodeCode("code")
                .setBus(BUS_VL_ID)
                .add();

        BoundaryLine boundaryLine = network.getBoundaryLine("danglingId");
        assertEquals(0.0, boundaryLine.getG(), 0.0);
        assertEquals(0.0, boundaryLine.getB(), 0.0);
    }

    @Test
    public void testInvalidR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBoundaryLine(INVALID, INVALID, Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void testInvalidX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBoundaryLine(INVALID, INVALID, 1.0, Double.NaN, 1.0, 1.0, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void testInvalidG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBoundaryLine(INVALID, INVALID, 1.0, 1.0, Double.NaN, 1.0, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("g is invalid"));
    }

    @Test
    public void testInvalidB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createBoundaryLine(INVALID, INVALID, 1.0, 1.0, 1.0, Double.NaN, 1.0, 1.0, "code"));
        assertTrue(e.getMessage().contains("b is invalid"));

    }

    @Test
    public void duplicateBoundaryLine() {
        createBoundaryLine(DUPLICATE, DUPLICATE, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
        assertNotNull(network.getBoundaryLine(DUPLICATE));
        PowsyblException e = assertThrows(PowsyblException.class, () -> createBoundaryLine(DUPLICATE, DUPLICATE, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code"));
    }

    @Test
    public void testRemove() {
        createBoundaryLine(TO_REMOVE, TO_REMOVE, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
        BoundaryLine boundaryLine = network.getBoundaryLine(TO_REMOVE);
        int count = network.getBoundaryLineCount();
        assertNotNull(boundaryLine);
        boundaryLine.remove();
        assertEquals(count - 1L, network.getBoundaryLineCount());
        assertNull(network.getBoundaryLine(TO_REMOVE));
        assertNotNull(boundaryLine);
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createBoundaryLine(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 1.0, 1.1, 2.2, 1.0, 1.0, 1.2, "code");
        BoundaryLine boundaryLine = network.getBoundaryLine(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(1.0, boundaryLine.getP0(), 0.0);
        assertEquals(1.2, boundaryLine.getQ0(), 0.0);
        // change values in s4
        boundaryLine.setP0(3.0);
        boundaryLine.setQ0(2.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(3.0, boundaryLine.getP0(), 0.0);
        assertEquals(2.0, boundaryLine.getQ0(), 0.0);
        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(1.0, boundaryLine.getP0(), 0.0);
        assertEquals(1.2, boundaryLine.getQ0(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            boundaryLine.getQ0();
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
        String ucteXnodeCode = "code";
        BoundaryLineAdder adder = voltageLevel.newBoundaryLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setP0(p0)
                .setQ0(q0)
                .setUcteXnodeCode(ucteXnodeCode)
                .setBus(BUS_VL_ID)
                .setConnectableBus(BUS_VL_ID)
                .newGeneration()
                    .setTargetP(440)
                    .setMaxP(900)
                    .setMinP(0)
                    .setTargetV(400)
                    .setVoltageRegulationOn(true)
                .add();
        BoundaryLine bl = adder.add();

        BoundaryLine.Generation generation = bl.getGeneration();
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
        assertTrue(generation.getReactiveLimits() instanceof MinMaxReactiveLimits);

        // Test if new Generation is instantiate at each add
        BoundaryLine bl2 = adder.setId(id + "_2").add();
        assertNotSame(bl.getGeneration(), bl2.getGeneration());
    }

    private void createBoundaryLine(String id, String name, double r, double x, double g, double b,
                                    double p0, double q0, String ucteCode) {
        voltageLevel.newBoundaryLine()
                        .setId(id)
                        .setName(name)
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setP0(p0)
                        .setQ0(q0)
                        .setUcteXnodeCode(ucteCode)
                        .setBus(BUS_VL_ID)
                        .setConnectableBus(BUS_VL_ID)
                    .add();
    }

}
