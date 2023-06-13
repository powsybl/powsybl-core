/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.extensions.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ZipFileDataSource;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public abstract class AbstractCgmesMappingTest extends AbstractConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCgmesMappingTest.class);
    private static final String EXPORTED_CGMES_PREFIX = "exportedCgmes";
    private static final String CGMES_FORMAT = "CGMES";

    public void testExportUsingCgmesNamingStrategy(String namingStrategy, Network network, String baseName, Properties reimportParams, Set<String> knownErrorsSubstationsIds, ReadOnlyDataSource originalDataSource) throws IOException {
        Properties exportParams = new Properties();
        exportParams.put(CGMES_EXPORT_NAMING_STRATEGY, namingStrategy);
        String outputFolder = EXPORTED_CGMES_PREFIX + baseName;
        DataSource exportedCgmes = tmpDataSource(outputFolder, baseName);
        network.write(CGMES_FORMAT, exportParams, exportedCgmes);
        if (originalDataSource != null) {
            copyBoundary(outputFolder, baseName, originalDataSource);
        }

        // Load the exported CGMES model without the ID mapping,
        // to ensure that all objects have valid CGMES identifiers
        Network network1 = importExportedCgmesWithoutMapping(exportedCgmes, baseName, reimportParams);
        checkAllIdentifiersAreValidCimCgmesIdentifiers(network1);

        // Compare original network with re-imported using ID mapping
        // We do not compare XIIDM files, as the structure may have significant changes:
        // CGMES exported always node/breaker, if original was bus/branch a lot of different elements
        // Even if original was node/breaker, we may have introduced fictitious switches during import,
        // resulting in different number of nodes and connections

        // By default, the identity naming strategy is configured, we have to set a specific one if we have a mapping file
        Properties reimportParams1 = new Properties(reimportParams);
        reimportParams1.put(CGMES_IMPORT_ID_MAPPING_FILE_NAMING_STRATEGY, namingStrategy);

        Network networkActual = Importers.importData(CGMES_FORMAT, exportedCgmes, reimportParams1);
        Collection<Diff> diffs = compareNetworksUsingConnectedEquipment(network, networkActual, tmpDir.resolve(EXPORTED_CGMES_PREFIX + baseName));
        checkDiffs(diffs, knownErrorsSubstationsIds);

        // In the Network created from CGMES + mapping all Identifiables that do not have a valid CIM mRID
        // must have a valid UUID alias
        for (Identifiable<?> i : networkActual.getIdentifiables()) {
            if (!i.isFictitious() && !isValidCimMasterRID(i.getId())) {
                Optional<String> uuid = i.getAliasFromType(CONVERSION_CGMES_PREFIX_ALIAS_PROPERTIES + "UUID");
                assertTrue(uuid.isPresent());
                uuid.ifPresent(s -> assertTrue(isValidCimMasterRID(s)));
            }
        }

        // Now that we have valid identifiers stored as aliases, we should be able to re-export to CGMES
        // without the mapping generated in the previous step,
        // but keeping the same naming strategy to use aliases to fix bad mrids
        Properties reExportParams = exportParams;
        String reOutputFolder = "reExportedCgmes" + baseName;
        DataSource reExportedCgmes = tmpDataSource(reOutputFolder, baseName);
        networkActual.write(CGMES_FORMAT, reExportParams, reExportedCgmes);
        if (originalDataSource != null) {
            copyBoundary(reOutputFolder, baseName, originalDataSource);
        }
        Network networkActualReimportedWithoutMapping = Importers.importData(CGMES_FORMAT, reExportedCgmes, reimportParams1);
        // Convert to strings with newlines for easier visual comparison in case of differences
        assertEquals(
                Arrays.toString(network1.getIdentifiables().stream()
                        .filter(i -> !(i instanceof Network) && !i.isFictitious())
                        .map(i -> i.getType() + "::" + i.getId())
                        .sorted().toArray()).replace(",", System.lineSeparator()),
                Arrays.toString(networkActualReimportedWithoutMapping.getIdentifiables().stream()
                        .filter(i -> !(i instanceof Network) && !i.isFictitious())
                        .map(i -> i.getType() + "::" + i.getId())
                        .sorted().toArray()).replace(",", System.lineSeparator()));
    }

    private void checkDiffs(Collection<Diff> diffs, Set<String> knownErrorsSubstationsIds) {
        if (!diffs.isEmpty()) {
            LOG.error("differences found:");
            diffs.forEach(d -> LOG.error(d.toString()));
        }
        Collection<Diff> notExpected = diffs.stream().filter(d -> !knownErrorsSubstationsIds.contains(d.substationId)).collect(Collectors.toList());
        if (!notExpected.isEmpty()) {
            LOG.error("differences found and not previously known:");
            notExpected.forEach(d -> LOG.error(d.toString()));
            fail();
        }
    }

    static class Diff {
        String substationId;
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

                    // At least all the expected buses must be present in actual network,
                    // and maybe the actual contains additional buses
                    boolean isRelevantDiff = busesExpectedBusBreakerView.stream()
                            .anyMatch(b -> !busesActualBusBreakerView.contains(b));
                    if (isRelevantDiff) {
                        try {
                            vle.exportTopology(tmp.resolve(se.getNameOrId() + "-expected.gv"));
                            vla.exportTopology(tmp.resolve(se.getNameOrId() + "-actual.gv"));
                        } catch (IOException e) {
                            throw new PowsyblException(e);
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
        Path repackaged = tmpDir.resolve(EXPORTED_CGMES_PREFIX + baseName).resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with(dataSource.getBaseName() + "_EQ_BD.xml", Repackager::eqBd)
                .with(dataSource.getBaseName() + "_EQ.xml", Repackager::eq)
                .with(dataSource.getBaseName() + "_SSH.xml", Repackager::ssh)
                .with(dataSource.getBaseName() + "_TP.xml", Repackager::tp)
                .with(dataSource.getBaseName() + "_SV.xml", Repackager::sv);
        r.zip(repackaged);
        return Importers.importData(CGMES_FORMAT, new ZipFileDataSource(repackaged), reimportParams);
    }

    private void checkAllIdentifiersAreValidCimCgmesIdentifiers(Network network) {
        CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
        Supplier<Stream<String>> badIds = () -> Stream.of(
                        network.getIdentifiables().stream().filter(i -> !i.isFictitious()).map(Identifiable::getId),
                        // Some CGMES identifiers do not end as Network identifiables
                        cgmes.terminals().stream().map(o -> o.getId(CgmesNames.TERMINAL)),
                        cgmes.connectivityNodes().stream().map(o -> o.getId(CgmesNames.CONNECTIVITY_NODE)),
                        cgmes.topologicalNodes().stream().map(o -> o.getId(CgmesNames.TOPOLOGICAL_NODE)),
                        cgmes.topologicalIslands().stream().flatMap(o -> Stream.of(
                                o.getId(CgmesNames.TOPOLOGICAL_ISLAND),
                                o.getId(CgmesNames.ANGLEREF_TOPOLOGICALNODE),
                                o.getId(CgmesNames.TOPOLOGICAL_NODES))),
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
                .filter(id -> !isValidCimMasterRID(id));
        assertEquals(
                0,
                badIds.get().count(),
                String.format("Identifiers not valid as CIM mRIDs : %s", badIds.get().collect(Collectors.joining(","))));
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

    private DataSource tmpDataSource(String folder, String baseName) throws IOException {
        Path exportFolder = tmpDir.resolve(folder);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        return new FileDataSource(exportFolder, baseName);
    }

    private void copyBoundary(String outputFolderName, String baseName, ReadOnlyDataSource originalDataSource) throws IOException {
        Path outputFolder = tmpDir.resolve(outputFolderName);
        String eqbd = originalDataSource.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
        if (eqbd != null) {
            try (InputStream is = originalDataSource.newInputStream(eqbd)) {
                Files.copy(is, outputFolder.resolve(baseName + "_EQ_BD.xml"));
            }
        }
    }

    // FIXME(Luma) duplicated constants and methods

    private static final String CGMES_EXPORT_NAMING_STRATEGY = "iidm.export.cgmes.naming-strategy";
    private static final String CGMES_IMPORT_ID_MAPPING_FILE_NAMING_STRATEGY = "iidm.import.cgmes.id-mapping-file-naming-strategy";
    private static final String CONVERSION_CGMES_PREFIX_ALIAS_PROPERTIES = "CGMES.";

    private static final Pattern CIM_MRID_PATTERN = Pattern.compile("(?i)[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}");
    private static final Pattern URN_UUID_PATTERN = Pattern.compile("(?i)urn:uuid:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}");
    private static final Pattern ENTSOE_BD_EXCEPTIONS_PATTERN1 = Pattern.compile("(?i)[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{7}");
    private static final Pattern ENTSOE_BD_EXCEPTIONS_PATTERN2 = Pattern.compile("(?i)[a-f\\d]{8}[a-f\\d]{4}[a-f\\d]{4}[a-f\\d]{4}[a-f\\d]{12}");

    private static boolean isValidCimMasterRID(String id) {
        return CIM_MRID_PATTERN.matcher(id).matches()
                || URN_UUID_PATTERN.matcher(id).matches()
                || ENTSOE_BD_EXCEPTIONS_PATTERN1.matcher(id).matches()
                || ENTSOE_BD_EXCEPTIONS_PATTERN2.matcher(id).matches();
    }
}
