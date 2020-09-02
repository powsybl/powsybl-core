/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.update.NetworkChanges;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ExportTest {

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testSvExportAlternativesBusBranch() throws IOException {
        exportUsingCgmesModelUsingOnlyNetworkAndCompareSV(CgmesConformity1Catalog.smallBusBranch().dataSource());
    }

    @Ignore("not yet implemented")
    @Test
    public void testSvExportAlternativesNodeBreaker() throws IOException {
        exportUsingCgmesModelUsingOnlyNetworkAndCompareSV(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
    }

    private void exportUsingCgmesModelUsingOnlyNetworkAndCompareSV(ReadOnlyDataSource ds) throws IOException {
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), null);
        NetworkChanges.modifyStateVariables(network0);
        network0.setProperty("baseName", ds.getBaseName());

        CgmesExport e = new CgmesExport();

        // Export modified network to new CGMES using two alternatives
        DataSource tmpUsingCgmes = tmpDataSource(fileSystem, "usingCgmes");
        DataSource tmpUsingOnlyNetwork = tmpDataSource(fileSystem, "usingOnlyNetwork");
        Properties ep = new Properties();
        e.export(network0, ep, tmpUsingCgmes);
        ep.setProperty("cgmes.export.usingOnlyNetwork", "true");
        network0.removeExtension(CgmesModelExtension.class);
        e.export(network0, ep, tmpUsingOnlyNetwork);

        // Compare resulting SV of both variants
        String sv1 = tmpUsingCgmes.listNames(".*SV.*").parallelStream().findFirst().orElse("-");
        String sv2 = tmpUsingOnlyNetwork.listNames(".*SV.*").parallelStream().findFirst().orElse("-");
        LOG.debug("test SV export using CGMES original model and using only Network. Output files:");
        LOG.debug("   using CGMES        {}", sv1);
        LOG.debug("   using Network only {}", sv2);
        assertTrue(sv1.contains(ds.getBaseName()));
        assertTrue(sv2.contains(ds.getBaseName()));
        try (InputStream expected = tmpUsingCgmes.newInputStream(sv1)) {
            try (InputStream actual = tmpUsingOnlyNetwork.newInputStream(sv2)) {
                isOk(compare(diffSv(expected, actual).checkForSimilar()));
            }
        }
        try (InputStream expected = tmpUsingCgmes.newInputStream(sv1)) {
            try (InputStream actual = tmpUsingOnlyNetwork.newInputStream(sv2)) {
                onlyNodeListSequenceDiffs(compare(diffSv(expected, actual).checkForIdentical()));
            }
        }
    }

    DiffBuilder diffSv(InputStream expected, InputStream actual) {
        return selectingSvVoltageSameTopologicalNode(ignoringSvIds(onlySvVoltages(diff(expected, actual))));
    }

    private void isOk(Diff diff) {
        assertTrue(!diff.hasDifferences());
    }

    private void onlyNodeListSequenceDiffs(Diff diff) {
        for (Difference d : diff.getDifferences()) {
            assertEquals(ComparisonType.CHILD_NODELIST_SEQUENCE, d.getComparison().getType());
            assertEquals(ComparisonResult.SIMILAR, d.getResult());
        }
    }

    private DiffBuilder diff(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        return DiffBuilder.compare(control).withTest(test).ignoreWhitespace().ignoreComments();
    }

    private DiffBuilder onlySvVoltages(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> {
            return n.getLocalName().equals("RDF") || n.getLocalName().equals("SvVoltage");
        });
    }

    private static DiffBuilder ignoringSvIds(DiffBuilder diffBuilder) {
        return diffBuilder.withAttributeFilter(attr -> {
            // Identifiers of SV objects are not persistent,
            // can be completely ignored for comparison with control
            String elementName = attr.getOwnerElement().getLocalName();
            boolean isSvId = elementName != null && elementName.startsWith("Sv") && attr.getLocalName().equals("ID");
            return !isSvId;
        });
    }

    private static DiffBuilder selectingSvVoltageSameTopologicalNode(DiffBuilder diffBuilder) {
        Map<String, String> prefixUris = new HashMap<>(2);
        prefixUris.put("cim", CgmesExport.CIM_NAMESPACE);
        prefixUris.put("rdf", CgmesExport.RDF_NAMESPACE);
        ElementSelector elementSelector = ElementSelectors.conditionalBuilder().whenElementIsNamed("SvVoltage")
                .thenUse(ElementSelectors.byXPath("./cim:SvVoltage.TopologicalNode", prefixUris,
                        ElementSelectors.byNameAndAllAttributes))
                .elseUse(ElementSelectors.byName).build();
        return diffBuilder.withNodeMatcher(new DefaultNodeMatcher(elementSelector));
    }

    private static Diff compare(DiffBuilder diffBuilder) {
        Diff diff = diffBuilder.build();
        boolean hasDiff = diff.hasDifferences();
        if (hasDiff && LOG.isDebugEnabled()) {
            LOG.debug("Differences:");
            for (Difference d : diff.getDifferences()) {
                LOG.debug("  {}", d.getComparison().toString());
            }
        }
        return diff;
    }

    public static DataSource tmpDataSource(FileSystem fileSystem, String name) throws IOException {
        // Path exportFolder = fileSystem.getPath(name);
        Path exportFolder = Paths.get("/", "Users", "zamarrenolm", "Downloads", name);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;

    private static final Logger LOG = LoggerFactory.getLogger(ExportTest.class);
}
