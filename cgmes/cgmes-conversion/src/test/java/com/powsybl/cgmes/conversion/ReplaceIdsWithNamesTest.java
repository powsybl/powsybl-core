/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.ReplaceIdsByNames;
import com.powsybl.iidm.modification.ReplaceIdsByNames.ChangeSkippedReason;
import com.powsybl.iidm.modification.ReplaceIdsByNamesPostProcessor;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static com.powsybl.iidm.modification.ReplaceIdsByNames.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ReplaceIdsWithNamesTest {

    private FileSystem fileSystem;
    private ReplaceIdsByNamesPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        // Run the tests with a post-processor configured so skipped changes are reported, to be able to check the results
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        platformConfig.createModuleConfig(ReplaceIdsByNames.CONFIG_MODULE_NAME)
                .setStringProperty(ReplaceIdsByNames.CONFIG_REPORT_SKIPPED_CHANGES, "true");
        postProcessor = new ReplaceIdsByNamesPostProcessor(platformConfig);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testDisconnectedTerminal() {
        // A minimal network with a disconnected terminal
        testReplaceIdsByNames(new ResourceDataSource("disconnected_terminal",
                new ResourceSet("/issues/switches/",
                        "disconnected_terminal_EQ.xml",
                        "disconnected_terminal_SSH.xml")));
    }

    @Test
    void testImportRenameUpdateLoads() {
        // A minimal network with loads
        Network n = Network.read(new ResourceDataSource("update_load_eq",
                new ResourceSet("/update/load/", "load_EQ.xml")));
        Load l = n.getLoad("EnergyConsumer");
        l.setName("LOAD_NAME");
        Load a = n.getLoad("AsynchronousMachine");
        a.setName("ASYNC_MACHINE_NAME");

        // Rename the identifiers before calling the update
        new ReplaceIdsByNames().apply(n);

        // We can use either the original identifiers or the name, to locate the elements again
        assertNotNull(n.getLoad("EnergyConsumer"));
        assertNotNull(n.getLoad("AsynchronousMachine"));
        assertNotNull(n.getLoad("LOAD_NAME"));
        assertNotNull(n.getLoad("ASYNC_MACHINE_NAME"));

        // Update the network after being renamed, using SSH data with the original identifiers
        n.update(new ResourceDataSource("update_load_ssh",
                new ResourceSet("/update/load/", "load_SSH.xml")));

        // Check that values coming from SSH have been put in the elements of the network
        double epsilon = 0.001;
        assertEquals(10.0, l.getP0(), epsilon);
        assertEquals(5.0, l.getQ0(), epsilon);
        assertEquals(200.0, a.getP0(), epsilon);
        assertEquals(50.0, a.getQ0(), epsilon);
    }

    @Test
    void testImportRenameUpdateVoltages() {
        // A minimal bus/breaker network
        Network n = Network.read(new ResourceDataSource("bus_breaker_eq_tp",
                new ResourceSet("/update/voltage/", "bus_breaker_EQ.xml", "bus_breaker_TP.xml")));
        Load l = n.getLoad("EnergyConsumer");
        l.setName("LOAD_NAME");
        Bus b = n.getBusBreakerView().getBus("TopologicalNode1");
        // This bus already has BUS1_NAME in the input TP

        // Rename the identifiers before calling the update
        new ReplaceIdsByNames().apply(n);

        // We can use either the original identifiers or the name, to locate the elements again
        assertNotNull(n.getLoad("EnergyConsumer"));
        assertNotNull(n.getLoad("LOAD_NAME"));
        assertNotNull(n.getBusBreakerView().getBus("TopologicalNode1"));
        assertNotNull(n.getBusBreakerView().getBus("BUS1_NAME"));

        // Update the network after being renamed, using SV data with the original identifiers
        n.update(new ResourceDataSource("bus_breaker_sv",
                new ResourceSet("/update/voltage/", "bus_breaker_SV.xml")));

        // Check that values coming from SV have been put in the elements of the network
        double epsilon = 0.001;
        assertEquals(412.0, b.getV(), epsilon);
        assertEquals(-6.7, b.getAngle(), epsilon);
    }

    @Test
    @Disabled("slow test using a big network")
    void testSvedala() {
        testReplaceIdsByNames(Cgmes3Catalog.svedala().dataSource());
    }

    private void testReplaceIdsByNames(ReadOnlyDataSource ds) {
        // Import the CGMES case applying the replacement of ids by names as a post-processor
        ImportersLoaderList loader = new ImportersLoaderList(List.of(new CgmesImport()), List.of(postProcessor));
        ReportNode reportNode = newReport();
        Network network = Network.read(ds,
                LocalComputationManager.getDefault(),
                new ImportConfig(postProcessor.getName()),
                new Properties(),
                NetworkFactory.findDefault(),
                loader,
                reportNode);

        checkChangedIdentifiables(network);

        // The maximum of expected changes: all identifiables in the network except the network itself
        long maxExpectedChanges = network.getIdentifiables().size() - 1;

        // Some skipped changes are expected, at least for the fictitious switches created for disconnected terminals
        ReportNode skippedChanges = findSkippedChanges(reportNode);
        assertNotNull(skippedChanges);
        long skippedSize = skippedChanges.getChildren().size();
        assertTrue(skippedSize > 0);

        // Fictitious switches for disconnected terminals take their name from their terminal identifier.
        // And the terminal identifiers have been added as aliases of the related equipment.
        // Check that all situations where we have not been able to change the id because name was already an id
        // correspond to these elements.
        List<ReportNode> uniqueNameIsAlreadyAnId = skippedChanges.getChildren().stream()
                .filter(s -> ChangeSkippedReason.UNIQUE_NAME_IS_ALREADY_AN_ID.name().equals(reason(s)))
                .toList();
        assertFalse(uniqueNameIsAlreadyAnId.isEmpty(), "expected UNIQUE_NAME_IS_ALREADY_AN_ID skips for fictitious switches");
        assertTrue(uniqueNameIsAlreadyAnId.stream().allMatch(s -> id(s).equals(name(s) + "_SW_fict")),
                "unexpected UNIQUE_NAME_IS_ALREADY_AN_ID skip found, not a fictitious switch derived from a terminal");
        long countChanged = maxExpectedChanges - skippedSize;

        // If we try again over the already changed identifiers,
        // Now all identifiables (except the Network) must be skipped,
        // and there should be skipped changes with reason ORIGINAL_ID_ALIAS_ALREADY_PRESENT
        // (the ones successfully changed in the first replacement)
        ReportNode reportNode2 = newReport();
        postProcessor.process(network, LocalComputationManager.getDefault(), reportNode2);
        skippedChanges = findSkippedChanges(reportNode2);
        assertNotNull(skippedChanges);
        assertEquals(maxExpectedChanges, skippedChanges.getChildren().size());
        assertEquals(countChanged, skippedChanges.getChildren().stream()
                .filter(p -> ChangeSkippedReason.ORIGINAL_ID_ALIAS_ALREADY_PRESENT.name().equals(reason(p)))
                .count());
    }

    private ReportNode newReport() {
        return ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();
    }

    // Look for the skipped changes report by key, returns the first one found (depth-first) or null
    private static ReportNode findSkippedChanges(ReportNode reportNode) {
        for (ReportNode child : reportNode.getChildren()) {
            if (REPORT_SKIPPED_REPORT.equals(child.getMessageKey())) {
                return child;
            }
            ReportNode found = findSkippedChanges(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private String reason(ReportNode skippedChange) {
        return reportValue(skippedChange, REPORT_SKIPPED_REASON);
    }

    private String id(ReportNode skippedChange) {
        return reportValue(skippedChange, REPORT_SKIPPED_ID);
    }

    private String name(ReportNode skippedChange) {
        return reportValue(skippedChange, REPORT_SKIPPED_NAME);
    }

    private String reportValue(ReportNode skippedChange, String key) {
        return skippedChange.getValue(key).map(Object::toString).orElse(null);
    }

    private void checkChangedIdentifiables(Network network) {
        boolean changed = false;
        for (Identifiable<?> i : network.getIdentifiables()) {
            // An identifiable has been changed if it has an alias for the original id
            Optional<String> originalId = i.getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID);
            if (originalId.isEmpty()) {
                continue;
            }
            changed = true;

            // The original id must still resolve to the same object, now through the alias
            assertSame(i, network.getIdentifiable(originalId.get()));

            // And this object must have a name, and its id must be the name,
            // or the name suffixed with the original id if the name is duplicated
            String name = i.getOptionalName().orElseThrow();
            assertTrue(i.getId().equals(name) || i.getId().equals(name + "_" + originalId.get()),
                    () -> "unexpected id " + i.getId() + " for name " + name + " and original id " + originalId.get());
        }
        assertTrue(changed, "No identifiables were changed");
    }
}
