/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportConstants;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.iidm.network.*;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.ucte.converter.util.UcteConverterConstants.GEOGRAPHICAL_NAME_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class UcteExporterReportTest extends AbstractSerDeTest {

    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath),
                new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    private static ReportNode newTestRootReportNode() {
        return ReportNode.newRootReportNode()
                         .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME,
                                 PowsyblCoreReportResourceBundle.BASE_NAME)
                         .withMessageTemplate("testExportReportNode")
                         .build();
    }

    /**
     * Recursively collect all children nodes and their children. The tree is visited vertically.
     *
     * @param node a node
     * @return a {@link List} containing all descendant nodes. Their order respects a vertical visiting strategy.
     */
    private static List<ReportNode> getAllDescendantNodes(ReportNode node) {
        List<ReportNode> result = new ArrayList<>();
        for (ReportNode child : node.getChildren()) {
            result.add(child);
            result.addAll(getAllDescendantNodes(child));
        }
        return result;
    }

    @Test
    void testExportUcteWithoutReportNodeDoesntThrow() {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        new UcteExporter().export(network, new Properties(), new MemDataSource());
    }

    /**
     * Checks that exporting a network produces exactly one top-level report node, with message key
     * {@code core.ucte.export.UcteExport}, under the root report node passed to the exporter. The network
     * itself (loaded from {@code /expectedExport.uct}) is not relevant here, only the shape of the resulting
     * report tree.
     */
    @Test
    void testExportCreatesSingleTopLevelReportNode() {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        ReportNode rootReportNode = newTestRootReportNode();

        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertEquals(1, rootReportNode.getChildren().size());
        ReportNode exportReportNode = rootReportNode.getChildren().getFirst();
        assertEquals("core.ucte.export.UcteExport", exportReportNode.getMessageKey());
    }

    /**
     * Checks that YNodes and the equipment attached to them are correctly reported when exporting a network
     * containing three distinct YNode situations: an isolated YNode, a YNode with a boundary line, and a YNode
     * on one side of a two-windings transformer.
     *
     *
     * <pre>
     * Network layout:
     *
     *     VL_MAIN                                     VL_BOUNDARY                            VL_TRANSFORMER
     *
     *   FFFFFF11           YNODE_1                 YNODE_2                                      YNODE_3
     *       o                 o                       o                                            o
     *       |                                         |                                            |
     *       |                                         +---- BL_AT_YNODE                            |
     *       |                                                                                      |
     *       +-------------------------------FFFFFF11   FFFFFF12 1----------------------------------+
     *
     * </pre>
     * YNODE_1, YNODE_2 and YNODE_3 are each reported as an ignored YNode, regardless of the equipment attached
     * to them: the boundary line at YNODE_2 is additionally reported as ignored, while the transformer between
     * FFFFFF11 and YNODE_3 is still reported as exported (warning severity)
     */
    @Test
    void testYNodeExclusionsReported() {
        Network network = NetworkFactory.findDefault().createNetwork("ynode-test", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();

        VoltageLevel vlMain = substation.newVoltageLevel()
                                        .setId("VL_MAIN")
                                        .setNominalV(380)
                                        .setTopologyKind(TopologyKind.BUS_BREAKER)
                                        .add();
        vlMain.getBusBreakerView().newBus().setId("FFFFFF11").add();
        // TODO change me when ynodes detection is enriched
        vlMain.getBusBreakerView().newBus().setId("YNODE_1").add();

        VoltageLevel vlBoundary = substation.newVoltageLevel()
                                            .setId("VL_BOUNDARY")
                                            .setNominalV(380)
                                            .setTopologyKind(TopologyKind.BUS_BREAKER)
                                            .add();
        vlBoundary.getBusBreakerView().newBus().setId("YNODE_2").add();
        vlBoundary.newBoundaryLine()
                  .setId("BL_AT_YNODE")
                  .setBus("YNODE_2")
                  .setConnectableBus("YNODE_2")
                  .setR(0.0)
                  .setX(0.2)
                  .setG(0.0)
                  .setB(0.0)
                  .setP0(0.0)
                  .setQ0(0.0)
                  .setPairingKey("XXXXXX11")
                  .newGeneration()
                  .setTargetP(0.0)
                  .setTargetQ(0.0)
                  .add()
                  .add();

        VoltageLevel vlTransformer = substation.newVoltageLevel()
                                                .setId("VL_TRANSFORMER")
                                                .setNominalV(220)
                                                .setTopologyKind(TopologyKind.BUS_BREAKER)
                                                .add();
        vlTransformer.getBusBreakerView().newBus().setId("YNODE_3").add();
        substation.newTwoWindingsTransformer()
                  .setId("FFFFFF11 FFFFFF12 1")
                  .setVoltageLevel1("VL_MAIN")
                  .setBus1("FFFFFF11")
                  .setConnectableBus1("FFFFFF11")
                  .setRatedU1(380.0)
                  .setVoltageLevel2("VL_TRANSFORMER")
                  .setBus2("YNODE_3")
                  .setConnectableBus2("YNODE_3")
                  .setRatedU2(220.0)
                  .setR(1.0)
                  .setX(10.0)
                  .setG(0.0)
                  .setB(0.0)
                  .add();

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        List<ReportNode> allNodes = getAllDescendantNodes(rootReportNode);

        // The bus itself is reported as an ignored YNode for every YNode bus, regardless of what else
        // is attached to it (boundary line, transformer).
        List<ReportNode> ignoredYNode = allNodes.stream()
                .filter(n -> n.getMessageKey().equals("core.ucte.export.ignoredYNode"))
                .toList();
        assertEquals(3, ignoredYNode.size());
        Set<String> ignoredYNodeBusIds = ignoredYNode.stream()
                .map(n -> n.getValue("busId").map(Object::toString).orElseThrow())
                .collect(Collectors.toSet());
        assertEquals(Set.of("YNODE_1", "YNODE_2", "YNODE_3"), ignoredYNodeBusIds);
        ignoredYNode.forEach(n -> assertEquals(Optional.of(TypedValue.WARN_SEVERITY),
                n.getValue(ReportConstants.SEVERITY_KEY)));

        List<ReportNode> ignoredBoundaryLine = allNodes.stream()
                .filter(n -> n.getMessageKey().equals("core.ucte.export.ignoredBoundaryLineAtYNode"))
                .toList();
        assertEquals(1, ignoredBoundaryLine.size());
        assertEquals(Optional.of("BL_AT_YNODE"),
                ignoredBoundaryLine.getFirst().getValue("boundaryLineId").map(Object::toString));
        assertEquals(Optional.of(TypedValue.WARN_SEVERITY),
                ignoredBoundaryLine.getFirst().getValue(ReportConstants.SEVERITY_KEY));

        List<ReportNode> transformerAtBoundary = allNodes.stream()
                .filter(n -> n.getMessageKey().equals("core.ucte.export.transformerAtBoundaryExported"))
                .toList();
        assertEquals(1, transformerAtBoundary.size());
        assertEquals(Optional.of("FFFFFF11 FFFFFF12 1"),
                transformerAtBoundary.getFirst().getValue("transformerId").map(Object::toString));
        assertEquals(Optional.of(TypedValue.INFO_SEVERITY),
                transformerAtBoundary.getFirst().getValue(ReportConstants.SEVERITY_KEY));
    }

    /**
     * Checks that inconsistencies between merged boundary line properties are correctly reported when exporting
     * a merged network. The FR and BE networks (loaded from {@code /frForMergeProperties.uct} and
     * {@code /beForMergeProperties.uct}) share three boundary lines pairing on XNodes XXXXXX11, XXXXXX12 and
     * XXXXXX13, whose {@code geographicalName} property is set up to differ across sides:
     * <ul>
     *     <li>XXXXXX11: empty on the FR side, {@code "XNODE 1"} on the BE side</li>
     *     <li>XXXXXX12: {@code "XNODE 2"} on the FR side, empty on the BE side</li>
     *     <li>XXXXXX13: {@code "XNODE 5"} on the FR side, {@code "XNODE 3"} on the BE side (both non-empty
     *     and different)</li>
     * </ul>
     * After merging, exporting reports {@code mergedPropertyInconsistent} for XXXXXX13 (both sides non-empty
     * and different), and either {@code mergedPropertySide1Empty} or {@code mergedPropertySide2Empty} for
     * XXXXXX11 and XXXXXX12 (one side empty), depending on which BoundaryLine ends up as side 1 vs side 2 in
     * the merge.
     */
    @Test
    void testMergedPropertyInconsistenciesReported() {
        Network networkFR = loadNetworkFromResourceFile("/frForMergeProperties.uct");
        Network networkBE = loadNetworkFromResourceFile("/beForMergeProperties.uct");
        Network mergedNetwork = Network.merge(networkBE, networkFR);

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(mergedNetwork, new Properties(), new MemDataSource(), rootReportNode);

        List<ReportNode> mergedPropertyNodes = getAllDescendantNodes(rootReportNode)
                .stream()
                .filter(n -> n.getMessageKey().startsWith("core.ucte.export.mergedProperty"))
                .toList();

        // XXXXXX13's geographicalName differs on both sides ("XNODE 5" on the FR side, "XNODE 3" on the
        // BE side): both non-empty and different -> mergedPropertyInconsistent fires, regardless of which
        // BoundaryLine ends up as side 1 vs side 2 in the merge.
        List<ReportNode> inconsistent = mergedPropertyNodes
                .stream()
                .filter(n -> n.getMessageKey().equals("core.ucte.export.mergedPropertyInconsistent"))
                .filter(n -> n.getValue("key").map(Object::toString).map(GEOGRAPHICAL_NAME_PROPERTY_KEY::equals)
                              .orElse(false))
                .toList();
        assertEquals(1, inconsistent.size());
        assertEquals(
                "XNODE 3",
                inconsistent.getFirst().getValue("side1Value").map(Object::toString).orElseThrow()
        );
        assertEquals(
                "XNODE 5",
                inconsistent.getFirst().getValue("side2Value").map(Object::toString).orElseThrow()
        );

        // XXXXXX11's geographicalName is empty on the FR side and "XNODE 1" on the BE side, and XXXXXX12's
        // geographicalName is "XNODE 2" on the FR side and empty on the BE side: for each, one side is
        // empty -> either mergedPropertySide1Empty or mergedPropertySide2Empty fires (depending on side
        // assignment), and whichever one fires keeps the surviving non-empty value.
        List<ReportNode> oneSideEmpty = mergedPropertyNodes
                .stream()
                .filter(n -> n.getMessageKey().equals("core.ucte.export.mergedPropertySide1Empty")
                        || n.getMessageKey().equals("core.ucte.export.mergedPropertySide2Empty"))
                .filter(n -> n.getValue("key").map(Object::toString).map(GEOGRAPHICAL_NAME_PROPERTY_KEY::equals)
                              .orElse(false))
                .toList();
        assertEquals(2, oneSideEmpty.size());
        Set<String> survivingValues = oneSideEmpty
                .stream()
                .map(n -> {
                    String survivingValueKey;
                    if (n.getMessageKey().equals("core.ucte.export.mergedPropertySide1Empty")) {
                        survivingValueKey = "side2Value";
                    } else {
                        survivingValueKey = "side1Value";
                    }
                    return n.getValue(survivingValueKey).map(Object::toString).orElseThrow();
                })
                .collect(Collectors.toSet());
        assertEquals(Set.of("XNODE 1", "XNODE 2"), survivingValues);
    }

    /**
     * Checks that a closed switch with no current limit is reported when exporting the network.
     * <p>
     * Network layout:
     * <pre>
     *   VL
     *   FFFFFF11 --- FFFFFF11 FFFFFF12 1 --- FFFFFF12
     * </pre>
     * The switch {@code FFFFFF11 FFFFFF12 1} is closed and has no current limit set.
     */
    @Test
    void testSwitchCurrentLimitMissingReported() {
        Network network = NetworkFactory.findDefault().createNetwork("switch-test", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                              .setId("VL")
                                              .setNominalV(380)
                                              .setTopologyKind(TopologyKind.BUS_BREAKER)
                                              .add();
        voltageLevel.getBusBreakerView().newBus().setId("FFFFFF11").add();
        voltageLevel.getBusBreakerView().newBus().setId("FFFFFF12").add();
        voltageLevel.getBusBreakerView().newSwitch()
                    .setId("FFFFFF11 FFFFFF12 1")
                    .setBus1("FFFFFF11")
                    .setBus2("FFFFFF12")
                    .setOpen(false)
                    .add();

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        List<ReportNode> matches = getAllDescendantNodes(rootReportNode)
                .stream()
                .filter(n -> n.getMessageKey().equals("core.ucte.export.switchCurrentLimitMissing"))
                .toList();
        assertEquals(1, matches.size());
        assertEquals(Optional.of("FFFFFF11 FFFFFF12 1"), matches.getFirst().getValue("switchId").map(Object::toString));
        assertEquals(Optional.of(TypedValue.WARN_SEVERITY), matches.getFirst().getValue(ReportConstants.SEVERITY_KEY));
    }
}
