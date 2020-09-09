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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.update.NetworkChanges;
import com.powsybl.cgmes.model.CgmesNames;
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
    public void testExportAlternativesBusBranch() throws IOException {
        DifferenceEvaluator knownDiffs = this::ignoringJunctionsTerminals;
        exportUsingCgmesModelUsingOnlyNetworkAndCompare(CgmesConformity1Catalog.smallBusBranch().dataSource(), knownDiffs);
    }

    @Ignore("not yet implemented")
    @Test
    public void testExportAlternativesNodeBreaker() throws IOException {
        DifferenceEvaluator knownDiffs = this::ignoringJunctionsTerminals;
        exportUsingCgmesModelUsingOnlyNetworkAndCompare(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), knownDiffs);
    }

    static final Set<String> JUNCTIONS_TERMINALS = Stream.of(
            "#_65a95678-1819-43cd-94d2-a03756822725",
            "#_5c96df9d-39fa-46fe-a424-7b3e50184f79",
            "#_9c6a1e49-17b2-46fc-8e32-c95dec33eba8")
            .collect(Collectors.toCollection(HashSet::new));

    ComparisonResult ignoringJunctionsTerminals(Comparison comparison, ComparisonResult result) {
        // If control node is a terminal of a junction, ignore the difference
        // Means that we also have to ignore length of children of RDF element
        if (result == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
                && comparison.getControlDetails().getTarget().getLocalName().equals("RDF")) {
                return ComparisonResult.EQUAL;
            } else if (isJunctionTerminal(comparison.getControlDetails().getTarget())) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    private boolean isJunctionTerminal(Node n) {
        if (n != null && n.getLocalName().equals(CgmesNames.TERMINAL)) {
            String about = n.getAttributes().getNamedItemNS(CgmesExport.RDF_NAMESPACE, "about").getTextContent();
            if (JUNCTIONS_TERMINALS.contains(about)) {
                return true;
            }
        }
        return false;
    }

    private void exportUsingCgmesModelUsingOnlyNetworkAndCompare(ReadOnlyDataSource ds, DifferenceEvaluator knownDiffs) throws IOException {
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

        // Check resulting SV and SSH of both variants
        compare(tmpUsingCgmes, tmpUsingOnlyNetwork, "SV", this::diffSV, knownDiffs, ds.getBaseName());
        compare(tmpUsingCgmes, tmpUsingOnlyNetwork, "SSH", this::diffSSH, knownDiffs, ds.getBaseName());
    }

    static interface DifferenceBuilder {
        DiffBuilder build(InputStream control, InputStream test, DifferenceEvaluator de);
    }

    private void compare(DataSource dsExpected, DataSource dsActual, String profile, DifferenceBuilder diff, DifferenceEvaluator knownDiffs, String originalBaseName)
            throws IOException {
        String svExpected = dsExpected.listNames(".*" + profile + ".*").stream().findFirst().orElse("-");
        String svActual = dsActual.listNames(".*" + profile + ".*").stream().findFirst().orElse("-");
        LOG.debug("Compare {} export using CGMES original model and using only Network. Files:", profile);
        LOG.debug("   using CGMES        {}", svExpected);
        LOG.debug("   using Network only {}", svActual);
        assertTrue(svExpected.contains(originalBaseName));
        assertTrue(svActual.contains(originalBaseName));
        // Check that files are similar according to the diff function given
        try (InputStream expected = dsExpected.newInputStream(svExpected)) {
            try (InputStream actual = dsActual.newInputStream(svActual)) {
                isOk(compare(diff.build(expected, actual, knownDiffs).checkForSimilar()));
            }
        }
        // Check again that only differences reported when checking for identical contents are the order of elements
        try (InputStream expected = dsExpected.newInputStream(svExpected)) {
            try (InputStream actual = dsActual.newInputStream(svActual)) {
                onlyNodeListSequenceDiffs(compare(diff.build(expected, actual, knownDiffs).checkForIdentical()));
            }
        }
    }

    DiffBuilder diffSV(InputStream expected, InputStream actual, DifferenceEvaluator de) {
        return selectingEquivalentSvObjects(ignoringNonPersistentIds(withSelectedSvNodes(diff(expected, actual, de))));
    }

    DiffBuilder diffSSH(InputStream expected, InputStream actual, DifferenceEvaluator de) {
        return selectingEquivalentSshObjects(withSelectedSshNodes(diff(expected, actual, de)));
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

    private DiffBuilder diff(InputStream expected, InputStream actual, DifferenceEvaluator de) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        return DiffBuilder.compare(control).withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(DifferenceEvaluators.Default, de))
                .withComparisonListeners(ExportTest::debugComparison);
    }

    private static void debugComparison(Comparison comparison, ComparisonResult comparisonResult) {
        if (comparisonResult.equals(ComparisonResult.DIFFERENT)) {
            LOG.error("comparison {}", comparison.getType());
            LOG.error("    control {}", comparison.getControlDetails().getXPath());
            debugNode(comparison.getControlDetails().getTarget());
            LOG.error("    test    {}", comparison.getTestDetails().getXPath());
            debugNode(comparison.getTestDetails().getTarget());
            LOG.error("    result  {}", comparisonResult);
        }
    }

    private static void debugNode(Node n) {
        if (n != null) {
            debugAttributes(n, "            ");
            int maxNodes = 5;
            for (int k = 0; k < maxNodes && k < n.getChildNodes().getLength(); k++) {
                Node n1 = n.getChildNodes().item(k);
                LOG.error("            {} {}", n1.getLocalName(), n1.getTextContent());
                debugAttributes(n1, "                ");
            }
            if (n.getChildNodes().getLength() > maxNodes) {
                LOG.error("            ...");
            }
        }
    }

    private static void debugAttributes(Node n, String indent) {
        if (n.getAttributes() != null) {
            debugAttribute(n, CgmesExport.RDF_NAMESPACE, "resource", indent);
            debugAttribute(n, CgmesExport.RDF_NAMESPACE, "about", indent);
        }
    }

    private static void debugAttribute(Node n, String namespace, String localName, String indent) {
        Node a = n.getAttributes().getNamedItemNS(namespace, localName);
        if (a != null) {
            LOG.error("{}{} = {}", indent, localName, a.getTextContent());
        }
    }

    private DiffBuilder withSelectedSvNodes(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> n.getNodeType() == Node.TEXT_NODE || isConsideredSvNode(n));
    }

    private DiffBuilder withSelectedSshNodes(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> n.getNodeType() == Node.TEXT_NODE || isConsideredSshNode(n));
    }

    private static boolean isConsideredSvNode(Node n) {
        return n.getLocalName() != null && (n.getLocalName().equals("RDF")
                || n.getLocalName().startsWith("SvVoltage")
                || n.getLocalName().startsWith("SvShuntCompensatorSections")
                || n.getLocalName().startsWith("SvTapStep")
                || n.getLocalName().startsWith("SvStatus")
                || n.getLocalName().startsWith("SvPowerFlow")
                || n.getLocalName().equals("FullModel")
                || n.getLocalName().startsWith("Model.") && !n.getLocalName().equals("Model.created")
                || n.getLocalName().startsWith("TopologicalIsland") && !n.getLocalName().equals("TopologicalIsland.AngleRefTopologicalNode"));
    }

    private static boolean isConsideredSshNode(Node n) {
        return n.getLocalName() != null
                && (n.getLocalName().equals("RDF")
                        || n.getLocalName().startsWith("EnergyConsumer")
                        || n.getLocalName().startsWith("Terminal")
                        || n.getLocalName().startsWith("EquivalentInjection")
                        || n.getLocalName().contains("TapChanger")
                        || n.getLocalName().contains("ShuntCompensator")
// FIXME(Luma) check also model description data
//                        || n.getLocalName().equals("FullModel")
//                        || n.getLocalName().startsWith("Model.") && !n.getLocalName().equals("Model.created")
                        );
    }

    private static DiffBuilder ignoringNonPersistentIds(DiffBuilder diffBuilder) {
        return diffBuilder.withAttributeFilter(attr -> {
            String elementName = attr.getOwnerElement().getLocalName();
            boolean ignored = false;
            if (elementName != null) {
                // Identifiers of SV objects are not persistent,
                // can be ignored for comparison with control
                if (elementName.startsWith("Sv")) {
                    ignored = attr.getLocalName().equals("ID");
                } else if (elementName.equals("FullModel")) {
                    ignored = attr.getLocalName().equals("about");
                } else if (elementName.equals("TopologicalIsland")) {
                    ignored = attr.getLocalName().equals("ID");
                }
            }
            return !ignored;
        });
    }

    private static DiffBuilder selectingEquivalentSvObjects(DiffBuilder diffBuilder) {
        Map<String, String> prefixUris = new HashMap<>(2);
        prefixUris.put("cim", CgmesExport.CIM_NAMESPACE);
        prefixUris.put("rdf", CgmesExport.RDF_NAMESPACE);
        QName resourceAttribute = new QName(CgmesExport.RDF_NAMESPACE, "resource");
        ElementSelector byResource = ElementSelectors.byNameAndAttributes(resourceAttribute);
        ElementSelector elementSelector = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed("SvShuntCompensatorSections")
                .thenUse(ElementSelectors.byXPath("./cim:SvShuntCompensatorSections.ShuntCompensator", prefixUris, byResource))
                .whenElementIsNamed("SvVoltage")
                .thenUse(ElementSelectors.byXPath("./cim:SvVoltage.TopologicalNode", prefixUris, byResource))
                .whenElementIsNamed("SvTapStep")
                .thenUse(ElementSelectors.byXPath("./cim:SvTapStep.TapChanger", prefixUris, byResource))
                .whenElementIsNamed("SvStatus")
                .thenUse(ElementSelectors.byXPath("./cim:SvStatus.ConductingEquipment", prefixUris, byResource))
                .whenElementIsNamed("SvPowerFlow")
                .thenUse(ElementSelectors.byXPath("./cim:SvPowerFlow.Terminal", prefixUris, byResource))
                .whenElementIsNamed("TopologicalIsland.TopologicalNodes")
                .thenUse(byResource)
                .whenElementIsNamed("Model.DependentOn")
                .thenUse(byResource)
                .elseUse(ElementSelectors.byName)
                .build();
        return diffBuilder.withNodeMatcher(new DefaultNodeMatcher(elementSelector));
    }

    private static DiffBuilder selectingEquivalentSshObjects(DiffBuilder diffBuilder) {
        QName aboutAttribute = new QName(CgmesExport.RDF_NAMESPACE, "about");
        return diffBuilder.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAttributes(aboutAttribute)));
    }

    private static Diff compare(DiffBuilder diffBuilder) {
        Diff diff = diffBuilder.build();
        boolean hasDiff = diff.hasDifferences();
        if (hasDiff && LOG.isErrorEnabled()) {
            for (Difference d : diff.getDifferences()) {
                if (d.getResult() == ComparisonResult.DIFFERENT) {
                    LOG.error("XML difference {}", d.getComparison());
                }
            }
        }
        return diff;
    }

    public static DataSource tmpDataSource(FileSystem fileSystem, String name) throws IOException {
        Path exportFolder = fileSystem.getPath(name);
        // XXX (local testing) Path exportFolder = Paths.get("/", "Users", "zamarrenolm", "Downloads", "temp_work", name);
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
