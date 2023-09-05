/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractMergeNetworkTest {

    private static final String MERGE2 = "merge";

    Network merge;
    Network n1;
    Network n2;

    @BeforeEach
    public void setup() {
        merge = Network.create(MERGE2, "asdf");
        n1 = Network.create("n1", "asdf");
        n2 = Network.create("n2", "qwer");
    }

    @Test
    public void failMergeIfMultiVariants() {
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "Totest");
        PowsyblException e = assertThrows(PowsyblException.class, () -> merge.merge(n1));
        assertTrue(e.getMessage().contains("Merging of multi-variants network is not supported"));
    }

    @Test
    public void failMergeWithSameObj() {
        addSubstation(n1, "P1");
        addSubstation(n2, "P1");
        merge.merge(n1);
        PowsyblException e = assertThrows(PowsyblException.class, () -> merge.merge(n2));
        assertTrue(e.getMessage().contains("The following object(s) of type SubstationImpl exist(s) in both networks: [P1]"));
    }

    @Test
    public void failMergeValidationLevelLowerThanMin() {
        n1.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        addSubstationAndVoltageLevel();
        n1.getVoltageLevel("vl1").newLoad()
                .setId("unchecked1")
                .setBus("b1")
                .setConnectableBus("b1")
                .add();
        PowsyblException e = assertThrows(PowsyblException.class, () -> merge.merge(n1));
        assertTrue(e.getMessage().contains("cannot be merged: its validation level is lower than the minimum acceptable " +
                "validation level of network"));
    }

    @Test
    public void testMerge() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl1", "code", "dl2", "code");
        merge.merge(n1, n2);
        assertNotNull(merge.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", merge.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", merge.getTieLine("dl1 + dl2").getNameOrId());
    }

    @Test
    public void failMergeDanglingLinesWithSameId() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl", null, "dl", "code");
        PowsyblException e = assertThrows(PowsyblException.class, () -> merge.merge(n1, n2));
        assertTrue(e.getMessage().contains("The following object(s) of type DanglingLineImpl exist(s) in both networks: [dl]"));
    }

    private void addSubstation(Network network, String substationId) {
        network.newSubstation()
                            .setId(substationId)
                            .setCountry(Country.FR)
                            .setTso("RTE")
                            .setGeographicalTags("A")
                        .add();
    }

    private void addSubstationAndVoltageLevel() {
        Substation s1 = n1.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();

        Substation s2 = n2.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
    }

    private void addDanglingLines(String dl1, String code1, String dl2, String code2) {
        addDanglingLine(n1, "vl1", dl1, code1, "b1", "b1");
        addDanglingLine(n2, "vl2", dl2, code2, "b2", "b2");
    }

    private static void addDanglingLine(Network n, String voltageLevelId, String id, String code, String connectableBus, String bus) {
        n.getVoltageLevel(voltageLevelId).newDanglingLine()
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
                .setUcteXnodeCode(code)
                .add();
    }

    @Test
    public void test() {
        merge.merge(n1, n2);
        assertEquals(MERGE2, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
    }

    @Test
    public void checkMergingSameFormat() {
        merge.merge(n1);
        assertEquals(MERGE2, merge.getId());
        assertEquals("asdf", merge.getSourceFormat());
    }

    @Test
    public void checkMergingDifferentFormat() {
        merge.merge(n2);
        assertEquals(MERGE2, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
    }

    @Test
    public void mergeThenCloneVariantBug() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl1", "code", "dl2", "code");
        Load ld2 = n2.getVoltageLevel("vl2").newLoad()
                .setId("ld2")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .add();
        n1.merge(n2);
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "test");
        n1.getVariantManager().setWorkingVariant("test");
        ld2.setP0(10);
        n1.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(0, ld2.getP0(), 0);
    }

    @Test
    public void multipleDanglingLinesInMergedNetwork() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n2, "vl2", "dl3", "code", "b2", null);
        n1.merge(n2);
        assertNotNull(n1.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getNameOrId());
    }

    @Test
    public void multipleDanglingLinesInMergingNetwork() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n1, "vl1", "dl3", "code", "b1", null);
        n1.merge(n2);
        assertNotNull(n1.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getNameOrId());
    }
}
