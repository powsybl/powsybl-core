/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.SecurityAnalysisTestNetworkFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractOverloadManagementSystemTest {
    @Test
    public void baseTests() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        OverloadManagementSystem oms1 = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setName("1st OMS")
                .setEnabled(true)
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newSwitchTripping()
                    .setKey("SwTrip")
                    .setName("Tripping on switch")
                    .setCurrentLimit(80.)
                    .setOpenAction(true)
                    .setSwitchToOperateId("S1VL2_LINES1S2V1_1_BREAKER")
                    .add()
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setCurrentLimit(50)
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .add()
                .add();
        OverloadManagementSystem oms2 = substation.newOverloadManagementSystem()
                .setId("OMS2")
                .setEnabled(false)
                .setMonitoredElementId("LINE_S1S2V1_2")
                .setMonitoredElementSide(ThreeSides.TWO)
                .newBranchTripping()
                    .setKey("2wtTrip")
                    .setName("Tripping on 2 windings transformer")
                    .setCurrentLimit(100.)
                    .setOpenAction(true)
                    .setBranchToOperateId("TWT")
                    .setSideToOperate(TwoSides.TWO)
                    .add()
                .add();

        assertNotNull(oms1);
        assertNotNull(oms2);

        assertEquals(2, substation.getOverloadManagementSystemCount());

        Iterable<OverloadManagementSystem> iterable = substation.getOverloadManagementSystems();
        List<OverloadManagementSystem> retreived = StreamSupport.stream(iterable.spliterator(), false).toList();
        assertEquals(2, retreived.size());
        assertTrue(retreived.containsAll(ImmutableSet.of(oms1, oms2)));

        retreived = substation.getOverloadManagementSystemStream().toList();
        assertEquals(2, retreived.size());
        assertTrue(retreived.containsAll(ImmutableSet.of(oms1, oms2)));

        assertEquals("OMS1", oms1.getId());
        assertEquals("OMS2", oms2.getId());
        assertEquals("1st OMS", oms1.getNameOrId());
        assertEquals("OMS2", oms2.getNameOrId());
        assertTrue(oms1.isEnabled());
        assertFalse(oms2.isEnabled());
        assertEquals("LINE_S1S2V1_1", oms1.getMonitoredElementId());
        assertEquals("LINE_S1S2V1_2", oms2.getMonitoredElementId());
        assertEquals(ThreeSides.ONE, oms1.getMonitoredSide());
        assertEquals(ThreeSides.TWO, oms2.getMonitoredSide());

        List<OverloadManagementSystem.Tripping> trippings = oms1.getTrippings();
        assertEquals(2, trippings.size());

        assertEquals(OverloadManagementSystem.Tripping.Type.SWITCH_TRIPPING, trippings.get(0).getType());
        OverloadManagementSystem.SwitchTripping swTripping = (OverloadManagementSystem.SwitchTripping) trippings.get(0);
        assertEquals("SwTrip", swTripping.getKey());
        assertEquals("Tripping on switch", swTripping.getNameOrKey());
        assertEquals(80., swTripping.getCurrentLimit());
        assertTrue(swTripping.isOpenAction());
        assertEquals("S1VL2_LINES1S2V1_1_BREAKER", swTripping.getSwitchToOperateId());

        assertEquals(OverloadManagementSystem.Tripping.Type.BRANCH_TRIPPING, trippings.get(1).getType());
        OverloadManagementSystem.BranchTripping brTripping = (OverloadManagementSystem.BranchTripping) trippings.get(1);
        assertEquals("LineTrip", brTripping.getKey());
        assertEquals("LineTrip", brTripping.getNameOrKey());
        assertEquals(50., brTripping.getCurrentLimit());
        assertFalse(brTripping.isOpenAction());
        assertEquals("LINE_S1S2V1_2", brTripping.getBranchToOperateId());
        assertEquals(TwoSides.ONE, brTripping.getSideToOperate());

        trippings = oms2.getTrippings();
        assertEquals(1, trippings.size());
        assertEquals(OverloadManagementSystem.Tripping.Type.BRANCH_TRIPPING, trippings.get(0).getType());
        brTripping = (OverloadManagementSystem.BranchTripping) trippings.get(0);
        assertEquals("2wtTrip", brTripping.getKey());
        assertEquals("Tripping on 2 windings transformer", brTripping.getNameOrKey());
        assertEquals(100., brTripping.getCurrentLimit());
        assertTrue(brTripping.isOpenAction());
        assertEquals("TWT", brTripping.getBranchToOperateId());
        assertEquals(TwoSides.TWO, brTripping.getSideToOperate());
    }

    @Test
    public void threeWindingsTransformerTrippingTest() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        Substation substation = network.getSubstation("SUBSTATION");
        OverloadManagementSystem oms = substation.newOverloadManagementSystem()
                .setId("OMS")
                .setName("An OMS")
                .setEnabled(true)
                .setMonitoredElementId("3WT")
                .setMonitoredElementSide(ThreeSides.TWO)
                .newThreeWindingsTransformerTripping()
                    .setKey("3wtTrip")
                    .setName("Tripping on 3 windings transformer")
                    .setCurrentLimit(60.)
                    .setOpenAction(true)
                    .setThreeWindingsTransformerToOperateId("3WT")
                    .setSideToOperate(ThreeSides.THREE)
                    .add()
                .add();
        assertNotNull(oms);

        List<OverloadManagementSystem.Tripping> trippings = oms.getTrippings();
        assertEquals(1, trippings.size());
        assertEquals(OverloadManagementSystem.Tripping.Type.THREE_WINDINGS_TRANSFORMER_TRIPPING, trippings.get(0).getType());
        OverloadManagementSystem.ThreeWindingsTransformerTripping twtTripping =
                (OverloadManagementSystem.ThreeWindingsTransformerTripping) trippings.get(0);
        assertEquals("3wtTrip", twtTripping.getKey());
        assertEquals("Tripping on 3 windings transformer", twtTripping.getNameOrKey());
        assertEquals(60., twtTripping.getCurrentLimit());
        assertTrue(twtTripping.isOpenAction());
        assertEquals("3WT", twtTripping.getThreeWindingsTransformerToOperateId());
        assertEquals(ThreeSides.THREE, twtTripping.getSideToOperate());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        OverloadManagementSystem oms1 = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setEnabled(true)
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setCurrentLimit(50)
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .add()
                .add();
        VariantManager variantManager = network.getVariantManager();
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check value cloned by extend
        assertTrue(oms1.isEnabled());
        // change value in s4
        oms1.setEnabled(false);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check value cloned by allocate
        assertFalse(oms1.isEnabled());

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertTrue(oms1.isEnabled());

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertThrows(Exception.class, oms1::isEnabled);
    }

    @Test
    public void invalidCurrentLimitsTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        OverloadManagementSystemAdder omsAdder = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE);
        OverloadManagementSystemAdder.BranchTrippingAdder trippingAdder = omsAdder
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(-50);
        trippingAdder.add();
        assertThrows(ValidationException.class, omsAdder::add);

        trippingAdder.setCurrentLimit(Double.NaN);
        assertThrows(ValidationException.class, omsAdder::add);
    }

    @Test
    public void duplicateTrippingKeysOnSameOmsTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        String duplicateKey = "duplicate";
        OverloadManagementSystemAdder omsAdder = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newSwitchTripping()
                    .setKey(duplicateKey)
                    .setCurrentLimit(80.)
                    .setOpenAction(true)
                    .setSwitchToOperateId("S1VL2_LINES1S2V1_1_BREAKER")
                    .add()
                .newBranchTripping()
                    .setKey(duplicateKey)
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(-50)
                    .add();
        // duplicate tripping keys are NOT allowed if they are on the same OverloadManagementSystem
        assertThrows(ValidationException.class, omsAdder::add);
    }

    @Test
    public void duplicateTrippingKeysOnDifferentOmsTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        String duplicateKey = "duplicate";
        substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newSwitchTripping()
                    .setKey(duplicateKey)
                    .setCurrentLimit(80.)
                    .setOpenAction(true)
                    .setSwitchToOperateId("S1VL2_LINES1S2V1_1_BREAKER")
                    .add()
                .add();
        OverloadManagementSystemAdder omsAdder = substation.newOverloadManagementSystem()
                .setId("OMS2")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.TWO)
                .newBranchTripping()
                    .setKey(duplicateKey)
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(50)
                    .add();
        // duplicate tripping keys are allowed if they are on distinct OverloadManagementSystems
        assertDoesNotThrow(omsAdder::add);
    }

    @Test
    public void unknownMonitoredElementTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        OverloadManagementSystemAdder omsAdder = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(50)
                    .add();
        ValidationException ex = assertThrows(ValidationException.class, omsAdder::add);
        assertTrue(ex.getMessage().contains("monitoredElementId is not set"));

        omsAdder.setMonitoredElementId("UNKNOWN");
        ex = assertThrows(ValidationException.class, omsAdder::add);
        assertTrue(ex.getMessage().contains("'UNKNOWN' not found"));
    }

    @Test
    public void unknownTrippingElementTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        OverloadManagementSystemAdder omsAdder = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setOpenAction(false)
                    .setBranchToOperateId("UNKNOWN")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(50)
                    .add();
        ValidationException ex = assertThrows(ValidationException.class, omsAdder::add);
        assertTrue(ex.getMessage().contains("'UNKNOWN' not found"));
    }

    @Test
    public void removalTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substation = network.getSubstation("S1");
        OverloadManagementSystem overloadManagementSystem = substation.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(50)
                    .add()
                .add();
        assertEquals(1, network.getOverloadManagementSystemCount());
        assertEquals(1, substation.getOverloadManagementSystemCount());
        assertNotNull(network.getOverloadManagementSystem("OMS1"));

        assertEquals(network, overloadManagementSystem.getNetwork());
        assertEquals(network, overloadManagementSystem.getParentNetwork());
        assertEquals(substation, overloadManagementSystem.getSubstation());

        overloadManagementSystem.remove();

        assertEquals(0, network.getOverloadManagementSystemCount());
        assertEquals(0, substation.getOverloadManagementSystemCount());
        assertNull(network.getOverloadManagementSystem("OMS1"));

        assertThrows(PowsyblException.class, overloadManagementSystem::getNetwork);
        assertThrows(PowsyblException.class, overloadManagementSystem::getParentNetwork);
        assertThrows(PowsyblException.class, overloadManagementSystem::getSubstation);
    }

    @Test
    public void substationRemovalTest() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Substation substationS3 = network.newSubstation().setId("S3").add();
        substationS3.newOverloadManagementSystem()
                .setId("OMS1")
                .setMonitoredElementId("LINE_S1S2V1_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("LineTrip")
                    .setOpenAction(false)
                    .setBranchToOperateId("LINE_S1S2V1_2")
                    .setSideToOperate(TwoSides.ONE)
                    .setCurrentLimit(50)
                    .add()
                .add();
        assertEquals(1, network.getOverloadManagementSystemCount());
        substationS3.remove();
        assertEquals(0, network.getOverloadManagementSystemCount());
    }
}
