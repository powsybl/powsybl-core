/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.model.CgmesNames;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;
import org.xmlunit.util.IsNullPredicate;
import org.xmlunit.util.Linqy;
import org.xmlunit.util.Nodes;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
final class ExportXmlCompare {

    private ExportXmlCompare() {
    }

    static boolean compareNetworks(Path expected, Path actual) {
        try (InputStream expectedIs = Files.newInputStream(expected);
            InputStream actualIs = Files.newInputStream(actual)) {
            return compareNetworks(expectedIs, actualIs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean compareNetworks(Path expected, Path actual, DifferenceEvaluator knownDiffs) {
        try (InputStream expectedIs = Files.newInputStream(expected);
             InputStream actualIs = Files.newInputStream(actual)) {
            return compareNetworks(expectedIs, actualIs, knownDiffs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean compareEQNetworks(Path expected, Path actual, DifferenceEvaluator knownDiffs) {
        try (InputStream expectedIs = Files.newInputStream(expected);
             InputStream actualIs = Files.newInputStream(actual)) {
            return compareEQNetworks(expectedIs, actualIs, knownDiffs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean compareNetworks(InputStream expected, InputStream actual) {
        return compareNetworks(expected, actual, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator));
    }

    static boolean compareNetworks(InputStream expected, InputStream actual, DifferenceEvaluator knownDiffs) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff diff = DiffBuilder
                .compare(control)
                .withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withAttributeFilter(ExportXmlCompare::isConsideredForNetwork)
                .withDifferenceEvaluator(knownDiffs)
                .withComparisonListeners(ExportXmlCompare::debugComparison)
                .build();
        assertFalse(diff.hasDifferences());
        return !diff.hasDifferences();
    }

    static boolean compareEQNetworks(InputStream expected, InputStream actual, DifferenceEvaluator knownDiffs) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff diff = DiffBuilder
                .compare(control)
                .withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .withAttributeFilter(ExportXmlCompare::isConsideredForEQNetwork)
                .withNodeFilter(ExportXmlCompare::isConsideredEQNode)
                .withDifferenceEvaluator(knownDiffs)
                .withComparisonListeners(ExportXmlCompare::debugComparison)
                .build();
        assertFalse(diff.hasDifferences());
        return !diff.hasDifferences();
    }

    static boolean isConsideredForNetwork(Attr attr) {
        if (attr.getLocalName().equals("forecastDistance")) {
            return false;
        }
        if (attr.getLocalName().equals("value")) {
            Element e = attr.getOwnerElement();
            return !e.getLocalName().equals("property")
                    || !"CGMES.TP_ID".equals(e.getAttribute("name")) && !"CGMES.SSH_ID".equals(e.getAttribute("name"));
        }
        return true;
    }

    private static boolean isConsideredForEQNetwork(Attr attr) {
        String elementName = attr.getOwnerElement().getLocalName();
        boolean ignored = false;
        if (elementName != null) {
            if (elementName.equals("danglingLine")) {
                ignored = true;
            } else if (elementName.startsWith("network")) {
                ignored = attr.getLocalName().equals("id") || attr.getLocalName().equals("forecastDistance") || attr.getLocalName().equals("caseDate") || attr.getLocalName().equals("sourceFormat");
            } else if (elementName.startsWith("voltageLevel")) {
                ignored = attr.getLocalName().equals("topologyKind") || attr.getLocalName().equals("lowVoltageLimit") || attr.getLocalName().equals("highVoltageLimit");
            } else if (elementName.startsWith("hvdcLine")) {
                ignored = attr.getLocalName().equals("converterStation1") || attr.getLocalName().equals("converterStation2") || attr.getLocalName().equals("convertersMode");
            } else if (elementName.contains("TapChanger")) {
                ignored = attr.getLocalName().equals("regulating") || attr.getLocalName().equals("regulationMode") || attr.getLocalName().equals("regulationValue")
                        || attr.getLocalName().equals("targetV") || attr.getLocalName().equals("targetDeadband");
            } else if (elementName.startsWith("generator")) {
                // ratedS is optional for generators,
                // but we always export it to keep up with the quality of datasets rules.
                // So we do not enforce this attribute to be equal in the original and exported network
                ignored = attr.getLocalName().equals("ratedS");
            } else {
                ignored = attr.getLocalName().contains("node") || attr.getLocalName().contains("bus") || attr.getLocalName().contains("Bus");
            }
        }
        return !ignored;
    }

    // Present in small grid HVDC
    private static final Set<String> SMALLGRID_SUBSTATIONS = Stream.of(
            "68-116_SUBSTATION",
            "71-73_SUBSTATION",
            "12-117_SUBSTATION")
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> SMALLGRID_VOLTAGELEVELS = Stream.of(
            "68-116_VL",
            "71-73_VL",
            "12-117_VL")
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> SMALLGRID_LINES = Stream.of(
            "68-116_DL",
            "71-73_DL",
            "12-117_DL")
            .collect(Collectors.toCollection(HashSet::new));

    // Present in mini grid
    private static final Set<String> MINIGRID_SUBSTATIONS = Stream.of(
            "XQ2-N5_SUBSTATION",
            "XQ1-N1_SUBSTATION")
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> MINIGRID_VOLTAGELEVELS = Stream.of(
            "XQ2-N5_VL",
            "XQ1-N1_VL")
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> MINIGRID_LINES = Stream.of(
            "XQ2-N5_DL",
            "XQ1-N1_DL")
            .collect(Collectors.toCollection(HashSet::new));

    // Present in micro grid
    private static final Set<String> MICROGRID_SUBSTATIONS = Stream.of(
            "BE-Line_1_SUBSTATION",
            "BE-Line_3_SUBSTATION",
            "BE-Line_4_SUBSTATION",
            "BE-Line_5_SUBSTATION",
            "BE-Line_7_SUBSTATION")
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> MICROGRID_VOLTAGELEVELS = Stream.of(
            "BE-Line_1_VL",
            "BE-Line_3_VL",
            "BE-Line_4_VL",
            "BE-Line_5_VL",
            "BE-Line_7_VL")
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> MICROGRID_LINES = Stream.of(
            "BE-Line_1_DL",
            "BE-Line_3_DL",
            "BE-Line_4_DL",
            "BE-Line_5_DL",
            "BE-Line_7_DL")
            .collect(Collectors.toCollection(HashSet::new));

    private static boolean isConsideredEQNode(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            return !name.startsWith("danglingLine") && !name.contains("BreakerTopology") && !name.startsWith("internalConnection")
                    && !name.startsWith("property") && !name.startsWith("regulatingTerminal") && !name.startsWith("terminalRef")
                    && !isDanglingLineConversion(n) && !isNodeBreakerProperty(n);
        }
        return false;
    }

    private static boolean isNodeBreakerProperty(Node n) {
        if (n.getAttributes().getNamedItem("name") != null && n.getAttributes().getNamedItem("name").getTextContent() != null) {
            return n.getAttributes().getNamedItem("name").getTextContent().equals("CGMESModelDetail");
        }
        return false;
    }

    private static boolean isDanglingLineConversion(Node n) {
        String name = n.getLocalName();
        return name.startsWith("substation") && SMALLGRID_SUBSTATIONS.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("voltageLevel") && SMALLGRID_VOLTAGELEVELS.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("line") && SMALLGRID_LINES.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("substation") && MINIGRID_SUBSTATIONS.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("voltageLevel") && MINIGRID_VOLTAGELEVELS.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("line") && MINIGRID_LINES.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("substation") && MICROGRID_SUBSTATIONS.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("voltageLevel") && MICROGRID_VOLTAGELEVELS.contains(n.getAttributes().getNamedItem("name").getTextContent())
                || name.startsWith("line") && MICROGRID_LINES.contains(n.getAttributes().getNamedItem("name").getTextContent());
    }

    static DiffBuilder diffSSH(InputStream expected, InputStream actual, DifferenceEvaluator de) {
        // Original CGMES PhaseTapChangerLinear, PhaseTapChangerSymmetrical and PhaseTapChangerAsymmetrical
        // have been exported as PhaseTapChangerTabular.
        // We just check that the objects being compared keep a PhaseTapChanger class, not exactly the same one.
        DifferenceEvaluator de1 = DifferenceEvaluators.chain(de, (comparison, result) -> {
            if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.ELEMENT_TAG_NAME) {
                if (comparison.getControlDetails().getTarget().getLocalName().startsWith("PhaseTapChanger") &&
                        comparison.getTestDetails().getTarget().getLocalName().startsWith("PhaseTapChanger")) {
                    return ComparisonResult.EQUAL;
                }
            }
            return result;
        });
        return selectingEquivalentSshObjects(ignoringNonPersistentSshIds(withSelectedSshNodes(diff(expected, actual, de1))));
    }

    static boolean compareSSH(InputStream expected, InputStream actual, DifferenceEvaluator knownDiffs) {
        return onlyNodeListSequenceDiffs(compare(diffSSH(expected, actual, knownDiffs).checkForIdentical()));
    }

    static ComparisonResult ignoringCgmesMetadataModels(Comparison comparison, ComparisonResult result) {
        // FIXME(Luma) Refactoring in progress. cgmes metadata models should be serialized in the same way, maybe we could check something else
        if (result == ComparisonResult.DIFFERENT) {
            String xpath = comparison.getControlDetails().getXPath();
            if (xpath == null) {
                xpath = comparison.getControlDetails().getParentXPath();
            }
            if (xpath != null && xpath.contains("cgmesMetadataModels")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ensuringIncreasedModelVersion(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            if (comparison.getType() == ComparisonType.TEXT_VALUE && control.getParentNode().getLocalName().equals("Model.version")) {
                Node test = comparison.getTestDetails().getTarget();
                int vcontrol = Integer.parseInt(control.getTextContent());
                int vtest = Integer.parseInt(test.getTextContent());
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
                ZonedDateTime dcontrol = ZonedDateTime.parse(scontrol, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC));
                ZonedDateTime dtest = ZonedDateTime.parse(stest);
                if (dcontrol.equals(dtest)) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringCreatedTime(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            Node control = comparison.getControlDetails().getTarget();
            Node test = comparison.getTestDetails().getTarget();
            if (test != null && control != null && control.getParentNode().getLocalName().equals("Model.created")) {
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

    static ComparisonResult ignoringHvdcLinePmax(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                if (control != null && control.getLocalName().equals("maxP")) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringNonEQ(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                if (control != null && control.getLocalName().equals("geographicalTags")) {
                    return ComparisonResult.EQUAL;
                } else if (control != null && control.getLocalName().equals("targetQ")) {
                    return ComparisonResult.EQUAL;
                } else if (comparison.getControlDetails().getXPath().contains("temporaryLimit")) {
                    // If the control node is a temporary limit, the order depends on the name attribute,
                    // this attribute is generated as a unique identifier in the EQ export to avoid duplicates in CGMES
                    return ComparisonResult.EQUAL;
                }
            } else if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
                if (control.getLocalName().equals("network") || control.getLocalName().equals("shunt")
                        || control.getLocalName().equals("generator")) {
                    return ComparisonResult.EQUAL;
                }
            } else if (comparison.getType() == ComparisonType.ELEMENT_TAG_NAME) {
                if (control.getLocalName().equals("temporaryLimits")) {
                    return ComparisonResult.EQUAL;
                }
            }
            return result;
        }
        return result;
    }

    static ComparisonResult ignoringOperationalLimitsGroupId(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Node control = comparison.getControlDetails().getTarget();
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                if (comparison.getControlDetails().getXPath().contains("operationalLimitsGroup") && control != null
                        && control.getLocalName().equals("id")) {
                    return ComparisonResult.EQUAL;
                } else if (control != null && control.getLocalName().contains("selectedOperationalLimitsGroupId")) {
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
            return JUNCTIONS_TERMINALS.contains(about) || BUSBAR_TERMINALS.contains(about);
        }
        return false;
    }

    private static boolean onlyNodeListSequenceDiffs(Diff diff) {
        for (Difference d : diff.getDifferences()) {
            if (ComparisonType.CHILD_NODELIST_SEQUENCE != d.getComparison().getType()) {
                return false;
            }
            if (ComparisonResult.SIMILAR != d.getResult()) {
                return false;
            }
        }
        return true;
    }

    static ComparisonResult ignoringFullModelAbout(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                Comparison.Detail control = comparison.getControlDetails();
                if (control.getXPath().contains("FullModel")
                        && control.getTarget().getLocalName().equals("about")) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringFullModelDependentOn(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("FullModel") && cxpath.contains("Model.DependentOn")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringFullModelModelingAuthoritySet(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("FullModel") && cxpath.contains("Model.modelingAuthoritySet")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringSubstationNumAttributes(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.ELEMENT_NUM_ATTRIBUTES) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("network") && cxpath.contains("substation")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringSubstationLookup(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.ATTR_NAME_LOOKUP) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("network") && cxpath.contains("substation")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringGeneratorAttributes(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.ATTR_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("generator")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringLoadChildNodeListLength(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("load")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringRdfChildNodeListLength(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("RDF")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringChildLookupNull(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.CHILD_LOOKUP) {
            if (comparison.getControlDetails().getXPath() == null) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringTextValueShuntCompensatorControlEnabled(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("ShuntCompensator") && cxpath.contains("controlEnabled")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringRdfChildLookupTerminal(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.CHILD_LOOKUP) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("RDF") && cxpath.contains("Terminal")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringRdfChildLookupEquivalentInjection(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.CHILD_LOOKUP) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("RDF") && cxpath.contains(CgmesNames.EQUIVALENT_INJECTION)) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringStaticVarCompensatorControlEnabled(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("StaticVarCompensator") && cxpath.contains(".controlEnabled")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringStaticVarCompensatorQ(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("StaticVarCompensator.q")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringRegulatingControl(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("RDF") && cxpath.contains("RegulatingControl")) {
                return ComparisonResult.EQUAL;
            }
        } else if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.CHILD_LOOKUP) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("RDF") && cxpath.contains("RegulatingControl")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringTextValueTapChangerControlEnabled(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("TapChanger") && cxpath.contains("controlEnabled")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringConformLoad(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.ELEMENT_TAG_NAME) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("ConformLoad")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringTextValueEquivalentInjection(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT && comparison.getType() == ComparisonType.TEXT_VALUE) {
            String cxpath = comparison.getControlDetails().getXPath();
            if (cxpath != null && cxpath.contains("EquivalentInjection")) {
                return ComparisonResult.EQUAL;
            }
        }
        return result;
    }

    static ComparisonResult ignoringOperationalLimitIds(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Comparison.Detail control = comparison.getControlDetails();
            String cxpath = control.getXPath();
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                String cname = control.getTarget().getLocalName();
                if (cxpath.contains("CurrentLimit") && (cname.equals("ID") || cname.equals("resource"))) {
                    return ComparisonResult.EQUAL;
                } else if (cxpath.contains("OperationalLimit") && cname.equals("ID")) {
                    return ComparisonResult.EQUAL;
                }
            } else if (comparison.getType() == ComparisonType.TEXT_VALUE) {
                if (cxpath.contains("CurrentLimit") || cxpath.contains("OperationalLimit")) {
                    if (control.getTarget().getParentNode().getLocalName().endsWith(".mRID")) {
                        return ComparisonResult.EQUAL;
                    }
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringLoadAreaIds(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Comparison.Detail control = comparison.getControlDetails();
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                if (control.getXPath().contains("SubLoadArea")
                        && control.getTarget().getLocalName().equals("ID")) {
                    return ComparisonResult.EQUAL;
                } else if (control.getXPath().contains("LoadArea")
                        && control.getTarget().getLocalName().equals("ID")) {
                    return ComparisonResult.EQUAL;
                } else if (control.getXPath().contains("SubLoadArea.LoadArea")
                        && control.getTarget().getLocalName().equals("resource")) {
                    return ComparisonResult.EQUAL;
                }
            } else if (comparison.getType() == ComparisonType.TEXT_VALUE) {
                if (control.getXPath().contains("SubLoadArea") || control.getXPath().contains("LoadArea")) {
                    if (control.getTarget().getParentNode().getLocalName().endsWith(".mRID")) {
                        return ComparisonResult.EQUAL;
                    }
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringEnergyAreaIdOfControlArea(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            Comparison.Detail control = comparison.getControlDetails();
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                if (control.getXPath().contains("ControlArea.EnergyArea")
                        && control.getTarget().getLocalName().equals("resource")) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
    }

    static ComparisonResult ignoringSVIds(Comparison comparison, ComparisonResult result) {
        if (result == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.ATTR_VALUE) {
                Comparison.Detail control = comparison.getControlDetails();
                if (control.getXPath().contains("SvVoltage")
                        && control.getTarget().getLocalName().equals("ID")) {
                    return ComparisonResult.EQUAL;
                } else if (control.getXPath().contains("SvPowerFlow")
                        && control.getTarget().getLocalName().equals("ID")) {
                    return ComparisonResult.EQUAL;
                } else if (control.getXPath().contains("SvShuntCompensatorSections")
                        && control.getTarget().getLocalName().equals("ID")) {
                    return ComparisonResult.EQUAL;
                } else if (control.getXPath().contains("SvStatus")
                        && control.getTarget().getLocalName().equals("ID")) {
                    return ComparisonResult.EQUAL;
                }
            }
        }
        return result;
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
            // DanglingLine p, q, p0, q0 attributes in IIDM Network
            // TapChanger r, x, g, b, rho, alpha attributes in IIDM Network
            // Line b1, b2, g1, g2 attributes in IIDM Network
            // Shunt bPerSection attribute in IIDM Network
            String name = n.getLocalName();
            return name.equals("p") || name.equals("q") || name.equals("p0") || name.equals("q0")
                || name.equals("r") || name.equals("x") || name.equals("g") || name.equals("b") || name.equals("rho") || name.equals("alpha")
                || name.equals("b1") || name.equals("b2") || name.equals("g1") || name.equals("g2")
                || name.equals("bPerSection") || name.equals("activePowerSetpoint")
                || isAttrValueOfNumericProperty((Attr) n);
        }
        return false;
    }

    private static boolean isAttrValueOfNumericProperty(Attr attr) {
        // Check if we are inside a property element and if the name of property is voltage or angle
        if (attr.getLocalName().equals("value")) {
            Node p = attr.getOwnerElement();
            if (p.getNodeType() == Node.ELEMENT_NODE && p.getLocalName().equals("property")) {
                Node npname = p.getAttributes().getNamedItem("name");
                if (npname != null) {
                    String pname = npname.getTextContent();
                    return pname.equals("v") || pname.equals("angle");
                }
            }
        }
        return false;
    }

    private static double toleranceForNumericContent(Node n) {
        if (n.getLocalName().endsWith(".p") || n.getLocalName().endsWith(".q")) {
            return 1e-5;
        } else if (n.getLocalName().equals("p") || n.getLocalName().equals("q")) {
            return 1e-1;
        } else if (n.getLocalName().equals("p0") || n.getLocalName().equals("q0")) {
            return 1e-5;
        } else if (n.getLocalName().equals("r") || n.getLocalName().equals("x")
                || n.getLocalName().equals("b") || n.getLocalName().equals("g")
                || n.getLocalName().equals("b1") || n.getLocalName().equals("b2")
                || n.getLocalName().equals("g1") || n.getLocalName().equals("g2")
                || n.getLocalName().equals("alpha") || n.getLocalName().equals("rho")
                || n.getLocalName().equals("bPerSection") || n.getLocalName().equals("activePowerSetpoint")) {
            return 1e-5;
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
                if (n1.getLocalName() != null) {
                    LOG.error("            {} {}", n1.getLocalName(), n1.getTextContent());
                } else {
                    LOG.error("            {}", n1.getTextContent());
                }
                debugAttributes(n1, "                ");
            }
            if (n.getChildNodes().getLength() > maxNodes) {
                LOG.error("            ...");
            }
        }
    }

    private static void debugAttributes(Node n, String indent) {
        debugAttribute(n, RDF_NAMESPACE, "resource", indent);
        debugAttribute(n, RDF_NAMESPACE, "about", indent);
    }

    private static void debugAttribute(Node n, String namespace, String localName, String indent) {
        if (n.getAttributes() != null) {
            Node a = n.getAttributes().getNamedItemNS(namespace, localName);
            if (a != null) {
                LOG.error("{}{} = {}", indent, localName, a.getTextContent());
            }
        }
    }

    private static DiffBuilder withSelectedSshNodes(DiffBuilder diffBuilder) {
        return diffBuilder.withNodeFilter(n -> n.getNodeType() == Node.TEXT_NODE || isConsideredSshNode(n));
    }

    private static boolean isConsideredSshNode(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String name = n.getLocalName();
            // Optional attributes with default values
            if (name.equals("EquivalentInjection.regulationStatus") && n.getTextContent().equals("false")) {
                return false;
            } else if (name.equals("EquivalentInjection.regulationTarget") && Double.parseDouble(n.getTextContent()) == 0) {
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

    private static DiffBuilder selectingEquivalentSshObjects(DiffBuilder diffBuilder) {
        QName about = new QName(RDF_NAMESPACE, "about");
        ElementSelector elementSelector = ElementSelectors.conditionalBuilder()
                // If element has rdf:about attribute and is not FullModel
                .when(e -> !e.getAttributeNS(RDF_NAMESPACE, "about").isEmpty() && !e.getLocalName().equals("FullModel"))
                // Then select the same elements based on content of rdf:about attribute
                .thenUse(elementSelectorByAttributes(about))
                .elseUse(ElementSelectors.byName)
                .build();
        return diffBuilder.withNodeMatcher(new DefaultNodeMatcher(elementSelector));
    }

    private static boolean bothNullOrEqual(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    private static boolean mapsEqualForKeys(Map<QName, String> control, Map<QName, String> test, Iterable<QName> keys) {
        Iterator<QName> i = keys.iterator();
        QName q;
        do {
            if (!i.hasNext()) {
                return true;
            }
            q = i.next();
        } while (bothNullOrEqual(control.get(q), test.get(q)));
        return false;
    }

    private static ElementSelector elementSelectorByAttributes(QName... attribs) {
        if (attribs == null) {
            throw new IllegalArgumentException("attributes must not be null");
        } else {
            final Collection<QName> qs = Arrays.asList(attribs);
            if (Linqy.any(qs, new IsNullPredicate())) {
                throw new IllegalArgumentException("attributes must not contain null values");
            } else {
                return (controlElement, testElement) -> mapsEqualForKeys(Nodes.getAttributes(controlElement), Nodes.getAttributes(testElement), qs);
            }
        }
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
