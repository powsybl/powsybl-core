/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.update.CgmesExportContext;
import com.powsybl.cgmes.conversion.update.StateVariablesExport;
import com.powsybl.cgmes.conversion.update.SteadyStateHypothesisExport;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.powsybl.cgmes.model.CgmesNamespace.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SteadyStateHypothesisExportTest extends AbstractConverterTest {

    // FIXME(Luma) Pending output of StaticVarCompensator.q

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

    @Test
    public void smallGrid() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
    }

    private void test(ReadOnlyDataSource dataSource, int sshVersion) throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SSH");

        // Import original
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export SSH
        Path test = tmpDir.resolve("test.xml");
        try (OutputStream os = Files.newOutputStream(test)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            context.getSshModelDescription().setVersion(sshVersion);
            SteadyStateHypothesisExport.write(expected, writer, context);
        }

        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                SteadyStateHypothesisExportTest::ignoringMissingTopologicalIslandInControl,
                SteadyStateHypothesisExportTest::ignoringSynchronousMachinesWithTargetDeadband,
                SteadyStateHypothesisExportTest::ignoringJunctionOrBusbarTerminals);

        try (InputStream expectedssh = newInputStream(dataSource, this::ssh);
                InputStream actualssh = Files.newInputStream(test)) {
            DiffBuilder diff = selectingEquivalentSshObjects(ignoringNonPersistentSshIds(withSelectedSshNodes(diff(expectedssh, actualssh, knownDiffs))));
            isOk(compare(diff.checkForSimilar()));
        }
        try (InputStream expectedssh = newInputStream(dataSource, this::ssh);
                InputStream actualssh = Files.newInputStream(test)) {
            DiffBuilder diff = selectingEquivalentSshObjects(ignoringNonPersistentSshIds(withSelectedSshNodes(diff(expectedssh, actualssh, knownDiffs))));
            onlyNodeListSequenceDiffs(compare(diff.checkForIdentical()));
        }

        // Zip with new SV
        try (OutputStream fos = Files.newOutputStream(tmpDir.resolve("repackaged.zip"));
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            try (InputStream eqIs = newInputStream(dataSource, this::eq)) {
                zipFile("EQ.xml", eqIs, zipOut);
            }
            try (InputStream tpIs = newInputStream(dataSource, this::tp)) {
                zipFile("TP.xml", tpIs, zipOut);
            }
            try (InputStream sshIs = Files.newInputStream(test)) {
                zipFile("SSH.xml", sshIs, zipOut);
            }
            try (InputStream svIs = newInputStream(dataSource, this::sv)) {
                zipFile("SV.xml", svIs, zipOut);
            }
            try (InputStream eqBdIs = newInputStream(dataSource, this::eqBd)) {
                zipFile("EQ_BD.xml", eqBdIs, zipOut);
            }
            try (InputStream tpBdIs = newInputStream(dataSource, this::tpBd)) {
                zipFile("TP_BD.xml", tpBdIs, zipOut);
            }
        }

        // Import with new SSH
        Network actual = Importers.loadNetwork(tmpDir.resolve("repackaged.zip"),
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SSH
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        try (InputStream expIs = Files.newInputStream(tmpDir.resolve("expected.xml"));
             InputStream actIs = Files.newInputStream(tmpDir.resolve("actual.xml"))) {
            compareXmlWithDelta(expIs, actIs);
        }
    }

    private InputStream newInputStream(ReadOnlyDataSource ds, Predicate<String> file) throws IOException {
        return ds.newInputStream(getName(ds, file));
    }

    private String getName(ReadOnlyDataSource ds, Predicate<String> file) {
        CgmesOnDataSource ns = new CgmesOnDataSource(ds);
        return ns.names().stream().filter(n -> file.test(n)).findFirst().get();
    }

    private boolean eq(String name) {
        return !name.contains("_BD") && name.contains("_EQ");
    }

    private boolean tp(String name) {
        return !name.contains("_BD") && name.contains("_TP");
    }

    private boolean ssh(String name) {
        return name.contains("_SSH");
    }

    private boolean sv(String name) {
        return name.contains("_SV");
    }

    private boolean eqBd(String name) {
        return name.contains("_EQ") && name.contains("_BD");
    }

    private boolean tpBd(String name) {
        return name.contains("_TP") && name.contains("_BD");
    }

    private static void zipFile(String entryName, InputStream toZip, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = toZip.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
    }

    private static void compareXmlWithDelta(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff myDiff = DiffBuilder.compare(control).withTest(test).ignoreWhitespace().ignoreComments().build();
        for (Difference diff : myDiff.getDifferences()) {
            if (diff.getComparison().getControlDetails().getXPath().endsWith("forecastDistance")) {
                continue;
            }
            debugComparison(diff.getComparison(), diff.getResult());
            double exp = Double.parseDouble((String) diff.getComparison().getControlDetails().getValue());
            double act = Double.parseDouble((String) diff.getComparison().getTestDetails().getValue());
            assertEquals(exp, act, getDelta(diff.getComparison().getControlDetails().getXPath()));
        }
    }

    private static double getDelta(String xPath) {
        if (xPath.contains("danglingLine")) {
            if (xPath.endsWith("p") || xPath.endsWith("q")) {
                return 1e-1;
            }
            return 1e-5;
        }
        return 0.0;
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

    private void isOk(Diff diff) {
        assertTrue(!diff.hasDifferences());
    }

    private void onlyNodeListSequenceDiffs(Diff diff) {
        for (Difference d : diff.getDifferences()) {
            assertEquals(ComparisonType.CHILD_NODELIST_SEQUENCE, d.getComparison().getType());
            assertEquals(ComparisonResult.SIMILAR, d.getResult());
        }
    }

    ComparisonResult numericDifferenceEvaluator(Comparison comparison, ComparisonResult result) {
        // If both control and test nodes are text that can be converted to a number
        // check that they represent the same number
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            // If different result for control and test,
            // node must have a parent
            Node n = comparison.getControlDetails().getTarget().getParentNode();
            if (isTextContentNumeric(n)) {
                try {
                    double control = Double.parseDouble(comparison.getControlDetails().getTarget().getTextContent());
                    try {
                        double test = Double.parseDouble(comparison.getTestDetails().getTarget().getTextContent());
                        if (Math.abs(control - test) < toleranceForNumericContent(n)) {
                            return ComparisonResult.EQUAL;
                        }
                    } catch (NumberFormatException x) {
                        // not numeric, keep previous comparison result
                    }
                } catch (NumberFormatException x) {
                    // not numeric, keep previous comparison result
                }
            }
        }
        return result;
    }

    private boolean isTextContentNumeric(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            return name.endsWith(".p") || name.endsWith(".q")
                    || name.endsWith(".v") || name.endsWith(".angle")
                    || name.endsWith(".regulationTarget") || name.endsWith(".targetValue") || name.endsWith(".targetDeadband")
                    || name.endsWith(".normalPF");
        }
        return false;
    }

    private double toleranceForNumericContent(Node n) {
        if (n.getLocalName().endsWith(".p") || n.getLocalName().endsWith(".q")) {
            return 1e-5;
        }
        return 1e-10;
    }

    private DiffBuilder diff(InputStream expected, InputStream actual, DifferenceEvaluator user) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        return DiffBuilder.compare(control).withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(DifferenceEvaluators.Default, this::numericDifferenceEvaluator, user))
                .withComparisonListeners(SteadyStateHypothesisExportTest::debugComparison);
    }

    private DiffBuilder withSelectedSshNodes(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> n.getNodeType() == Node.TEXT_NODE || isConsideredSshNode(n));
    }

    private static boolean isConsideredSshNode(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            return name.equals("RDF")
                    || name.startsWith("EnergyConsumer")
                    || name.startsWith("Terminal")
                    || name.startsWith("EquivalentInjection")
                    || name.contains("TapChanger")
                    || name.contains("ShuntCompensator")
                    || name.startsWith("SynchronousMachine")
                    || name.startsWith("RotatingMachine")
                    || name.startsWith("RegulatingCondEq")
                    || name.startsWith("RegulatingControl")
                    // Previous condition includes this one
                    // but we state explicitly that we consider tap changer control objects
                    || name.startsWith("TapChangerControl")
                    || name.startsWith("ControlArea")
                    || name.contains("GeneratingUnit")
                    || name.equals("FullModel")
                    || name.startsWith("Model.") && !name.equals("Model.created");
        }
        return false;
    }

    private static DiffBuilder ignoringNonPersistentSshIds(DiffBuilder diffBuilder) {
        return diffBuilder.withAttributeFilter(attr -> {
            String elementName = attr.getOwnerElement().getLocalName();
            boolean ignored = false;
            if (elementName != null) {
                if (elementName.equals("FullModel")) {
                    ignored = attr.getLocalName().equals("about");
                }
            }
            return !ignored;
        });
    }

    private static ComparisonResult ignoringMissingTopologicalIslandInControl(Comparison comparison, ComparisonResult result) {
        // If control node is a terminal of a junction, ignore the difference
        // Means that we also have to ignore length of children of RDF element
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            Node test = comparison.getTestDetails().getTarget();
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
                    && control.getLocalName().equals("RDF")) {
                return ComparisonResult.EQUAL;
            } else if (comparison.getType() == ComparisonType.CHILD_LOOKUP
                    && control == null
                    && test.getNodeType() == Node.ELEMENT_NODE
                    && test.getLocalName().equals("TopologicalIsland")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    private static ComparisonResult ignoringSynchronousMachinesWithTargetDeadband(Comparison comparison, ComparisonResult result) {
        // In micro grid there are two regulating controls for synchronous machines
        // that have a target deadband of 0.5
        // PowSyBl does not allow deadband for generator regulation
        // we export deadband 0
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            Node control = comparison.getControlDetails().getTarget();
            // XPtah is RDF/RegulatingControl/RegulatingControl.targetDeadband/text()
            // check that parent of current text node is a regulating control target deadband,
            // then check grand parent is one of the known diff identifiers
            if (control.getParentNode() != null
                    && control.getParentNode().getNodeType() == Node.ELEMENT_NODE
                    && control.getParentNode().getLocalName().equals("RegulatingControl.targetDeadband")) {
                Node rccontrol = control.getParentNode().getParentNode();
                String about = rccontrol.getAttributes().getNamedItemNS(RDF_NAMESPACE, "about").getTextContent();
                if (about.equals("#_84bf5be8-eb59-4555-b131-fce4d2d7775d")
                        || about.equals("#_6ba406ce-78cf-4485-9b01-a34e584f1a8d")) {
                    // FIXME(Luma) The regulating control _6ba406ce-78cf-4485-9b01-a34e584f1a8d
                    // is shared between a synchronous machine and a tap changer
                    // In PowSyBl, from the point of view of the generator,
                    // we consider the regulating control without deadband
                    // But the transformer should be allowed to use a deadband
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    private static ComparisonResult ignoringJunctionOrBusbarTerminals(Comparison comparison, ComparisonResult result) {
        // If control node is a terminal of a junction, ignore the difference
        // Means that we also have to ignore length of children of RDF element
        if (result == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
                    && comparison.getControlDetails().getTarget().getLocalName().equals("RDF")) {
                return ComparisonResult.EQUAL;
            } else if (isJunctionOrBusbarTerminal(comparison.getControlDetails().getTarget())) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    private static boolean isJunctionOrBusbarTerminal(Node n) {
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals(CgmesNames.TERMINAL)) {
            String about = n.getAttributes().getNamedItemNS(RDF_NAMESPACE, "about").getTextContent();
            if (JUNCTIONS_TERMINALS.contains(about)) {
                return true;
            } else if (BUSBAR_TERMINALS.contains(about)) {
                return true;
            }
        }
        return false;
    }

    // Present in small grid
    private static final Set<String> JUNCTIONS_TERMINALS = Stream.of(
            "#_65a95678-1819-43cd-94d2-a03756822725",
            "#_5c96df9d-39fa-46fe-a424-7b3e50184f79",
            "#_9c6a1e49-17b2-46fc-8e32-c95dec33eba8")
            .collect(Collectors.toCollection(HashSet::new));

    // Present in micro grid
    private static final Set<String> BUSBAR_TERMINALS = Stream.of(
            "#_3c6d83a3-b5f9-41a2-a3d9-cf15d903ed0a",
            "#_ad794c0e-b9ec-420b-ada1-97680e3dde05",
            "#_a1b46f53-86f1-497e-bf57-c3b6268bcd6c",
            "#_65b8c937-9b25-4b9e-addf-602dbc1337f9",
            "#_62fc0a4e-00aa-4bf7-b1a0-3a5b2c0b5492",
            "#_800ada75-8c8c-4568-aec5-20f799e45f3c",
            "#_fa9e0f4d-8a2f-45e1-9e36-3611600d1c94",
            "#_302fe23a-f64d-41bd-8a81-78130433916d",
            "#_8f1c492f-a7cc-4160-9a14-54f1743e4850")
            .collect(Collectors.toCollection(HashSet::new));

    private static DiffBuilder selectingEquivalentSshObjects(DiffBuilder diffBuilder) {
        QName aboutAttribute = new QName(RDF_NAMESPACE, "about");
        ElementSelector elementSelector = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed("FullModel")
                .thenUse(ElementSelectors.byName)
                .elseUse(ElementSelectors.byNameAndAttributes(aboutAttribute))
                .build();
        return diffBuilder.withNodeMatcher(new DefaultNodeMatcher(elementSelector));
    }

    private static ComparisonResult noKnownDiffs(Comparison comparison, ComparisonResult result) {
        // No previously known differences that should be filtered
        return result;
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
            debugAttribute(n, RDF_NAMESPACE, "resource", indent);
            debugAttribute(n, RDF_NAMESPACE, "about", indent);
        }
    }

    private static void debugAttribute(Node n, String namespace, String localName, String indent) {
        Node a = n.getAttributes().getNamedItemNS(namespace, localName);
        if (a != null) {
            LOG.error("{}{} = {}", indent, localName, a.getTextContent());
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);
}
