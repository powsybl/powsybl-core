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
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.elements.hvdc.Adjacency.AdjacentType;
import com.powsybl.cgmes.conversion.elements.hvdc.NodeEquipment.EquipmentReference;
import com.powsybl.commons.PowsyblException;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class IslandEndHvdc {

    // T1: no transformer, C1: one acDcConverter, LS1: one dcLineSegment
    // T1: one transformer, C1: one acDcConverter, LS1: one dcLineSegment
    // T2: two transformers, C2: two acDcConverters, LS2: two dcLineSegments
    // TN: n transformers (usually 2), CN: n acDcConverters (usually 2), LSN: n dcLineSegments (usually 2)
    enum HvdcEndType {
        HVDC_T0_C1_LS1, HVDC_T0_C1_LS2, HVDC_T0_C2_LS1, HVDC_T1_C1_LS1, HVDC_TN_CN_LSN, HVDC_T1_C1_LS2, HVDC_T2_C2_LS1,
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

        // Take a non-visited node with transformers
        int k = 0;
        while (k < islandNodesEnd.size()) {
            String nodeEnd = islandNodesEnd.get(k);
            if (!visitedNodes.contains(nodeEnd)
                && nodeEquipment.containsAnyTransformer(nodeEnd)) {
                add(adjacency, nodeEquipment, visitedNodes, nodeEnd, islandNodesEnd);
            }
            k++;
        }

        if (!visitedNodes.isEmpty()) {
            return;
        }
        // IslandsEnds without transformers
        // Take a non-visited node with acDcConverters
        k = 0;
        while (k < islandNodesEnd.size()) {
            String nodeEnd = islandNodesEnd.get(k);
            if (!visitedNodes.contains(nodeEnd)
                && nodeEquipment.containsAnyAcDcConverter(nodeEnd)) {
                add(adjacency, nodeEquipment, visitedNodes, nodeEnd, islandNodesEnd);
            }
            k++;
        }
    }

    private void add(Adjacency adjacency, NodeEquipment nodeEquipment, Set<String> visitedNodes,
        String nodeEnd, List<String> islandNodesEnd) {

        List<String> hvdcNodes = computeHvdcNodes(adjacency, nodeEquipment, visitedNodes,
            nodeEnd, islandNodesEnd);
        Set<String> transformers = computeEquipment(nodeEquipment, hvdcNodes,
            NodeEquipment.EquipmentType.TRANSFORMER);
        Set<String> acDcConverters = computeEquipment(nodeEquipment, hvdcNodes,
            NodeEquipment.EquipmentType.AC_DC_CONVERTER);
        Set<String> dcLineSegment = computeEquipment(nodeEquipment, hvdcNodes,
            NodeEquipment.EquipmentType.DC_LINE_SEGMENT);

        HvdcEnd hvdcEnd = new HvdcEnd(hvdcNodes, transformers, acDcConverters, dcLineSegment);
        hvdc.add(hvdcEnd);
    }

    private static List<String> computeHvdcNodes(Adjacency adjacency, NodeEquipment nodeEquipment,
        Set<String> visitedNodes,
        String nodeEnd, List<String> islandNodesEnd) {
        List<String> listNodes = new ArrayList<>();

        listNodes.add(nodeEnd);
        visitedNodes.add(nodeEnd);

        int k = 0;
        while (k < listNodes.size()) {
            String node = listNodes.get(k);
            if (adjacency.get().containsKey(node)) {
                adjacency.get().get(node).forEach(adjacent -> {
                    if (isAdjacentOk(nodeEquipment, visitedNodes, islandNodesEnd,
                        adjacent.type, adjacent.node)) {
                        listNodes.add(adjacent.node);
                        visitedNodes.add(adjacent.node);
                    }
                });
            }
            k++;
        }
        return listNodes;
    }

    private static boolean isAdjacentOk(NodeEquipment nodeEquipment, Set<String> visitedNodes,
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

    HvdcEnd selectSymmetricHvdcEnd(HvdcEnd hvdcEnd1) {
        return hvdc.stream().filter(h -> isCompatible(hvdcEnd1, h)).findFirst().orElse(null);
    }

    private static boolean isCompatible(HvdcEnd hvdcEnd1, HvdcEnd hvdcEnd2) {
        if (hvdcEnd1.acDcConvertersEnd.size() != hvdcEnd2.acDcConvertersEnd.size()) {
            return false;
        }
        if (hvdcEnd1.dcLineSegmentsEnd.size() != hvdcEnd2.dcLineSegmentsEnd.size()) {
            return false;
        }

        return hvdcEnd1.dcLineSegmentsEnd.stream()
            .allMatch(hvdcEnd2.dcLineSegmentsEnd::contains);
    }

    List<HvdcEnd> getHvdc() {
        return hvdc;
    }

    void debug() {
        LOG.debug("IslandEndHvdc");
        hvdc.forEach(HvdcEnd::debug);
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

            if (t == 0 && c == 1 && ls == 1) {
                return HvdcEndType.HVDC_T0_C1_LS1;
            } else if (t == 0 && c == 1 && ls == 2) {
                return HvdcEndType.HVDC_T0_C1_LS2;
            } else if (t == 0 && c == 2 && ls == 1) {
                return HvdcEndType.HVDC_T0_C2_LS1;
            } else if (t == 1 && c == 1 && ls == 1) {
                return HvdcEndType.HVDC_T1_C1_LS1;
            } else if (t == 1 && c == 1 && ls == 2) {
                return HvdcEndType.HVDC_T1_C1_LS2;
            } else if (t == 2 && c == 2 && ls == 1) {
                return HvdcEndType.HVDC_T2_C2_LS1;
            } else if (t == c && c == ls && t > 1) {
                return HvdcEndType.HVDC_TN_CN_LSN;
            }

            throw new PowsyblException(String.format("Unexpected HVDC configuration: Transformers %d Converters %d DcLineSegments %d", t, c, ls));
        }

        void debug() {
            LOG.debug("    nodesEnd: {}", this.nodesEnd);
            LOG.debug("    transformersEnd: {}", this.transformersEnd);
            LOG.debug("    acDcConvertersEnd: {}", this.acDcConvertersEnd);
            LOG.debug("    dcLineSegmentsEnd: {}", this.dcLineSegmentsEnd);
            LOG.debug("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IslandEndHvdc.class);
}
