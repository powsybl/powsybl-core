/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MergeNetworkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Network n1;
    Network n2;

    @Before
    public void setup() {
        n1 = new NetworkImpl("n1", "network1", "asdf");
        n2 = new NetworkImpl("n2", "network2", "qwer");
    }

    @Test
    public void failMergeIfMultiStates() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Merging of multi-states network is not supported");
        n1.getStateManager().cloneState(StateManagerConstants.INITIAL_STATE_ID, "Totest");
        n1.merge(n2);
    }

    @Test
    public void failMergeWithSameObj() {
        addSubstation(n1, "P1");
        addSubstation(n2, "P1");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The following object(s) of type SubstationImpl exist(s) in both networks: [P1]");
        n1.merge(n2);
    }

    @Test
    public void xnodeNonCompatible() {
        addSubstationAndVoltageLevel();
        addDanglingLine("dl", "code", "dl", "deco");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Dangling line couple dl have inconsistent Xnodes (code!=deco)");
        n1.merge(n2);
    }

    @Test
    public void testMerge() {
        addSubstationAndVoltageLevel();
        addDanglingLine("dl1", "code", "dl2", "code");
        n1.merge(n2);
        assertNotNull(n1.getLine("dl1 + dl2"));
    }

    private void addSubstation(Network network, String substationId) {
        Substation substation = network.newSubstation()
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

    private void addDanglingLine(String dl1, String code1, String dl2, String code2) {
        n1.getVoltageLevel("vl1").newDanglingLine()
                .setId(dl1)
                .setConnectableBus("b1")
                .setBus("b1")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setUcteXnodeCode(code1)
                .add();
        n2.getVoltageLevel("vl2").newDanglingLine()
                .setId(dl2)
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(10.0)
                .setX(20.0)
                .setG(30.0)
                .setB(40.0)
                .setUcteXnodeCode(code2)
                .add();
    }

    @Test
    public void test() {
        n1.merge(n2);
        assertEquals("n1 + n2", n1.getId());
        assertEquals(n1.getId(), n1.getName());
        assertEquals("hybrid", n1.getSourceFormat());
    }
}
