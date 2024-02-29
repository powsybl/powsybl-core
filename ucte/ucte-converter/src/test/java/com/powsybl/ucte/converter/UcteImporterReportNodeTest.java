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
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.reporter.*;
import com.powsybl.commons.test.AbstractSerDeTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class UcteImporterReportNodeTest extends AbstractSerDeTest {

    private static final String WORK_DIR = "/tmp";

    @Test
    void testReportElementName() throws Exception {
        ReadOnlyDataSource dataSource = new ResourceDataSource("elementName", new ResourceSet("/", "elementName.uct"));

        ReportNode reporter = new ReportRootImpl().newReportNode()
                .withMessageTemplate("testReportVoltageRegulatingXnode", "Test importing UCTE file ${file}")
                .withTypedValue("file", "elementName.uct", TypedValue.FILENAME)
                .add();

        reporter.newReportNode()
                .withMessageTemplate("reportTest", "Report test ${unknownKey}")
                .withUntypedValue("nonPrintedString", "Non printed String")
                .add();
        Optional<ReportNode> reportNode = reporter.getChildren().stream().findFirst();
        assertTrue(reportNode.isPresent());
        assertEquals("Report test ${unknownKey}", reportNode.get().getMessage());
        assertEquals("Non printed String", reportNode.get().getValue("nonPrintedString").map(Object::toString).orElse(null));

        new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null, reporter);

        StringWriter sw = new StringWriter();
        reporter.print(sw);

        InputStream refStream = getClass().getResourceAsStream("/elementNameImportReport.txt");
        String refLogExport = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = TestUtil.normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

    @Test
    void roundTripReporterJsonTest() throws Exception {
        String filename = "frVoltageRegulatingXnode.uct";
        ReportRootImpl reportRoot = new ReportRootImpl();
        ReportNode reporter = reportRoot.newReportNode().withMessageTemplate("roundTripReporterJsonTest", "Test importing UCTE file frVoltageRegulatingXnode.uct").add();
        reporter.newReportNode().withMessageTemplate("novalueReport", "No value report").add();
        Network.read(filename, getClass().getResourceAsStream("/" + filename), reporter);
        roundTripTest(reportRoot, ReportRootSerializer::write, ReportRootDeserializer::read, "/frVoltageRegulatingXnodeReport.json");

        // Testing deserializing with unknown specified dictionary
        ReportRoot rr = ReportRootDeserializer.read(getClass().getResourceAsStream("/frVoltageRegulatingXnodeReport.json"), "de");
        assertEquals(1, rr.getChildren().size());

        ReportNode rn = rr.getChildren().iterator().next();
        assertEquals(2, rn.getChildren().size());

        Iterator<ReportNode> childrenIt = rn.getChildren().iterator();
        ReportNode node1 = childrenIt.next();
        assertEquals("No value report", node1.getMessage());

        ReportNode node2 = childrenIt.next();
        assertEquals("Reading UCTE network file", node2.getMessage());
    }

    @Test
    void jsonDeserializeNoSpecifiedDictionary() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ReportNodeJsonModule());
        ReportRootImpl rr = mapper.readValue(getClass().getResource("/frVoltageRegulatingXnodeReport.json"), ReportRootImpl.class);
        assertEquals(1, rr.getChildren().size());
        ReportNode rn = rr.getChildren().iterator().next();
        assertEquals(2, rn.getChildren().size());

        Iterator<ReportNode> childrenIt = rn.getChildren().iterator();
        ReportNode node1 = childrenIt.next();
        assertTrue(node1 instanceof ReportNode);
        assertEquals("No value report", node1.getMessage());
        ReportNode node2 = childrenIt.next();
        assertEquals("Reading UCTE network file", node2.getMessage());

        mapper.setInjectableValues(new InjectableValues.Std().addValue("foo", "bar"));
        rr = mapper.readValue(getClass().getResource("/frVoltageRegulatingXnodeReport.json"), ReportRootImpl.class);
        assertEquals(1, rr.getChildren().size());
        rn = rr.getChildren().iterator().next();
        assertEquals(2, rn.getChildren().size());

        childrenIt = rn.getChildren().iterator();
        node1 = childrenIt.next();
        node2 = childrenIt.next();
        assertEquals("Reading UCTE network file", node2.getMessage());
        assertEquals("No value report", node1.getMessage());
    }

    @Test
    void roundTripReporterJsonParallelImportTest() throws InterruptedException, ExecutionException, IOException {
        Path workDir = Files.createDirectory(fileSystem.getPath(WORK_DIR));
        Files.copy(getClass().getResourceAsStream("/frVoltageRegulatingXnode.uct"), fileSystem.getPath(WORK_DIR, "frVoltageRegulatingXnode.uct"));
        Files.copy(getClass().getResourceAsStream("/frTestGridForMerging.uct"), fileSystem.getPath(WORK_DIR, "frTestGridForMerging.uct"));
        Files.copy(getClass().getResourceAsStream("/germanTsos.uct"), fileSystem.getPath(WORK_DIR, "germanTsos.uct"));

        List<Network> networkList = Collections.synchronizedList(new ArrayList<>());
        ReportRootImpl reportRoot = new ReportRootImpl();
        ReportNode reporter = reportRoot.newReportNode()
                .withMessageTemplate("importAllParallel", "Test importing UCTE files in parallel: ${file1}, ${file2}, ${file3}")
                .withTypedValue("file1", "frVoltageRegulatingXnode.uct", TypedValue.FILENAME)
                .withTypedValue("file2", "frTestGridForMerging.uct", TypedValue.FILENAME)
                .withTypedValue("file3", "germanTsos.uct", TypedValue.FILENAME)
                .add();
        Importers.importAll(workDir, new UcteImporter(), true, null, networkList::add, null, reporter);
        assertEquals(3, networkList.size());
        assertEquals(3, reporter.getChildren().size());

        roundTripTest(reportRoot, ReportRootSerializer::write, ReportRootDeserializer::read, "/parallelUcteImportReport.json");
    }

}

