/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import eu.itesla_project.iidm.network.Country;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolationFilterTest {

    private FileSystem fileSystem;
    private MapModuleConfig moduleConfig;
    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = platformConfig.createModuleConfig("limit-violation-default-filter");
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
        assertEquals(150f, filter.getMinBaseVoltage(), 0f);
        assertEquals(EnumSet.of(Country.FR, Country.BE), filter.getCountries());
        filter.setViolationTypes(EnumSet.of(LimitViolationType.HIGH_VOLTAGE));
        assertEquals(EnumSet.of(LimitViolationType.HIGH_VOLTAGE), filter.getViolationTypes());
        filter.setMinBaseVoltage(225f);
        filter.setCountries(EnumSet.of(Country.FR));
        assertEquals(EnumSet.of(Country.FR), filter.getCountries());
        assertEquals(225f, filter.getMinBaseVoltage(), 0f);
        filter.setViolationTypes(null);
        assertNull(filter.getViolationTypes());
        filter.setCountries(null);
        assertNull(filter.getCountries());
        try {
            filter.setViolationTypes(EnumSet.noneOf(LimitViolationType.class));
            fail();
        } catch (Exception ignored) {
        }
        try {
            filter.setMinBaseVoltage(-3f);
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
        LimitViolation line1Violation = new LimitViolation("line1", LimitViolationType.CURRENT, 1000f, "", 1, 1100f, Country.FR, 380f);
        LimitViolation line2Violation = new LimitViolation("line2", LimitViolationType.CURRENT, 900f, "", 1, 950f, Country.BE, 220f);
        LimitViolation voltageLeve11Violation = new LimitViolation("voltageLeve11", LimitViolationType.HIGH_VOLTAGE, 200f, "", 1, 250f, Country.FR, 220f);
        
        LimitViolationFilter filter = new LimitViolationFilter();
        List<LimitViolation> filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, voltageLeve11Violation));
        assertEquals(3, filteredViolations.size());
        
        filter = new LimitViolationFilter();
        filter.setViolationTypes(EnumSet.of(LimitViolationType.HIGH_VOLTAGE));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, voltageLeve11Violation));
        checkFilteredViolations(filteredViolations, "voltageLeve11", LimitViolationType.HIGH_VOLTAGE, 220f, Country.FR);
        
        filter = new LimitViolationFilter();
        filter.setMinBaseVoltage(300f);
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, voltageLeve11Violation));
        checkFilteredViolations(filteredViolations, "line1", LimitViolationType.CURRENT, 380f, Country.FR);
        
        filter = new LimitViolationFilter();
        filter.setCountries(EnumSet.of(Country.BE));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, voltageLeve11Violation));
        checkFilteredViolations(filteredViolations, "line2", LimitViolationType.CURRENT, 220f, Country.BE);
        
        filter = new LimitViolationFilter(EnumSet.of(LimitViolationType.CURRENT), 300f, EnumSet.of(Country.FR));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, voltageLeve11Violation));
        checkFilteredViolations(filteredViolations, "line1", LimitViolationType.CURRENT, 380f, Country.FR);
        
    }
    
    private void checkFilteredViolations(List<LimitViolation> filteredViolations, String equipmentId, LimitViolationType violationType, 
                                         float baseVoltage, Country country) {
        assertEquals(1, filteredViolations.size());
        assertEquals(equipmentId, filteredViolations.get(0).getSubjectId());
        assertEquals(violationType, filteredViolations.get(0).getLimitType());
        assertEquals(baseVoltage, filteredViolations.get(0).getBaseVoltage(), 0f);
        assertEquals(country, filteredViolations.get(0).getCountry());
    }
}
