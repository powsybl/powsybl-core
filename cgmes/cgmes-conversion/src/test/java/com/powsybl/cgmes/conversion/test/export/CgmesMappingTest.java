/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.NamingStrategyFactory;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLImporter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesMappingTest extends AbstractConverterTest {

    @Test
    public void testExportUsingCgmesNamingStrategyNordic32() throws IOException {
        testExportUsingCgmesNamingStrategy("nordic32", "G9_______SM");
    }

    @Test
    public void testExportUsingCgmesNamingStrategyIEEE14() throws IOException {
        testExportUsingCgmesNamingStrategy("ieee14", "GEN____8_SM");
    }

    private void testExportUsingCgmesNamingStrategy(String baseName, String generatorForSlack) throws IOException {
        ReadOnlyDataSource inputIidm = new ResourceDataSource(baseName, new ResourceSet("/cim14", baseName + ".xiidm"));
        Network network = new XMLImporter().importData(inputIidm, NetworkFactory.findDefault(), null);
        // Force writing CGMES topological island by assigning a slack bus
        SlackTerminal.attach(network.getGenerator(generatorForSlack).getTerminal().getBusBreakerView().getBus());
        testExportUsingCgmesNamingStrategy(network, baseName);
    }

    public void testExportUsingCgmesNamingStrategy(Network network, String baseName) throws IOException {
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.NAMING_STRATEGY, NamingStrategyFactory.CGMES);
        DataSource exportedCgmes = tmpDataSource("exportedCgmes", baseName);
        Exporters.export("CGMES", network, exportParams, exportedCgmes);

        // Load the exported CGMES model without the ID mapping,
        // to ensure that all objects have valid CGMES identifiers

        // Build a zip file that does not contain the CSV file for the id mappings, only CGMES exported files
        Path repackaged = tmpDir.resolve("exportedCgmes").resolve("repackaged.zip");
        Repackager r = new Repackager(exportedCgmes)
                .with("test_EQ.xml", Repackager::eq)
                .with("test_SSH.xml", Repackager::ssh)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SV.xml", Repackager::sv);
        r.zip(repackaged);

        Network network1 = Importers.importData("CGMES", new ZipFileDataSource(repackaged), null);
        CgmesModel cgmes = network1.getExtension(CgmesModelExtension.class).getCgmesModel();
        Supplier<Stream<String>> badIds = () -> Stream.of(
                        network1.getIdentifiables().stream().filter(i -> !i.isFictitious()).map(Identifiable::getId),
                        // Some CGMES identifiers do not end Network identifiables
                        cgmes.connectivityNodes().stream().map(o -> o.getId(CgmesNames.CONNECTIVITY_NODE)),
                        cgmes.topologicalNodes().stream().map(o -> o.getId(CgmesNames.TOPOLOGICAL_NODE)),
                        cgmes.topologicalIslands().stream().map(o -> o.getId(CgmesNames.TOPOLOGICAL_ISLAND)),
                        cgmes.transformerEnds().stream().map(o -> o.getId(CgmesNames.TRANSFORMER_END)),
                        cgmes.phaseTapChangers().stream().map(o -> o.getId(CgmesNames.PHASE_TAP_CHANGER)),
                        cgmes.ratioTapChangers().stream().map(o -> o.getId(CgmesNames.RATIO_TAP_CHANGER)),
                        cgmes.operationalLimits().stream().map(o -> o.getId("OperationalLimit"))
                )
                .flatMap(id -> id)
                .filter(id -> !CgmesExportUtil.isValidCimMasterRID(id));
        assertEquals(String.format("Identifiers not valid as CIM mRIDs : %s", badIds.get().collect(Collectors.joining(","))),
                0,
                badIds.get().count());

        // Compare original network with re-imported using id mappings
        // We do not compare XIIDM files, as the structure may have significant changes:
        // CGMES exported always node/breaker, if original was bus/branch a lot of different elements
        // Even if original was node/breaker, we may have introduced fictitious switches during import,
        // resulting in different number of nodes and connections
        Network networkActual = Importers.importData("CGMES", exportedCgmes, null);
        Network networkExpected = network;
        for (Substation se : networkExpected.getSubstations()) {
            Substation sa = networkActual.getSubstation(se.getId());
            assertEquals(se.getNameOrId(), sa.getNameOrId());
            for (VoltageLevel vle : se.getVoltageLevels()) {
                VoltageLevel vla = networkActual.getVoltageLevel(vle.getId());
                assertEquals(vle.getNameOrId(), vla.getNameOrId());
                SortedSet<String> busesExpected = buildBusIdsBasedOnConnectedEquipment(vle);
                SortedSet<String> busesActual = buildBusIdsBasedOnConnectedEquipment(vla);
                assertEquals(busesExpected, busesActual);
            }
        }
    }

    private static SortedSet<String> buildBusIdsBasedOnConnectedEquipment(VoltageLevel vl) {
        SortedSet<String> busIds = new TreeSet<>();
        for (Bus be : vl.getBusView().getBuses()) {
            // Build an id for the bus based on the concat of ids of connected equipment
            SortedSet<String> eqIds = new TreeSet<>();
            be.getConnectedTerminals().iterator().forEachRemaining(t -> eqIds.add(t.getConnectable().getId()));
            String busId = String.join(",", eqIds);
            busIds.add(busId);
        }
        return busIds;
    }

    @Test
    public void compareCgmesAndIidmExports() throws IOException {
        String baseName = "nordic32";
        ReadOnlyDataSource dataSource = new ResourceDataSource(baseName, new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        DataSource exportDataSource1 = tmpDataSource("export1", baseName);
        DataSource exportDataSource2 = tmpDataSource("export2", baseName);
        exportNetwork(network, exportDataSource1, baseName);
        Network network1 = export2IidmAndImport(network);
        network1.setCaseDate(network.getCaseDate());
        exportNetwork(network1, exportDataSource2, baseName);
        compareFiles("export1", "export2");
    }

    @Test
    public void compare2Exports() throws IOException {
        String baseName = "nordic32";
        ReadOnlyDataSource dataSource = new ResourceDataSource(baseName, new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        DataSource exportDataSource1 = tmpDataSource("export1", baseName);
        DataSource exportDataSource2 = tmpDataSource("export2", baseName);
        exportNetwork(network, exportDataSource1, baseName);
        exportNetwork(network, exportDataSource2, baseName);
        compareFiles("export1", "export2");
    }

    private void compareFiles(String export1, String export2) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(tmpDir.resolve(export1))) {
            files = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::ignoringCreatedTime,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringFullModelAbout,
                ExportXmlCompare::ignoringFullModelDependentOn,
                ExportXmlCompare::ignoringOperationalLimitIds,
                ExportXmlCompare::ignoringSVIds);
        for (Path file : files) {
            ExportXmlCompare.compareNetworks(file, tmpDir.resolve(export2).resolve(file.getFileName().toString()), knownDiffs);
        }

    }

    private void exportNetwork(Network network, DataSource exportDataSource, String baseName) {
        CgmesExport e = new CgmesExport();
        Properties ep = new Properties();
        ep.setProperty(CgmesExport.BASE_NAME, baseName);
        e.export(network, ep, exportDataSource);
    }

    private Network export2IidmAndImport(Network network) {
        NetworkXml.write(network, tmpDir.resolve("export.iidm"));
        return NetworkXml.read(tmpDir.resolve("export.iidm"));
    }

    private DataSource tmpDataSource(String name, String baseName) throws IOException {
        Path exportFolder = tmpDir.resolve(name);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        return new FileDataSource(exportFolder, baseName);
    }
}
