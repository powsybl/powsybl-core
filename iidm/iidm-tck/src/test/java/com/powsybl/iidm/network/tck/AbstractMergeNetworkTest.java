/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractMergeNetworkTest {

    private static final String MERGE = "merge";
    public static final String N1 = "n1";
    public static final String N2 = "n2";

    Network merge;
    Network n1;
    Network n2;

    @BeforeEach
    public void setup() {
        merge = Network.create(MERGE, "asdf");
        n1 = Network.create(N1, "asdf");
        n2 = Network.create(N2, "qwer");
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
    public void xnodeNonCompatible() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl", "code", "dl", "deco");
        merge.merge(n1);
        PowsyblException e = assertThrows(PowsyblException.class, () -> merge.merge(n2));
        assertTrue(e.getMessage().contains("Dangling line couple dl have inconsistent Xnodes (code!=deco)"));
    }

    @Test
    public void testMerge() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        merge.merge(n1, n2);
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
        merge = Network.create(MERGE, n1, n2);
        TieLine tieLine = merge.getTieLine("dl1 + dl2");
        assertNotNull(tieLine);
        assertEquals("dl1_name + dl2_name", tieLine.getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", tieLine.getNameOrId());
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

    private void checkValidPAndQ(DanglingLine dl) {
        assertEquals(0.0, dl.getP0());
        assertEquals(0.0, dl.getQ0());
    }

    @Test
    public void failDetachWithALineBetween2Subnetworks() {
        addCommonSubstationsAndVoltageLevels();
        merge = Network.create(MERGE, n1, n2);
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
        assertTrue(e.getMessage().contains("Some un-splittable boundary elements prevent the subnetwork to be detached"));
    }

    @Test
    public void failDetachIfMultiVariants() {
        addCommonSubstationsAndVoltageLevels();
        merge = Network.create(MERGE, n1, n2);
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
        merge = Network.create(MERGE, n1, n2, n3);
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
    public void testMergeSameId() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl", null, "dl", "code");
        merge.merge(n1, n2);
        assertNotNull(merge.getTieLine("dl"));
        assertEquals("dl", merge.getTieLine("dl").getId());
        assertEquals("dl_name", merge.getTieLine("dl").getOptionalName().orElse(null));
        assertEquals("dl_name", merge.getTieLine("dl").getNameOrId());
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
                .setUcteXnodeCode(code)
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
        DateTime d1 = n1.getCaseDate();
        DateTime d2 = n2.getCaseDate();
        addCommonSubstationsAndVoltageLevels();
        addLoad(n1, 1);
        addLoad(n2, 2);
        merge.merge(n1, n2);
        assertEquals(MERGE, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
        assertEquals(3, merge.getSubnetworks().size());
        checks(merge, 1, "asdf", d1);
        checks(merge, 2, "qwer", d2);
        // Subnetwork without elements shall still be empty
        Network m = merge.getSubnetwork(MERGE);
        assertNotNull(m);
        assertEquals(0, m.getSubstationCount());
        assertEquals(0, m.getVoltageLevelCount());
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

    private static void checks(Network merge, int num, String sourceFormat, DateTime d) {
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
        merge.merge(n1);
        assertEquals(MERGE, merge.getId());
        assertEquals("asdf", merge.getSourceFormat());
    }

    @Test
    public void checkMergingDifferentFormat() {
        merge.merge(n2);
        assertEquals(MERGE, merge.getId());
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
        n1.merge(n2);
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "test");
        n1.getVariantManager().setWorkingVariant("test");
        ld2.setP0(10);
        n1.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(0, ld2.getP0(), 0);
    }

    @Test
    public void multipleDanglingLinesInMergedNetwork() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n2, "vl2", "dl3", "code", "b2", null);
        n1.merge(n2);
        assertNotNull(n1.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getNameOrId());
    }

    @Test
    public void multipleDanglingLinesInMergingNetwork() {
        addCommonSubstationsAndVoltageLevels();
        addCommonDanglingLines("dl1", "code", "dl2", "code");
        addDanglingLine(n1, "vl1", "dl3", "code", "b1", null);
        n1.merge(n2);
        assertNotNull(n1.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", n1.getTieLine("dl1 + dl2").getNameOrId());
    }
}
