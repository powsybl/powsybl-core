/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelectors;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class StateVariablesAdderTest {

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
    public void conformityMicroGridBaseCase() throws IOException {
        importExportTest(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource());
    }

    @Test
    public void smallGrigNodeBreaker() throws IOException {
        importExportTest(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
    }

    @Test
    public void svAlternatives() throws IOException {
        exportUsingCgmesModelUsingOnlyNetworkAndCompareSV(CgmesConformity1Catalog.smallBusBranch().dataSource());
        exportUsingCgmesModelUsingOnlyNetworkAndCompareSV(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
    }

    private void exportUsingCgmesModelUsingOnlyNetworkAndCompareSV(ReadOnlyDataSource ds) throws IOException {
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), importParameters("false"));
        NetworkChanges.modifyStateVariables(network0);
        network0.setProperty("baseName",  ds.getBaseName());

        CgmesExport e = new CgmesExport();

        // Export modified network to new CGMES using two different variants
        DataSource tmpUsingCgmes = tmpDataSource("usingCgmes");
        DataSource tmpUsingOnlyNetwork = tmpDataSource("usingOnlyNetwork");
        Properties ep = new Properties();
        e.export(network0, ep, tmpUsingCgmes);
        ep.setProperty("cgmes.export.usingOnlyNetwork", "true");
        network0.removeExtension(CgmesModelExtension.class);
        e.export(network0, ep, tmpUsingOnlyNetwork);

        // Compare resulting SV of both variants
        System.err.println("Output files:");
        String sv1 = tmpUsingCgmes.listNames(".*SV.*").parallelStream().findFirst().orElse("-");
        String sv2 = tmpUsingOnlyNetwork.listNames(".*SV.*").parallelStream().findFirst().orElse("-");
        System.err.println("   using CGMES " + sv1);
        System.err.println("   using CGMES " + sv2);
        assertTrue(sv1.contains(ds.getBaseName()));
        assertTrue(sv2.contains(ds.getBaseName()));
        try (InputStream is1 = tmpUsingCgmes.newInputStream(sv1)) {
            try (InputStream is2 = tmpUsingOnlyNetwork.newInputStream(sv2)) {
                compareXmlStepByStep(is1, is2);
            }
        }
    }

    protected static void compareXmlStepByStep(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Map<String, String> prefixUris = new HashMap<>(2);
        prefixUris.put("cim", CgmesExport.CIM_NAMESPACE);
        prefixUris.put("rdf", CgmesExport.RDF_NAMESPACE);
        Diff myDiff = DiffBuilder.compare(control).withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withNodeFilter(node -> {
                    String localName = node.getLocalName();
                    // FIXME(Luma) Only checking voltages at this stage
                    return localName != null && (localName.equals("RDF") || localName.startsWith("SvVoltage"));
                })
                .withAttributeFilter(attr -> {
                    // Identifiers of SV objects are not persistent,
                    // can be completely ignored for comparison with control
                    String elementName = attr.getOwnerElement().getLocalName();
                    boolean ignored = elementName != null && elementName.startsWith("Sv") && attr.getLocalName().equals("ID");
                    System.err.println("elem " + elementName + ", attr " + attr + ", ignored ? " + ignored);
                    return !ignored;
                })
                // FIXME(Luma) Trying to compare SvVoltage objects
                // that have same topological node
                // (does not work)
                .withNodeMatcher(new DefaultNodeMatcher(
                        ElementSelectors.conditionalBuilder()
                            .whenElementIsNamed("SvVoltage")
                            .thenUse(ElementSelectors.byXPath("./cim:SvVoltage.TopologicalNode/@rdf:resource", prefixUris, ElementSelectors.byNameAndText))
                            .elseUse(ElementSelectors.byName)
                            .build()))
                .withComparisonListeners((comparison, outcome) -> {
                    System.err.println("comparison " + comparison.getType() + " was " + outcome);
                    System.err.println("    " + comparison.getControlDetails().getXPath());
                    System.err.println("    " + comparison.getTestDetails().getXPath());
                    System.err.printf("");
                })
                .build();
        boolean hasDiff = myDiff.hasDifferences();
        if (hasDiff) {
            for (Difference diff : myDiff.getDifferences()) {
                System.out.println(diff.getComparison().toString());
            }
        }
        assertFalse(hasDiff);
    }

    private void importExportTest(ReadOnlyDataSource ds) throws IOException {
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), importParameters("false"));
        CgmesModelExtension ext0 = network0.getExtension(CgmesModelExtension.class);
        if (ext0 == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        CgmesModel cgmes0 = ext0.getCgmesModel();

        PropertyBags topologicalIslands0 = cgmes0.topologicalIslands();
        PropertyBags fullModel0 = cgmes0.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());
        NetworkChanges.modifyStateVariables(network0);

        // Export modified network to new CGMES
        DataSource tmp = tmpDataSource("export-modified");
        CgmesExport e = new CgmesExport();
        e.export(network0, new Properties(), tmp);

        // Recreate new network with modified State Variables
        Network network1 = cgmesImport.importData(tmp, NetworkFactory.findDefault(), importParameters("false"));
        CgmesModelExtension ext1 = network1.getExtension(CgmesModelExtension.class);
        if (ext1 == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        CgmesModel cgmes1 = ext1.getCgmesModel();
        PropertyBags topologicalIslands1 = cgmes1.topologicalIslands();
        PropertyBags fullModel1 = cgmes1.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());

        // Compare
        assertEquals(topologicalIslands0, topologicalIslands1);
        assertEquals(fullModel0, fullModel1);
        compareVoltages(network0, network1);
    }

    private Properties importParameters(String convertBoundary) {
        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.CONVERT_BOUNDARY, convertBoundary);
        return importParameters;
    }

    private void compareVoltages(Network network0, Network network1) {
        new Comparison(network0, network1, new ComparisonConfig()).compareBuses();
    }

    private DataSource tmpDataSource(String name) throws IOException {
        Path exportFolder = fileSystem.getPath(name);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;
}
