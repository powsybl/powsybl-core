/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.powsybl.cgmes.conversion.elements.hvdc.Adjacency.AdjacentType;
import com.powsybl.cgmes.conversion.elements.hvdc.NodeEquipment.EquipmentReference;
import com.powsybl.commons.PowsyblException;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class IslandEndHvdc {

    // TN: n transformers (n >= 0), C1: one acDcConverter, LS1: one dcLineSegment
    // TN: n transformers (n >= 0), C2: two acDcConverters, LS1: one dcLineSegment
    // TN: n transformers (n >= 0), C1: one acDcConverter, LS2: two dcLineSegments
    // TN: n transformers (n >= 0), CN: n acDcConverters (usually 2), LSN: n dcLineSegments (usually 2)
    enum HvdcEndType {
        HVDC_TN_C1_LS1, HVDC_TN_C2_LS1, HVDC_TN_C1_LS2, HVDC_TN_CN_LSN,
    }

    private final List<HvdcEnd> hvdc;

    IslandEndHvdc() {
        hvdc = new ArrayList<>();
    }

    void add(Adjacency adjacency, NodeEquipment nodeEquipment, List<String> islandNodesEnd) {
        if (islandNodesEnd.isEmpty()) {
            return;
        }
        Set<String> visitedNodes = new HashSet<>();

        // islandNodesEnd can contain more than one hvdc configuration
        // Select the node with transformers where are more acDcConverters connected to
        Optional<String> nodeEndWithTransformers = nodeConnectedToTransformersWithMoreAcDcConvertersConnectedTo(nodeEquipment, islandNodesEnd, visitedNodes);
        while (nodeEndWithTransformers.isPresent()) {
            add(adjacency, nodeEquipment, visitedNodes, nodeEndWithTransformers.get(), islandNodesEnd);
            nodeEndWithTransformers = nodeConnectedToTransformersWithMoreAcDcConvertersConnectedTo(nodeEquipment, islandNodesEnd, visitedNodes);
        }

        if (!visitedNodes.isEmpty()) {
            return;
        }

        // IslandsEnds without transformers
        // Select the node where are more acDcConverters connected to
        Optional<String> nodeEnd = nodeWithMoreAcDcConvertersConnectedTo(nodeEquipment, islandNodesEnd, visitedNodes);
        while (nodeEnd.isPresent()) {
            add(adjacency, nodeEquipment, visitedNodes, nodeEnd.get(), islandNodesEnd);
            nodeEnd = nodeWithMoreAcDcConvertersConnectedTo(nodeEquipment, islandNodesEnd, visitedNodes);
        }
    }

    private static Optional<String> nodeConnectedToTransformersWithMoreAcDcConvertersConnectedTo(NodeEquipment nodeEquipment,
        List<String> islandNodesEnd, Set<String> visitedNodes) {
        return notVisitedNodesSortedByAcDcConvertersConnectedTo(nodeEquipment, islandNodesEnd, visitedNodes).stream()
            .filter(nodeEquipment::containsAnyTransformer).findFirst();
    }

    private static Optional<String> nodeWithMoreAcDcConvertersConnectedTo(NodeEquipment nodeEquipment,
        List<String> islandNodesEnd, Set<String> visitedNodes) {
        return notVisitedNodesSortedByAcDcConvertersConnectedTo(nodeEquipment, islandNodesEnd, visitedNodes).stream().findFirst();
    }

    // Not visited nodes connected to acDcConverters sorted by number of acDcConverters connected to
    // First node = node with more acDcConverters connected to
    private static List<String> notVisitedNodesSortedByAcDcConvertersConnectedTo(NodeEquipment nodeEquipment,
        List<String> islandNodesEnd, Set<String> visitedNodes) {
        return islandNodesEnd.stream().filter(nodeEnd -> !visitedNodes.contains(nodeEnd) && nodeEquipment.containsAnyAcDcConverter(nodeEnd))
            .sorted(Comparator.<String>comparingInt(nodeEquipment::acDcConvertersConnectedTo).reversed().thenComparing(nodeEnd -> nodeEnd))
            .collect(Collectors.toList());
    }

    private void add(Adjacency adjacency, NodeEquipment nodeEquipment, Set<String> visitedNodes, String nodeEnd,
        List<String> islandNodesEnd) {

        List<String> hvdcNodes = computeHvdcNodes(adjacency, nodeEquipment, nodeEnd, islandNodesEnd);
        Set<String> transformers = computeEquipment(nodeEquipment, hvdcNodes,
            NodeEquipment.EquipmentType.TRANSFORMER);
        Set<String> acDcConverters = computeEquipment(nodeEquipment, hvdcNodes,
            NodeEquipment.EquipmentType.AC_DC_CONVERTER);
        Set<String> dcLineSegment = computeEquipment(nodeEquipment, hvdcNodes,
            NodeEquipment.EquipmentType.DC_LINE_SEGMENT);

        visitedNodes.addAll(hvdcNodes);

        HvdcEnd hvdcEnd = new HvdcEnd(hvdcNodes, transformers, acDcConverters, dcLineSegment);
        hvdc.add(hvdcEnd);
    }

    private static List<String> computeHvdcNodes(Adjacency adjacency, NodeEquipment nodeEquipment,
        String nodeEnd, List<String> islandNodesEnd) {
        List<String> listNodes = new ArrayList<>();

        listNodes.add(nodeEnd);

        int k = 0;
        while (k < listNodes.size()) {
            String node = listNodes.get(k);
            if (adjacency.get().containsKey(node)) {
                adjacency.get().get(node).forEach(adjacent -> {
                    if (isAdjacentOk(nodeEquipment, listNodes, islandNodesEnd,
                        adjacent.type, adjacent.node)) {
                        listNodes.add(adjacent.node);
                    }
                });
            }
            k++;
        }
        return listNodes;
    }

    private static boolean isAdjacentOk(NodeEquipment nodeEquipment, List<String> visitedNodes,
        List<String> islandNodesEnd, AdjacentType adType, String adNode) {
        if (Adjacency.isDcLineSegment(adType)) {
            return false;
        }
        if (!islandNodesEnd.contains(adNode)) {
            return false;
        }
        if (visitedNodes.contains(adNode)) {
            return false;
        }
        return !nodeEquipment.multiAcDcConverter(adNode);
    }

    private static Set<String> computeEquipment(NodeEquipment nodeEquipment, List<String> hvdcNode,
        NodeEquipment.EquipmentType type) {
        Set<String> listEq = new HashSet<>();

        hvdcNode.forEach(n -> addEquipment(nodeEquipment, n, type, listEq));
        return listEq;
    }

    private static void addEquipment(NodeEquipment nodeEquipment, String node,
        NodeEquipment.EquipmentType type, Set<String> listEq) {
        List<EquipmentReference> listEqNode = nodeEquipment.getNodeEquipment().get(node);
        if (listEqNode == null) {
            return;
        }
        listEqNode.stream()
            .filter(eq -> eq.type == type)
            .forEachOrdered(eq -> listEq.add(eq.equipmentId));
    }

    List<HvdcEnd> getHvdc() {
        return hvdc;
    }

    static class HvdcEnd {
        final List<String> nodesEnd;
        final Set<String> transformersEnd;
        final Set<String> acDcConvertersEnd;
        final Set<String> dcLineSegmentsEnd;

        HvdcEnd(List<String> nodesEnd, Set<String> transformersEnd, Set<String> acDcConvertersEnd, Set<String> dcLineSegmentsEnd) {
            Objects.requireNonNull(nodesEnd);
            Objects.requireNonNull(transformersEnd);
            Objects.requireNonNull(acDcConvertersEnd);
            Objects.requireNonNull(dcLineSegmentsEnd);
            this.nodesEnd = nodesEnd;
            this.transformersEnd = transformersEnd;
            this.acDcConvertersEnd = acDcConvertersEnd;
            this.dcLineSegmentsEnd = dcLineSegmentsEnd;
        }

        HvdcEndType computeType() {
            int t = this.transformersEnd.size();
            int c = this.acDcConvertersEnd.size();
            int ls = this.dcLineSegmentsEnd.size();

            if (t >= 0 && c == 1 && ls == 1) {
                return HvdcEndType.HVDC_TN_C1_LS1;
            } else if (t >= 0 && c == 1 && ls == 2) {
                return HvdcEndType.HVDC_TN_C1_LS2;
            } else if (t >= 0 && c == 2 && ls == 1) {
                return HvdcEndType.HVDC_TN_C2_LS1;
            } else if (t >= 0 && c == ls && c > 1) {
                return HvdcEndType.HVDC_TN_CN_LSN;
            }

            throw new PowsyblException(String.format("Unexpected HVDC configuration: Transformers %d Converters %d DcLineSegments %d", t, c, ls));
        }

        boolean isMatchingTo(HvdcEnd otherHvdcEnd) {
            if (this.acDcConvertersEnd.size() != otherHvdcEnd.acDcConvertersEnd.size()) {
                return false;
            }
            if (this.dcLineSegmentsEnd.size() != otherHvdcEnd.dcLineSegmentsEnd.size()) {
                return false;
            }

            return this.dcLineSegmentsEnd.stream()
                .allMatch(otherHvdcEnd.dcLineSegmentsEnd::contains);
        }

        boolean isAssociatedWith(HvdcEnd otherHvdcEnd) {
            return this.dcLineSegmentsEnd.stream().anyMatch(otherHvdcEnd.dcLineSegmentsEnd::contains);
        }

        static HvdcEnd joinAll(List<HvdcEnd> listHvdcEnd) {

            HvdcEnd finalHvdcEnd = new HvdcEnd(new ArrayList<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());

            listHvdcEnd.stream().forEach(hvdcEnd -> {
                finalHvdcEnd.nodesEnd.addAll(hvdcEnd.nodesEnd);
                finalHvdcEnd.transformersEnd.addAll(hvdcEnd.transformersEnd);
                finalHvdcEnd.acDcConvertersEnd.addAll(hvdcEnd.acDcConvertersEnd);
                finalHvdcEnd.dcLineSegmentsEnd.addAll(hvdcEnd.dcLineSegmentsEnd);
            });

            return finalHvdcEnd;
        }
    }
}
