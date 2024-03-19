/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.dsl;

import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.contingency.*;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class GroovyContingencyListTest {
    private FileSystem fileSystem;

    private Path dslFile;

    private Network network;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dslFile = fileSystem.getPath("/test.groovy");
        network = EurostagTutorialExample1Factory.create();
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    private void writeToDslFile(String... lines) throws IOException {
        try (Writer writer = Files.newBufferedWriter(dslFile, StandardCharsets.UTF_8)) {
            writer.write(String.join(System.lineSeparator(), lines));
        }
    }

    @Test
    void test() throws IOException {
        writeToDslFile("contingency('c1') {",
                "    equipments 'NHV1_NHV2_1'",
                "}");
        ContingencyList contingencyList = ContingencyList.load(dslFile);
        assertEquals("test.groovy", contingencyList.getName());
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        Contingency contingency = contingencies.get(0);
        assertEquals("c1", contingency.getId());
        assertEquals(0, contingency.getExtensions().size());
        assertEquals(1, contingency.getElements().size());
        ContingencyElement element = contingency.getElements().iterator().next();
        assertTrue(element instanceof LineContingency);
        assertEquals("NHV1_NHV2_1", element.getId());
    }

    @Test
    void testOrder() throws IOException {
        writeToDslFile("contingency('c1') {",
                "    equipments 'NHV1_NHV2_1'",
                "}",
                "contingency('c2') {",
                "    equipments 'NHV1_NHV2_2'",
                "}");
        List<Contingency> contingencies = ContingencyList.load(dslFile)
                .getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals("c1", contingencies.get(0).getId());
        assertEquals("c2", contingencies.get(1).getId());
    }

    private static Set<String> getContingenciesNames(List<Contingency> contingencies) {
        return contingencies.stream().map(Contingency::getId).collect(Collectors.toSet());
    }

    @Test
    void testAutomaticList() throws IOException {
        writeToDslFile("for (l in network.lines) {",
                "    contingency(l.id) {",
                "        equipments l.id",
                "    }",
                "}");
        List<Contingency> contingencies = ContingencyList.load(dslFile)
                .getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(Sets.newHashSet("NHV1_NHV2_1", "NHV1_NHV2_2"), getContingenciesNames(contingencies));
    }

    private static String createAllBranchesDsl() {
        return String.join(System.lineSeparator(),
                "for (b in network.branches) {",
                "    contingency(b.id) {",
                "        equipments b.id",
                "    }",
                "}");
    }

    @Test
    void withComparison() throws IOException {
        writeToDslFile("for (l in network.lines) {",
                "    if (l.terminal1.voltageLevel.nominalV >= 380) {",
                "        contingency(l.id) { equipments l.id }",
                "    }",
                "}");
        List<Contingency> contingencies = ContingencyList.load(dslFile)
                .getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(Sets.newHashSet("NHV1_NHV2_1", "NHV1_NHV2_2"), getContingenciesNames(contingencies));

        writeToDslFile("for (l in network.lines) {",
                "    if (l.terminal1.voltageLevel.nominalV >= 500) {",
                "        contingency(l.id) { equipments l.id }",
                "    }",
                "}");
        contingencies = ContingencyList.load(dslFile)
                .getContingencies(network);
        assertTrue(contingencies.isEmpty());
    }

    @Test
    void testExtension() throws IOException {
        writeToDslFile(
                "contingency('test') {",
                "    equipments 'NHV1_NHV2_1'",
                "    probability {",
                "        base 0.1",
                "        tsName 'myTs'",
                "    }",
                "}");
        List<Contingency> contingencies = ContingencyList.load(dslFile)
                .getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(1, contingencies.get(0).getExtensions().size());
        assertEquals("myTs", contingencies.get(0).getExtension(ProbabilityContingencyExtension.class).getProbabilityTimeSeriesRef());
    }

    @Test
    void testElementsNotFound() throws IOException {
        writeToDslFile(
                "contingency('c1') {",
                "    equipments 'NHV1_NHV2_1'",
                "}",
                "contingency('c2') {",
                "    equipments 'NHV1_NHV2_2'",
                "}",
                "contingency('c3') {",
                "    equipments 'unknown'",
                "}"
        );
        List<Contingency> contingencies = ContingencyList.load(dslFile)
                .getContingencies(network);
        assertEquals(2, contingencies.size());
        Map<String, Set<String>> elementsNotFound = ((GroovyContingencyList) ContingencyList.load(dslFile)).getNotFoundElements(network);
        assertEquals(1, elementsNotFound.size());
        assertEquals(1, elementsNotFound.get("c3").size());
        assertTrue(elementsNotFound.get("c3").contains("unknown"));
    }
}
