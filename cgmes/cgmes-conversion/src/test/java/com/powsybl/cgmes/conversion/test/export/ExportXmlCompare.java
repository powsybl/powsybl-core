/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.cgmes.model.CgmesNamespace.CIM_16_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class ExportXmlCompare {

    private ExportXmlCompare() {
    }

    static void compareNetworks(Path expected, Path actual) throws IOException {
        try (InputStream expectedIs = Files.newInputStream(expected);
            InputStream actualIs = Files.newInputStream(actual)) {
            compareNetworks(expectedIs, actualIs);
        }
    }

    static void compareNetworks(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff diff = DiffBuilder
                .compare(control)
                .withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withAttributeFilter(ExportXmlCompare::isConsideredForNetwork)
                .withDifferenceEvaluator(DifferenceEvaluators.chain(
                        DifferenceEvaluators.Default,
                        ExportXmlCompare::numericDifferenceEvaluator))
                .withComparisonListeners(ExportXmlCompare::debugComparison)
                .build();
        assertTrue(!diff.hasDifferences());
    }

    static boolean isConsideredForNetwork(Attr attr) {
        return !(attr.getLocalName().equals("forecastDistance"));
    }

    static interface DifferenceBuilder {
        DiffBuilder build(InputStream control, InputStream test, DifferenceEvaluator de);
    }

    static DiffBuilder diffSV(InputStream expected, InputStream actual, DifferenceEvaluator de) {
        return selectingEquivalentSvObjects(ignoringNonPersistentSvIds(withSelectedSvNodes(diff(expected, actual, de))));
    }

    static DiffBuilder diffSSH(InputStream expected, InputStream actual, DifferenceEvaluator de) {
        return selectingEquivalentSshObjects(ignoringNonPersistentSshIds(withSelectedSshNodes(diff(expected, actual, de))));
    }

    static void compareSSH(InputStream expected, InputStream actual, DifferenceEvaluator knownDiffs) throws IOException {
        onlyNodeListSequenceDiffs(compare(diffSSH(expected, actual, knownDiffs).checkForIdentical()));
    }

    static void compare(ReadOnlyDataSource dsExpected, DataSource dsActual, String profile, DifferenceBuilder diff, DifferenceEvaluator knownDiffs, String originalBaseName)
            throws IOException {
        String svExpected = dsExpected.listNames(".*" + profile + ".*").stream().findFirst().orElse("-");
        String svActual = dsActual.listNames(".*" + profile + ".*").stream().findFirst().orElse("-");
        LOG.debug("Compare {} export using CGMES original model and using only Network. Files:", profile);
        LOG.debug("   using CGMES        {}", svExpected);
        LOG.debug("   using Network only {}", svActual);
        assertTrue(svExpected.contains(originalBaseName));
        assertTrue(svActual.contains(originalBaseName));
        // Check that files are similar according to the diff function given
        try (InputStream expected = dsExpected.newInputStream(svExpected); InputStream actual = dsActual.newInputStream(svActual)) {
            isOk(compare(diff.build(expected, actual, knownDiffs).checkForSimilar()));
        }
        // Check again that only differences reported when checking for identical contents are the order of elements
        try (InputStream expected = dsExpected.newInputStream(svExpected); InputStream actual = dsActual.newInputStream(svActual)) {
            onlyNodeListSequenceDiffs(compare(diff.build(expected, actual, knownDiffs).checkForIdentical()));
        }
    }

    static ComparisonResult noKnownDiffs(Comparison comparison, ComparisonResult result) {
        // No previously known differences that should be filtered
        return result;
    }

    static ComparisonResult ensuringIncreasedModelVersion(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            if (comparison.getType() == ComparisonType.TEXT_VALUE && control.getParentNode().getLocalName().equals("Model.version")) {
                Node test = comparison.getTestDetails().getTarget();
                int vcontrol = Integer.valueOf(control.getTextContent());
                int vtest = Integer.valueOf(test.getTextContent());
                if (vtest == vcontrol + 1) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult sameScenarioTime(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            Node control = comparison.getControlDetails().getTarget();
            Node test = comparison.getTestDetails().getTarget();
            if (test != null && control != null && control.getParentNode().getLocalName().equals("Model.scenarioTime")) {
                String scontrol = control.getTextContent();
                String stest = test.getTextContent();
                DateTime dcontrol = DateTime.parse(scontrol, ISODateTimeFormat.dateTimeParser().withOffsetParsed().withZoneUTC());
                DateTime dtest = DateTime.parse(stest, ISODateTimeFormat.dateTimeParser().withOffsetParsed().withZoneUTC());
                if (dcontrol.equals(dtest)) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringStaticVarCompensatorDiffq(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            if (comparison.getType() == ComparisonType.TEXT_VALUE && control.getParentNode().getLocalName().equals("StaticVarCompensator.q")) {
                Node test = comparison.getTestDetails().getTarget();
                // Both elements must exist and have valid numeric values
                double qcontrol = Double.valueOf(control.getTextContent());
                double qtest = Double.valueOf(test.getTextContent());
                // But they could be different
                // When we export we save in SSH.q the value of the SVC.terminal.q
                // It would be the result of the power flow calculation
                // or the value originally seen in the import in SV.q
                if (qcontrol != qtest) {
                    LOG.warn("Different values for StaticVarCompensator.q: control {}, test {}", qcontrol, qtest);
                }
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringMissingTopologicalIslandInControl(Comparison comparison, ComparisonResult result) {
        // If control node is a terminal of a junction, ignore the difference
        // Means that we also have to ignore length of children of RDF element
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            Node test = comparison.getTestDetails().getTarget();
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
                    && control.getNodeType() == Node.ELEMENT_NODE
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

    static ComparisonResult ignoringSynchronousMachinesSVCsWithTargetDeadband(Comparison comparison, ComparisonResult result) {
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
                    || about.equals("#_6ba406ce-78cf-4485-9b01-a34e584f1a8d")
                    || about.equals("#_caf65447-3cfb-48d7-aaaa-cd9af3d34261")) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringJunctionOrBusbarTerminals(Comparison comparison, ComparisonResult result) {
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

    private static void isOk(Diff diff) {
        assertTrue(!diff.hasDifferences());
    }

    private static void onlyNodeListSequenceDiffs(Diff diff) {
        for (Difference d : diff.getDifferences()) {
            assertEquals(ComparisonType.CHILD_NODELIST_SEQUENCE, d.getComparison().getType());
            assertEquals(ComparisonResult.SIMILAR, d.getResult());
        }
    }

    static ComparisonResult numericDifferenceEvaluator(Comparison comparison, ComparisonResult result) {
        // If both control and test nodes are text that can be converted to a number
        // check that they represent the same number
        if (result == ComparisonResult.DIFFERENT
                && (comparison.getType() == ComparisonType.TEXT_VALUE || comparison.getType() == ComparisonType.ATTR_VALUE)) {
            // If different result for control and test values, check node
            // (parent for elements, target for attributes)
            Node n = comparison.getControlDetails().getTarget();
            if (n.getNodeType() == Node.TEXT_NODE) {
                n = n.getParentNode();
            }
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

    private static boolean isTextContentNumeric(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            return name.endsWith(".p") || name.endsWith(".q")
                    || name.endsWith(".v") || name.endsWith(".angle")
                    || name.endsWith(".sections")
                    || name.endsWith("TapChanger.step")
                    || name.endsWith(".regulationTarget") || name.endsWith(".targetValue") || name.endsWith(".targetDeadband")
                    || name.endsWith("pTolerance")
                    || name.endsWith(".normalPF");
        } else if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
            // DanglingLine p, q attributes in IIDM Network
            String name = n.getLocalName();
            return name.equals("p") || name.equals("q");
        }
        return false;
    }

    private static double toleranceForNumericContent(Node n) {
        if (n.getLocalName().endsWith(".p") || n.getLocalName().endsWith(".q")) {
            return 1e-5;
        } else if (n.getLocalName().equals("p") || n.getLocalName().equals("q")) {
            return 1e-1;
        }
        return 1e-10;
    }

    private static DiffBuilder diff(InputStream expected, InputStream actual, DifferenceEvaluator user) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        return DiffBuilder.compare(control).withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(DifferenceEvaluators.Default, ExportXmlCompare::numericDifferenceEvaluator, user))
                .withComparisonListeners(ExportXmlCompare::debugComparison);
    }

    static void debugComparison(Comparison comparison, ComparisonResult comparisonResult) {
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
            if (n.getNodeType() == Node.TEXT_NODE) {
                LOG.error("            {}", n.getTextContent());
            }
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

    private static DiffBuilder withSelectedSvNodes(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> n.getNodeType() == Node.TEXT_NODE || isConsideredSvNode(n));
    }

    private static DiffBuilder withSelectedSshNodes(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> n.getNodeType() == Node.TEXT_NODE || isConsideredSshNode(n));
    }

    private static boolean isConsideredSvNode(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            return name != null && (name.equals("RDF")
                    || name.startsWith("SvVoltage")
                    || name.startsWith("SvShuntCompensatorSections")
                    || name.startsWith("SvTapStep")
                    || name.startsWith("SvStatus")
                    || name.startsWith("SvPowerFlow")
                    || name.startsWith("TopologicalIsland") && !name.equals("TopologicalIsland.AngleRefTopologicalNode")
                    || isConsideredModelElementName(name));
        }
        return false;
    }

    private static boolean isConsideredSshNode(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            // Optional attributes with default values
            if (name.equals("EquivalentInjection.regulationStatus") && n.getTextContent().equals("false")) {
                return false;
            } else if (name.equals("EquivalentInjection.regulationTarget") && Double.valueOf(n.getTextContent()) == 0) {
                return false;
            }
            return name.equals("RDF")
                    || name.startsWith("EnergyConsumer") || name.startsWith("ConformLoad") || name.startsWith("NonConformLoad")
                    || name.startsWith("Terminal")
                    || name.startsWith("EquivalentInjection")
                    || name.contains("TapChanger")
                    || name.contains("ShuntCompensator")
                    || name.startsWith("SynchronousMachine")
                    || name.startsWith("RotatingMachine")
                    || name.startsWith("RegulatingCondEq")
                    || name.startsWith("RegulatingControl")
                    || name.startsWith("StaticVarCompensator")
                    // Previous condition includes this one
                    // but we state explicitly that we consider tap changer control objects
                    || name.startsWith("TapChangerControl")
                    // TODO include control areas
                    || name.contains("GeneratingUnit")
                    || isConsideredModelElementName(name);
        }
        return false;
    }

    private static boolean isConsideredModelElementName(String name) {
        return name.equals("FullModel")
                || name.startsWith("Model.")
                && !(name.equals("Model.created")
                        || name.equals("Model.Supersedes")
                        || name.equals("Model.createdBy"));
    }

    private static DiffBuilder ignoringNonPersistentSvIds(DiffBuilder diffBuilder) {
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

    private static DiffBuilder selectingEquivalentSvObjects(DiffBuilder diffBuilder) {
        Map<String, String> prefixUris = new HashMap<>(2);
        prefixUris.put("cim", CIM_16_NAMESPACE);
        prefixUris.put("rdf", RDF_NAMESPACE);
        QName resourceAttribute = new QName(RDF_NAMESPACE, "resource");
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
        QName aboutAttribute = new QName(RDF_NAMESPACE, "about");
        ElementSelector elementSelector = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed("FullModel")
                .thenUse(ElementSelectors.byName)
                .elseUse(ElementSelectors.byNameAndAttributes(aboutAttribute))
                .build();
        return diffBuilder.withNodeMatcher(new DefaultNodeMatcher(elementSelector));
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

    private static final Logger LOG = LoggerFactory.getLogger(ExportXmlCompare.class);
}
