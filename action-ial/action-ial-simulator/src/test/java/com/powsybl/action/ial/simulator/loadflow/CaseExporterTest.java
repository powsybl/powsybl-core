/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.ExportersLoader;
import com.powsybl.iidm.network.ExportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.XMLExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class CaseExporterTest {

    private FileSystem fileSystem;

    private Path tmpDir;

    private Contingency contingency;

    private Network network;

    private final ExportersLoader loader = new ExportersLoaderList(new XMLExporter());

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));

        contingency = new Contingency("contingency");
        network = Network.create("id", "test");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testExportEachRound() throws IOException {
        CaseExporter exporter = new CaseExporter(tmpDir, "basename", "XIIDM", CompressionFormat.GZIP, true, loader);

        // Export N state
        RunningContext runningContext = new RunningContext(network, null);

        // stop when loadflow diverges at round 1
        runningContext.setRound(0);
        exporter.loadFlowConverged(runningContext, null);
        Path path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        runningContext.setRound(1);
        exporter.loadFlowDiverged(runningContext);
        path = tmpDir.resolve("basename-N-R1.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        // stop when no more violations are found at round 0
        runningContext.setRound(0);
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        exporter.noMoreViolations(runningContext);
        assertFalse(Files.exists(path));

        // stop when no more actions are available at round 0
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        exporter.violationsAnymoreAndNoRulesMatch(runningContext);
        assertFalse(Files.exists(path));

        // stop when max iterations are reached at round 1
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        runningContext.setRound(1);
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R1.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        exporter.maxIterationsReached(runningContext);
        assertFalse(Files.exists(path));

        // Export N-1 state
        RunningContext runningContext1 = new RunningContext(network, contingency);
        runningContext1.setRound(0);
        exporter.loadFlowConverged(runningContext1, null);
        path = tmpDir.resolve("basename-contingency-R0.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        runningContext1.setRound(1);
        exporter.loadFlowDiverged(runningContext1);
        path = tmpDir.resolve("basename-contingency-R1.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @Test
    void testExportOnlyLastRound() throws IOException {
        CaseExporter exporter = new CaseExporter(tmpDir, "basename", "XIIDM", CompressionFormat.GZIP, false, loader);

        // Export N state
        RunningContext runningContext = new RunningContext(network, null);

        // stop when loadflow diverges at round 1
        runningContext.setRound(0);
        exporter.loadFlowConverged(runningContext, null);
        Path path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertFalse(Files.exists(path));

        runningContext.setRound(1);
        exporter.loadFlowDiverged(runningContext);
        path = tmpDir.resolve("basename-N-R1.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);

        // stop when no more violations are found at round 0
        runningContext.setRound(0);
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertFalse(Files.exists(path));

        exporter.noMoreViolations(runningContext);
        assertTrue(Files.exists(path));
        Files.delete(path);

        // stop when no more actions are available at round 0
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertFalse(Files.exists(path));

        exporter.violationsAnymoreAndNoRulesMatch(runningContext);
        assertTrue(Files.exists(path));
        Files.delete(path);

        // stop when max iterations are reached at round 1
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R0.xiidm.gz");
        assertFalse(Files.exists(path));

        runningContext.setRound(1);
        exporter.loadFlowConverged(runningContext, null);
        path = tmpDir.resolve("basename-N-R1.xiidm.gz");
        assertFalse(Files.exists(path));

        exporter.maxIterationsReached(runningContext);
        assertTrue(Files.exists(path));
        Files.delete(path);

        // Export N-1 state
        RunningContext runningContext1 = new RunningContext(network, contingency);
        runningContext1.setRound(0);
        exporter.loadFlowConverged(runningContext1, null);
        path = tmpDir.resolve("basename-contingency-R0.xiidm.gz");
        assertFalse(Files.exists(path));

        runningContext1.setRound(1);
        exporter.loadFlowDiverged(runningContext1);
        path = tmpDir.resolve("basename-contingency-R1.xiidm.gz");
        assertTrue(Files.exists(path));
        Files.delete(path);
    }
}
