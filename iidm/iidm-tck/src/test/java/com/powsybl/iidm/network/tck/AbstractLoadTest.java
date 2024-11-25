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

public abstract class AbstractLoadTest {

    private static final String TO_REMOVE = "toRemove";

    Network network;
    VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = FictitiousSwitchFactory.create();
        voltageLevel = network.getVoltageLevel("C");
    }

    @Test
    public void testSetterGetter() {
        Load load = network.getLoad("CE");
        load.setP0(-1.0);
        assertEquals(-1.0, load.getP0(), 0.0);
        load.setQ0(-2.0);
        assertEquals(-2.0, load.getQ0(), 0.0);
        load.setP0(1.0);
        assertEquals(1.0, load.getP0(), 0.0);
        load.setQ0(0.0);
        assertEquals(0.0, load.getQ0(), 0.0);
        load.setLoadType(LoadType.AUXILIARY);
        assertEquals(LoadType.AUXILIARY, load.getLoadType());
    }

    @Test
    public void invalidP0() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLoad("invalid", Double.NaN, 1.0));
        assertTrue(e.getMessage().contains("p0 is invalid"));
    }

    @Test
    public void invalidQ0() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLoad("invalid", 20.0, Double.NaN));
        assertTrue(e.getMessage().contains("q0 is invalid"));
    }

    @Test
    public void testChangesNotification() {
        // Changes listener
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);

        // Tested instance
        Load load = network.getLoad("CE");
        // Get initial values
        double p0OldValue = load.getP0();
        double q0OldValue = load.getQ0();
        // Change values P0 & Q0
        load.setP0(-1.0);
        load.setQ0(-2.0);

        // Check update notification
        Mockito.verify(mockedListener, Mockito.times(1))
               .onUpdate(load, "p0", VariantManagerConstants.INITIAL_VARIANT_ID, p0OldValue, -1.0);
        Mockito.verify(mockedListener, Mockito.times(1))
               .onUpdate(load, "q0", VariantManagerConstants.INITIAL_VARIANT_ID, q0OldValue, -2.0);

        // At this point
        // no more changes is taking into account

        // Simulate exception for onUpdate calls
        Mockito.doThrow(new PowsyblException()).when(mockedListener)
               .onUpdate(load, "p0", VariantManagerConstants.INITIAL_VARIANT_ID, p0OldValue, -1.0);

        // Case when same values P0 & Q0 are set
        load.setP0(-1.0);
        load.setQ0(-2.0);
        // Case when no listener is registered
        network.removeListener(mockedListener);
        load.setP0(1.0);
        load.setQ0(0.0);

        // Check no notification
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void duplicateEquipment() {
        voltageLevel.newLoad()
                        .setId("duplicate")
                        .setP0(2.0)
                        .setQ0(1.0)
                        .setNode(1)
                    .add();
        PowsyblException e = assertThrows(PowsyblException.class, () -> createLoad("duplicate", 2.0, 1.0));
        assertTrue(e.getMessage().contains("with the id 'duplicate'"));
    }

    @Test
    public void duplicateId() {
        // "C" id of voltageLevel
        PowsyblException e = assertThrows(PowsyblException.class, () -> createLoad("C", 2.0, 1.0));
        assertTrue(e.getMessage().contains("with the id 'C'"));
    }

    @Test
    public void testAdder() {
        Load load = voltageLevel.newLoad()
                        .setId("testAdder")
                        .setP0(2.0)
                        .setQ0(1.0)
                        .setLoadType(LoadType.AUXILIARY)
                        .setNode(1)
                    .add();
        assertEquals(2.0, load.getP0(), 0.0);
        assertEquals(1.0, load.getQ0(), 0.0);
        assertEquals("testAdder", load.getId());
        assertEquals(LoadType.AUXILIARY, load.getLoadType());
    }

    @Test
    public void testRemove() {
        createLoad(TO_REMOVE, 2.0, 1.0);
        Load load = network.getLoad(TO_REMOVE);
        int loadCount = network.getLoadCount();
        assertNotNull(load);
        load.remove();
        assertNotNull(load);
        assertNull(network.getLoad(TO_REMOVE));
        assertEquals(loadCount - 1L, network.getLoadCount());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        // Changes listener
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        // Set observer changes
        network.addListener(mockedListener);

        // Init variant manager
        VariantManager variantManager = network.getVariantManager();
        createLoad("testMultiVariant", 0.6d, 0.7d);
        Load load = network.getLoad("testMultiVariant");
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(0.6d, load.getP0(), 0.0);
        assertEquals(0.7d, load.getQ0(), 0.0);
        // change values in s4
        load.setP0(3.0);
        load.setQ0(2.0);
        // Check P0 & Q0 update notification
        Mockito.verify(mockedListener, Mockito.times(1)).onUpdate(load, "p0", "s4", 0.6d, 3.0);
        Mockito.verify(mockedListener, Mockito.times(1)).onUpdate(load, "q0", "s4", 0.7d, 2.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(3.0, load.getP0(), 0.0);
        assertEquals(2.0, load.getQ0(), 0.0);
        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(0.6, load.getP0(), 0.0);
        assertEquals(0.7, load.getQ0(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            load.getQ0();
            fail();
        } catch (Exception ignored) {
            // ignore
        }

        // Remove observer changes
        network.removeListener(mockedListener);
    }

    @Test
    public void move() {
        Load loadNbv = network.getLoad("CF");

        // Moving to existing node (with no equipment)
        loadNbv.getTerminal().getNodeBreakerView().moveConnectable(3, voltageLevel.getId());
        assertEquals(3, loadNbv.getTerminal().getNodeBreakerView().getNode());

        // Moving to existing node (with equipment)
        Terminal.NodeBreakerView nbv0 = loadNbv.getTerminal().getNodeBreakerView();
        try {
            nbv0.moveConnectable(4, "C");
            fail();
        } catch (ValidationException e) {
            assertEquals("Load 'CF': an equipment (CJ) is already connected to node 4 of voltage level C", e.getMessage());
        }

        // Moving to non existing node: node created
        loadNbv.getTerminal().getNodeBreakerView().moveConnectable(6, voltageLevel.getId());
        assertEquals(6, loadNbv.getTerminal().getNodeBreakerView().getNode());

        // Moving to non-connected bus in bus breaker view
        VoltageLevel vlBbv = network.newVoltageLevel()
                .setId("vlBbv")
                .setNominalV(45)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bbvBus0 = vlBbv.getBusBreakerView().newBus()
                .setId("bbvBus0")
                .add();
        loadNbv.getTerminal().getBusBreakerView().moveConnectable(bbvBus0.getId(), false);
        assertNull(loadNbv.getTerminal().getBusBreakerView().getBus());
        assertEquals(bbvBus0, loadNbv.getTerminal().getBusBreakerView().getConnectableBus());

        // Moving to connected bus in bus breaker view
        Bus bbvBus1 = vlBbv.getBusBreakerView().newBus()
                .setId("bbvBus1")
                .add();
        loadNbv.getTerminal().getBusBreakerView().moveConnectable(bbvBus1.getId(), true);
        assertEquals(bbvBus1, loadNbv.getTerminal().getBusBreakerView().getBus());
        assertEquals(bbvBus1, loadNbv.getTerminal().getBusBreakerView().getConnectableBus());

        // Moving to unknown bus in bus breaker view
        Terminal.BusBreakerView bbv0 = loadNbv.getTerminal().getBusBreakerView();
        try {
            bbv0.moveConnectable("unknownBus", true);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Bus 'unknownBus' not found", e.getMessage());
        }

        // Moving load in bus breaker view to connected bus in bus breaker view
        Load loadBbv = vlBbv.newLoad()
                .setId("loadBbv")
                .setP0(2.0)
                .setQ0(1.0)
                .setBus(bbvBus1.getId())
                .add();
        loadBbv.getTerminal().getBusBreakerView().moveConnectable(bbvBus1.getId(), true);
        assertEquals(bbvBus1, loadBbv.getTerminal().getBusBreakerView().getBus());
        assertEquals(bbvBus1, loadBbv.getTerminal().getBusBreakerView().getConnectableBus());

        // Moving to (now existing & available) node
        loadBbv.getTerminal().getNodeBreakerView().moveConnectable(6, voltageLevel.getId());
        assertEquals(6, loadBbv.getTerminal().getNodeBreakerView().getNode());

        // Moving to unknown voltage level
        Terminal.NodeBreakerView nbv = loadBbv.getTerminal().getNodeBreakerView();
        try {
            nbv.moveConnectable(6, "unknownVl");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Voltage level 'unknownVl' not found", e.getMessage());
        }
    }

    private void createLoad(String id, double p0, double q0) {
        voltageLevel.newLoad()
                        .setId(id)
                        .setP0(p0)
                        .setQ0(q0)
                        .setNode(1)
                    .add();
    }

    @Test
    public void removePropertyTest() {
        Load load = network.getLoad("CE");
        assertNotNull(load);
        assertFalse(load.hasProperty("a"));
        assertNull(load.getProperty("a"));
        load.setProperty("a", "b");
        assertTrue(load.hasProperty("a"));
        assertNotNull(load.getProperty("a"));
        assertEquals("b", load.getProperty("a"));
        load.removeProperty("a");
        assertFalse(load.hasProperty("a"));
        assertNull(load.getProperty("a"));
    }

    @Test
    public void setNameTest() {
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        network.addListener(mockedListener);
        Load load = network.getLoad("CE");
        assertNotNull(load);
        assertTrue(load.getOptionalName().isEmpty());
        load.setName("FOO");
        assertEquals("FOO", load.getOptionalName().orElseThrow());
        Mockito.verify(mockedListener, Mockito.times(1)).onUpdate(load, "name", null, null, "FOO");
    }

    @Test
    public void testZipLoadModel() {
        Load load = voltageLevel.newLoad()
                .setId("newZipLoad")
                .setP0(2.0)
                .setQ0(1.0)
                .setNode(1)
                .newZipModel()
                    .setC0p(0.3)
                    .setC1p(0.5)
                    .setC2p(0.2)
                    .setC0q(0.1)
                    .setC1q(0.2)
                    .setC2q(0.7)
                    .add()
                .add();
        assertEquals(2.0, load.getP0(), 0.0);
        assertEquals(1.0, load.getQ0(), 0.0);
        ZipLoadModel loadModel = (ZipLoadModel) load.getModel().orElseThrow();
        assertEquals(0.3, loadModel.getC0p(), 0);
        assertEquals(0.5, loadModel.getC1p(), 0);
        assertEquals(0.2, loadModel.getC2p(), 0);
        assertEquals(0.1, loadModel.getC0q(), 0);
        assertEquals(0.2, loadModel.getC1q(), 0);
        assertEquals(0.7, loadModel.getC2q(), 0);
        loadModel.setC0p(0.31);
        loadModel.setC1p(0.51);
        loadModel.setC2p(0.21);
        loadModel.setC0q(0.11);
        loadModel.setC1q(0.21);
        loadModel.setC2q(0.71);
        assertEquals(0.31, loadModel.getC0p(), 0);
        assertEquals(0.51, loadModel.getC1p(), 0);
        assertEquals(0.21, loadModel.getC2p(), 0);
        assertEquals(0.11, loadModel.getC0q(), 0);
        assertEquals(0.21, loadModel.getC1q(), 0);
        assertEquals(0.71, loadModel.getC2q(), 0);
        ValidationException e = assertThrows(ValidationException.class, () -> loadModel.setC0p(Double.NaN));
        assertEquals("Load 'newZipLoad': Invalid zip load model coefficient: NaN", e.getMessage());
        loadModel.setC0p(-0.3);
        assertEquals(-0.3, loadModel.getC0p(), 0);
    }

    @Test
    public void testExponentialLoadModel() {
        Load load = voltageLevel.newLoad()
                .setId("newZipLoad")
                .setP0(2.0)
                .setQ0(1.0)
                .setNode(1)
                .newExponentialModel()
                    .setNp(0.6)
                    .setNq(0.5)
                    .add()
                .add();
        assertEquals(2.0, load.getP0(), 0.0);
        assertEquals(1.0, load.getQ0(), 0.0);
        ExponentialLoadModel loadModel = (ExponentialLoadModel) load.getModel().orElseThrow();
        assertEquals(0.6, loadModel.getNp(), 0);
        assertEquals(0.5, loadModel.getNq(), 0);
        loadModel.setNp(0.61);
        loadModel.setNq(0.51);
        assertEquals(0.61, loadModel.getNp(), 0);
        assertEquals(0.51, loadModel.getNq(), 0);
        ValidationException e = assertThrows(ValidationException.class, () -> loadModel.setNp(-2));
        assertEquals("Load 'newZipLoad': Invalid load model exponential value: -2.0", e.getMessage());
    }
}
