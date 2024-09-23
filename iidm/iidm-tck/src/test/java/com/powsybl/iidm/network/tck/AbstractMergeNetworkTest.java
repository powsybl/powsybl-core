/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.apache.commons.lang3.mutable.MutableBoolean;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractMergeNetworkTest {

    private static final String MERGE = "merge";
    private static final String MERGE_DEFAULT_ID = "n1+n2";
    public static final String N1 = "n1";
    public static final String N2 = "n2";
    public static final String PROPERTY_NAME_1 = "property_name1";
    public static final String PROPERTY_NAME_2 = "property_name2";
    public static final String PROPERTY_NAME_3 = "property_name3";

    Network n0;
    Network n1;
    Network n2;

    @BeforeEach
    public void setup() {
        n0 = Network.create("a", "asdf");
        n1 = Network.create(N1, "asdf");
        n2 = Network.create(N2, "qwer");
    }

    @Test
    public void failMergeIfMultiVariants() {
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "Totest");
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(n2, n1));
        assertTrue(e.getMessage().contains("Merging of multi-variants network is not supported"));
    }

    @Test
    public void failMergeWithSameObj() {
        addSubstation(n1, "P1");
        addSubstation(n2, "P1");
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(n1, n2));
        assertEquals("The following object(s) of type SubstationImpl exist(s) in both networks: [P1]", e.getMessage());
    }

    @Test
    public void testMerge() {
        addCommonSubstationsAndVoltageLevels();
        Network n0 = Network.create("n0", "rid");
        addSubstationAndVoltageLevel(n0, "s0", Country.FR, "vl0", "b0");
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        Network merge = Network.merge(n0, n1, n2);
        assertEquals(3, merge.getSubnetworks().size());
        assertEquals(1, merge.getSubnetwork(n0.getId()).getVoltageLevelCount());
        assertEquals(1, merge.getSubnetwork(N1).getVoltageLevelCount());
        assertEquals(1, merge.getSubnetwork(N2).getVoltageLevelCount());

        TieLine tieLine = merge.getTieLine("dl1 + dl2");
        assertNotNull(tieLine);
        assertEquals("dl1_name + dl2_name", tieLine.getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", tieLine.getNameOrId());

        Network subnetwork1 = merge.getSubnetwork(N1);
        Network subnetwork2 = merge.getSubnetwork(N2);
        checkDanglingLineStatusCount(merge, 0, 2);
        checkDanglingLineStatusCount(subnetwork1, 0, 1);
        checkDanglingLineStatusCount(subnetwork2, 0, 1);

        assertEquals(merge, tieLine.getParentNetwork());
        assertEquals(subnetwork1, merge.getDanglingLine("dl1").getParentNetwork());
        assertEquals(subnetwork2, merge.getDanglingLine("dl2").getParentNetwork());
    }

    @Test
    public void testMergeAndDetach() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        // merge(n1, n2)
        Network merge = Network.merge(MERGE, n1, n2);
        TieLine tieLine = merge.getTieLine("dl1 + dl2");
        assertNotNull(tieLine);
        assertEquals("dl1_name + dl2_name", tieLine.getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", tieLine.getNameOrId());
        assertEquals("dl1", tieLine.getDanglingLine1().getId());
        assertEquals("dl2", tieLine.getDanglingLine2().getId());
        assertEquals(0.0, tieLine.getDanglingLine1().getP0());
        assertEquals(0.0, tieLine.getDanglingLine1().getQ0());
        assertEquals(0.0, tieLine.getDanglingLine2().getP0());
        assertEquals(0.0, tieLine.getDanglingLine2().getQ0());

        Network subnetwork1 = merge.getSubnetwork(N1);
        Network subnetwork2 = merge.getSubnetwork(N2);
        checkDanglingLineStatusCount(merge, 0, 2);
        checkDanglingLineStatusCount(subnetwork1, 0, 1);
        checkDanglingLineStatusCount(subnetwork2, 0, 1);
        checkSubstationAndVoltageLevelCounts(merge, 2, 2);

        assertEquals(merge, tieLine.getParentNetwork());
        assertEquals(subnetwork1, merge.getDanglingLine("dl1").getParentNetwork());
        assertEquals(subnetwork2, merge.getDanglingLine("dl2").getParentNetwork());

        // detach(n1)
        assertTrue(subnetwork1.isDetachable());
        Network detachedN1 = subnetwork1.detach();
        checkDanglingLineStatusCount(merge, 1, 0);
        checkDanglingLineStatusCount(detachedN1, 1, 0);
        checkDanglingLineStatusCount(subnetwork2, 1, 0);
        checkSubstationAndVoltageLevelCounts(merge, 1, 1);
        checkSubstationAndVoltageLevelCounts(detachedN1, 1, 1);
        DanglingLine dl1 = detachedN1.getDanglingLine("dl1");
        DanglingLine dl2 = merge.getDanglingLine("dl2");
        // - P0 and Q0 of the removed tie line's underlying dangling lines were updated:
        assertEquals(-1724.437, dl1.getP0(), 0.001);
        assertEquals(1605.281, dl1.getQ0(), 0.001);
        assertEquals(-1724.437, dl2.getP0(), 0.001);
        assertEquals(1605.281, dl2.getQ0(), 0.001);

        // detach(n2)
        assertTrue(subnetwork2.isDetachable());
        Network detachedN2 = subnetwork2.detach();
        checkDanglingLineStatusCount(merge, 0, 0);
        checkDanglingLineStatusCount(detachedN1, 1, 0);
        checkDanglingLineStatusCount(detachedN2, 1, 0);
        checkSubstationAndVoltageLevelCounts(merge, 0, 0);
        checkSubstationAndVoltageLevelCounts(detachedN1, 1, 1);
        checkSubstationAndVoltageLevelCounts(detachedN2, 1, 1);
    }

    private void checkSubstationAndVoltageLevelCounts(Network n, long substationCount, long voltageLevelCount) {
        assertEquals(substationCount, n.getSubstationCount());
        assertEquals(voltageLevelCount, n.getVoltageLevelCount());
    }

    @Test
    public void testMergeAndFlatten() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        // merge(n1, n2)
        Network network = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = network.getSubnetwork(N1);
        Network subnetwork2 = network.getSubnetwork(N2);

        // flatten
        network.flatten();
        checkDanglingLineStatusCount(network, 0, 2);
        checkSubstationAndVoltageLevelCounts(network, 2, 2);
        checkSubstationAndVoltageLevelsNetworks(network, network, network);
        assertTrue(network.getSubnetworks().isEmpty());
        checkDanglingLineStatusCount(subnetwork1, 0, 0);
        checkDanglingLineStatusCount(subnetwork2, 0, 0);
        checkSubstationAndVoltageLevelCounts(subnetwork1, 0, 0);
        checkSubstationAndVoltageLevelCounts(subnetwork2, 0, 0);

        // Use the flatten network in another merge
        Network network2 = Network.merge(network, Network.create("n3", "manual"));
        Network mergedAsSubnetwork = network2.getSubnetwork("merge");
        checkSubstationAndVoltageLevelCounts(network2, 2, 2);
        checkSubstationAndVoltageLevelsNetworks(network2, network2, mergedAsSubnetwork);

        // And even if detached, everything is alright
        Network detachedN2 = mergedAsSubnetwork.detach();
        checkSubstationAndVoltageLevelCounts(detachedN2, 2, 2);
        checkSubstationAndVoltageLevelsNetworks(detachedN2, detachedN2, detachedN2);
    }

    private void checkSubstationAndVoltageLevelsNetworks(Network network, Network expectedNetwork, Network expectedParentNetwork) {
        Stream.concat(network.getSubstationStream().map(Identifiable.class::cast),
                        network.getVoltageLevelStream().map(Identifiable.class::cast))
                .forEach(substation -> {
                    assertEquals(expectedNetwork, substation.getNetwork());
                    assertEquals(expectedParentNetwork, substation.getParentNetwork());
                });
    }

    @Test
    public void testMergeAndDetachWithProperties() {
        // Add properties to the networks
        n1.setProperty(PROPERTY_NAME_1, "property_value1");
        n2.setProperty(PROPERTY_NAME_2, "property_value2");

        // Merge the networks and check that the properties have been transferred to subnetworks
        Network merged = Network.merge(n1, n2);
        assertFalse(merged.hasProperty());
        assertEquals("property_value1", merged.getSubnetwork(N1).getProperty(PROPERTY_NAME_1));
        assertEquals("property_value2", merged.getSubnetwork(N2).getProperty(PROPERTY_NAME_2));

        // Detach the subnetworks and check that the properties have been transferred to the detached networks
        Network detached1 = merged.getSubnetwork(N1).detach();
        Network detached2 = merged.getSubnetwork(N2).detach();
        assertEquals("property_value1", detached1.getProperty(PROPERTY_NAME_1));
        assertEquals("property_value2", detached2.getProperty(PROPERTY_NAME_2));
    }

    @Test
    public void testMergeAndFlattenWithProperties() {
        n1.setProperty(PROPERTY_NAME_1, "n1-val1");
        n1.setProperty(PROPERTY_NAME_2, "n1-val2");
        n2.setProperty(PROPERTY_NAME_1, "n2-val1");
        n2.setProperty(PROPERTY_NAME_3, "n2-val3");
        // merge(n1, n2)
        Network network = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = network.getSubnetwork(N1);
        Network subnetwork2 = network.getSubnetwork(N2);
        network.setProperty("property_name_4", "val");

        // flatten
        network.flatten();
        // The 1st property is present in both subnetworks.
        // The value of the 1st one (in the merging order) is set in the flatten network and
        // the property is kept in subnetwork2. This way, it is possible to detect duplicates and
        // to handle them manually if wanted.
        assertEquals("n1-val1", network.getProperty(PROPERTY_NAME_1));
        assertEquals("n1-val2", network.getProperty(PROPERTY_NAME_2));
        assertEquals("n2-val3", network.getProperty(PROPERTY_NAME_3));
        assertEquals("val", network.getProperty("property_name_4"));
        assertTrue(subnetwork1.getPropertyNames().isEmpty());
        assertEquals(1, subnetwork2.getPropertyNames().size());
        assertEquals(PROPERTY_NAME_1, subnetwork2.getPropertyNames().stream().findFirst().orElseThrow());
    }

    @Test
    public void testMergeAndDetachWithExtensions() {
        n1 = EurostagTutorialExample1Factory.createWithMoreGenerators();
        addSubstationAndVoltageLevel(n2, "s2", Country.BE, "vl2", "b2");
        addDanglingLines(n1, "VLGEN", "dl1", "code", "NGEN");
        addDanglingLines(n2, "vl2", "dl2", "code", "b2");

        // Add extension at network level
        n1.newExtension(SecondaryVoltageControlAdder.class)
                .newControlZone()
                    .withName("z1")
                    .newPilotPoint()
                        .withBusbarSectionsOrBusesIds(List.of("NLOAD"))
                        .withTargetV(15d)
                    .add()
                    .newControlUnit()
                        .withId("GEN")
                        .withParticipate(false)
                    .add()
                    .newControlUnit()
                        .withId("GEN2")
                        .add()
                    .add()
                .add();
        // Add extension at inner element level
        n1.getLoad("LOAD").newExtension(LoadDetailAdder.class)
                .withFixedActivePower(40f)
                .withFixedReactivePower(20f)
                .withVariableActivePower(60f)
                .withVariableReactivePower(30f)
                .add();

        Network merge = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = merge.getSubnetwork("sim1");
        checkExtensions(subnetwork1);

        Network detachedN1 = subnetwork1.detach();
        checkExtensions(detachedN1);
    }

    private static void checkExtensions(Network network) {
        // Check that the Network extension is present on the given network
        assertEquals(1, network.getExtensions().size());
        assertNotNull(network.getExtensionByName(SecondaryVoltageControl.NAME));
        assertNotNull(network.getExtension(SecondaryVoltageControl.class));

        // Check that the Load extension is visible from the given network
        assertEquals(1, network.getLoad("LOAD").getExtensions().size());
        assertNotNull(network.getLoad("LOAD").getExtensionByName(LoadDetail.NAME));
        assertNotNull(network.getLoad("LOAD").getExtension(LoadDetail.class));
    }

    @Test
    public void testMergeAndFlattenWithExtensions() {
        n1.addExtension(FooNetworkExtension.class, new FooNetworkExtensionImpl(N1));
        n2.addExtension(FooNetworkExtension.class, new FooNetworkExtensionImpl(N2));
        n2.addExtension(BarNetworkExtension.class, new BarNetworkExtensionImpl());

        Network merge = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = merge.getSubnetwork(N1);
        Network subnetwork2 = merge.getSubnetwork(N2);
        assertEquals(0, merge.getExtensions().size());
        assertEquals(1, subnetwork1.getExtensions().size());
        assertEquals(2, subnetwork2.getExtensions().size());

        merge.flatten();
        // A FooNetworkExtension extension is present in both subnetworks.
        // The 1st one (in the merging order) is transferred to the flatten network and
        // the extension is kept in subnetwork2. This way, it is possible to detect duplicates and
        // to handle them manually if wanted.
        assertEquals(2, merge.getExtensions().size());
        assertEquals(0, subnetwork1.getExtensions().size());
        assertEquals(1, subnetwork2.getExtensions().size());
        Extension<Network> extension = subnetwork2.getExtensions().stream().findFirst().orElseThrow();
        assertInstanceOf(FooNetworkExtension.class, extension);
        assertEquals(N2, ((FooNetworkExtension) extension).getVal());
    }

    interface FooNetworkExtension {
        String getVal();
    }

    static class FooNetworkExtensionImpl extends AbstractExtension<Network> implements FooNetworkExtension {
        private final String val;

        public FooNetworkExtensionImpl(String val) {
            this.val = val;
        }

        @Override
        public String getName() {
            return "Foo";
        }

        @Override
        public String getVal() {
            return val;
        }
    }

    interface BarNetworkExtension {
    }

    static class BarNetworkExtensionImpl extends AbstractExtension<Network> implements BarNetworkExtension {
        @Override
        public String getName() {
            return "Bar";
        }
    }

    @Test
    public void failFlattenSubnetwork() {
        Network merged = Network.merge(n1, n2);
        Network subnetwork1 = merged.getSubnetwork(N1);
        Exception e = assertThrows(UnsupportedOperationException.class, subnetwork1::flatten);
        assertEquals("Subnetworks cannot be flattened.", e.getMessage());
    }

    @Test
    public void failDetachWithALineBetween2Subnetworks() {
        addCommonSubstationsAndVoltageLevels();
        Network merge = Network.merge(MERGE, n1, n2);
        merge.newLine()
                .setId("line1")
                .setVoltageLevel1("vl1")
                .setVoltageLevel2("vl2")
                .setBus1("b1")
                .setBus2("b2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();
        Network subnetwork1 = merge.getSubnetwork(N1);
        assertFalse(subnetwork1.isDetachable());
        PowsyblException e = assertThrows(PowsyblException.class, subnetwork1::detach);
        assertEquals("Un-splittable boundary elements prevent the subnetwork to be detached: line1", e.getMessage());
    }

    @Test
    public void failDetachIfMultiVariants() {
        addCommonSubstationsAndVoltageLevels();
        Network merge = Network.merge(MERGE, n1, n2);
        merge.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "Totest");

        Network subnetwork1 = merge.getSubnetwork(N1);
        assertFalse(subnetwork1.isDetachable());
        PowsyblException e = assertThrows(PowsyblException.class, subnetwork1::detach);
        assertTrue(e.getMessage().contains("Detaching from multi-variants network is not supported"));
    }

    @Test
    public void testMerge3Networks() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        Network n3 = Network.create("n3", "test");
        addSubstationAndVoltageLevel(n3, "s3", Country.DE, "vl3", "b3");
        addDanglingLines(n1, "vl1", "dl3", "code2", "b1");
        addDanglingLines(n3, "vl3", "dl4", "code2", "b3");

        //merge.merge(n1, n2, n3);
        Network merge = Network.merge(MERGE, n1, n2, n3);
        TieLine tieLine1 = merge.getTieLine("dl1 + dl2");
        assertNotNull(tieLine1);
        assertEquals("dl1_name + dl2_name", tieLine1.getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", tieLine1.getNameOrId());
        TieLine tieLine2 = merge.getTieLine("dl3 + dl4");
        assertNotNull(tieLine2);
        assertEquals("dl3_name + dl4_name", tieLine2.getOptionalName().orElse(null));
        assertEquals("dl3_name + dl4_name", tieLine2.getNameOrId());

        Network subnetwork1 = merge.getSubnetwork(N1);
        Network subnetwork2 = merge.getSubnetwork(N2);
        Network subnetwork3 = merge.getSubnetwork("n3");
        checkDanglingLineStatusCount(merge, 0, 4);
        checkDanglingLineStatusCount(subnetwork1, 0, 2);
        checkDanglingLineStatusCount(subnetwork2, 0, 1);
        checkDanglingLineStatusCount(subnetwork3, 0, 1);

        assertEquals(merge, tieLine1.getParentNetwork());
        assertEquals(merge, tieLine2.getParentNetwork());
        assertEquals(subnetwork1, merge.getDanglingLine("dl1").getParentNetwork());
        assertEquals(subnetwork1, merge.getDanglingLine("dl3").getParentNetwork());
        assertEquals(subnetwork2, merge.getDanglingLine("dl2").getParentNetwork());
        assertEquals(subnetwork3, merge.getDanglingLine("dl4").getParentNetwork());
    }

    private void checkDanglingLineStatusCount(Network network, long unpairedNb, long pairedNb) {
        assertEquals(pairedNb, network.getDanglingLineStream(DanglingLineFilter.PAIRED).count());
        assertEquals(unpairedNb, network.getDanglingLineStream(DanglingLineFilter.UNPAIRED).count());
    }

    @Test
    public void failMergeDanglingLinesWithSameId() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl", null, "dl", "code");
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(n0, n1, n2));
        assertTrue(e.getMessage().contains("The following object(s) of type DanglingLineImpl exist(s) in both networks: [dl]"));
    }

    @Test
    public void testValidationLevelWhenMerging2Eq() {
        addCommonSubstationsAndVoltageLevels();
        n1.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        n1.getVoltageLevel("vl1").newLoad()
                .setId("unchecked1")
                .setBus("b1")
                .setConnectableBus("b1")
                .add();
        n2.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        n2.getVoltageLevel("vl2").newLoad()
                .setId("unchecked2")
                .setBus("b2")
                .setConnectableBus("b2")
                .add();

        // merge(n1, n2)
        Network merge = Network.merge(MERGE, n1, n2);

        assertValidationLevels(merge, ValidationLevel.EQUIPMENT);
    }

    @Test
    public void testValidationLevelWhenMergingEqAndSsh() {
        addCommonSubstationsAndVoltageLevels();
        n1.getVoltageLevel("vl1").newLoad()
                .setId("unchecked1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setP0(1.0).setQ0(1.0)
                .add();
        n2.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        n2.getVoltageLevel("vl2").newLoad()
                .setId("unchecked2")
                .setBus("b2")
                .setConnectableBus("b2")
                .add();

        // merge(n1, n2)
        Network merge = Network.merge(MERGE, n1, n2);

        assertValidationLevels(merge, ValidationLevel.EQUIPMENT);
    }

    @Test
    public void testValidationLevelWhenMerging2Ssh() {
        addCommonSubstationsAndVoltageLevels();
        n1.getVoltageLevel("vl1").newLoad()
                .setId("unchecked1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setP0(1.0).setQ0(1.0)
                .add();
        n2.getVoltageLevel("vl2").newLoad()
                .setId("unchecked2")
                .setBus("b2")
                .setConnectableBus("b2")
                .setP0(1.0).setQ0(1.0)
                .add();

        // merge(n1, n2)
        Network merge = Network.merge(MERGE, n1, n2);

        assertValidationLevels(merge, ValidationLevel.STEADY_STATE_HYPOTHESIS);
    }

    void assertValidationLevels(Network merge, ValidationLevel expected) {
        // The validation level must be the same between the root network and its subnetworks
        assertEquals(expected, merge.getValidationLevel());
        assertEquals(expected, merge.getSubnetwork(N1).getValidationLevel());
        assertEquals(expected, merge.getSubnetwork(N2).getValidationLevel());
    }

    @Test
    public void failMergeOnlyOneNetwork() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> Network.merge(MERGE, n1));
        assertTrue(e.getMessage().contains("At least 2 networks are expected"));
    }

    @Test
    public void failMergeOnSubnetworks() {
        Network merge = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = merge.getSubnetwork(N1);
        Network other1 = Network.create("other1", "format");
        Network other2 = Network.create("other2", "format");

        Exception e = assertThrows(IllegalArgumentException.class, () -> Network.merge(subnetwork1, other1));
        assertEquals("The network n1 is already a subnetwork", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Network.merge(subnetwork1, other1, other2));
        assertEquals("The network n1 is already a subnetwork", e.getMessage());
    }

    @Test
    public void failMergeSubnetworks() {
        Network merge = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = merge.getSubnetwork(N1);
        Network other = Network.create("other", "format");

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> Network.merge("test", other, subnetwork1));
        assertTrue(e.getMessage().contains("is already a subnetwork"));
    }

    @Test
    public void failMergeContainingSubnetworks() {
        Network merge = Network.merge(MERGE, n1, n2);
        Network other = Network.create("other", "format");

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> Network.merge("test", other, merge));
        assertTrue(e.getMessage().contains("already contains subnetworks"));
    }

    @Test
    public void testMergeAndDetachWithDistinctAreas() {
        addCommonSubstationsAndVoltageLevels();
        Area n1TsoA = addArea(n1, "tsoA", "tso");
        Area n2BzB = n2.newArea().setId("bzB").setName("bzB_Name").setAreaType("bz").setFictitious(true).setInterchangeTarget(100.).add();
        n1TsoA.addVoltageLevel(n1.getVoltageLevel("vl1"));
        n2BzB.addVoltageLevel(n2.getVoltageLevel("vl2"));

        // Merge
        Network merged = Network.merge(n1, n2);
        checkAreas(merged, List.of("tsoA", "bzB"));
        checkAreaTypes(merged, List.of("tso", "bz"));
        final Area mergedBzB = merged.getArea("bzB");
        assertNotNull(mergedBzB);
        assertTrue(mergedBzB.isFictitious());
        assertTrue(mergedBzB.getInterchangeTarget().isPresent());
        assertEquals(100., mergedBzB.getInterchangeTarget().getAsDouble());

        // Detach n1, and check its content
        Network n1Detached = merged.getSubnetwork(n1.getId()).detach();
        checkAreas(n1Detached, List.of("tsoA"));
        checkAreaTypes(n1Detached, List.of("tso"));
        // Detached elements were removed from merged network
        checkAreas(merged, List.of("bzB"));
        checkAreaTypes(merged, List.of("bz"));

        // Detach n2, and check its content
        Network n2Detached = merged.getSubnetwork(n2.getId()).detach();
        checkAreas(n2Detached, List.of("bzB"));
        checkAreaTypes(n2Detached, List.of("bz"));
        final Area detachedBzB = n2Detached.getArea("bzB");
        assertNotNull(detachedBzB);
        assertTrue(detachedBzB.isFictitious());
        assertTrue(detachedBzB.getInterchangeTarget().isPresent());
        assertEquals(100., detachedBzB.getInterchangeTarget().getAsDouble());
    }

    @Test
    public void testNoEmptyAdditionalSubnetworkIsCreated() {
        Network merge = Network.merge(MERGE, n1, n2);
        assertEquals(2, merge.getSubnetworks().size());
        assertNull(merge.getSubnetwork(MERGE));
        assertNotNull(merge.getSubnetwork(N1));
        assertNotNull(merge.getSubnetwork(N2));
    }

    private static void checkAreas(final Network merged, final List<String> expectedAreaIds) {
        assertEquals(expectedAreaIds.size(), merged.getAreaStream().count());
        assertTrue(merged.getAreaStream().map(Area::getId).toList().containsAll(expectedAreaIds));
        final List<String> expectedNames = expectedAreaIds.stream().map(id -> id + "_Name").toList();
        assertTrue(merged.getAreaStream().map(Area::getNameOrId).toList().containsAll(expectedNames));
    }

    private static void checkAreaTypes(final Network merged, final List<String> expectedAreaTypeIds) {
        assertEquals(expectedAreaTypeIds.size(), merged.getAreaTypeStream().count());
        assertTrue(merged.getAreaTypeStream().toList().containsAll(expectedAreaTypeIds));
    }

    @Test
    public void failMergeWithCommonAreaConflict() {
        addArea(n1, "bza", "bz");
        addArea(n2, "bza", "tso");

        // Merge should fail, because area "bza" is present in both network but with different attribute values
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(n1, n2));
        assertEquals("The following object(s) of type AreaImpl exist(s) in both networks: [bza]", e.getMessage());
    }

    @Test
    public void testListeners() {
        MutableBoolean listenerCalled = new MutableBoolean(false);
        NetworkListener listener = new DefaultNetworkListener() {
            @Override
            public void onCreation(Identifiable identifiable) {
                listenerCalled.setTrue();
            }
        };

        // The listener works on n1.
        n1.addListener(listener);
        addSubstation(n1, "s1");
        assertTrue(listenerCalled.booleanValue());

        Network merge = Network.merge(MERGE, n1, n2);
        Network subnetwork1 = merge.getSubnetwork(N1);
        Network subnetwork2 = merge.getSubnetwork(N2);

        // After the merge, changes on "merge" or on "subnetwork1" are not reported to the listener.
        listenerCalled.setFalse();
        addSubstation(merge, "s2");
        assertFalse(listenerCalled.booleanValue());
        addSubstation(subnetwork1, "s3");
        assertFalse(listenerCalled.booleanValue());

        // Add the listener to "merge". Changes on subnetwork1 are reported.
        merge.addListener(listener);
        addSubstation(subnetwork1, "s4");
        assertTrue(listenerCalled.booleanValue());

        // Detach "subnetwork1". Changes on the new Network aren't reported to the listener.
        Network n = subnetwork1.detach();
        listenerCalled.setFalse();
        addSubstation(n, "s5");
        assertFalse(listenerCalled.booleanValue());

        // Changes on "merge" or "subnetwork2" are still reported to the listener.
        addSubstation(merge, "s6");
        assertTrue(listenerCalled.booleanValue());
        listenerCalled.setFalse();
        addSubstation(subnetwork2, "s7");
        assertTrue(listenerCalled.booleanValue());
    }

    private static Area addArea(Network network, String id, String areaType) {
        return network.newArea()
                      .setId(id)
                      .setName(id + "_Name")
                      .setAreaType(areaType)
                      .add();
    }

    private void addSubstation(Network network, String substationId) {
        network.newSubstation()
                            .setId(substationId)
                            .setCountry(Country.FR)
                            .setTso("RTE")
                            .setGeographicalTags("A")
                        .add();
    }

    private void addCommonSubstationsAndVoltageLevels() {
        addSubstationAndVoltageLevel(n1, "s1", Country.FR, "vl1", "b1");
        addSubstationAndVoltageLevel(n2, "s2", Country.BE, "vl2", "b2");
    }

    private void addSubstationAndVoltageLevel(Network network, String substationId, Country country, String vlId, String busId) {
        Substation s = network.newSubstation()
                .setId(substationId)
                .setCountry(country)
                .add();
        addVoltageLevel(s.newVoltageLevel(), vlId, 380, busId);
    }

    private static void addVoltageLevel(VoltageLevelAdder s, String vlId, int nominalV, String busId) {
        VoltageLevel vl = s
                .setId(vlId)
                .setNominalV(nominalV)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId(busId)
                .add();
    }

    private void addCommonDanglingLines(String dl1, String code1, String dl2, String code2) {
        addDanglingLines(n1, "vl1", dl1, code1, "b1");
        addDanglingLines(n2, "vl2", dl2, code2, "b2");
    }

    private void addDanglingLines(Network network, String voltageLevelId, String dlId, String code, String busId) {
        addDanglingLine(network, voltageLevelId, dlId, code, busId, busId);
    }

    private static void addDanglingLine(Network n, String voltageLevelId, String id, String code, String connectableBus, String bus) {
        DanglingLine dl = n.getVoltageLevel(voltageLevelId).newDanglingLine()
                .setId(id)
                .setName(id + "_name")
                .setConnectableBus(connectableBus)
                .setBus(bus)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setPairingKey(code)
                .add();
        Terminal t = dl.getTerminal();
        t.setP(1.);
        t.setQ(2.);
        Bus b = t.getBusView().getBus();
        if (b != null) {
            b.setAngle(3.);
            b.setV(4.);
        }
    }

    @Test
    public void test() {
        ZonedDateTime d1 = n1.getCaseDate();
        ZonedDateTime d2 = n2.getCaseDate();
        addCommonSubstationsAndVoltageLevels();
        addLoad(n1, 1);
        addLoad(n2, 2);
        Network merge = Network.merge(MERGE, n0, n1, n2);
        assertEquals(MERGE, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
        assertEquals(3, merge.getSubnetworks().size());
        checks(merge, 1, "asdf", d1);
        checks(merge, 2, "qwer", d2);

        // Parent network should remain indexed with the same id
        Identifiable<?> m = merge.getIdentifiable(MERGE);
        assertEquals(m, merge);
        // Subnetwork with elements shall keep its elements
        Network m1 = merge.getSubnetwork(N1);
        assertNotNull(m1);
        assertEquals(1, m1.getSubstationCount());
        assertEquals(1, m1.getVoltageLevelCount());
        // Substation and voltage level created on subnetwork shall be affected to subnetwork
        merge.getSubnetwork(N1).newSubstation().setId("s1bis").add().newVoltageLevel().setId("vl1bis").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(90.0).add();
        Substation s1bis = merge.getSubstation("s1bis");
        assertNotNull(s1bis);
        assertSame(s1bis, merge.getSubnetwork(N1).getSubstation("s1bis"));
        assertSame(merge.getSubnetwork(N1), s1bis.getParentNetwork());
        assertNull(merge.getSubnetwork(N2).getSubstation("s1bis"));
        VoltageLevel vl1bis = merge.getVoltageLevel("vl1bis");
        assertNotNull(vl1bis);
        assertSame(vl1bis, merge.getSubnetwork(N1).getVoltageLevel("vl1bis"));
        assertSame(merge.getSubnetwork(N1), vl1bis.getParentNetwork());
        assertNull(merge.getSubnetwork(N2).getVoltageLevel("vl1bis"));
        // Voltage level created on subnetwork shall be affected to subnetwork
        merge.getSubnetwork(N2).newVoltageLevel().setId("vl2bis").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(90.0).add();
        VoltageLevel vl2bis = merge.getVoltageLevel("vl2bis");
        assertNotNull(vl2bis);
        assertSame(vl2bis, merge.getSubnetwork(N2).getVoltageLevel("vl2bis"));
        assertSame(merge.getSubnetwork(N2), vl2bis.getParentNetwork());
        assertNull(merge.getSubnetwork(N1).getVoltageLevel("vl2bis"));
    }

    private static void addLoad(Network n, int num) {
        n.getVoltageLevel("vl" + num).newLoad()
                .setId("l" + num)
                .setBus("b" + num)
                .setP0(0.0)
                .setQ0(0.0)
                .add();
    }

    private static void checks(Network merge, int num, String sourceFormat, ZonedDateTime d) {
        Network n = merge.getSubnetwork("n" + num);
        assertNotNull(n);
        assertEquals(sourceFormat, n.getSourceFormat());
        assertEquals(d, n.getCaseDate());
        assertEquals(1, n.getSubstationCount());
        assertEquals(1, n.getVoltageLevelCount());
        Substation s = n.getSubstation("s" + num);
        assertNotNull(s);
        assertSame(merge, s.getNetwork());
        VoltageLevel vl = n.getVoltageLevel("vl" + num);
        assertNotNull(vl);
        assertSame(merge, vl.getNetwork());
        Bus b = n.getBusBreakerView().getBus("b" + num);
        assertNotNull(b);
        assertSame(merge, b.getNetwork());
        assertSame(n, merge.getSubstation("s" + num).getParentNetwork());
        assertSame(n, merge.getVoltageLevel("vl" + num).getParentNetwork());
        assertSame(n, merge.getBusBreakerView().getBus("b" + num).getParentNetwork());
        Load l = merge.getLoad("l" + num);
        Load other = merge.getLoad("l" + (3 - num));
        assertNotNull(l);
        assertNotNull(other);
        assertSame(n, l.getParentNetwork());
        Load lBis = n.getLoad("l" + num);
        assertSame(l, lBis);
        Load otherBis = n.getLoad("l" + (3 - num));
        assertNull(otherBis);
    }

    @Test
    public void checkMergingSameFormat() {
        Network merge = Network.merge(MERGE, n0, n1);
        assertEquals(MERGE, merge.getId());
        assertEquals("asdf", merge.getSourceFormat());
    }

    @Test
    public void checkMergingDifferentFormat() {
        Network merge = Network.merge(n1, n2);
        assertEquals(MERGE_DEFAULT_ID, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
    }

    @Test
    public void mergeThenCloneVariantBug() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        Load ld2 = n2.getVoltageLevel("vl2").newLoad()
                .setId("ld2")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .add();
        Network merge = Network.merge(n1, n2);
        merge.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "test");
        merge.getVariantManager().setWorkingVariant("test");
        ld2.setP0(10);
        merge.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(0, ld2.getP0(), 0);
    }

    @Test
    public void invertDanglingLinesWhenCreatingATieLine() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl2", "code", "dl1", "code");
        Network merge = Network.merge(n1, n2);
        assertEquals(1, merge.getTieLineCount());
        TieLine tieLine = merge.getTieLine("dl1 + dl2");
        assertNotNull(tieLine);
        assertEquals("dl1", tieLine.getDanglingLine1().getId());
        assertEquals("dl2", tieLine.getDanglingLine2().getId());
    }

    @Test
    public void dontCreateATieLineWithAlreadyMergedDanglingLinesInMergedNetwork() {
        // The code is not the same if the dangling line with the duplicate pairing key is in the current network
        // or in the network we are adding when we try to associate dangling lines.
        //
        // This test covers the case where the duplicate pairing key is in the CURRENT network.
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n1, "vl1", "dl3", "code", "b1", "b1");
        n1.newTieLine()
                .setId("dl1 + dl3")
                .setDanglingLine1("dl1")
                .setDanglingLine2("dl3")
                .add();
        Network merge = Network.merge(n1, n2);
        assertNotNull(merge.getTieLine("dl1 + dl3"));
        assertNull(merge.getTieLine("dl1 + dl2"));
        assertNull(merge.getTieLine("dl2 + dl3"));
        assertEquals(1, merge.getTieLineCount());
    }

    @Test
    public void dontCreateATieLineWithAlreadyMergedDanglingLinesInMergingNetwork() {
        // The code is not the same if the dangling line with the duplicate pairing key is in the current network
        // or in the network we are adding when we try to associate dangling lines.
        //
        // This test covers the case where the duplicate pairing key is in the ADDED network.
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n2, "vl2", "dl3", "code", "b2", "b2");
        n2.newTieLine()
                .setId("dl2 + dl3")
                .setDanglingLine1("dl2")
                .setDanglingLine2("dl3")
                .add();
        Network merge = Network.merge(n1, n2);
        assertNotNull(merge.getTieLine("dl2 + dl3"));
        assertNull(merge.getTieLine("dl1 + dl2"));
        assertNull(merge.getTieLine("dl1 + dl3"));
        assertEquals(1, merge.getTieLineCount());
    }

    @Test
    public void multipleDanglingLinesInMergedNetwork() {
        // The code is not the same if the dangling line with the duplicate pairing key is in the current network
        // or in the network we are adding when we try to associate dangling lines.
        //
        // This test covers the case where the duplicate pairing key is in the CURRENT network.
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n2, "vl2", "dl3", "code", "b2", null);
        Network merge = Network.merge(n1, n2);
        // Since n2 has ONLY ONE connected dangling line with the pairing key, the tie line is created
        assertNotNull(merge.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", merge.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", merge.getTieLine("dl1 + dl2").getNameOrId());
    }

    @Test
    public void multipleConnectedDanglingLinesInMergedNetwork() {
        // The code is not the same if the dangling line with the duplicate pairing key is in the current network
        // or in the network we are adding when we try to associate dangling lines.
        //
        // This test covers the case where the duplicate pairing key is in the CURRENT network.
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n2, "vl2", "dl3", "code", "b2", "b2");
        Network merge = Network.merge(n1, n2);
        // Since n2 has SEVERAL connected dangling lines with the pairing key, we don't create the tie line
        assertEquals(0, merge.getTieLineCount());
    }

    @Test
    public void multipleDanglingLinesInMergingNetwork() {
        // The code is not the same if the dangling line with the duplicate pairing key is in the current network
        // or in the network we are adding when we try to associate dangling lines.
        //
        // This test covers the case where the duplicate pairing key is in the ADDED network.
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n1, "vl1", "dl3", "code", "b1", null);
        Network merge = Network.merge(n1, n2);
        // Since n1 has ONLY ONE connected dangling line with the pairing key, the tie line is created
        assertNotNull(merge.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", merge.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", merge.getTieLine("dl1 + dl2").getNameOrId());
    }

    @Test
    public void multipleConnectedDanglingLinesWithSamePairingKey() {
        // The code is not the same if the dangling line with the duplicate pairing key is in the current network
        // or in the network we are adding when we try to associate dangling lines.
        //
        // This test covers the case where the duplicate pairing key is in the ADDED network.
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n1, "vl1", "dl3", "code", "b1", "b1");
        Network merge = Network.merge(n1, n2);
        // Since n1 has SEVERAL connected dangling lines with the pairing key, we don't create the tie line
        assertEquals(0, merge.getTieLineCount());
    }
}
