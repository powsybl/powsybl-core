/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.*;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLImporter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesMappingTest extends AbstractCgmesMappingTest {

    @Test
    public void testExplicitMappingConstructors() {
        AbstractCgmesAliasNamingStrategy nss = new SimpleCgmesAliasNamingStrategy(Map.of("uuid1", "1"));
        assertEquals("uuid1", nss.getCgmesId("1"));
        AbstractCgmesAliasNamingStrategy nsf = new FixedCgmesAliasNamingStrategy(Map.of("uuid1", "1"));
        assertEquals("uuid1", nsf.getCgmesId("1"));
    }

    @Test
    public void testExportUsingCgmesNamingStrategyNordic32() throws IOException {
        testExportUsingCgmesNamingStrategy(NamingStrategyFactory.CGMES, "nordic32", "G9_______SM");
    }

    @Test
    public void testExportUsingCgmesNamingStrategyIEEE14() throws IOException {
        testExportUsingCgmesNamingStrategy(NamingStrategyFactory.CGMES, "ieee14", "GEN____8_SM");
    }

    @Test
    public void testExportUsingCgmesNamingStrategyMicroGrid() throws IOException {
        // We select a case that contains invalid IDs
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledBadIds().dataSource();
        Network network = Importers.importData("CGMES", ds, null);
        testExportUsingCgmesNamingStrategy(NamingStrategyFactory.CGMES_FIX_ALL_INVALID_IDS, network, "MicroGrid", null, Collections.emptySet(), ds);
    }

    private void testExportUsingCgmesNamingStrategy(String namingStrategy, String baseName, String generatorForSlack) throws IOException {
        ReadOnlyDataSource inputIidm = new ResourceDataSource(baseName, new ResourceSet("/cim14", baseName + ".xiidm"));
        Network network = new XMLImporter().importData(inputIidm, NetworkFactory.findDefault(), null);
        // Force writing CGMES topological island by assigning a slack bus
        SlackTerminal.attach(network.getGenerator(generatorForSlack).getTerminal().getBusBreakerView().getBus());
        testExportUsingCgmesNamingStrategy(namingStrategy, network, baseName, null, Collections.emptySet(), null);
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
}
