/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class MultiCriteriaVoltageStabilityIndexTest extends AbstractSecurityIndexTest {

    @Override
    protected MultiCriteriaVoltageStabilityIndex create() {
        Map<String, Float> lockedTapChangerLoads = new LinkedHashMap<>();
        lockedTapChangerLoads.put("locked1", 1.0f);
        lockedTapChangerLoads.put("locked2", 2.0f);

        Map<String, Float> stoppedTapChangerLoads = new LinkedHashMap<>();
        stoppedTapChangerLoads.put("stopped1", 1.0f);
        stoppedTapChangerLoads.put("stopped2", 2.0f);

        Map<String, Float> underVoltageAutomatonGenerators = new LinkedHashMap<>();
        underVoltageAutomatonGenerators.put("gen1", 1.0f);
        underVoltageAutomatonGenerators.put("gen2", 1.0f);

        Set<String> underVoltageBuses = new LinkedHashSet<>();
        underVoltageBuses.add("bus1");
        underVoltageBuses.add("bus2");

        return new MultiCriteriaVoltageStabilityIndex("contingency", lockedTapChangerLoads, stoppedTapChangerLoads,
            underVoltageAutomatonGenerators, underVoltageBuses);
    }

    @Test
    public void test() throws IOException {
        roundTripTest(create(), MultiCriteriaVoltageStabilityIndexTest::write, MultiCriteriaVoltageStabilityIndexTest::read, "/multiCriteriaVoltageStabilityIndex.xml");
    }

    private static MultiCriteriaVoltageStabilityIndex read(Path path) {
        List<SecurityIndex> indexes = AbstractSecurityIndexTest.readSecurityIndexes(path);
        Assert.assertEquals(1, indexes.size());
        assertEquals(MultiCriteriaVoltageStabilityIndex.class, indexes.get(0).getClass());

        MultiCriteriaVoltageStabilityIndex securityIndex = (MultiCriteriaVoltageStabilityIndex) indexes.get(0);
        assertEquals("contingency", securityIndex.getId().getContingencyId());
        assertEquals(SecurityIndexType.MULTI_CRITERIA_VOLTAGE_STABILITY, securityIndex.getId().getSecurityIndexType());
        assertEquals(Sets.newHashSet("locked1", "locked2"), securityIndex.getLockedTapChangerLoads().keySet());
        assertEquals(Sets.newHashSet("stopped1", "stopped2"), securityIndex.getStoppedTapChangerLoads().keySet());
        assertEquals(Sets.newHashSet("gen1", "gen2"), securityIndex.getUnderVoltageAutomatonGenerators().keySet());
        assertEquals(Sets.newHashSet("bus1", "bus2"), securityIndex.getUnderVoltageBuses());

        return (MultiCriteriaVoltageStabilityIndex) securityIndex;
    }
}
