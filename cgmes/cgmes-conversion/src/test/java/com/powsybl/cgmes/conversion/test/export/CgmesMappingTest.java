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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesMappingTest extends AbstractConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesMappingTest.class);

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
        testExportUsingCgmesNamingStrategy(network, baseName, null, Collections.emptySet());
    }

    public void testExportUsingCgmesNamingStrategy(Network network, String baseName, Properties reimportParams, Set<String> knownErrorsSubstationsIds) throws IOException {
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.NAMING_STRATEGY, NamingStrategyFactory.CGMES);
        DataSource exportedCgmes = tmpDataSource("exportedCgmes" + baseName, baseName);
        Exporters.export("CGMES", network, exportParams, exportedCgmes);

        // Load the exported CGMES model without the ID mapping,
        // to ensure that all objects have valid CGMES identifiers
        Network network1 = importExportedCgmesWithoutMapping(exportedCgmes, baseName, reimportParams);
        checkAllIdentifiersAreValidCimCgmesIdentifiers(network1);

        // Compare original network with re-imported using ID mapping
        // We do not compare XIIDM files, as the structure may have significant changes:
        // CGMES exported always node/breaker, if original was bus/branch a lot of different elements
        // Even if original was node/breaker, we may have introduced fictitious switches during import,
        // resulting in different number of nodes and connections
        Network networkActual = Importers.importData("CGMES", exportedCgmes, reimportParams);
        Collection<Diff> diffs = compareNetworksUsingConnectedEquipment(network, networkActual, tmpDir.resolve("exportedCgmes" + baseName));
        checkDiffs(diffs, knownErrorsSubstationsIds);
    }

    private void checkDiffs(Collection<Diff> diffs, Set<String> knownErrorsSubstationsIds) {
        if (diffs.size() > 0) {
            LOG.error("differences found:");
            diffs.forEach(d -> LOG.error(d.toString()));
        }
        Collection<Diff> notExpected = diffs.stream().filter(d -> !knownErrorsSubstationsIds.contains(d.substationId)).collect(Collectors.toList());
        if (notExpected.size() > 0) {
            System.out.println("differences found and not previously known:");
            notExpected.forEach(d -> LOG.error(d.toString()));
            fail();
        }
    }

    static class Diff {
        String substationId;
        String voltageLevelId;
        SortedSet<String> busesExpectedBusView;
        SortedSet<String> busesActualBusView;
        SortedSet<String> busesExpectedBusBreakerView;
        SortedSet<String> busesActualBusBreakerView;

        @Override
        public String toString() {
            return "RelevantDiff in substation " + substationId + System.lineSeparator() +
                    "  BusView expected" + System.lineSeparator() +
                    "    " + busesExpectedBusView.stream().collect(Collectors.joining(System.lineSeparator() + "    ")) + System.lineSeparator() +
                    "  BusView actual" + System.lineSeparator() +
                    "    " + busesActualBusView.stream().collect(Collectors.joining(System.lineSeparator() + "    ")) + System.lineSeparator() +
                    "  BusBreakerView expected" + System.lineSeparator() +
                    "    " + busesExpectedBusBreakerView.stream().collect(Collectors.joining(System.lineSeparator() + "    ")) + System.lineSeparator() +
                    "  BusBreakerView actual" + System.lineSeparator() +
                    "    " + busesActualBusBreakerView.stream().collect(Collectors.joining(System.lineSeparator() + "    ")) + System.lineSeparator();
        }
    }

    private Collection<Diff> compareNetworksUsingConnectedEquipment(Network expected, Network actual, Path tmp) {
        Collection<Diff> diffs = new ArrayList<>();
        for (Substation se : expected.getSubstations()) {
            Substation sa = actual.getSubstation(se.getId());
            assertEquals(se.getNameOrId(), sa.getNameOrId());
            for (VoltageLevel vle : se.getVoltageLevels()) {
                VoltageLevel vla = actual.getVoltageLevel(vle.getId());
                assertEquals(vle.getNameOrId(), vla.getNameOrId());
                SortedSet<String> busesExpectedBusView = buildBusIdsBasedOnConnectedEquipment(vle.getBusView().getBuses());
                SortedSet<String> busesActualBusView = buildBusIdsBasedOnConnectedEquipment(vla.getBusView().getBuses());
                if (!busesExpectedBusView.equals(busesActualBusView)) {

                    // Because we may start from a bus/branch network and compare to a node/breaker reimported network
                    // We may have some mismatches in the calculated buses.
                    // This happens in buses where only one line ends in the original network,
                    // they are re-imported with no bus at bus view level

                    // For these situations, we look at the bus/breaker level
                    // At this level, all the buses in the original network must be present in the reimported network
                    // Maybe there could be more bus/breaker view buses in the re-imported network,
                    // representing the end points of disconnected equipment,
                    // this is way we do not check the two sets of buses with "equals"

                    SortedSet<String> busesExpectedBusBreakerView = buildBusIdsBasedOnConnectedEquipment(vle.getBusBreakerView().getBuses());
                    SortedSet<String> busesActualBusBreakerView = buildBusIdsBasedOnConnectedEquipment(vla.getBusBreakerView().getBuses());

                    //assertEquals(busesExpectedBusBreakerView, busesActualBusBreakerView);
                    // At least all the expected buses must be present in actual network,
                    // and maybe the actual contains additional buses
                    boolean isRelevantDiff = busesExpectedBusBreakerView.stream()
                            .anyMatch(b -> !busesActualBusBreakerView.contains(b));
                    if (isRelevantDiff) {
                        try {
                            vle.exportTopology(tmp.resolve(se.getNameOrId() + "-expected.gv"));
                            vla.exportTopology(tmp.resolve(se.getNameOrId() + "-actual.gv"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Diff diff = new Diff();
                        diff.substationId = se.getId();
                        diff.busesExpectedBusView = busesExpectedBusView;
                        diff.busesActualBusView = busesActualBusView;
                        diff.busesExpectedBusBreakerView = busesExpectedBusBreakerView;
                        diff.busesActualBusBreakerView = busesActualBusBreakerView;
                        diffs.add(diff);
                    }
                }
            }
        }
        return diffs;
    }

    private Network importExportedCgmesWithoutMapping(ReadOnlyDataSource dataSource, String baseName, Properties reimportParams) throws IOException {
        // Build a zip file that does not contain the CSV file for the id mappings, only CGMES exported files
        Path repackaged = tmpDir.resolve("exportedCgmes" + baseName).resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with(dataSource.getBaseName() + "_EQ.xml", Repackager::eq)
                .with(dataSource.getBaseName() + "_SSH.xml", Repackager::ssh)
                .with(dataSource.getBaseName() + "_TP.xml", Repackager::tp)
                .with(dataSource.getBaseName() + "_SV.xml", Repackager::sv);
        r.zip(repackaged);
        return Importers.importData("CGMES", new ZipFileDataSource(repackaged), reimportParams);
    }

    private void checkAllIdentifiersAreValidCimCgmesIdentifiers(Network network) {
        CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
        Supplier<Stream<String>> badIds = () -> Stream.of(
                        network.getIdentifiables().stream().filter(i -> !i.isFictitious()).map(Identifiable::getId),
                        // Some CGMES identifiers do not end as Network identifiables
                        cgmes.terminals().stream().map(o -> o.getId(CgmesNames.TERMINAL)),
                        cgmes.connectivityNodes().stream().map(o -> o.getId(CgmesNames.CONNECTIVITY_NODE)),
                        cgmes.topologicalNodes().stream().map(o -> o.getId(CgmesNames.TOPOLOGICAL_NODE)),
                        cgmes.topologicalIslands().stream().map(o -> o.getId(CgmesNames.TOPOLOGICAL_ISLAND)),
                        cgmes.transformerEnds().stream().map(o -> o.getId(CgmesNames.TRANSFORMER_END)),
                        cgmes.phaseTapChangers().stream().map(o -> o.getId(CgmesNames.PHASE_TAP_CHANGER)),
                        cgmes.ratioTapChangers().stream().map(o -> o.getId(CgmesNames.RATIO_TAP_CHANGER)),
                        cgmes.regulatingControls().stream().map(o -> o.getId("RegulatingControl")),
                        cgmes.controlAreas().stream().map(o -> o.getId("ControlArea")),
                        cgmes.synchronousMachines().stream().map(o -> o.getId("GeneratingUnit")),
                        cgmes.operationalLimits().stream().map(o -> o.getId("OperationalLimit")),
                        cgmes.substations().stream().map(o -> o.getId("Region")),
                        cgmes.substations().stream().map(o -> o.getId("SubRegion"))
                )
                .flatMap(id -> id)
                .filter(id -> !CgmesExportUtil.isValidCimMasterRID(id));
        assertEquals(String.format("Identifiers not valid as CIM mRIDs : %s", badIds.get().collect(Collectors.joining(","))),
                0,
                badIds.get().count());
    }

    private static SortedSet<String> buildBusIdsBasedOnConnectedEquipment(Iterable<Bus> buses) {
        SortedSet<String> busIds = new TreeSet<>();
        for (Bus be : buses) {
            // Build an id for the bus based on the concat of ids of connected equipment
            SortedSet<String> eqIds = new TreeSet<>();
            be.getConnectedTerminals().iterator().forEachRemaining(t -> eqIds.add(t.getConnectable().getId()));
            // Ignore empty buses
            if (!eqIds.isEmpty()) {
                String busId = String.join(",", eqIds);
                busIds.add(busId);
            }
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
