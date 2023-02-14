/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.reporter.*;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class UcteImporterReporterTest extends AbstractConverterTest {

    private static final String WORK_DIR = "/tmp";

    @Test
    void testReportElementName() throws Exception {
        ReadOnlyDataSource dataSource = new ResourceDataSource("elementName", new ResourceSet("/", "elementName.uct"));

        ReporterModel reporter = new ReporterModel("testReportVoltageRegulatingXnode", "Test importing UCTE file ${file}",
            Map.of("file", new TypedValue("elementName.uct", TypedValue.FILENAME)));

        PowsyblException e = assertThrows(PowsyblException.class, () -> reporter.report("ObjectReport", "Object ${object} report", "object", dataSource));
        assertEquals("TypedValue expects only Float, Double, Integer, Long and String values (value is an instance of class com.powsybl.commons.datasource.ResourceDataSource)", e.getMessage());

        reporter.report("reportTest", "Report test ${unknownKey}", "nonPrintedString", "Non printed String");
        Optional<Report> report = reporter.getReports().stream().findFirst();
        assertTrue(report.isPresent());
        assertEquals("Report test ${unknownKey}", report.get().getDefaultMessage());
        assertEquals("Non printed String", report.get().getValue("nonPrintedString").getValue());

        new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null, reporter);

        StringWriter sw = new StringWriter();
        reporter.export(sw);

        InputStream refStream = getClass().getResourceAsStream("/elementNameImportReport.txt");
        String refLogExport = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = TestUtil.normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

    @Test
    void roundTripReporterJsonTest() throws Exception {
        String filename = "frVoltageRegulatingXnode.uct";
        ReporterModel reporter = new ReporterModel("roundTripReporterJsonTest", "Test importing UCTE file frVoltageRegulatingXnode.uct");
        reporter.report("novalueReport", "No value report");
        Network.read(filename, getClass().getResourceAsStream("/" + filename), reporter);
        roundTripTest(reporter, ReporterModelSerializer::write, ReporterModelDeserializer::read, "/frVoltageRegulatingXnodeReport.json");

        // Testing deserializing with unknown specified dictionary
        ReporterModel rm = ReporterModelDeserializer.read(getClass().getResourceAsStream("/frVoltageRegulatingXnodeReport.json"), "de");
        assertEquals(1, rm.getReports().size());
        assertEquals("No value report", rm.getReports().iterator().next().getDefaultMessage());
        assertEquals(1, rm.getSubReporters().size());
        assertEquals("Reading UCTE network file", rm.getSubReporters().get(0).getDefaultName());
    }

    @Test
    void jsonDeserializeNoSpecifiedDictionary() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ReporterModelJsonModule());
        ReporterModel rm = mapper.readValue(getClass().getResource("/frVoltageRegulatingXnodeReport.json"), ReporterModel.class);
        assertEquals(1, rm.getReports().size());
        assertEquals("No value report", rm.getReports().iterator().next().getDefaultMessage());
        assertEquals(1, rm.getSubReporters().size());
        assertEquals("Reading UCTE network file", rm.getSubReporters().get(0).getDefaultName());

        mapper.setInjectableValues(new InjectableValues.Std().addValue("foo", "bar"));
        rm = mapper.readValue(getClass().getResource("/frVoltageRegulatingXnodeReport.json"), ReporterModel.class);
        assertEquals(1, rm.getReports().size());
        assertEquals("No value report", rm.getReports().iterator().next().getDefaultMessage());
        assertEquals(1, rm.getSubReporters().size());
        assertEquals("Reading UCTE network file", rm.getSubReporters().get(0).getDefaultName());
    }

    @Test
    void roundTripReporterJsonParallelImportTest() throws InterruptedException, ExecutionException, IOException {
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

