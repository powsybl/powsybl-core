/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.action.GeneratorAction;
import com.powsybl.action.PhaseTapChangerTapPositionAction;
import com.powsybl.action.ShuntCompensatorPositionAction;
import com.powsybl.action.SwitchAction;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.contingency.ThreeWindingsTransformerContingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.nc.model.NcDataset;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.io.NcDatasetReader;
import com.powsybl.security.monitor.StateMonitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
class NcConverterTest {

    private static final OffsetDateTime TIMESTAMP = OffsetDateTime.parse("2024-01-31T12:00:00Z");
    private static final String MONITORED_BRANCH = "FFR3AA1  FFR5AA1  1";
    private static final String MONITORED_TRANSFORMER = "BBE2AA1  BBE3AA1  1";

    private static Network network;
    private static NcDataset dataset;
    private static NcModel model;
    private static NcConversionResult result;

    @BeforeAll
    static void convertOnce() {
        network = readNetwork();
        dataset = readDataset();
        model = dataset.forTimestamp(TIMESTAMP);
        result = NcConverter.convert(model, network);
    }

    @AfterAll
    static void closeDataset() {
        dataset.close();
    }

    @Test
    void networkFixtureExposesVoltageLimitsAndOperationalLimitProperties() {
        assertEquals(395, network.getVoltageLevel("FFR3AA1").getLowVoltageLimit());
        assertEquals(405, network.getVoltageLevel("FFR3AA1").getHighVoltageLimit());
        assertTrue(network.getVoltageLevel("FFR3AA1").getProperty("CGMES.OperationalLimit_lowVoltageLimit")
            .contains("voltage-limit-terminal-min"));
        assertTrue(network.getVoltageLevel("FFR3AA1").getProperty("CGMES.OperationalLimit_highVoltageLimit")
            .contains("voltage-limit-busbar-max"));
    }

    @Test
    void resolvesAssessedElementConductingEquipment() {
        String voltageEquipmentId = model.getAssessedElements().stream()
            .filter(assessedElement -> "ae_voltage_level".equals(assessedElement.mrid()))
            .findFirst().orElseThrow().conductingEquipment();
        assertEquals(MONITORED_BRANCH, voltageEquipmentId);
    }

    @Test
    void convertsContingencies() {
        assertEquals(3, result.contingencyList().getContingencies(network).size());
        assertTrue(result.contingencyList().getContingencies(network).stream()
            .flatMap(contingency -> contingency.getElements().stream())
            .anyMatch(ThreeWindingsTransformerContingency.class::isInstance));
    }

    @Test
    void convertsPreventiveStateMonitor() {
        StateMonitor preventiveMonitor = monitors(ContingencyContextType.NONE).stream()
            .findFirst().orElseThrow();
        assertEquals(Set.of(MONITORED_BRANCH), preventiveMonitor.getBranchIds());
        assertEquals(Set.of("FFR3AA1"), preventiveMonitor.getVoltageLevelIds());
        assertEquals(Set.of(MONITORED_TRANSFORMER), preventiveMonitor.getThreeWindingsTransformerIds());
    }

    @Test
    void convertsPostContingencyStateMonitors() {
        List<StateMonitor> specific = monitors(ContingencyContextType.SPECIFIC);
        assertEquals(3, specific.size());
        assertEquals(3, countSpecificMonitorsOnBranch(MONITORED_BRANCH));
        assertEquals(1, countSpecificMonitorsOnBranch("FFR2AA1  FFR3AA1  1"));
        assertEquals(1, specific.stream()
            .filter(monitor -> monitor.getThreeWindingsTransformerIds().contains(MONITORED_TRANSFORMER))
            .count());
    }

    @Test
    void monitorsOnlyVoltageLevelsDefiningVoltageLimits() {
        assertEquals(Set.of("FFR3AA1"), result.stateMonitors().stream()
            .flatMap(monitor -> monitor.getVoltageLevelIds().stream())
            .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void convertsElementaryActions() {
        List<String> actionTypes = result.actionList().getActions().stream()
            .map(action -> action.getClass().getSimpleName())
            .toList();
        assertTrue(result.actionList().getActions().stream().anyMatch(SwitchAction.class::isInstance), actionTypes.toString());
        assertTrue(result.actionList().getActions().stream().anyMatch(GeneratorAction.class::isInstance), actionTypes.toString());
        assertTrue(result.actionList().getActions().stream().anyMatch(ShuntCompensatorPositionAction.class::isInstance),
            actionTypes.toString());
    }

    @Test
    void convertsAbsolutePhaseTapPositionAction() {
        assertTrue(result.actionList().getActions().stream()
            .filter(PhaseTapChangerTapPositionAction.class::isInstance)
            .map(PhaseTapChangerTapPositionAction.class::cast)
            .anyMatch(action -> action.getTapPosition() == 2 && !action.isRelativeValue()));
    }

    @Test
    void convertsOperatorStrategies() {
        assertTrue(result.operatorStrategyList().getOperatorStrategies().size() > 1);
    }

    @Test
    void reportsIgnoredSourceObjects() throws IOException {
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withResourceBundles(PowsyblCoreReportResourceBundle.BASE_NAME)
            .withMessageTemplate("core.nc.conversion.convertingNcModel")
            .build();

        NcConverter.convert(model, network, reportNode);

        StringWriter writer = new StringWriter();
        reportNode.print(writer);
        String report = writer.toString();
        assertTrue(report.contains("Converting NC model to security analysis inputs"), report);
        assertTrue(report.contains("Converting contingencies"), report);
        assertTrue(report.contains("Converting assessed elements"), report);
        assertTrue(report.contains("Converting remedial actions"), report);
        assertTrue(report.contains("Converting operator strategies"), report);
        // The fixture contains an assessed element combinable with any contingency, whose explicit
        // associations the converter drops. Such decisions must be visible in the report.
        assertTrue(report.contains("explicit contingency associations are ignored"), report);
    }

    private static List<StateMonitor> monitors(ContingencyContextType contextType) {
        return result.stateMonitors().stream()
            .filter(monitor -> monitor.getContingencyContext().getContextType() == contextType)
            .toList();
    }

    private static long countSpecificMonitorsOnBranch(String branchId) {
        return monitors(ContingencyContextType.SPECIFIC).stream()
            .filter(monitor -> monitor.getBranchIds().contains(branchId))
            .count();
    }

    private static Network readNetwork() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("TestCase16Nodes");
        dataSource.putData("TestCase16Nodes_EQ.xml", resource("TestCase16Nodes_EQ.xml"));
        dataSource.putData("TestCase16Nodes_TP.xml", resource("TestCase16Nodes_TP.xml"));
        return new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), new Properties());
    }

    private static InputStream resource(String fileName) {
        return NcConverterTest.class.getResourceAsStream("/" + fileName);
    }

    private static NcDataset readDataset() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("nc-profiles");
        dataSource.putData("RTE_AE.xml", resource("RTE_AE.xml"));
        dataSource.putData("RTE_CO.xml", resource("RTE_CO.xml"));
        dataSource.putData("RTE_RA.xml", resource("RTE_RA.xml"));
        return NcDatasetReader.read(dataSource);
    }
}
