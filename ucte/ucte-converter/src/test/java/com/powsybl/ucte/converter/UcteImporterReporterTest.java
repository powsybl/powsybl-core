/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.reporter.ReporterModelDeserializer;
import com.powsybl.commons.reporter.ReporterModelSerializer;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class UcteImporterReporterTest extends AbstractConverterTest {

    private static final String WORK_DIR = "/tmp";

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n").replace("\r", "\n");
    }

    @Test
    public void testReportVoltageRegulatingXnode() throws Exception {
        ReadOnlyDataSource dataSource = new ResourceDataSource("frVoltageRegulatingXnode", new ResourceSet("/", "frVoltageRegulatingXnode.uct"));

        ReporterModel reporter = new ReporterModel("testReportVoltageRegulatingXnode", "Test importing UCTE file ${file}",
            Map.of("file", new TypedValue("frVoltageRegulatingXnode.uct", TypedValue.FILENAME)));
        new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null, reporter);

        StringWriter sw = new StringWriter();
        reporter.export(sw);

        InputStream refStream = getClass().getResourceAsStream("/frVoltageRegulatingXnodeReport.txt");
        String refLogExport = normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

    @Test
    public void roundTripReporterJsonTest() throws Exception {
        String filename = "frVoltageRegulatingXnode.uct";
        ReporterModel reporter = new ReporterModel("roundTripReporterJsonTest", "Test importing UCTE file ${file}",
            Map.of("file", new TypedValue("frVoltageRegulatingXnode.uct", TypedValue.FILENAME)));
        Importers.loadNetwork(filename, getClass().getResourceAsStream("/" + filename), reporter);
        roundTripTest(reporter, ReporterModelSerializer::write, ReporterModelDeserializer::read, "/frVoltageRegulatingXnodeReport.json");
    }

    @Test
    public void roundTripReporterJsonParallelImportTest() throws InterruptedException, ExecutionException, IOException {
        Path workDir = Files.createDirectory(fileSystem.getPath(WORK_DIR));
        Files.copy(getClass().getResourceAsStream("/frVoltageRegulatingXnode.uct"), fileSystem.getPath(WORK_DIR, "frVoltageRegulatingXnode.uct"));
        Files.copy(getClass().getResourceAsStream("/frTestGridForMerging.uct"), fileSystem.getPath(WORK_DIR, "frTestGridForMerging.uct"));
        Files.copy(getClass().getResourceAsStream("/germanTsos.uct"), fileSystem.getPath(WORK_DIR, "germanTsos.uct"));

        List<Network> networkList = Collections.synchronizedList(new ArrayList<>());
        ReporterModel reporter = new ReporterModel("importAllParallel", "Test importing UCTE files in parallel: ${file1}, ${file2}, ${file3}",
            Map.of("file1", new TypedValue("frVoltageRegulatingXnode.uct", TypedValue.FILENAME),
                "file2", new TypedValue("frTestGridForMerging.uct", TypedValue.FILENAME),
                "file3", new TypedValue("germanTsos.uct", TypedValue.FILENAME)));
        Importers.importAll(workDir, new UcteImporter(), true, null, networkList::add, null, reporter);
        assertEquals(3, networkList.size());
        assertEquals(3, reporter.getSubReporters().size());

        roundTripTest(reporter, ReporterModelSerializer::write, ReporterModelDeserializer::read, "/parallelUcteImportReport.json");
    }

}

