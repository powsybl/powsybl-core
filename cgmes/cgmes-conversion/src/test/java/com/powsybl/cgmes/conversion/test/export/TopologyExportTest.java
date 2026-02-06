/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.CgmesExport.CIM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TopologyExportTest extends AbstractSerDeTest {

    @Test
    void smallGridHVDC() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerHvdcEqTp().dataSource()));
    }

    @Test
    void smallGridBusBranch() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallBusBranchEqTp().dataSource()));
    }

    @Test
    void smallGridNodeBreaker() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerEqTp().dataSource()));
    }

    @Test
    void smallGridNodeBreakerSsh() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerEqTpSsh().dataSource(), true));
    }

    @Test
    void exportPairedDanglingLinesInBusBreakerModelWithoutBoundaryCim16() throws IOException {
        Network network = createPairedDanglingLinesBusBreakerModel();
        assertTrue(exportPairedDanglingLinesWithoutBoundary(network, "16"));
    }

    @Test
    void exportPairedDanglingLinesInBusBreakerModelWithBoundaryCim16() throws IOException {
        Network network = createPairedDanglingLinesBusBreakerModel();
        assertTrue(exportPairedDanglingLinesWithBoundary(network, "16"));
    }

    @Test
    void exportPairedDanglingLinesInBusBreakerModelWithoutBoundaryCim100() throws IOException {
        Network network = createPairedDanglingLinesBusBreakerModel();
        assertTrue(exportPairedDanglingLinesWithoutBoundary(network, "100"));
    }

    @Test
    void exportPairedDanglingLinesInBusBreakerModelWithBoundaryCim100() throws IOException {
        Network network = createPairedDanglingLinesBusBreakerModel();
        assertTrue(exportPairedDanglingLinesWithBoundary(network, "100"));
    }

    @Test
    void exportPairedDanglingLinesInNodeBreakerModelWithoutBoundaryCim16() throws IOException {
        Network network = createPairedDanglingLinesNodeBreakerModel();
        assertTrue(exportPairedDanglingLinesWithoutBoundary(network, "16"));
    }

    @Test
    void exportPairedDanglingLinesInNodeBreakerModelWithBoundaryCim16() throws IOException {
        Network network = createPairedDanglingLinesNodeBreakerModel();
        assertTrue(exportPairedDanglingLinesWithBoundary(network, "16"));
    }

    @Test
    void exportPairedDanglingLinesInNodeBreakerModelWithoutBoundaryCim100() throws IOException {
        Network network = createPairedDanglingLinesNodeBreakerModel();
        assertTrue(exportPairedDanglingLinesWithoutBoundary(network, "100"));
    }

    @Test
    void exportPairedDanglingLinesInNodeBreakerModelWithBoundaryCim100() throws IOException {
        Network network = createPairedDanglingLinesNodeBreakerModel();
        assertTrue(exportPairedDanglingLinesWithBoundary(network, "100"));
    }

    private static boolean exportPairedDanglingLinesWithoutBoundary(Network network, String cimVersion) throws IOException {

        ReadOnlyDataSource ds = createBoundaryReadOnlyDataSource(cimVersion);

        String exportFolder = "/test-paired-dl-bus-breaker";
        String baseName = "pairedDanglingLines_exported";

        // Export without boundary data
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpExportDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportProperties = new Properties();
            exportProperties.put(CIM_VERSION, cimVersion);
            new CgmesExport().export(network, exportProperties, new DirectoryDataSource(tmpExportDir, baseName));

            Path repackaged = createRepackaged(ds, cimVersion, tmpExportDir);
            Network actual = Network.read(repackaged);

            assertEquals(0, actual.getTieLineCount());
            assertEquals(0, actual.getDanglingLineCount());
            assertEquals(2, actual.getLineCount());
            assertEquals(actual.getLine("danglingLine1").getTerminal2().getBusBreakerView().getBus().getId(), actual.getLine("danglingLine2").getTerminal2().getBusBreakerView().getBus().getId());
        }

        return true;
    }

    private static boolean exportPairedDanglingLinesWithBoundary(Network network, String cimVersion) throws IOException {

        ReadOnlyDataSource ds = createBoundaryReadOnlyDataSource(cimVersion);

        String exportFolder = "/test-paired-dl-bus-breaker";
        String baseName = "pairedDanglingLines_exported";

        // Export with boundary data
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);

            Path tmpExportDir = Files.createDirectory(fs.getPath(exportFolder));

            Path boundary = tmpExportDir.resolve("boundary.zip");
            Repackager rb;
            if (cimVersion.equals("16")) {
                rb = new Repackager(ds)
                        .with("pairedDanglingLines_EQ_BD.xml", Repackager::eqBd)
                        .with("pairedDanglingLines_TP_BD.xml", Repackager::tpBd);

            } else {
                rb = new Repackager(ds)
                        .with("pairedDanglingLines_EQ_BD.xml", Repackager::eqBd);
            }
            rb.zip(boundary);

            platformConfig.createModuleConfig("import-export-parameters-default-value")
                    .setStringProperty(CgmesImport.BOUNDARY_LOCATION, boundary.toString());

            Properties exportProperties = new Properties();
            exportProperties.put(CIM_VERSION, cimVersion);
            new CgmesExport(platformConfig).export(network, exportProperties, new DirectoryDataSource(tmpExportDir, baseName));

            Path repackaged = createRepackaged(ds, cimVersion, tmpExportDir);
            Network actual = Network.read(repackaged);

            assertEquals(1, actual.getTieLineCount());
            assertEquals(2, actual.getDanglingLineCount());
            assertTrue(actual.getDanglingLine("danglingLine1").isPaired());
            assertTrue(actual.getDanglingLine("danglingLine2").isPaired());
            assertEquals(0, actual.getLineCount());
        }

        return true;
    }

    private static ReadOnlyDataSource createBoundaryReadOnlyDataSource(String cimVersion) {
        String importDir = "/issues/export-paired-dangling-lines";
        ReadOnlyDataSource ds;
        if (cimVersion.equals("16")) {
            ds = new ResourceDataSource("CGMES input file(s)", new ResourceSet(importDir, "pairedDanglingLinesCim16_EQ_BD.xml", "pairedDanglingLinesCim16_TP_BD.xml"));
        } else {
            ds = new ResourceDataSource("CGMES input file(s)", new ResourceSet(importDir, "pairedDanglingLinesCim100_EQ_BD.xml"));
        }
        return ds;
    }

    private static Path createRepackaged(ReadOnlyDataSource ds, String cimVersion, Path tmpExportDir) throws IOException {
        Path repackaged = tmpExportDir.resolve("repackaged.zip");
        Repackager r;
        if (cimVersion.equals("16")) {
            r = new Repackager(ds)
                    .with("pairedDanglingLines_exported_EQ.xml", tmpExportDir.resolve("pairedDanglingLines_exported_EQ.xml"))
                    .with("pairedDanglingLines_exported_TP.xml", tmpExportDir.resolve("pairedDanglingLines_exported_TP.xml"))
                    .with("pairedDanglingLines_exported_SSH.xml", tmpExportDir.resolve("pairedDanglingLines_exported_SSH.xml"))
                    .with("pairedDanglingLines_EQ_BD.xml", Repackager::eqBd)
                    .with("pairedDanglingLines_TP_BD.xml", Repackager::tpBd);

        } else {
            r = new Repackager(ds)
                    .with("pairedDanglingLines_exported_EQ.xml", tmpExportDir.resolve("pairedDanglingLines_exported_EQ.xml"))
                    .with("pairedDanglingLines_exported_TP.xml", tmpExportDir.resolve("pairedDanglingLines_exported_TP.xml"))
                    .with("pairedDanglingLines_exported_SSH.xml", tmpExportDir.resolve("pairedDanglingLines_exported_SSH.xml"))
                    .with("pairedDanglingLines_EQ_BD.xml", Repackager::eqBd);

        }
        r.zip(repackaged);

        return repackaged;
    }

    private boolean test(ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return test(dataSource, false);
    }

    private boolean test(ReadOnlyDataSource dataSource, boolean importSsh) throws IOException, XMLStreamException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        // Import original
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);

        // Export TP
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        try (OutputStream os = Files.newOutputStream(exportedTp)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            TopologyExport.write(expected, writer, context);
        }

        // Zip with new TP
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", exportedTp)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        if (importSsh) {
            r.with("test_SSH.xml", Repackager::ssh);
        }
        r.zip(repackaged);

        // Import with new TP
        Network actual = Network.read(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), importParams);

        prepareNetworkForComparison(expected);
        prepareNetworkForComparison(actual);

        // Export original and with new TP
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setIncludedExtensions(Collections.emptySet());
        exportOptions.setSorted(true);
        Path expectedPath = tmpDir.resolve("expected.xml");
        Path actualPath = tmpDir.resolve("actual.xml");
        NetworkSerDe.write(expected, exportOptions, expectedPath);
        NetworkSerDe.write(actual, exportOptions, actualPath);
        NetworkSerDe.validate(actualPath);

        // Compare
        return ExportXmlCompare.compareNetworks(expectedPath, actualPath);
    }

    private void prepareNetworkForComparison(Network network) {
        network.getAliases().forEach(network::removeAlias);
        network.getIdentifiables().forEach(identifiable -> {
            identifiable.getAliases().forEach(identifiable::removeAlias);
            identifiable.removeProperty("v");
            identifiable.removeProperty("angle");
        });
        // As the network does not have SSH or SV data, the buses are not exported to the IIDM file,
        // in order to verify that the nodes that make up each bus are correct, Nomianl V are copied from the voltage level
        // to the bus.
        network.getBusView().getBuses().forEach(bus -> bus.setV(bus.getVoltageLevel().getNominalV()));
    }

    private static Network createPairedDanglingLinesBusBreakerModel() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "busBreakerModelTest");
        Substation substation1 = createSubstation(network, "substation1", Country.FR);
        VoltageLevel voltageLevel1 = createVoltageLevel(substation1, "voltageLevel1", TopologyKind.BUS_BREAKER);
        Substation substation2 = createSubstation(network, "substation2", Country.ES);
        VoltageLevel voltageLevel2 = createVoltageLevel(substation2, "voltageLevel2", TopologyKind.BUS_BREAKER);

        voltageLevel1.getBusBreakerView().newBus().setId("bus1").add();
        voltageLevel2.getBusBreakerView().newBus().setId("bus2").add();

        DanglingLine dl1 = createDanglingLineInBusBreakerModel(voltageLevel1, "danglingLine1", "bus1");
        DanglingLine dl2 = createDanglingLineInBusBreakerModel(voltageLevel2, "danglingLine2", "bus2");

        createTieLine(network, dl1.getId(), dl2.getId());

        return network;
    }

    private static Network createPairedDanglingLinesNodeBreakerModel() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "nodeBreakerModelTest");
        Substation substation1 = createSubstation(network, "substation1", Country.FR);
        VoltageLevel voltageLevel1 = createVoltageLevel(substation1, "voltageLevel1", TopologyKind.NODE_BREAKER);
        Substation substation2 = createSubstation(network, "substation2", Country.ES);
        VoltageLevel voltageLevel2 = createVoltageLevel(substation2, "voltageLevel2", TopologyKind.NODE_BREAKER);

        voltageLevel1.getNodeBreakerView().newBusbarSection().setId("bus1").setNode(1);
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(1).setNode2(10).add();

        voltageLevel2.getNodeBreakerView().newBusbarSection().setId("bus2").setNode(2);
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(20).add();

        DanglingLine dl1 = createDanglingLineInNodeBreakerModel(voltageLevel1, "danglingLine1", 10);
        DanglingLine dl2 = createDanglingLineInNodeBreakerModel(voltageLevel2, "danglingLine2", 20);

        createTieLine(network, dl1.getId(), dl2.getId());

        return network;
    }

    private static Substation createSubstation(Network network, String substationId, Country country) {
        return network.newSubstation()
                .setId(substationId)
                .setCountry(country)
                .add();
    }

    private static VoltageLevel createVoltageLevel(Substation substation, String voltageLevelId, TopologyKind topologyKind) {
        return substation.newVoltageLevel()
                .setId(voltageLevelId)
                .setNominalV(400.0)
                .setTopologyKind(topologyKind)
                .add();
    }

    private static DanglingLine createDanglingLineInBusBreakerModel(VoltageLevel voltageLevel, String danglingLineId, String busId) {
        return voltageLevel.newDanglingLine()
                .setId(danglingLineId)
                .setR(0.001)
                .setX(0.001)
                .setP0(0.0)
                .setQ0(0.0)
                .setPairingKey("XCA_AL11")
                .setBus(busId)
                .setConnectableBus(busId)
                .add();
    }

    private static DanglingLine createDanglingLineInNodeBreakerModel(VoltageLevel voltageLevel, String danglingLineId, int node) {
        return voltageLevel.newDanglingLine()
                .setId(danglingLineId)
                .setR(0.001)
                .setX(0.001)
                .setP0(0.0)
                .setQ0(0.0)
                .setPairingKey("XCA_AL11")
                .setNode(node)
                .add();
    }

    private static void createTieLine(Network network, String danglingLineId1, String danglingLineId2) {
        network.newTieLine()
                .setId("tieLine")
                .setDanglingLine1(danglingLineId1)
                .setDanglingLine2(danglingLineId2)
                .add();
    }
}
