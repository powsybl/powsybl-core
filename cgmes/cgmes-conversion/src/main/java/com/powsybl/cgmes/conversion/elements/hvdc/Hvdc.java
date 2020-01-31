/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.elements.hvdc.TPnodeEquipments.TPnodeEquipment;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class Hvdc {

    private Set<HvdcEquipment> hvdcData;

    // The island includes dcTopologicalNodes and first acTopologicalNode
    Hvdc() {
        hvdcData = new HashSet<>();
    }

    void add(Adjacency adjacency, TPnodeEquipments tpNodeEqipments, String commonTopologicalNode1,
        List<String> islandNodesEnd1, String commonTopologicalNode2, List<String> islandNodesEnd2) {
        if (islandNodesEnd1.isEmpty() || islandNodesEnd2.isEmpty()) {
            return;
        }
        Set<String> visitedTopologicalNodes = new HashSet<>();

        int k = 0;
        while (k < islandNodesEnd1.size()) {
            String topologicalNodeEnd1 = islandNodesEnd1.get(k);
            if (!visitedTopologicalNodes.contains(topologicalNodeEnd1)
                && !commonTopologicalNode1.equals(topologicalNodeEnd1)) {
                add(adjacency, tpNodeEqipments, visitedTopologicalNodes, topologicalNodeEnd1, commonTopologicalNode1,
                    islandNodesEnd1, commonTopologicalNode2, islandNodesEnd2);
            }
            k++;
        }
    }

    private void add(Adjacency adjacency, TPnodeEquipments tpNodeEqipments, Set<String> visitedTopologicalNodes,
        String topologicalNodeEnd1, String commonTopologicalNode1, List<String> islandNodesEnd1,
        String commonTopologicalNode2, List<String> islandNodesEnd2) {

        List<String> hvdcNodes1 = computeHvdcNodes(adjacency, visitedTopologicalNodes, topologicalNodeEnd1,
            commonTopologicalNode1, islandNodesEnd1);
        printAd(adjacency, hvdcNodes1);
        Set<String> transformers1 = computeEquipments(tpNodeEqipments, hvdcNodes1,
            TPnodeEquipments.EquipmentType.TRANSFORMER);
        Set<String> acDcConverters1 = computeEquipments(tpNodeEqipments, hvdcNodes1,
            TPnodeEquipments.EquipmentType.AC_DC_CONVERTER);
        Set<String> dcLineSegment1 = computeEquipments(tpNodeEqipments, hvdcNodes1,
            TPnodeEquipments.EquipmentType.DC_LINE_SEGMENT);

        String topologicalNodeEnd2 = computeTopologicalNodeEnd2(tpNodeEqipments, islandNodesEnd2, dcLineSegment1);
        if (topologicalNodeEnd2 == null) {
            return;
        }

        List<String> hvdcNodes2 = computeHvdcNodes(adjacency, visitedTopologicalNodes, topologicalNodeEnd2,
            commonTopologicalNode2, islandNodesEnd2);
        Set<String> transformers2 = computeEquipments(tpNodeEqipments, hvdcNodes2,
            TPnodeEquipments.EquipmentType.TRANSFORMER);
        Set<String> acDcConverters2 = computeEquipments(tpNodeEqipments, hvdcNodes2,
            TPnodeEquipments.EquipmentType.AC_DC_CONVERTER);
        Set<String> dcLineSegment2 = computeEquipments(tpNodeEqipments, hvdcNodes2,
            TPnodeEquipments.EquipmentType.DC_LINE_SEGMENT);

        HvdcEquipment hvdcEq = new HvdcEquipment(transformers1, acDcConverters1, dcLineSegment1, transformers2,
            acDcConverters2, dcLineSegment2);
        hvdcData.add(hvdcEq);

    }

    private List<String> computeHvdcNodes(Adjacency adjacency, Set<String> visitedTopologicalNodes,
        String topologicalNodeEnd1, String commonTopologicalNode1, List<String> islandNodesEnd1) {
        List<String> listTp = new ArrayList<>();

        listTp.add(topologicalNodeEnd1);
        visitedTopologicalNodes.add(topologicalNodeEnd1);

        int k = 0;
        while (k < listTp.size()) {
            String topologicalNode = listTp.get(k);
            if (adjacency.adjacency.containsKey(topologicalNode)) {
                adjacency.adjacency.get(topologicalNode).forEach(adjacent -> {
                    if (Adjacency.isDcLineSegment(adjacent.type)) {
                        return;
                    }
                    if (visitedTopologicalNodes.contains(adjacent.topologicalNode)) {
                        return;
                    }
                    if (commonTopologicalNode1.equals(adjacent.topologicalNode)) {
                        return;
                    }
                    listTp.add(adjacent.topologicalNode);
                    visitedTopologicalNodes.add(adjacent.topologicalNode);
                });
            }
            k++;
        }
        return listTp;
    }

    private Set<String> computeEquipments(TPnodeEquipments tpNodeEqipments, List<String> hvdcNode,
        TPnodeEquipments.EquipmentType type) {
        Set<String> listEq = new HashSet<>();

        hvdcNode.forEach(n -> addEquipments(tpNodeEqipments, n, type, listEq));
        return listEq;
    }

    private void addEquipments(TPnodeEquipments tpNodeEqipments, String topologicalNode,
        TPnodeEquipments.EquipmentType type, Set<String> listEq) {
        List<TPnodeEquipment> listEqNode = tpNodeEqipments.nodeEquipments.get(topologicalNode);
        if (listEqNode == null) {
            return;
        }
        listEqNode.stream()
            .filter(eq -> eq.type == type)
            .forEachOrdered(eq -> listEq.add(eq.equipmentId));
    }

    private String computeTopologicalNodeEnd2(TPnodeEquipments tpNodeEqipments, List<String> islandNodesEnd,
        Set<String> dcLineSegment) {
        return islandNodesEnd.stream()
            .filter(n -> dcLineSegmentInNode(tpNodeEqipments, dcLineSegment, n))
            .findFirst()
            .orElse(null);
    }

    private boolean dcLineSegmentInNode(TPnodeEquipments tpNodeEqipments, Set<String> dcLineSegment,
        String topologicalNode) {
        return dcLineSegment.stream()
            .anyMatch(dcls -> tpNodeEqipments.containsDcLineSegmentInNode(dcls, topologicalNode));
    }

    private void printAd(Adjacency adjacency, List<String> lnodes) {
        lnodes.forEach(n -> printAd(adjacency, n));
    }

    private void printAd(Adjacency adjacency, String node) {
        System.err.printf("JAM. Nodo %s %n", node);
        if (adjacency.adjacency.containsKey(node)) {
            adjacency.adjacency.get(node).forEach(ad -> System.err.printf("    %s %s %n", ad.type, ad.topologicalNode));
        }
    }

    void print() {
        LOG.info("Hvdc");
        hvdcData.forEach(islandEnd -> islandEnd.print());
    }

    static class HvdcEquipment {
        Set<String> transformersEnd1;
        Set<String> acDcConvertersEnd1;
        Set<String> dcLineSegmentsEnd1;
        Set<String> transformersEnd2;
        Set<String> acDcConvertersEnd2;
        Set<String> dcLineSegmentsEnd2;

        HvdcEquipment(Set<String> transformersEnd1, Set<String> acDcConvertersEnd1, Set<String> dcLineSegmentsEnd1,
            Set<String> transformersEnd2, Set<String> acDcConvertersEnd2, Set<String> dcLineSegmentsEnd2) {
            this.transformersEnd1 = transformersEnd1;
            this.acDcConvertersEnd1 = acDcConvertersEnd1;
            this.dcLineSegmentsEnd1 = dcLineSegmentsEnd1;
            this.transformersEnd2 = transformersEnd2;
            this.acDcConvertersEnd2 = acDcConvertersEnd2;
            this.dcLineSegmentsEnd2 = dcLineSegmentsEnd2;
        }

        void print() {
            LOG.info("    transformersEnd1: {}", this.transformersEnd1);
            LOG.info("    acDcConvertersEnd1: {}", this.acDcConvertersEnd1);
            LOG.info("    dcLineSegmentsEnd1: {}", this.dcLineSegmentsEnd1);
            LOG.info("    transformersEnd2: {}", this.transformersEnd2);
            LOG.info("    acDcConvertersEnd2: {}", this.acDcConvertersEnd2);
            LOG.info("    dcLineSegmentsEnd2: {}", this.dcLineSegmentsEnd2);
            LOG.info("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Hvdc.class);
}
