/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

import static com.powsybl.commons.test.TestUtil.normalizeLineSeparator;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class XMLImporterTest extends AbstractIidmSerDeTest {

    private XMLImporter importer;

    private void writeNetwork(String fileName, IidmVersion version, boolean writeExt) throws IOException {
        writeNetwork(fileName, version.getNamespaceURI(), writeExt);
    }

    private void writeNetwork(String fileName, String namespaceUri, boolean writeExt) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(fileName), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write("<iidm:network xmlns:iidm=\"" + namespaceUri + "\" id=\"test\" caseDate=\"2013-01-15T18:45:00.000+01:00\" forecastDistance=\"0\" sourceFormat=\"test\" minimumValidationLevel=\"STEADY_STATE_HYPOTHESIS\">");
            writer.newLine();
            writer.write("    <iidm:substation id=\"P1\" country=\"FR\"/>");
            writer.newLine();
            if (writeExt) {
                writer.write("    <iidm:extension id=\"P1\">");
                writer.write("    <foo/>");
                writer.write("    </iidm:extension>");
            }
            writer.write("</iidm:network>");
            writer.newLine();
        }
    }

    private void writeNetworkWithExtension(String fileName, String namespaceUri) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(fileName), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write("<iidm:network xmlns:iidm=\"" + namespaceUri + "\" id=\"test\" caseDate=\"2013-01-15T18:45:00.000+01:00\" forecastDistance=\"0\" sourceFormat=\"test\" minimumValidationLevel=\"STEADY_STATE_HYPOTHESIS\">");
            writer.newLine();
            writer.write("    <iidm:substation id=\"P1\" country=\"FR\"/>");
            writer.newLine();
            writer.write("    <iidm:extension id=\"P1\">");
            writer.write("    <substationPosition>");
            writer.write("    <coordinate latitude=\"1\" longitude=\"2\" />");
            writer.write("    </substationPosition>");
            writer.write("    </iidm:extension>");
            writer.write("</iidm:network>");
            writer.newLine();
        }
    }

    private void writeNetworkWithComment(String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(fileName), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<!--sfsfs-->");
            writer.write("<iidm:network xmlns:iidm=\"" + CURRENT_IIDM_VERSION.getNamespaceURI() + "\" id=\"test\" caseDate=\"2013-01-15T18:45:00.000+01:00\" forecastDistance=\"0\" sourceFormat=\"test\" minimumValidationLevel=\"STEADY_STATE_HYPOTHESIS\">");
            writer.newLine();
            writer.write("    <iidm:substation id=\"P1\" country=\"FR\"/>");
            writer.newLine();
            writer.write("</iidm:network>");
            writer.newLine();
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        // create test files
        //   /test0.xiidm
        //   /test1.iidm
        //   /test2.xml
        //   /test3.txt
        //   /test5.xiidm that contains unsupported extensions
        //   /test6.xiidm + /test6_mapping.csv
        //   /test7.xiidm that contains a comment after xml prolog
        writeNetwork("/test0.xiidm", CURRENT_IIDM_VERSION, false);
        writeNetwork("/test1.iidm", CURRENT_IIDM_VERSION, false);
        writeNetwork("/test2.xml", CURRENT_IIDM_VERSION, false);
        writeNetwork("/test3.txt", CURRENT_IIDM_VERSION, false);
        writeNetwork("/test5.xiidm", CURRENT_IIDM_VERSION, true);
        writeNetwork("/test6.xiidm", CURRENT_IIDM_VERSION, false);
        writeNetwork("/testDummy.xiidm", "http://wwww.dummy.foo/", false);
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath("/test6_mapping.csv"), StandardCharsets.UTF_8)) {
            writer.write("ZZ;test");
            writer.newLine();
            writer.write("X1;P1");
            writer.newLine();
        }
        writeNetworkWithComment("/test7.xiidm");
        writeNetworkWithExtension("/test8.xiidm", CURRENT_IIDM_VERSION.getNamespaceURI());
        writeNetwork("/test9.0.xiidm", CURRENT_IIDM_VERSION, true);
        writeNetwork("/test10.0.xiidm", CURRENT_IIDM_VERSION, false);

        importer = new XMLImporter();
    }

    @Test
    void backwardCompatibilityTest() throws IOException {
        // create network and datasource
        writeNetwork("/v_1_0.xiidm", IidmVersion.V_1_0, false);
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "v_1_0");

        // exists
        assertTrue(importer.exists(dataSource));

        // importData
        Network network = importer.importData(dataSource, NetworkFactory.findDefault(), null);
        assertNotNull(network.getSubstation("P1"));
    }

    @Test
    void getFormat() {
        assertEquals("XIIDM", importer.getFormat());
    }

    @Test
    void getParameters() {
        assertEquals(5, importer.getParameters().size());
        assertEquals("iidm.import.xml.throw-exception-if-extension-not-found", importer.getParameters().get(0).getName());
        assertEquals(Arrays.asList("iidm.import.xml.throw-exception-if-extension-not-found", "throwExceptionIfExtensionNotFound"), importer.getParameters().get(0).getNames());
    }

    @Test
    void getComment() {
        assertEquals("IIDM XML v " + CURRENT_IIDM_VERSION.toString(".") + " importer", importer.getComment());
    }

    @Test
    void exists() {
        assertTrue(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test0")));
        assertTrue(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test1")));
        assertTrue(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test2")));
        assertFalse(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test3"))); // wrong extension
        assertFalse(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test4"))); // does not exist
        assertFalse(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "testDummy"))); // namespace URI is not defined
        assertTrue(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test9.0")));
        assertTrue(importer.exists(new DirectoryDataSource(fileSystem.getPath("/"), "test10.0")));
    }

    @Test
    void copy() throws Exception {
        importer.copy(new DirectoryDataSource(fileSystem.getPath("/"), "test0"), new DirectoryDataSource(fileSystem.getPath("/"), "test0_copy"));
        assertTrue(Files.exists(fileSystem.getPath("/test0_copy.xiidm")));
        assertEquals(Files.readAllLines(fileSystem.getPath("/test0.xiidm"), StandardCharsets.UTF_8),
                Files.readAllLines(fileSystem.getPath("/test0_copy.xiidm"), StandardCharsets.UTF_8));

        // test copy with id mapping file
        importer.copy(new DirectoryDataSource(fileSystem.getPath("/"), "test6"), new DirectoryDataSource(fileSystem.getPath("/"), "test6_copy"));
        assertTrue(Files.exists(fileSystem.getPath("/test6_copy.xiidm")));
        assertTrue(Files.exists(fileSystem.getPath("/test6_copy_mapping.csv")));
        assertEquals(Files.readAllLines(fileSystem.getPath("/test6.xiidm"), StandardCharsets.UTF_8),
                Files.readAllLines(fileSystem.getPath("/test6_copy.xiidm"), StandardCharsets.UTF_8));
        assertEquals(Files.readAllLines(fileSystem.getPath("/test6_mapping.csv"), StandardCharsets.UTF_8),
                Files.readAllLines(fileSystem.getPath("/test6_copy_mapping.csv"), StandardCharsets.UTF_8));
    }

    @Test
    void importData() {
        // should be ok
        assertNotNull(importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test0"), NetworkFactory.findDefault(), null));

        // should fail because file that does not exist
        try {
            importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test4"), NetworkFactory.findDefault(), null);
            fail();
        } catch (RuntimeException ignored) {
        }

        // extension plugin will be not found but default option just warn
        assertNotNull(importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test5"), NetworkFactory.findDefault(), null));

        // extension plugin will be not found but option is set to throw an exception
        // (deprecated parameter name)
        Properties params = new Properties();
        params.put("throwExceptionIfExtensionNotFound", "true");
        try {
            importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test5"), NetworkFactory.findDefault(), params);
            fail();
        } catch (RuntimeException ignored) {
        }

        // extension plugin will be not found but option is set to throw an exception
        // (parameter name following same naming convention of XmlExporter)
        Properties params2 = new Properties();
        params2.put("iidm.import.xml.throw-exception-if-extension-not-found", "true");
        try {
            importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test5"), NetworkFactory.findDefault(), params2);
            fail();
        } catch (RuntimeException ignored) {
        }

        // read file with id mapping
        Network network = importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test6"), NetworkFactory.findDefault(), params);
        assertNotNull(network.getSubstation("X1")); // and not P1 !!!!!

        Network network2 = importer.importData(new DirectoryDataSource(fileSystem.getPath("/"), "test7"), NetworkFactory.findDefault(), null);
        assertNotNull(network2.getSubstation("P1"));
    }

    @Test
    void importDataReportNodeTest() throws IOException {
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "test8");
        importDataAndTestReportNode("/importXmlReport.txt", dataSource);
    }

    @Test
    void importDataReportNodeExtensionNotFoundTest() throws IOException {
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "test5");
        importDataAndTestReportNode("/importXmlReportExtensionsNotFound.txt", dataSource);
    }

    @Test
    void importDataReportNodeMultipleExtension() throws IOException {
        importDataAndTestReportNode("multiple-extensions",
                "multiple-extensions.xml",
                "/importXmlReportExtensions.txt");
    }

    @Test
    void importDataReportNodeValidationTest() throws IOException {
        importDataAndTestReportNode("twoWindingsTransformerPhaseAndRatioTap",
                "twoWindingsTransformerPhaseAndRatioTap.xml",
                "/importXmlReportValidation.txt");
    }

    @Test
    void importDataReportNodeValidationAndMultipleExtensionTest() throws IOException {
        importDataAndTestReportNode("twoWindingsTransformerPhaseAndRatioTapWithExtensions",
                "twoWindingsTransformerPhaseAndRatioTapWithExtensions.xml",
                "/importXmlReportExtensionsAndValidations.txt");
    }

    private void importDataAndTestReportNode(String dataSourceBaseName, String dataSourceFilename, String expectedContentFilename) throws IOException {
        ReadOnlyDataSource dataSource = new ResourceDataSource(dataSourceBaseName, new ResourceSet(getVersionDir(CURRENT_IIDM_VERSION), dataSourceFilename));
        importDataAndTestReportNode(expectedContentFilename, dataSource);
    }

    private void importDataAndTestReportNode(String expectedContentFilename, ReadOnlyDataSource dataSource) throws IOException {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("test", "test reportNode").build();
        assertNotNull(importer.importData(dataSource, NetworkFactory.findDefault(), null, reportNode));

        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        InputStream ref = XMLImporterTest.class.getResourceAsStream(expectedContentFilename);
        String refLogExport = normalizeLineSeparator(new String(ByteStreams.toByteArray(ref), StandardCharsets.UTF_8));
        String logExport = normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

}
