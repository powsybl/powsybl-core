/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.*;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setup() {
        Locale.setDefault(Locale.US);
    }

    @Test
    void testReportElementName() throws Exception {
        Locale.setDefault(Locale.US);
        ReadOnlyDataSource dataSource = new ResourceDataSource("elementName", new ResourceSet("/", "elementName.uct"));

        ReportNode rootReportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testReportVoltageRegulatingXnode")
                .withTypedValue("file", "elementName.uct", TypedValue.FILENAME)
                .build();

        rootReportNode.newReportNode()
                .withMessageTemplate("reportTest")
                .withUntypedValue("nonPrintedString", "Non printed String")
                .add();
        Optional<ReportNode> reportNode = rootReportNode.getChildren().stream().findFirst();
        assertTrue(reportNode.isPresent());
        assertEquals("Testing reportNode", reportNode.get().getMessage());
        assertEquals("Non printed String", reportNode.get().getValue("nonPrintedString").map(Object::toString).orElse(null));

        new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null, rootReportNode);

        StringWriter sw = new StringWriter();
        rootReportNode.print(sw);

        InputStream refStream = getClass().getResourceAsStream("/elementNameImportReport.txt");
        String refLogExport = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = TestUtil.normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

    @Test
    void roundTripReportNodeJsonTest() throws Exception {
        Locale.setDefault(Locale.US);
        String filename = "frVoltageRegulatingXnode.uct";
        ReportNode reportRoot = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("roundTripReportNodeJsonTest")
                .build();
        reportRoot.newReportNode()
                .withMessageTemplate("novalueReport")
                .add();
        Network.read(filename, getClass().getResourceAsStream("/" + filename), reportRoot);
        roundTripTest(reportRoot, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/frVoltageRegulatingXnodeReport.json");

        // Testing deserializing with unknown specified dictionary
        ReportNode rm = ReportNodeDeserializer.read(getClass().getResourceAsStream("/frVoltageRegulatingXnodeReport.json"), "de");
        assertEquals(2, rm.getChildren().size());

        ReportNode node1 = rm.getChildren().get(0);
        assertEquals("No value report", node1.getMessage());

        ReportNode node2 = rm.getChildren().get(1);
        assertEquals("Reading UCTE network file", node2.getMessage());
    }

    @Test
    void jsonDeserializeNoSpecifiedDictionary() throws Exception {
        Locale.setDefault(Locale.US);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ReportNodeJsonModule());
        ReportNode rm = mapper.readValue(getClass().getResource("/frVoltageRegulatingXnodeReport.json"), ReportNode.class);
        assertEquals(2, rm.getChildren().size());

        ReportNode node1 = rm.getChildren().get(0);
        assertEquals("No value report", node1.getMessage());
        ReportNode node2 = rm.getChildren().get(1);
        assertEquals("Reading UCTE network file", node2.getMessage());

        mapper.setInjectableValues(new InjectableValues.Std().addValue("foo", "bar"));
        rm = mapper.readValue(getClass().getResource("/frVoltageRegulatingXnodeReport.json"), ReportNode.class);
        assertEquals(2, rm.getChildren().size());

        node1 = rm.getChildren().get(0);
        node2 = rm.getChildren().get(1);
        assertEquals("Reading UCTE network file", node2.getMessage());
        assertEquals("No value report", node1.getMessage());
    }

    @Test
    void roundTripReportNodeJsonParallelImportTest() throws InterruptedException, ExecutionException, IOException {
        Locale.setDefault(Locale.US);
        Path workDir = Files.createDirectory(fileSystem.getPath(WORK_DIR));
        Files.copy(getClass().getResourceAsStream("/frVoltageRegulatingXnode.uct"), fileSystem.getPath(WORK_DIR, "frVoltageRegulatingXnode.uct"));
        Files.copy(getClass().getResourceAsStream("/frTestGridForMerging.uct"), fileSystem.getPath(WORK_DIR, "frTestGridForMerging.uct"));
        Files.copy(getClass().getResourceAsStream("/germanTsos.uct"), fileSystem.getPath(WORK_DIR, "germanTsos.uct"));

        List<Network> networkList = Collections.synchronizedList(new ArrayList<>());
        ReportNode reportRoot = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("importAllParallel")
                .withTypedValue("file1", "frVoltageRegulatingXnode.uct", TypedValue.FILENAME)
                .withTypedValue("file2", "frTestGridForMerging.uct", TypedValue.FILENAME)
                .withTypedValue("file3", "germanTsos.uct", TypedValue.FILENAME)
                .build();
        Importers.importAll(workDir, new UcteImporter(), true, null, networkList::add, null, reportRoot);
        assertEquals(3, networkList.size());
        assertEquals(3, reportRoot.getChildren().size());

        roundTripTest(reportRoot, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/parallelUcteImportReport.json");
    }

}

