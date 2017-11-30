/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyImpl;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class CaseExporterTest {

    private FileSystem fileSystem;

    private Path tmpDir;

    private Contingency contingency;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));

        contingency = new ContingencyImpl("contingency", Collections.emptyList());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() throws IOException {
        Network network = NetworkFactory.create("id", "test");

        CaseExporter exporter = new CaseExporter(tmpDir, "basename", "XIIDM", CompressionFormat.GZIP);

        // Export N state
        exporter.loadFlowConverged(null, null, network, 0);
        Path path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertTrue(Files.exists(path));

        exporter.loadFlowDiverged(null, network, 1);
        path = tmpDir.resolve("basename-N-R1.xiidm.gz");
        assertTrue(Files.exists(path));

        // Export N-1 state
        exporter.loadFlowConverged(contingency, null, network, 2);
        path = tmpDir.resolve("basename-contingency-R2.xiidm.gz");
        assertTrue(Files.exists(path));

        exporter.loadFlowDiverged(contingency, network, 3);
        path = tmpDir.resolve("basename-contingency-R3.xiidm.gz");
        assertTrue(Files.exists(path));
    }
}
