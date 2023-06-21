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

    private static final String MERGE2 = "merge";
    public static final String N1 = "n1";
    public static final String N2 = "n2";

    Network merge;
    Network n1;
    Network n2;

    @BeforeEach
    public void setup() {
        merge = Network.create(MERGE2, "asdf");
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
        addSubstationAndVoltageLevel();
        addDanglingLines("dl", "code", "dl", "deco");
        merge.merge(n1);
        PowsyblException e = assertThrows(PowsyblException.class, () -> merge.merge(n2));
        assertTrue(e.getMessage().contains("Dangling line couple dl have inconsistent Xnodes (code!=deco)"));
    }

    @Test
    public void testMerge() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl1", "code", "dl2", "code");
        merge.merge(n1, n2);
        TieLine tieLine = merge.getTieLine("dl1 + dl2");
        assertNotNull(tieLine);
        assertEquals("dl1_name + dl2_name", tieLine.getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", tieLine.getNameOrId());

        Network subNetwork1 = merge.getSubNetwork(N1);
        Network subNetwork2 = merge.getSubNetwork(N2);
        checkDanglingLineStatusCount(merge, 0, 2);
        checkDanglingLineStatusCount(subNetwork1, 0, 1);
        checkDanglingLineStatusCount(subNetwork2, 0, 1);

        assertEquals(merge, tieLine.getClosestNetwork());
        assertEquals(subNetwork1, merge.getDanglingLine("dl1").getClosestNetwork());
        assertEquals(subNetwork2, merge.getDanglingLine("dl2").getClosestNetwork());
    }

    private void checkDanglingLineStatusCount(Network network, long unpairedNb, long pairedNb) {
        assertEquals(pairedNb, network.getDanglingLineStream(DanglingLineFilter.PAIRED).count());
        assertEquals(unpairedNb, network.getDanglingLineStream(DanglingLineFilter.UNPAIRED).count());
    }

    @Test
    public void testMergeSameId() {
        addSubstationAndVoltageLevel();
        addDanglingLines("dl", null, "dl", "code");
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
        DateTime d1 = n1.getCaseDate();
        DateTime d2 = n2.getCaseDate();
        addSubstationAndVoltageLevel();
        addLoad(n1, 1);
        addLoad(n2, 2);
        merge.merge(n1, n2);
        assertEquals(MERGE2, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
        assertEquals(3, merge.getSubNetworks().size());
        checks(merge, 1, "asdf", d1);
        checks(merge, 2, "qwer", d2);
        // Subnetwork without elements shall still be empty
        Network m = merge.getSubNetwork(MERGE2);
        assertNotNull(m);
        assertEquals(0, m.getSubstationCount());
        assertEquals(0, m.getVoltageLevelCount());
        // Subnetwork with elements shall keep its elements
        Network m1 = merge.getSubNetwork(N1);
        assertNotNull(m1);
        assertEquals(1, m1.getSubstationCount());
        assertEquals(1, m1.getVoltageLevelCount());
        // Substation and voltage level created on subnetwork shall be affected to subnetwork
        merge.getSubNetwork(N1).newSubstation().setId("s1bis").add().newVoltageLevel().setId("vl1bis").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(90.0).add();
        Substation s1bis = merge.getSubstation("s1bis");
        assertNotNull(s1bis);
        assertSame(s1bis, merge.getSubNetwork(N1).getSubstation("s1bis"));
        assertSame(merge.getSubNetwork(N1), s1bis.getClosestNetwork());
        assertNull(merge.getSubNetwork(N2).getSubstation("s1bis"));
        VoltageLevel vl1bis = merge.getVoltageLevel("vl1bis");
        assertNotNull(vl1bis);
        assertSame(vl1bis, merge.getSubNetwork(N1).getVoltageLevel("vl1bis"));
        assertSame(merge.getSubNetwork(N1), vl1bis.getClosestNetwork());
        assertNull(merge.getSubNetwork(N2).getVoltageLevel("vl1bis"));
        // Voltage level created on subnetwork shall be affected to subnetwork
        merge.getSubNetwork(N2).newVoltageLevel().setId("vl2bis").setTopologyKind(TopologyKind.BUS_BREAKER).setNominalV(90.0).add();
        VoltageLevel vl2bis = merge.getVoltageLevel("vl2bis");
        assertNotNull(vl2bis);
        assertSame(vl2bis, merge.getSubNetwork(N2).getVoltageLevel("vl2bis"));
        assertSame(merge.getSubNetwork(N2), vl2bis.getClosestNetwork());
        assertNull(merge.getSubNetwork(N1).getVoltageLevel("vl2bis"));
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
        Network n = merge.getSubNetwork("n" + num);
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
        assertSame(n, merge.getSubstation("s" + num).getClosestNetwork());
        assertSame(n, merge.getVoltageLevel("vl" + num).getClosestNetwork());
        assertSame(n, merge.getBusBreakerView().getBus("b" + num).getClosestNetwork());
        Load l = merge.getLoad("l" + num);
        Load other = merge.getLoad("l" + (3 - num));
        assertNotNull(l);
        assertNotNull(other);
        assertSame(n, l.getClosestNetwork());
        Load lBis = n.getLoad("l" + num);
        assertSame(l, lBis);
        Load otherBis = n.getLoad("l" + (3 - num));
        assertNull(otherBis);
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
