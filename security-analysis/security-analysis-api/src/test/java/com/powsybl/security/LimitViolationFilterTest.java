/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.iidm.network.Country;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolationFilterTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("limit-violation-default-filter");
        moduleConfig.setStringListProperty("violationTypes", Arrays.asList("CURRENT", "LOW_VOLTAGE"));
        moduleConfig.setStringProperty("minBaseVoltage", "150");
        moduleConfig.setStringListProperty("countries", Arrays.asList("FR", "BE"));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void load() throws Exception {
        LimitViolationFilter filter = LimitViolationFilter.load(platformConfig);
        assertEquals(EnumSet.of(LimitViolationType.CURRENT, LimitViolationType.LOW_VOLTAGE), filter.getViolationTypes());
        assertEquals(150.0, filter.getMinBaseVoltage(), 0.0);
        assertEquals(EnumSet.of(Country.FR, Country.BE), filter.getCountries());
        filter.setViolationTypes(EnumSet.of(LimitViolationType.HIGH_VOLTAGE));
        assertEquals(EnumSet.of(LimitViolationType.HIGH_VOLTAGE), filter.getViolationTypes());
        filter.setMinBaseVoltage(225.0);
        filter.setCountries(EnumSet.of(Country.FR));
        assertEquals(EnumSet.of(Country.FR), filter.getCountries());
        assertEquals(225.0, filter.getMinBaseVoltage(), 0.0);
        filter.setViolationTypes(null);
        assertEquals(LimitViolationType.values().length, filter.getViolationTypes().size());
        filter.setCountries(null);
        assertEquals(Country.values().length, filter.getCountries().size());
        try {
            filter.setViolationTypes(EnumSet.noneOf(LimitViolationType.class));
            fail();
        } catch (Exception ignored) {
        }
        try {
            filter.setMinBaseVoltage(-3.0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            filter.setCountries(EnumSet.noneOf(Country.class));
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void apply() throws Exception {
        Network network = TestingNetworkFactory.create();

        LimitViolation line1Violation = new LimitViolation("LINE1", LimitViolationType.CURRENT, "", Integer.MAX_VALUE, 1000.0, 1, 1100.0, Branch.Side.ONE);
        LimitViolation line2Violation = new LimitViolation("LINE2", LimitViolationType.CURRENT, "", Integer.MAX_VALUE, 900.0, 1, 950.0, Branch.Side.TWO);
        LimitViolation vl1Violation = new LimitViolation("VL1", LimitViolationType.HIGH_VOLTAGE, 200.0, 1, 250.0);

        LimitViolationFilter filter = new LimitViolationFilter();
        List<LimitViolation> filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation), network);
        assertEquals(3, filteredViolations.size());

        filter = new LimitViolationFilter();
        filter.setViolationTypes(EnumSet.of(LimitViolationType.HIGH_VOLTAGE));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation), network);
        checkFilteredViolations(filteredViolations, network, "VL1", LimitViolationType.HIGH_VOLTAGE, 220.0, Country.FR, "VL1");

        filter = new LimitViolationFilter();
        filter.setMinBaseVoltage(300.0);
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation), network);
        checkFilteredViolations(filteredViolations, network, "LINE1", LimitViolationType.CURRENT, 380.0, Country.FR, "VL2");

        filter = new LimitViolationFilter();
        filter.setCountries(EnumSet.of(Country.BE));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation), network);
        checkFilteredViolations(filteredViolations, network, "LINE2", LimitViolationType.CURRENT, 220.0, Country.BE, "VL3");

        filter = new LimitViolationFilter(EnumSet.of(LimitViolationType.CURRENT), 300.0, EnumSet.of(Country.FR));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation), network);
        checkFilteredViolations(filteredViolations, network, "LINE1", LimitViolationType.CURRENT, 380.0, Country.FR, "VL2");
    }

    private void checkFilteredViolations(List<LimitViolation> filteredViolations, Network network, String equipmentId, LimitViolationType violationType,
                                         double baseVoltage, Country country, String voltageLevelId) {
        assertEquals(1, filteredViolations.size());

        LimitViolation violation = filteredViolations.get(0);
        assertEquals(equipmentId, violation.getSubjectId());
        assertEquals(violationType, violation.getLimitType());
        assertEquals(baseVoltage, LimitViolationHelper.getNominalVoltage(violation, network), 0.0);
        assertEquals(country, LimitViolationHelper.getCountry(violation, network));
        assertEquals(voltageLevelId, LimitViolationHelper.getVoltageLevelId(violation, network));
    }
}
