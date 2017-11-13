/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class MultiCriteriaVoltageStabilityIndex2Test extends AbstractSecurityIndexTest {

    @Override
    protected MultiCriteriaVoltageStabilityIndex2 create() {
        Map<String, Float> criteria1 = new LinkedHashMap<>();
        criteria1.put("c1.1", 1.1f);
        criteria1.put("c1.2", 1.2f);

        Map<String, Float> criteria2 = new LinkedHashMap<>();
        criteria2.put("c2.1", 2.1f);

        Set<String> criteria3 = new LinkedHashSet<>();
        criteria3.add("c3.1");
        criteria3.add("c3.2");
        criteria3.add("c3.3");

        return new MultiCriteriaVoltageStabilityIndex2("contingency", true, criteria1, criteria2, criteria3);
    }

    @Test
    public void test() throws IOException {
        roundTripTest(create(), MultiCriteriaVoltageStabilityIndex2Test::write, MultiCriteriaVoltageStabilityIndex2Test::read, "/multiCriteriaVoltageStabilityIndex2.xml");
    }

    private static MultiCriteriaVoltageStabilityIndex2 read(Path path) {
        List<SecurityIndex> indexes = AbstractSecurityIndexTest.readSecurityIndexes(path);
        assertEquals(1, indexes.size());
        assertEquals(MultiCriteriaVoltageStabilityIndex2.class, indexes.get(0).getClass());

        MultiCriteriaVoltageStabilityIndex2 securityIndex = (MultiCriteriaVoltageStabilityIndex2) indexes.get(0);
        assertEquals("contingency", securityIndex.getId().getContingencyId());
        assertEquals(SecurityIndexType.MULTI_CRITERIA_VOLTAGE_STABILITY2, securityIndex.getId().getSecurityIndexType());
        assertTrue(securityIndex.isConverge());
        assertEquals(Sets.newHashSet("c1.1", "c1.2"), securityIndex.getCriteria1().keySet());
        assertEquals(Sets.newHashSet("c2.1"), securityIndex.getCriteria2().keySet());
        assertEquals(Sets.newHashSet("c3.1", "c3.2", "c3.3"), securityIndex.getCriteria3());

        return (MultiCriteriaVoltageStabilityIndex2) securityIndex;
    }
}
