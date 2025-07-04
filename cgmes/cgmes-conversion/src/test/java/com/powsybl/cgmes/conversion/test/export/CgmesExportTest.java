/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.*;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static com.powsybl.commons.xml.XmlUtil.getXMLInputFactory;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesExportTest {

    private Properties importParams;

    @BeforeEach
    void setUp() {
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void testFromIidm() throws IOException {
        // Test from IIDM with configuration that does not exist in CGMES (disconnected node on switch and HVDC line)

        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vl = network.getVoltageLevel("C");

        // set as WIND generator
        network.getGenerator("CB").setEnergySource(EnergySource.WIND);

        // Add disconnected node on switch (side 2)
        vl.getNodeBreakerView().newSwitch().setId("TEST_SW")
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(true)
                .setNode1(0)
                .setNode2(6)
                .add();

        // Add disconnected node on DC converter station (side 2)
        vl.newVscConverterStation()
                .setId("C1")
                .setNode(5)
                .setLossFactor(1.1f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(5).add();
        vl.newVscConverterStation()
                .setId("C2")
                .setNode(6)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("hvdc_line")
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(50.0)
                .setActivePowerSetpoint(20.0)
                .setConverterStationId1("C1")
                .setConverterStationId2("C2")
                .add();

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            network.write("CGMES", null, tmpDir.resolve("tmp"));
            Network n2 = Network.read(new GenericReadOnlyDataSource(tmpDir, "tmp"), importParams);
            VoltageLevel c = n2.getVoltageLevel("C");
            assertNull(Networks.getEquivalentTerminal(c, c.getNodeBreakerView().getNode2("TEST_SW")));
            assertNull(n2.getVscConverterStation("C2").getTerminal().getBusView().getBus());
        }
    }

    @Test
    void testSynchronousMachinesWithSameGeneratingUnit() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseBEGenUnitWithTwoSyncMachines().dataSource();
        Network n = Importers.importData("CGMES", ds, importParams);
        String exportFolder = "/test-gu-with-2sm";
        String baseName = "testGU2SMs";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            // Export to CGMES and add boundary EQ for reimport
            n.write("CGMES", null, tmpDir.resolve(baseName));
            String eqbd = ds.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
            if (eqbd != null) {
                try (InputStream is = ds.newInputStream(eqbd)) {
                    Files.copy(is, tmpDir.resolve(baseName + "_EQ_BD.xml"));
                }
            }

            Network n2 = Network.read(new GenericReadOnlyDataSource(tmpDir, baseName), importParams);
            Generator g1 = n2.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
            Generator g2 = n2.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
            String gu1 = g1.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            String gu2 = g2.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            assertEquals(gu1, gu2);
        }
    }

    @Test
    void testPhaseTapChangerRegulatingControlAlwaysExported() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Network n = Importers.importData("CGMES", ds, importParams);
        TwoWindingsTransformer transformer = n.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        String regulatingControlId = "5fc492ab-fe33-423b-84f1-a47f87552427";
        String exportFolder = "/test-ptc-rc-not-exported";
        String baseName = "testPtcRcNotExported";

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));

            // With original regulating mode the regulating control should be written in the EQ output
            String baseNameWithRc = baseName + "-with-rc";
            n.write("CGMES", null, tmpDir.resolve(baseNameWithRc));
            assertTrue(transformer.getPhaseTapChanger().isRegulating());
            assertTrue(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameWithRc, "EQ"));
            assertTrue(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameWithRc, "SSH"));

            transformer.getPhaseTapChanger().setRegulating(false);
            String baseNameNoRc = baseName + "-no-rc";
            n.write("CGMES", null, tmpDir.resolve(baseNameNoRc));
            assertTrue(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameNoRc, "EQ"));
            assertTrue(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameNoRc, "SSH"));
        }
    }

    private static boolean cgmesFileContainsRegulatingControl(String regulatingControlId, Path folder, String baseName, String instanceFile) throws XMLStreamException, IOException {
        String file = String.format("%s_%s.xml", baseName, instanceFile);
        String rdfIdAttributeName;
        String expectedRdfIdAttributeValue;
        if (instanceFile.equals("EQ")) {
            rdfIdAttributeName = "ID";
            expectedRdfIdAttributeValue = "_" + regulatingControlId;
        } else if (instanceFile.equals("SSH")) {
            rdfIdAttributeName = "about";
            expectedRdfIdAttributeValue = "#_" + regulatingControlId;
        } else {
            return false;
        }
        return xmlFileContainsRegulatingControl(expectedRdfIdAttributeValue, rdfIdAttributeName, folder.resolve(file));
    }

    private static boolean xmlFileContainsRegulatingControl(String expectedRdfIdAttributeValue, String rdfIdAttributeName, Path file) throws IOException, XMLStreamException {
        try (InputStream is = Files.newInputStream(file)) {
            XMLStreamReader reader = getXMLInputFactory().createXMLStreamReader(is);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("TapChangerControl")) {
                    String id = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, rdfIdAttributeName);
                    if (expectedRdfIdAttributeValue.equals(id)) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        }
        return false;
    }

    @Test
    void testPhaseTapChangerType16() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        String transformerId = "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0";
        String phaseTapChangerId = "6ebbef67-3061-4236-a6fd-6ccc4595f6c3";
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, 16);
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, 100);
    }

    private void testPhaseTapChangerType(ReadOnlyDataSource ds, String transformerId, String phaseTapChangerId, int cimVersion) throws IOException {
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, cimVersion, importParams);
    }

    private static void testPhaseTapChangerType(ReadOnlyDataSource ds, String transformerId, String phaseTapChangerId, int cimVersion, Properties importParams) throws IOException {
        Network network = Importers.importData("CGMES", ds, importParams);
        String exportFolder = "/test-ptc-type";
        String baseName = "testPtcType";
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(transformerId);
        String typeOriginal = CgmesExportUtil.cgmesTapChangerType(transformer, phaseTapChangerId).orElseThrow(RuntimeException::new);
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));

            // When exporting only SSH (or SSH and SV), original type of tap changer should be kept
            Properties paramsOnlySsh = new Properties();
            paramsOnlySsh.put(CgmesExport.PROFILES, List.of("SSH"));
            paramsOnlySsh.put(CgmesExport.CIM_VERSION, "" + cimVersion);
            network.write("CGMES", paramsOnlySsh, tmpDir.resolve(baseName));
            String typeOnlySsh = CgmesExportUtil.cgmesTapChangerType(transformer, phaseTapChangerId).orElseThrow(RuntimeException::new);
            assertEquals(typeOriginal, typeOnlySsh);

            // If we export EQ and SSH (or all instance fiels), type of tap changer should be changed to tabular
            Properties paramsEqAndSsh = new Properties();
            paramsEqAndSsh.put(CgmesExport.CIM_VERSION, "" + cimVersion);
            network.write("CGMES", paramsEqAndSsh, tmpDir.resolve(baseName));
            String typeEqAndSsh = CgmesExportUtil.cgmesTapChangerType(transformer, phaseTapChangerId).orElseThrow(RuntimeException::new);
            assertEquals(CgmesNames.PHASE_TAP_CHANGER_TABULAR, typeEqAndSsh);
        }
    }

    @Test
    void testFromIidmBusBranch() throws IOException {
        // If we want to export an IIDM that contains dangling lines,
        // we will have to rely on some external boundaries definition

        Network network = DanglingLineNetworkFactory.create();
        DanglingLine expected = network.getDanglingLine("DL");
        Network merged = Network.merge(network, BatteryNetworkFactory.create()); // add battery
        Battery battery = merged.getBattery("BAT");

        // Before exporting, we have to define to which point
        // in the external boundary definition we want to associate this dangling line
        // For this test we chose the Conformity MicroGrid BaseCase
        ResourceSet boundaries = CgmesConformity1Catalog.microGridBaseCaseBoundaries();
        String boundaryTN = "d4affe50316740bdbbf4ae9c7cbf3cfd";
        expected.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY, boundaryTN);
        // We also inform the identifiers of the boundaries we depend on
        Properties exportParameters = new Properties();
        exportParameters.put(CgmesExport.BOUNDARY_EQ_ID, "urn:uuid:2399cbd0-9a39-11e0-aa80-0800200c9a66");
        exportParameters.put(CgmesExport.BOUNDARY_TP_ID, "urn:uuid:2399cbd1-9a39-11e0-aa80-0800200c9a66");

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            merged.write("CGMES", exportParameters, tmpDir.resolve("tmp"));

            // To be able to import from the exported CGMES data we must add the external boundary definitions
            // For bus/branch we need both EQ and TP instance files of boundaries
            try (InputStream is = boundaries.newInputStream("MicroGridTestConfiguration_EQ_BD.xml")) {
                Files.copy(is, tmpDir.resolve("tmp_EQ_BD.xml"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = boundaries.newInputStream("MicroGridTestConfiguration_TP_BD.xml")) {
                Files.copy(is, tmpDir.resolve("tmp_TP_BD.xml"), StandardCopyOption.REPLACE_EXISTING);
            }

            Network networkFromCgmes = Network.read(new GenericReadOnlyDataSource(tmpDir, "tmp"), importParams);
            DanglingLine actual = networkFromCgmes.getDanglingLine("DL");
            assertNotNull(actual);
            checkDanglingLineParams(expected, actual);
            Generator generator = networkFromCgmes.getGenerator("BAT");
            assertNotNull(generator);
            assertEquals(battery.getTargetP(), generator.getTargetP(), 0.0);
            assertEquals(battery.getTargetQ(), generator.getTargetQ(), 0.0);
            assertEquals(battery.getMinP(), generator.getMinP(), 0.0);
            assertEquals(battery.getMaxP(), generator.getMaxP(), 0.0);
        }
    }

    @Test
    void testFromIidmDanglingLineBusBranchNotBoundary() throws IOException {
        // If we want to export an IIDM that contains dangling lines,
        // we will have to rely on some external boundaries definition
        // If we do not provide this information,
        // we will re-import it as a regular line

        Network network = DanglingLineNetworkFactory.create();
        DanglingLine expected = network.getDanglingLine("DL");

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            network.write("CGMES", null, tmpDir.resolve("tmp"));

            Network networkFromCgmes = Network.read(new GenericReadOnlyDataSource(tmpDir, "tmp"), importParams);
            Line actual = networkFromCgmes.getLine("DL");
            assertNotNull(actual);
            checkDanglingLineParams(expected, actual);
            // The dangling line was exported as an ACLS plus an equivalent injection.
            // Equivalent injections inside an IGM are mapped to generators,
            // So we have to check that there is a generator at side 2 (boundary) of the line
            checkDanglingLineEquivalentInjection(expected, actual);
            checkFictitiousContainerAtBoundary(expected, actual);
        }
    }

    @Test
    void testFromIidmDanglingLineNodeBreaker() throws IOException {
        // If we want to export an IIDM that contains dangling lines,
        // we will have to rely on some external boundaries definition

        Network network = DanglingLineNetworkFactory.create();
        DanglingLine expected = network.getDanglingLine("DL");

        // Before exporting, we have to define to which point
        // in the external boundary definition we want to associate this dangling line
        // For this test we chose the Conformity MicroGrid BaseCase
        ResourceSet boundaries = CgmesConformity1Catalog.microGridBaseCaseBoundaries();
        String boundaryCN = "b675a570-cb6e-11e1-bcee-406c8f32ef58";
        expected.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY, boundaryCN);
        // We inform the identifier of the boundaries we depend on
        Properties exportParameters = new Properties();
        exportParameters.put(CgmesExport.BOUNDARY_EQ_ID, "urn:uuid:536f9bf1-3f8f-a546-87e3-7af2272f29b7");
        exportParameters.put(CgmesExport.TOPOLOGY_KIND, "NODE_BREAKER");

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            network.write("CGMES", exportParameters, tmpDir.resolve("tmp"));

            // To be able to import from the exported CGMES data we must add the external boundary definitions
            try (InputStream is = boundaries.newInputStream("MicroGridTestConfiguration_EQ_BD.xml")) {
                Files.copy(is, tmpDir.resolve("tmp_EQ_BD.xml"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = boundaries.newInputStream("MicroGridTestConfiguration_TP_BD.xml")) {
                Files.copy(is, tmpDir.resolve("tmp_TP_BD.xml"), StandardCopyOption.REPLACE_EXISTING);
            }

            Network networkFromCgmes = Network.read(new GenericReadOnlyDataSource(tmpDir, "tmp"), importParams);
            DanglingLine actual = networkFromCgmes.getDanglingLine("DL");
            assertNotNull(actual);
            checkDanglingLineParams(expected, actual);
        }
    }

    @Test
    void testFromIidmDanglingLineNodeBreakerNoBoundaries() throws IOException {
        // If we want to export an IIDM that contains dangling lines,
        // we will have to rely on some external boundaries definition
        // If we do not add boundary information
        // a node in the same voltage level of the dangling line will be created
        // and the re-imported network will not see it as a dangling line,
        // but as a regular transmission line

        Network network = DanglingLineNetworkFactory.create();
        DanglingLine expected = network.getDanglingLine("DL");

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            Properties exportParameters = new Properties();
            exportParameters.put(CgmesExport.TOPOLOGY_KIND, "NODE_BREAKER");
            network.write("CGMES", exportParameters, tmpDir.resolve("tmp"));

            Network networkFromCgmes = Network.read(new GenericReadOnlyDataSource(tmpDir, "tmp"), importParams);
            DanglingLine actualDanglingLine = networkFromCgmes.getDanglingLine("DL");
            assertNull(actualDanglingLine);
            Line actual = networkFromCgmes.getLine("DL");
            checkDanglingLineParams(expected, actual);
            // non-network end is always exported with terminal sequence 2
            // at that node there should be only the equipment corresponding to the equivalent injection
            checkDanglingLineEquivalentInjection(expected, actual);
            checkFictitiousContainerAtBoundary(expected, actual);
        }
    }

    @Test
    void testLineContainersNotInBoundaries() throws IOException {
        ReadOnlyDataSource ds = new ResourceDataSource("Node of T-junction in line container",
                new ResourceSet("/issues/node-containers/", "line_with_t-junction.xml"));
        Network network = Network.read(ds, importParams);

        String exportFolder = "/test-line-containers-not-in-boundaries";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            // Export to CGMES and add boundary EQ for reimport
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            String baseName = "testLineContainersNotInBoundaries";
            network.write("CGMES", null, tmpDir.resolve(baseName));
            ReadOnlyDataSource exportedCgmes = new GenericReadOnlyDataSource(tmpDir, baseName);

            // Check that the exported CGMES model contains a fictitious substation
            CgmesModel cgmes = CgmesModelFactory.create(exportedCgmes, TripleStoreFactory.defaultImplementation());
            assertTrue(cgmes.isNodeBreaker());
            assertTrue(cgmes.substations().stream().anyMatch(sub -> sub.getLocal("name").startsWith("fictS_")));

            // Verify that we re-import the exported CGMES data without problems
            Network networkReimported = Network.read(exportedCgmes, importParams);
            assertNotNull(networkReimported);
        }
    }

    @Test
    void testModelEquipmentOperationProfile() throws IOException {
        String importDir = "/issues/switches/";
        Network network = readCgmesResources(importDir, "disconnected_terminal_EQ.xml");

        String exportDir = "/testModelProfile";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportDir));
            String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

            String regex = "<md:Model.profile>http://entsoe.eu/CIM/EquipmentOperation/3/1</md:Model.profile>";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(eqFile);
            assertEquals(1, matcherCount(matcher));
        }
    }

    @Test
    void testModelDescription() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        String modelDescription = "powsybl community";
        Properties params = new Properties();
        params.put(CgmesExport.MODEL_DESCRIPTION, modelDescription);

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
            ZipArchiveDataSource zip = new ZipArchiveDataSource(tmpDir.resolve("."), "output");
            new CgmesExport().export(network, params, zip);
            Network network2 = Network.read(new GenericReadOnlyDataSource(tmpDir.resolve("output.zip")), importParams);
            CgmesMetadataModel sshMetadata = network2
                    .getExtension(CgmesMetadataModels.class)
                    .getModelForSubset(CgmesSubset.STATE_VARIABLES)
                    .orElseThrow();
            assertEquals(modelDescription, sshMetadata.getDescription());
        }
    }

    @Test
    void testModelVersion() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        Properties params = new Properties();
        params.put(CgmesExport.MODEL_VERSION, "9");

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
            ZipArchiveDataSource zip = new ZipArchiveDataSource(tmpDir.resolve("."), "output");
            new CgmesExport().export(network, params, zip);
            Network network2 = Network.read(new GenericReadOnlyDataSource(tmpDir.resolve("output.zip")), importParams);
            CgmesMetadataModel sshMetadata = network2.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow();
            assertEquals(9, sshMetadata.getVersion());
            CgmesMetadataModel svMetadata = network2.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STATE_VARIABLES).orElseThrow();
            assertEquals(9, svMetadata.getVersion());
        }
    }

    @Test
    void testModelDescriptionClosingXML() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        // Security test
        // Checking that putting end-tag does not corrupt the file
        String modelDescription = "powsybl community</md:Model.modelingAuthoritySet></md:FullModel>";
        Properties params = new Properties();
        params.put(CgmesExport.MODEL_DESCRIPTION, modelDescription);

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
            ZipArchiveDataSource zip = new ZipArchiveDataSource(tmpDir.resolve("."), "output");
            new CgmesExport().export(network, params, zip);

            // check network can be reimported and that ModelDescription still includes end-tag
            Network network2 = Network.read(new GenericReadOnlyDataSource(tmpDir.resolve("output.zip")), importParams);

            CgmesMetadataModel sshMetadata = network2.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow();
            assertEquals(modelDescription, sshMetadata.getDescription());
            CgmesMetadataModel svMetadata = network2.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STATE_VARIABLES).orElseThrow();
            assertEquals(modelDescription, svMetadata.getDescription());
        }

    }

    @Test
    void testExportWithModelingAuthorityFromReferenceData() throws IOException {
        // We want to test that information about sourcing actor read from reference data overrides
        // the default value for the parameter MAS URI

        // Minimal network with well-known country (BE)
        // that can be resolved to a sourcing actor (ELIA) using the reference data.
        // The reference data also contains a defined MAS URI (elia.be/OperationalPlanning) for this sourcing actor
        Network network = NetworkTest1Factory.create("minimal-network");
        network.getSubstations().iterator().next().setCountry(Country.BE);

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

            // To export using reference data we must prepare first the boundaries that will be used
            Path boundariesDir = Files.createDirectories(fileSystem.getPath("/boundaries"));
            try (InputStream is = this.getClass().getResourceAsStream("/reference-data-provider/sample_EQBD.xml")) {
                if (is != null) {
                    Files.copy(is, boundariesDir.resolve("sample_EQBD.xml"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            platformConfig.createModuleConfig("import-export-parameters-default-value")
                    .setStringProperty(CgmesImport.BOUNDARY_LOCATION, boundariesDir.toString());

            Path tmpDir = Files.createDirectories(fileSystem.getPath("/work"));
            Properties exportParams = new Properties();
            // It is enough to check that the MAS has been set correctly in the EQ instance file
            exportParams.put(CgmesExport.PROFILES, "EQ");
            new CgmesExport(platformConfig).export(network, exportParams, new DirectoryDataSource(tmpDir, network.getNameOrId()));

            String eq = Files.readString(tmpDir.resolve(network.getNameOrId() + "_EQ.xml"));
            assertTrue(eq.contains("modelingAuthoritySet>http://www.elia.be/OperationalPlanning"));
        }
    }

    private static void checkDanglingLineParams(DanglingLine expected, DanglingLine actual) {
        assertEquals(expected.getR(), actual.getR(), EPSILON);
        assertEquals(expected.getX(), actual.getX(), EPSILON);
        assertEquals(expected.getG(), actual.getG(), EPSILON);
        assertEquals(expected.getB(), actual.getB(), EPSILON);
        assertEquals(expected.getP0(), actual.getP0(), EPSILON);
        assertEquals(expected.getQ0(), actual.getQ0(), EPSILON);
    }

    private static void checkDanglingLineParams(DanglingLine expected, Line actual) {
        assertEquals(expected.getR(), actual.getR(), EPSILON);
        assertEquals(expected.getX(), actual.getX(), EPSILON);
        assertEquals(expected.getG(), actual.getG1() + actual.getG2(), EPSILON);
        assertEquals(expected.getB(), actual.getB1() + actual.getB2(), EPSILON);
    }

    private static void checkFictitiousContainerAtBoundary(DanglingLine expected, Line actual) {
        // Check that a fictitious voltage level and substation have been created
        // Voltage level and substation must be different at both ends of line
        assertNotEquals(expected.getTerminal().getVoltageLevel().getId(), actual.getTerminal2().getVoltageLevel().getId());
        assertNotEquals(expected.getTerminal().getVoltageLevel().getSubstation().orElseThrow().getId(), actual.getTerminal2().getVoltageLevel().getSubstation().orElseThrow().getId());
        // Names should end/start with known suffixes/prefixes
        assertTrue(actual.getTerminal2().getVoltageLevel().getNameOrId().endsWith("_VL"));
        assertTrue(actual.getTerminal2().getVoltageLevel().getSubstation().orElseThrow().getNameOrId().startsWith("fictS_"));
    }

    private static void checkDanglingLineEquivalentInjection(DanglingLine expected, Line actual) {
        Connectable<?> eqAtEnd2 = actual.getTerminal2().getBusView().getBus().getConnectedTerminalStream()
                .filter(t -> t.getConnectable() != actual)
                .findFirst()
                .map(Terminal::getConnectable)
                .orElseThrow();
        assertInstanceOf(Generator.class, eqAtEnd2);
        Generator actualEquivalentInjection = (Generator) eqAtEnd2;
        assertEquals(expected.getP0(), -actualEquivalentInjection.getTargetP(), EPSILON);
        assertEquals(expected.getQ0(), -actualEquivalentInjection.getTargetQ(), EPSILON);
    }

    @Test
    void testCanGeneratorControl() throws IOException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), new Properties());

        Generator generatorNoRcc = network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        Generator generatorRcc = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");

        generatorNoRcc.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);
        generatorRcc.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);

        String exportFolder = "/test-generator-control";
        String baseName = "testGeneratorControl";

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");
            new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
            String eq = Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));

            // Check that RegulatingControl is properly exported
            assertTrue(eq.contains("3a3b27be-b18b-4385-b557-6735d733baf0_RC"));
            assertTrue(eq.contains("550ebe0d-f2b2-48c1-991f-cebea43a21aa_RC"));
            generatorRcc.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);
            generatorNoRcc.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);

            // RegulatingControl is exported when targetV is not NaN, even if voltage regulation is disabled
            generatorRcc.setVoltageRegulatorOn(false);
            generatorNoRcc.setVoltageRegulatorOn(false);
            new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
            eq = Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));
            assertTrue(eq.contains("3a3b27be-b18b-4385-b557-6735d733baf0_RC"));
            assertTrue(eq.contains("550ebe0d-f2b2-48c1-991f-cebea43a21aa_RC"));
            generatorRcc.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);
            generatorNoRcc.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);

            // RegulatingControl isn't exported when targetV is NaN
            double rccTargetV = generatorRcc.getTargetV();
            generatorRcc.setTargetV(Double.NaN);
            double noRccTargetV = generatorNoRcc.getTargetV();
            generatorNoRcc.setTargetV(Double.NaN);
            new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
            eq = Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));
            assertFalse(eq.contains("3a3b27be-b18b-4385-b557-6735d733baf0_RC"));
            assertFalse(eq.contains("550ebe0d-f2b2-48c1-991f-cebea43a21aa_RC"));
            generatorRcc.setTargetV(rccTargetV);
            generatorRcc.setVoltageRegulatorOn(true);
            generatorNoRcc.setTargetV(noRccTargetV);
            generatorNoRcc.setVoltageRegulatorOn(true);

            // RegulatingControl isn't exported when Qmin and Qmax are the same
            ReactiveCapabilityCurveAdder rccAdder = generatorRcc.newReactiveCapabilityCurve();
            ReactiveCapabilityCurve rcc = (ReactiveCapabilityCurve) generatorRcc.getReactiveLimits();
            rcc.getPoints().forEach(point -> rccAdder.beginPoint().setP(point.getP()).setMaxQ(point.getMaxQ()).setMinQ(point.getMaxQ()).endPoint());
            rccAdder.add();
            MinMaxReactiveLimitsAdder mmrlAdder = generatorNoRcc.newMinMaxReactiveLimits();
            MinMaxReactiveLimits mmrl = (MinMaxReactiveLimits) generatorNoRcc.getReactiveLimits();
            mmrlAdder.setMinQ(mmrl.getMinQ());
            mmrlAdder.setMaxQ(mmrl.getMinQ());
            mmrlAdder.add();
            new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
            eq = Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));
            assertFalse(eq.contains("3a3b27be-b18b-4385-b557-6735d733baf0_RC"));
            assertFalse(eq.contains("550ebe0d-f2b2-48c1-991f-cebea43a21aa_RC"));

            // RegulatingControl is however exported when the corresponding CGMES property is present
            generatorRcc.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, "3a3b27be-b18b-4385-b557-6735d733baf0_RC");
            generatorNoRcc.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, "550ebe0d-f2b2-48c1-991f-cebea43a21aa_RC");
            new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
            eq = Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));
            assertTrue(eq.contains("3a3b27be-b18b-4385-b557-6735d733baf0_RC"));
            assertTrue(eq.contains("550ebe0d-f2b2-48c1-991f-cebea43a21aa_RC"));

        }
    }

    @Test
    void networkWithoutControlAreaInterchange() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        assertEquals(0, network.getAreaCount());

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/temp"));

            // Exporting with default behaviour, no default control area is written
            Path tmpDirNoCA = tmpDir.resolve("network-no-ca");
            Files.createDirectories(tmpDirNoCA);
            String eqFile = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDirNoCA);
            assertFalse(eqFile.contains("cim:ControlArea"));

            // Explicit creation of a default control area
            new CgmesExport().createDefaultControlAreaInterchange(network);

            // Check that a control area definition has been created before export
            assertEquals(1, network.getAreaCount());
            Area defaultControlArea = network.getAreas().iterator().next();
            assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, defaultControlArea.getAreaType());
            assertEquals(1, defaultControlArea.getAreaBoundaryStream().count());
            assertEquals(-50, defaultControlArea.getInterchangeTarget().orElse(Double.NaN));
            assertEquals("DL", defaultControlArea.getAreaBoundaryStream().findFirst()
                    .flatMap(AreaBoundary::getBoundary)
                    .map(Boundary::getDanglingLine)
                    .map(DanglingLine::getId)
                    .orElse(null));

            // Check that exported files now have a control area definition
            // No default value for tolerance
            Path tmpDirWithCA = tmpDir.resolve("network-with-ca");
            Files.createDirectories(tmpDirWithCA);
            eqFile = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDirWithCA);
            String sshFile = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDirWithCA);
            assertTrue(eqFile.contains("<cim:ControlArea rdf:ID=\"_dangling-line_N_CA\">"));
            assertTrue(sshFile.contains("<cim:ControlArea.netInterchange>-50</cim:ControlArea.netInterchange>"));
            // No default value for tolerance
            assertFalse(sshFile.contains("cim:ControlArea.pTolerance"));

            // Check that tolerance is exported only if explicitly defined
            Area area = network.getAreas().iterator().next();
            area.setProperty(CgmesNames.P_TOLERANCE, "1.01");
            Path tmpDirWithCaTolerance = tmpDir.resolve("network-with-ca-tolerance");
            Files.createDirectories(tmpDirWithCaTolerance);
            sshFile = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDirWithCaTolerance);
            assertTrue(sshFile.contains("<cim:ControlArea.pTolerance>1.01</cim:ControlArea.pTolerance>"));
        }
    }

    private static final double EPSILON = 1e-10;
}
