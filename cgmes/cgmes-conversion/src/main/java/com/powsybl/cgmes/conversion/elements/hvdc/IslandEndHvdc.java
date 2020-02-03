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
class IslandEndHvdc {

    private Set<HvdcEnd> hvdc;

    IslandEndHvdc() {
        hvdc = new HashSet<>();
    }

    void add(Adjacency adjacency, TPnodeEquipments tpNodeEquipments, List<String> islandNodesEnd) {
        if (islandNodesEnd.isEmpty()) {
            return;
        }
        printAd(adjacency, islandNodesEnd);
        printEq(tpNodeEquipments, islandNodesEnd);
        printDcLs(tpNodeEquipments, islandNodesEnd);
        Set<String> visitedTopologicalNodes = new HashSet<>();

        // Take a non-visited node with transformers
        int k = 0;
        while (k < islandNodesEnd.size()) {
            String topologicalNodeEnd = islandNodesEnd.get(k);
            if (!visitedTopologicalNodes.contains(topologicalNodeEnd)
                && tpNodeEquipments.containsAnyTransformer(topologicalNodeEnd)) {
                add(adjacency, tpNodeEquipments, visitedTopologicalNodes, topologicalNodeEnd, islandNodesEnd);
            }
            k++;
        }
    }

    private void add(Adjacency adjacency, TPnodeEquipments tpNodeEquipments, Set<String> visitedTopologicalNodes,
        String topologicalNodeEnd, List<String> islandNodesEnd) {

        List<String> hvdcNodes = computeHvdcNodes(adjacency, tpNodeEquipments, visitedTopologicalNodes,
            topologicalNodeEnd, islandNodesEnd);
        Set<String> transformers = computeEquipments(tpNodeEquipments, hvdcNodes,
            TPnodeEquipments.EquipmentType.TRANSFORMER);
        Set<String> acDcConverters = computeEquipments(tpNodeEquipments, hvdcNodes,
            TPnodeEquipments.EquipmentType.AC_DC_CONVERTER);
        Set<String> dcLineSegment = computeEquipments(tpNodeEquipments, hvdcNodes,
            TPnodeEquipments.EquipmentType.DC_LINE_SEGMENT);

        HvdcEnd hvdcEnd = new HvdcEnd(hvdcNodes, transformers, acDcConverters, dcLineSegment);
        hvdc.add(hvdcEnd);
    }

    private List<String> computeHvdcNodes(Adjacency adjacency, TPnodeEquipments tpNodeEquipments,
        Set<String> visitedTopologicalNodes,
        String topologicalNodeEnd, List<String> islandNodesEnd) {
        List<String> listTp = new ArrayList<>();

        listTp.add(topologicalNodeEnd);
        visitedTopologicalNodes.add(topologicalNodeEnd);

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
                    if (tpNodeEquipments.multiAcDcConverter(adjacent.topologicalNode)) {
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

    private Set<String> computeEquipments(TPnodeEquipments tpNodeEquipments, List<String> hvdcNode,
        TPnodeEquipments.EquipmentType type) {
        Set<String> listEq = new HashSet<>();

        hvdcNode.forEach(n -> addEquipments(tpNodeEquipments, n, type, listEq));
        return listEq;
    }

    private void addEquipments(TPnodeEquipments tpNodeEquipments, String topologicalNode,
        TPnodeEquipments.EquipmentType type, Set<String> listEq) {
        List<TPnodeEquipment> listEqNode = tpNodeEquipments.nodeEquipments.get(topologicalNode);
        if (listEqNode == null) {
            return;
        }
        listEqNode.stream()
            .filter(eq -> eq.type == type)
            .forEachOrdered(eq -> listEq.add(eq.equipmentId));
    }

    private void printAd(Adjacency adjacency, List<String> lnodes) {
        lnodes.forEach(n -> printAd(adjacency, n));
    }

    private void printAd(Adjacency adjacency, String node) {
        System.err.printf("JAM AD Nodo %s %n", node);
        if (adjacency.adjacency.containsKey(node)) {
            adjacency.adjacency.get(node).forEach(ad -> System.err.printf("    %s %s %n", ad.type, ad.topologicalNode));
        }
    }

    private void printEq(TPnodeEquipments tpNodeEquipments, List<String> lnodes) {
        lnodes.forEach(n -> printEq(tpNodeEquipments, n));
    }

    private void printEq(TPnodeEquipments tpNodeEquipments, String node) {
        System.err.printf("JAM EQ. Nodo %s %n", node);
        if (tpNodeEquipments.nodeEquipments.containsKey(node)) {
            tpNodeEquipments.nodeEquipments.get(node)
                .forEach(eq -> System.err.printf("    %s %s %n", eq.type, eq.equipmentId));
        }
    }

    private void printDcLs(TPnodeEquipments tpNodeEquipments, List<String> lnodes) {
        lnodes.forEach(n -> printDcLs(tpNodeEquipments, n));
    }

    private void printDcLs(TPnodeEquipments tpNodeEquipments, String node) {
        if (tpNodeEquipments.nodeEquipments.containsKey(node)) {
            tpNodeEquipments.nodeEquipments.get(node).stream()
                .filter(eq -> eq.type == TPnodeEquipments.EquipmentType.DC_LINE_SEGMENT)
                .forEach(eq -> System.err.printf("JAM - %s %s %n", eq.type, eq.equipmentId));
        }
    }

    void print() {
        LOG.info("IslandEndHvdc");
        hvdc.forEach(h -> h.print());
    }

    static class HvdcEnd {
        List<String> topologicalNodesEnd;
        Set<String> transformersEnd;
        Set<String> acDcConvertersEnd;
        Set<String> dcLineSegmentsEnd;

        HvdcEnd(List<String> topologicalNodesEnd, Set<String> transformersEnd, Set<String> acDcConvertersEnd, Set<String> dcLineSegmentsEnd) {
            this.topologicalNodesEnd = topologicalNodesEnd;
            this.transformersEnd = transformersEnd;
            this.acDcConvertersEnd = acDcConvertersEnd;
            this.dcLineSegmentsEnd = dcLineSegmentsEnd;
        }

        void print() {
            LOG.info("    topologicalNodesEnd: {}", this.topologicalNodesEnd);
            LOG.info("    transformersEnd: {}", this.transformersEnd);
            LOG.info("    acDcConvertersEnd: {}", this.acDcConvertersEnd);
            LOG.info("    dcLineSegmentsEnd: {}", this.dcLineSegmentsEnd);
            LOG.info("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IslandEndHvdc.class);
}
