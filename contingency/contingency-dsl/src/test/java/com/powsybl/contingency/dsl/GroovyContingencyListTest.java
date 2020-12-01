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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class GroovyContingencyListTest {
    private FileSystem fileSystem;

    private Path dslFile;

    private Network network;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dslFile = fileSystem.getPath("/test.groovy");
        network = EurostagTutorialExample1Factory.create();
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private void writeToDslFile(String... lines) throws IOException {
        try (Writer writer = Files.newBufferedWriter(dslFile, StandardCharsets.UTF_8)) {
            writer.write(String.join(System.lineSeparator(), lines));
        }
    }

    @Test
    public void test() throws IOException {
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
    public void testOrder() throws IOException {
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
    public void testAutomaticList() throws IOException {
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
    public void withComparison() throws IOException {
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
    public void testExtension() throws IOException {
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
}
