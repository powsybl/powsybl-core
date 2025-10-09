/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.*;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class CgmesNamingStrategyTest extends AbstractSerDeTest {

    @Test
    void testExportUsingCgmesNamingStrategyCgmesMicroGrid() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource();
        Network network = Network.read(ds);
        network.setName("MicroGrid-NS-CGMES");
        testExport(network, ds, NamingStrategyFactory.CGMES);
    }

    @Test
    void testExportUsingCgmesNamingStrategyMicroGrid() throws IOException {
        // We select a case that contains invalid IDs
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledBadIds().dataSource();
        Network network = Network.read(ds);
        network.setName("MicroGrid-NS-CGMES_FIX_ALL_INVALID_IDS");
        testExport(network, ds, NamingStrategyFactory.CGMES_FIX_ALL_INVALID_IDS);
    }

    void testExport(Network network, ReadOnlyDataSource originalDataSource, String namingStrategy) throws IOException {
        String baseName = network.getNameOrId();

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.NAMING_STRATEGY, namingStrategy);
        exportParams.put(CgmesExport.EXPORT_SV_INJECTIONS_FOR_SLACKS, "false");

        String outputFolder = "exportedCgmes" + baseName;
        DataSource exportedCgmes = tmpDataSource(outputFolder, baseName);
        network.write("CGMES", exportParams, exportedCgmes);
        if (originalDataSource != null) {
            copyBoundary(outputFolder, baseName, originalDataSource);
        }

        // Load the exported CGMES model and check that all objects have valid CGMES identifiers
        Network network1 = Network.read(exportedCgmes);
        checkAllIdentifiersAreValidCimCgmesIdentifiers(network1);
        // Also, all Identifiables that do not have a valid CIM mRID must have a valid UUID alias
        for (Identifiable<?> i : network1.getIdentifiables()) {
            if (!i.isFictitious() && !i.getType().equals(IdentifiableType.TIE_LINE) && !CgmesExportUtil.isValidCimMasterRID(i.getId())) {
                Optional<String> uuid = i.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "UUID");
                assertTrue(uuid.isPresent());
                assertTrue(CgmesExportUtil.isValidCimMasterRID(uuid.get()));
            }
        }

        // Now that we have valid identifiers stored as aliases, we should be able to re-export to CGMES
        // with the same naming strategy to use aliases to fix bad mrids
        String reOutputFolder = "reExportedCgmes" + baseName;
        DataSource reExportedCgmes = tmpDataSource(reOutputFolder, baseName);
        network1.write("CGMES", exportParams, reExportedCgmes);
        if (originalDataSource != null) {
            copyBoundary(reOutputFolder, baseName, originalDataSource);
        }
        Network network1Reimported = Network.read(reExportedCgmes);
        // Convert to strings with newlines for easier visual comparison in case of differences
        assertEquals(
                Arrays.toString(network1.getIdentifiables().stream()
                        .filter(i -> !(i instanceof Network) && !i.isFictitious())
                        .map(i -> i.getType() + "::" + i.getId())
                        .sorted().toArray()).replace(",", System.lineSeparator()),
                Arrays.toString(network1Reimported.getIdentifiables().stream()
                        .filter(i -> !(i instanceof Network) && !i.isFictitious())
                        .map(i -> i.getType() + "::" + i.getId())
                        .sorted().toArray()).replace(",", System.lineSeparator()));
    }

    private void checkAllIdentifiersAreValidCimCgmesIdentifiers(Network network) {
        CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
        Supplier<Stream<String>> badIds = () -> Stream.of(
                        network.getIdentifiables().stream().filter(i -> !i.isFictitious() && !i.getType().equals(IdentifiableType.TIE_LINE)).map(Identifiable::getId),
                        // Some CGMES identifiers do not end as Network identifiables
                        cgmes.terminals().stream().map(o -> o.getId(CgmesNames.TERMINAL)),
                        cgmes.connectivityNodes().stream().map(o -> o.getId(CgmesNames.CONNECTIVITY_NODE)),
                        cgmes.topologicalNodes().stream().map(o -> o.getId(CgmesNames.TOPOLOGICAL_NODE)),
                        cgmes.topologicalIslands().stream().flatMap(o -> Stream.of(
                                o.getId(CgmesNames.TOPOLOGICAL_ISLAND),
                                o.getId(CgmesNames.ANGLEREF_TOPOLOGICALNODE),
                                o.getId(CgmesNames.TOPOLOGICAL_NODES))),
                        cgmes.topologicalIslands().stream().map(o -> o.getId(CgmesNames.ANGLEREF_TOPOLOGICALNODE)),
                        cgmes.transformerEnds().stream().map(o -> o.getId(CgmesNames.TRANSFORMER_END)),
                        cgmes.phaseTapChangers().stream().map(o -> o.getId(CgmesNames.PHASE_TAP_CHANGER)),
                        cgmes.ratioTapChangers().stream().map(o -> o.getId(CgmesNames.RATIO_TAP_CHANGER)),
                        cgmes.regulatingControls().stream().map(o -> o.getId("RegulatingControl")),
                        cgmes.controlAreas().stream().map(o -> o.getId("ControlArea")),
                        cgmes.synchronousMachinesGenerators().stream().map(o -> o.getId("GeneratingUnit")),
                        cgmes.operationalLimits().stream().map(o -> o.getId("OperationalLimit")),
                        cgmes.substations().stream().map(o -> o.getId("Region")),
                        cgmes.substations().stream().map(o -> o.getId("SubRegion"))
                )
                .flatMap(id -> id)
                .filter(id -> !CgmesExportUtil.isValidCimMasterRID(id));
        assertEquals(0,
                badIds.get().count(),
                String.format("Identifiers not valid as CIM mRIDs : %s", badIds.get().collect(Collectors.joining(","))));
    }

    private DataSource tmpDataSource(String folder, String baseName) throws IOException {
        Path exportFolder = tmpDir.resolve(folder);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        return new DirectoryDataSource(exportFolder, baseName);
    }

    private void copyBoundary(String outputFolderName, String baseName, ReadOnlyDataSource originalDataSource) throws IOException {
        Path outputFolder = tmpDir.resolve(outputFolderName);
        String eqbd = originalDataSource.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
        if (eqbd != null) {
            try (InputStream is = originalDataSource.newInputStream(eqbd)) {
                Files.copy(is, outputFolder.resolve(baseName + "_EQ_BD.xml"));
            }
        }
        String tpbd = originalDataSource.listNames(".*TP_BD.*").stream().findFirst().orElse(null);
        if (tpbd != null) {
            try (InputStream is = originalDataSource.newInputStream(tpbd)) {
                Files.copy(is, outputFolder.resolve(baseName + "_TP_BD.xml"));
            }
        }
    }
}
