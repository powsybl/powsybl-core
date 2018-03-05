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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

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

        contingency = new Contingency("contingency");
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        Network network = NetworkFactory.create("id", "test");

        CaseExporter exporter = new CaseExporter(tmpDir, "basename", "XIIDM", CompressionFormat.GZIP);

        // Export N state
        RunningContext runningContext = new RunningContext(network, null);
        runningContext.setRound(0);
        exporter.loadFlowConverged(runningContext, null);
        Path path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertTrue(Files.exists(path));

        runningContext.setRound(1);
        exporter.loadFlowDiverged(runningContext);
        path = tmpDir.resolve("basename-N-R1.xiidm.gz");
        assertTrue(Files.exists(path));

        // Export N-1 state
        RunningContext runningContext1 = new RunningContext(network, contingency);
        runningContext1.setRound(2);
        exporter.loadFlowConverged(runningContext1, null);
        path = tmpDir.resolve("basename-contingency-R2.xiidm.gz");
        assertTrue(Files.exists(path));

        runningContext1.setRound(3);
        exporter.loadFlowDiverged(runningContext1);
        path = tmpDir.resolve("basename-contingency-R3.xiidm.gz");
        assertTrue(Files.exists(path));
    }
}
