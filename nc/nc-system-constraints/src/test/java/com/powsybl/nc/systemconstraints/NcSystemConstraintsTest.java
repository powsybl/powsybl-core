/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.nc.model.NcDataset;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.io.NcDatasetReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
class NcSystemConstraintsTest {
    private static final OffsetDateTime PROFILE_TIMESTAMP = OffsetDateTime.parse("2024-01-31T12:00:00Z");

    @Test
    void readsVoltageAngleLimitsAsModelExtension() throws Exception {
        try (NcDataset dataset = read("/profiles/voltage-angle-limit", "RTE_ER.xml", "RTE_SSI.xml")) {
            NcModel baselineModel = dataset.getModel();
            NcSystemConstraints baselineConstraints = baselineModel.getExtension(NcSystemConstraints.class)
                .orElseThrow();

            assertEquals(60d, voltageAngleLimitValue(baselineConstraints, "voltage-angle-limit-1"));

            NcModel modelWithOverride = baselineModel.forTimestamp(PROFILE_TIMESTAMP);
            NcSystemConstraints constraintsWithOverride = modelWithOverride.getExtension(NcSystemConstraints.class)
                .orElseThrow();

            assertEquals(13, constraintsWithOverride.getVoltageAngleLimits().size());
            assertEquals(55d, voltageAngleLimitValue(constraintsWithOverride, "voltage-angle-limit-1"));
            assertTrue(modelWithOverride.getAssessedElements().isEmpty());
            assertTrue(modelWithOverride.getGridStateAlterationRemedialActions().isEmpty());
        }
    }

    private static double voltageAngleLimitValue(NcSystemConstraints constraints, String mrid) {
        return constraints.getVoltageAngleLimits().stream()
            .filter(limit -> mrid.equals(limit.mrid()))
            .findFirst()
            .orElseThrow()
            .value();
    }

    @Test
    void readsPowerTransferCorridor() throws Exception {
        try (NcDataset dataset = read("/profiles/power-transfer-corridor",
            "RTE_ER.xml", "RTE_SSI.xml")) {
            NcModel model = dataset.forTimestamp(OffsetDateTime.parse("2029-01-10T00:30:00Z"));
            NcSystemConstraints systemConstraints = model.getExtension(NcSystemConstraints.class).orElseThrow();
            NcPowerTransferCorridor corridor = systemConstraints.getPowerTransferCorridors().stream().findFirst().orElseThrow();

            assertEquals(1, systemConstraints.getPowerTransferCorridors().size());
            assertEquals("power-transfer-corridor-1", corridor.mrid());
            assertEquals("Test power transfer corridor", corridor.name());
            assertFalse(corridor.enabled());
            assertEquals(2, corridor.infeedLimits().size());

            Map<Boolean, NcPowerTransferCorridor.InfeedLimit> limitsByMinimum = corridor.infeedLimits().stream()
                .collect(Collectors.toMap(NcPowerTransferCorridor.InfeedLimit::minimum, Function.identity()));
            NcPowerTransferCorridor.InfeedLimit minimum = limitsByMinimum.get(true);
            NcPowerTransferCorridor.InfeedLimit maximum = limitsByMinimum.get(false);
            assertEquals(0d, minimum.valueW());
            assertEquals(650d, maximum.valueW());
            assertEquals("minimum-main-terminal", minimum.mainTerminal());
            assertEquals("maximum-main-terminal", maximum.mainTerminal());
            assertEquals(2, minimum.infeedTerminals().size());
            assertEquals(2, maximum.infeedTerminals().size());
            assertEquals(Set.of("minimum-main-terminal", "minimum-terminal-1", "minimum-terminal-2"),
                minimum.terminals());
            assertEquals(Set.of("maximum-main-terminal", "maximum-terminal-1", "maximum-terminal-2"),
                maximum.terminals());
        }
    }

    @Test
    void ignoresInfeedLimitReferencingUnknownCorridor() throws Exception {
        String resource = "/profiles/power-transfer-corridor";
        var resourceUrl = NcSystemConstraintsTest.class.getResource(resource);
        assertNotNull(resourceUrl);
        Map<String, String> entries = readEntries(Path.of(resourceUrl.toURI()),
            "RTE_ER.xml", "RTE_SSI.xml");
        entries.computeIfPresent("RTE_ER.xml", (name, content) -> content.replace(
            "<nc:OperationalLimitSet.PowerTransferCorridor rdf:resource=\"#_power-transfer-corridor-1\"/>",
            "<nc:OperationalLimitSet.PowerTransferCorridor rdf:resource=\"#_unknown-corridor\"/>"));

        try (NcDataset dataset = NcDatasetReader.read(dataSource(entries))) {
            NcSystemConstraints constraints = dataset.forTimestamp(OffsetDateTime.parse("2029-01-10T00:30:00Z"))
                .getExtension(NcSystemConstraints.class).orElseThrow();

            assertTrue(constraints.getPowerTransferCorridors().isEmpty());
        }
    }

    private static NcDataset read(String resource, String... fileNames) throws Exception {
        var resourceUrl = NcSystemConstraintsTest.class.getResource(resource);
        assertNotNull(resourceUrl);
        return NcDatasetReader.read(dataSource(readEntries(Path.of(resourceUrl.toURI()), fileNames)));
    }

    private static Map<String, String> readEntries(Path directory, String... fileNames) throws Exception {
        Map<String, String> entries = new TreeMap<>();
        for (String fileName : fileNames) {
            entries.put(fileName, Files.readString(directory.resolve(fileName)));
        }
        return entries;
    }

    private static ReadOnlyMemDataSource dataSource(Map<String, String> entries) {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("nc-profiles");
        entries.forEach((name, content) -> dataSource.putData(name, content.getBytes(StandardCharsets.UTF_8)));
        return dataSource;
    }
}
