/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.violations;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LimitViolationFilterTest {

    private static final String DC_CONVERTER = "VscFr";
    private static final String DC_LINE = "dcLinePos";
    private static final String DC_CONVERTER_SUBSTATION = "SDC-FR-" + DcDetailedNetworkFactory.X_NODE_DC_1_FR;
    private static final String DC_CONVERTER_VOLTAGE_LEVEL = DcDetailedNetworkFactory.getVoltageLevelId(Country.FR,
            DcDetailedNetworkFactory.X_NODE_DC_1_FR, DcDetailedNetworkFactory.SUFFIX_150);

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("limit-violation-default-filter");
        moduleConfig.setStringListProperty("violationTypes", Arrays.asList("CURRENT", "LOW_VOLTAGE"));
        moduleConfig.setStringProperty("minBaseVoltage", "150");
        moduleConfig.setStringListProperty("countries", Arrays.asList("FR", "BE"));
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void load() throws Exception {
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
    }

    @Test
    void apply() throws Exception {
        Network network = TestingNetworkFactory.create();
        network.newVoltageAngleLimit()
                .setId("val")
                .setHighLimit(0.25)
                .from(network.getLine("LINE1").getTerminal1())
                .to(network.getLine("LINE1").getTerminal2())
                .add();

        LimitViolation line1Violation = new LimitViolationBuilder()
            .subject("LINE1")
            .type(LimitViolationType.CURRENT)
            .limitName("")
            .limit(1000.0)
            .value(1100.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation line2Violation = LimitViolation.builder()
            .subject("LINE2")
            .type(LimitViolationType.CURRENT)
            .limitName("")
            .duration(Integer.MAX_VALUE)
            .limit(900.0)
            .value(950.0)
            .side(TwoSides.TWO)
            .build();
        LimitViolation vl1Violation = LimitViolation.builder()
            .subject("VL1")
            .type(LimitViolationType.HIGH_VOLTAGE)
            .limit(200.0)
            .value(250.0)
            .build();
        LimitViolation line1ViolationAcP = LimitViolation.builder()
            .subject("VL1")
            .type(LimitViolationType.ACTIVE_POWER)
            .limitName("")
            .limit(200.0)
            .value(250.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolation line1ViolationApP = LimitViolation.builder()
            .subject("VL1")
            .type(LimitViolationType.APPARENT_POWER)
            .limitName("")
            .limit(200.0)
            .scaling(1)
            .value(250.0)
            .side(TwoSides.TWO)
            .build();
        LimitViolation voltageAngleLimit = LimitViolation.builder()
            .subject("val")
            .type(LimitViolationType.HIGH_VOLTAGE_ANGLE)
            .limitName("")
            .limit(0.25)
            .value(0.26)
            .build();

        LimitViolationFilter filter = new LimitViolationFilter();
        List<LimitViolation> filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation, line1ViolationAcP, line1ViolationApP, voltageAngleLimit), network);
        assertEquals(6, filteredViolations.size());

        filter = new LimitViolationFilter();
        filter.setViolationTypes(EnumSet.of(LimitViolationType.HIGH_VOLTAGE));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation), network);
        checkFilteredViolations(filteredViolations, network, "VL1", LimitViolationType.HIGH_VOLTAGE, 220.0, Country.FR, "VL1");

        filter = new LimitViolationFilter();
        filter.setViolationTypes(EnumSet.of(LimitViolationType.ACTIVE_POWER));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, vl1Violation, line1ViolationAcP), network);
        checkFilteredViolations(filteredViolations, network, "VL1", LimitViolationType.ACTIVE_POWER, 220.0, Country.FR, "VL1");

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

        filter = new LimitViolationFilter();
        filter.setViolationTypes(EnumSet.of(LimitViolationType.HIGH_VOLTAGE_ANGLE));
        filteredViolations = filter.apply(Arrays.asList(line1Violation, line2Violation, voltageAngleLimit), network);
        checkFilteredViolations(filteredViolations, network, "val", LimitViolationType.HIGH_VOLTAGE_ANGLE, 380.0, Country.FR, "VL2");
    }

    private void checkFilteredViolations(List<LimitViolation> filteredViolations, Network network, String equipmentId, LimitViolationType violationType,
                                         double baseVoltage, Country country, String voltageLevelId) {
        assertEquals(1, filteredViolations.size());

        LimitViolation violation = filteredViolations.get(0);
        assertEquals(equipmentId, violation.getSubjectId());
        assertEquals(violationType, violation.getLimitType());
        assertEquals(baseVoltage, LimitViolationHelper.getNominalVoltage(violation, network), 0.0);
        assertEquals(country, LimitViolationHelper.getCountry(violation, network).orElse(null));
        assertEquals(voltageLevelId, LimitViolationHelper.getVoltageLevelId(violation, network));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Detailed DC violations
    //
    // In the fixture, the AC terminal 1 of converter VscFr is in voltage level VLDC-FR-xNodeDc1fr-150 (150 kV), while
    // its PCC terminal is in voltage level VLDC-FR-xNodeDc1fr-400 (400 kV). Their substation has no country, unless a
    // test sets one.
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void detailedDcConverterViolationIsLocatedAtAcTerminalOne() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        network.getSubstation(DC_CONVERTER_SUBSTATION).setCountry(Country.FR);
        LimitViolation converterViolation = createDcConverterViolation();

        assertEquals(DC_CONVERTER_VOLTAGE_LEVEL, LimitViolationHelper.getVoltageLevelId(converterViolation, network));
        // 150 kV (AC terminal 1), not 400 kV (PCC terminal)
        assertEquals(150.0, LimitViolationHelper.getNominalVoltage(converterViolation, network), 0.0);
        assertEquals(Optional.of(Country.FR), LimitViolationHelper.getCountry(converterViolation, network));
    }

    @Test
    void detailedDcNodeAndLineViolationsHaveNoVoltageLevel() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

        for (LimitViolation violation : List.of(createDcNodeViolation(), createDcLineViolation())) {
            assertNull(LimitViolationHelper.getVoltageLevelId(violation, network));
            assertNull(LimitViolationHelper.getVoltageLevelId(violation, network, true));
            assertTrue(Double.isNaN(LimitViolationHelper.getNominalVoltage(violation, network)));
            assertEquals(Optional.empty(), LimitViolationHelper.getCountry(violation, network));
        }
    }

    @Test
    void applyMinBaseVoltageOnDetailedDcViolations() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        List<LimitViolation> violations = createDetailedDcViolations();
        List<String> all = List.of(DC_CONVERTER, DC_LINE, DcDetailedNetworkFactory.DC_NODE_FR_POS);

        // The default filter and an explicit 0 kV threshold keep every detailed DC violation
        assertEquals(all, subjects(new LimitViolationFilter().apply(violations, network)));
        assertEquals(all, subjects(new LimitViolationFilter().setMinBaseVoltage(0.0).apply(violations, network)));

        // Any threshold strictly greater than 0 removes the DC node and DC line violations
        assertEquals(List.of(DC_CONVERTER), subjects(new LimitViolationFilter().setMinBaseVoltage(1e-6).apply(violations, network)));

        // The converter violation is filtered on its AC terminal 1 (150 kV), not on its PCC terminal (400 kV)
        assertEquals(List.of(DC_CONVERTER), subjects(new LimitViolationFilter().setMinBaseVoltage(150.0).apply(violations, network)));
        assertEquals(List.of(), subjects(new LimitViolationFilter().setMinBaseVoltage(200.0).apply(violations, network)));
    }

    @Test
    void applyCountriesOnDetailedDcViolations() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        List<LimitViolation> violations = createDetailedDcViolations();

        // The converter substation has no country: an explicit empty set keeps the converter violation, as for AC
        // equipment, but removes the DC node and DC line violations
        assertEquals(List.of(DC_CONVERTER), subjects(new LimitViolationFilter().setCountries(Set.of()).apply(violations, network)));

        // Any explicit non-empty set also removes the DC node and DC line violations
        network.getSubstation(DC_CONVERTER_SUBSTATION).setCountry(Country.FR);
        assertEquals(List.of(DC_CONVERTER), subjects(new LimitViolationFilter().setCountries(EnumSet.of(Country.FR)).apply(violations, network)));
        assertEquals(List.of(), subjects(new LimitViolationFilter().setCountries(EnumSet.of(Country.BE)).apply(violations, network)));
    }

    private static LimitViolation createDcNodeViolation() {
        return LimitViolation.builder()
            .subject(DcDetailedNetworkFactory.DC_NODE_FR_POS)
            .type(LimitViolationType.HIGH_VOLTAGE)
            .operationalLimitsGroupId(null)
            .limit(525.0)
            .value(530.0)
            .build();
    }

    private static LimitViolation createDcLineViolation() {
        return LimitViolation.builder()
            .subject(DC_LINE)
            .type(LimitViolationType.CURRENT)
            .operationalLimitsGroupId(null)
            .limit(1000.0)
            .value(-1200.0)
            .side(TwoSides.ONE)
            .build();
    }

    private static LimitViolation createDcConverterViolation() {
        return LimitViolation.builder()
            .subject(DC_CONVERTER)
            .type(LimitViolationType.ACTIVE_POWER)
            .operationalLimitsGroupId(null)
            .limit(200.0)
            .value(250.0)
            .side(TwoSides.ONE)
            .build();
    }

    private static List<LimitViolation> createDetailedDcViolations() {
        return List.of(createDcNodeViolation(), createDcLineViolation(), createDcConverterViolation());
    }

    private static List<String> subjects(List<LimitViolation> violations) {
        return violations.stream().map(LimitViolation::getSubjectId).sorted().toList();
    }
}
