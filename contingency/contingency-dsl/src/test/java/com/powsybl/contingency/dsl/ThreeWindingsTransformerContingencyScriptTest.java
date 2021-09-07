/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.dsl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.ThreeWindingsTransformerContingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ThreeWindingsTransformerContingencyScriptTest {

    private FileSystem fileSystem;

    private Path dslFile;

    private Network network;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dslFile = fileSystem.getPath("/test.dsl");
        network = ThreeWindingsTransformerNetworkFactory.create();
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
        writeToDslFile("contingency('3WT_CONTINGENCY') {",
                "    equipments '3WT'",
                "}");
        List<Contingency> contingencies = new GroovyDslContingenciesProvider(dslFile)
                .getContingencies(network);
        assertEquals(1, contingencies.size());
        Contingency contingency = contingencies.get(0);
        assertEquals("3WT_CONTINGENCY", contingency.getId());
        assertEquals(0, contingency.getExtensions().size());
        assertEquals(1, contingency.getElements().size());
        ContingencyElement element = contingency.getElements().iterator().next();
        assertTrue(element instanceof ThreeWindingsTransformerContingency);
        assertEquals("3WT", element.getId());
    }
}
