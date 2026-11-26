/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.AbstractModificationTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ReplaceIdsByNamesTest extends AbstractModificationTest {

    // A new, empty configuration is created before each test (on the file system created by the parent class),
    // so tests are isolated: each test sets in it only the parameter values it needs, all others take default values
    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        super.setUp();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @Test
    void roundTripBusBreaker() throws IOException {
        Network network = createBusBreakerNetwork();
        new ReplaceIdsByNames(platformConfig).apply(network);
        writeXmlTest(network, "/network-bus-breaker-replace-ids-by-names.xml");
    }

    @Test
    void roundTripNodeBreaker() throws IOException {
        Network network = Network.read("network-node-breaker-with-names.xml", getClass().getResourceAsStream("/network-node-breaker-with-names.xml"));
        new ReplaceIdsByNames(platformConfig).apply(network);
        writeXmlTest(network, "/network-node-breaker-replace-ids-by-names.xml");
    }

    @Test
    void postProcessor() throws IOException {
        // Uses the service loader on purpose to check the post-processor registration has been made.
        // It runs with the default platform config (empty in this module)
        Network read = Network.read("network-node-breaker-with-names.xml",
                getClass().getResourceAsStream("/network-node-breaker-with-names.xml"),
                LocalComputationManager.getDefault(),
                new ImportConfig(ReplaceIdsByNamesPostProcessor.NAME),
                new Properties());
        writeXmlTest(read, "/network-node-breaker-replace-ids-by-names.xml");
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new ReplaceIdsByNames();
        assertEquals(ReplaceIdsByNames.NAME, networkModification.getName());
    }

    @Test
    void testHasImpact() {
        ReplaceIdsByNames modification = new ReplaceIdsByNames();
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }

    @Test
    void defaultParametersWhenNoConfigModule() {
        Network network = createNetworkWithDuplicatedName();
        ReportNode reportNode = createReportNode();

        new ReplaceIdsByNames(platformConfig).apply(network, reportNode);

        // suffix-with-id-if-name-is-duplicated = true: duplicated names are suffixed with the original id
        assertNotNull(network.getGenerator("DUP_GEN"));
        assertNotNull(network.getLoad("DUP_LOAD"));
        assertEquals("GEN", network.getGenerator("DUP_GEN").getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID).orElseThrow());
        // report-skipped-changes = false: no report of skipped changes
        assertTrue(reportNode.getChildren().isEmpty());
    }

    @Test
    void parametersFromConfigModule() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(ReplaceIdsByNames.CONFIG_MODULE_NAME);
        moduleConfig.setStringProperty(ReplaceIdsByNames.CONFIG_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED, "false");
        moduleConfig.setStringProperty(ReplaceIdsByNames.CONFIG_REPORT_SKIPPED_CHANGES, "true");
        Network network = createNetworkWithDuplicatedName();
        ReportNode reportNode = createReportNode();

        new ReplaceIdsByNames(platformConfig).apply(network, reportNode);

        // suffix-with-id-if-name-is-duplicated = false: identifiables with duplicated names keep their id
        assertNotNull(network.getGenerator("GEN"));
        assertNotNull(network.getLoad("LOAD"));
        assertTrue(network.getGenerator("GEN").getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID).isEmpty());
        // report-skipped-changes = true: skipped changes are reported
        assertEquals(1, reportNode.getChildren().size());
        ReportNode skipped = reportNode.getChildren().getFirst();
        assertEquals(ReplaceIdsByNames.REPORT_SKIPPED_REPORT, skipped.getMessageKey());
        assertEquals("Changes of ids by names skipped", skipped.getMessage());
        List<String> messages = skipped.getChildren().stream().map(ReportNode::getMessage).toList();
        assertEquals(List.of(
                "GENERATOR GEN with name DUP was not changed. Reason is DUPLICATED_NAME",
                "LOAD LOAD with name DUP was not changed. Reason is DUPLICATED_NAME"),
                messages.stream().sorted().toList());
    }

    @Test
    void reportSkippedChangeOfIdentifiableWithoutName() {
        platformConfig.createModuleConfig(ReplaceIdsByNames.CONFIG_MODULE_NAME)
                .setStringProperty(ReplaceIdsByNames.CONFIG_REPORT_SKIPPED_CHANGES, "true");
        // Identifiables of this network have no name.
        // Give one of them an original-id alias: this skip reason is checked (and reported) before NO_NAME,
        // so the skipped change is reported for an identifiable without name
        Network network = EurostagTutorialExample1Factory.create();
        Generator gen = network.getGenerator("GEN");
        assertTrue(gen.getOptionalName().isEmpty());
        gen.addAlias("GEN_ORIGINAL", ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID);
        ReportNode reportNode = createReportNode();

        assertDoesNotThrow(() -> new ReplaceIdsByNames(platformConfig).apply(network, reportNode));

        assertNotNull(network.getGenerator("GEN"));
        ReportNode skipped = reportNode.getChildren().getFirst();
        ReportNode skippedGen = skipped.getChildren().stream()
                .filter(s -> "GEN".equals(s.getValue(ReplaceIdsByNames.REPORT_SKIPPED_ID).map(Object::toString).orElse(null)))
                .findFirst()
                .orElseThrow();
        assertEquals(ReplaceIdsByNames.ChangeSkippedReason.ORIGINAL_ID_ALIAS_ALREADY_PRESENT.name(),
                skippedGen.getValue(ReplaceIdsByNames.REPORT_SKIPPED_REASON).map(Object::toString).orElse(null));
    }

    @Test
    void uniqueEmptyNameIsSkipped() {
        // An empty name can not be used as id, so the change must be skipped
        Network network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").setName("");

        assertDoesNotThrow(() -> new ReplaceIdsByNames(platformConfig).apply(network));

        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        assertTrue(gen.getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID).isEmpty());
    }

    @Test
    void uniqueBlankNameIsSkipped() {
        // A blank name is considered as no name, so the change must be skipped
        Network network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").setName("  ");

        new ReplaceIdsByNames(platformConfig).apply(network);

        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        assertNull(network.getIdentifiable("  "));
        assertTrue(gen.getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID).isEmpty());
    }

    @Test
    void duplicatedEmptyNamesAreSkipped() {
        // Duplicated empty names must not be suffixed with the original id
        Network network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").setName("");
        network.getLoad("LOAD").setName("");

        new ReplaceIdsByNames(platformConfig).apply(network);

        assertNotNull(network.getGenerator("GEN"));
        assertNotNull(network.getLoad("LOAD"));
        assertNull(network.getIdentifiable("_GEN"));
        assertNull(network.getIdentifiable("_LOAD"));
        assertTrue(network.getGenerator("GEN").getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID).isEmpty());
        assertTrue(network.getLoad("LOAD").getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID).isEmpty());
    }

    @Test
    void noSkippedChangesReportWhenNothingIsSkipped() {
        platformConfig.createModuleConfig(ReplaceIdsByNames.CONFIG_MODULE_NAME)
                .setStringProperty(ReplaceIdsByNames.CONFIG_REPORT_SKIPPED_CHANGES, "true");
        // All identifiables have a unique name, different from their id: all changes are done
        Network network = createBusBreakerNetwork();
        ReportNode reportNode = createReportNode();

        new ReplaceIdsByNames(platformConfig).apply(network, reportNode);

        assertNotNull(network.getGenerator("NAME_GEN"));
        // The skipped changes report is created only if some skipped change is reported
        assertTrue(reportNode.getChildren().isEmpty());
    }

    private static Network createNetworkWithDuplicatedName() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").setName("DUP");
        network.getLoad("LOAD").setName("DUP");
        return network;
    }

    private static ReportNode createReportNode() {
        return ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
    }

    private static Network createBusBreakerNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        // create() does not set a case date, so it defaults to the creation time: fix it to get a stable output
        network.setCaseDate(ZonedDateTime.parse(EurostagTutorialExample1Factory.CASE_DATE));
        // This network does not have names,
        // Assign names based on the ids
        resetNames(network);
        return network;
    }

    private static void resetNames(Network network) {
        for (Identifiable<?> i : network.getIdentifiables()) {
            i.setName("NAME_" + i.getId());
        }
    }
}
