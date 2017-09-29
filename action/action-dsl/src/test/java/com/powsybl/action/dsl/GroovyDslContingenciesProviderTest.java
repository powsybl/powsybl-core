/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.dsl;

import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import eu.itesla_project.contingency.BranchContingency;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
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
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyDslContingenciesProviderTest {

    private FileSystem fileSystem;

    private Path dslFile;

    private Network network;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dslFile = fileSystem.getPath("/test.dsl");
        network = EurostagTutorialExample1Factory.create();
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() throws IOException {
        try (Writer writer = Files.newBufferedWriter(dslFile, StandardCharsets.UTF_8)) {
            writer.write(String.join(System.lineSeparator(),
                    "contingency('c1') {",
                    "    equipments 'NHV1_NHV2_1'",
                    "}"));
        }
        List<Contingency> contingencies = new GroovyDslContingenciesProvider(dslFile)
                .getContingencies(network);
        assertEquals(1, contingencies.size());
        Contingency contingency = contingencies.get(0);
        assertEquals("c1", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        ContingencyElement element = contingency.getElements().iterator().next();
        assertTrue(element instanceof BranchContingency);
        assertEquals("NHV1_NHV2_1", element.getId());
    }

    @Test
    public void testAutomaticList() throws IOException {
        try (Writer writer = Files.newBufferedWriter(dslFile, StandardCharsets.UTF_8)) {
            writer.write(String.join(System.lineSeparator(),
                    "for (l in network.lines) {",
                    "    contingency(l.id) {",
                    "        equipments l.id",
                    "    }",
                    "}"));
        }
        List<Contingency> contingencies = new GroovyDslContingenciesProvider(dslFile)
                .getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(Sets.newHashSet("NHV1_NHV2_1", "NHV1_NHV2_2"), contingencies.stream().map(Contingency::getId).collect(Collectors.toSet()));
    }
}
