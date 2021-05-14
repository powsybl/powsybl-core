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
import com.powsybl.cgmes.conversion.elements.hvdc.TPnodeEquipments.TPnodeEquipment;
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

    void add(Adjacency adjacency, TPnodeEquipments tpNodeEquipments, List<String> islandNodesEnd) {
        if (islandNodesEnd.isEmpty()) {
            return;
        }
        Set<String> visitedNodes = new HashSet<>();

        // Take a non-visited node with transformers
        int k = 0;
        while (k < islandNodesEnd.size()) {
            String nodeEnd = islandNodesEnd.get(k);
            if (!visitedNodes.contains(nodeEnd)
                && tpNodeEquipments.containsAnyTransformer(nodeEnd)) {
                add(adjacency, tpNodeEquipments, visitedNodes, nodeEnd, islandNodesEnd);
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
                && tpNodeEquipments.containsAnyAcDcConverter(nodeEnd)) {
                add(adjacency, tpNodeEquipments, visitedNodes, nodeEnd, islandNodesEnd);
            }
            k++;
        }
    }

    private void add(Adjacency adjacency, TPnodeEquipments tpNodeEquipments, Set<String> visitedNodes,
        String nodeEnd, List<String> islandNodesEnd) {

        List<String> hvdcNodes = computeHvdcNodes(adjacency, tpNodeEquipments, visitedNodes,
            nodeEnd, islandNodesEnd);
        Set<String> transformers = computeEquipments(tpNodeEquipments, hvdcNodes,
            TPnodeEquipments.EquipmentType.TRANSFORMER);
        Set<String> acDcConverters = computeEquipments(tpNodeEquipments, hvdcNodes,
            TPnodeEquipments.EquipmentType.AC_DC_CONVERTER);
        Set<String> dcLineSegment = computeEquipments(tpNodeEquipments, hvdcNodes,
            TPnodeEquipments.EquipmentType.DC_LINE_SEGMENT);

        HvdcEnd hvdcEnd = new HvdcEnd(hvdcNodes, transformers, acDcConverters, dcLineSegment);
        hvdc.add(hvdcEnd);
    }

    private static List<String> computeHvdcNodes(Adjacency adjacency, TPnodeEquipments tpNodeEquipments,
        Set<String> visitedNodes,
        String nodeEnd, List<String> islandNodesEnd) {
        List<String> listTp = new ArrayList<>();

        listTp.add(nodeEnd);
        visitedNodes.add(nodeEnd);

        int k = 0;
        while (k < listTp.size()) {
            String node = listTp.get(k);
            if (adjacency.get().containsKey(node)) {
                adjacency.get().get(node).forEach(adjacent -> {
                    if (isAdjacentOk(tpNodeEquipments, visitedNodes, islandNodesEnd,
                        adjacent.type, adjacent.node)) {
                        listTp.add(adjacent.node);
                        visitedNodes.add(adjacent.node);
                    }
                });
            }
            k++;
        }
        return listTp;
    }

    private static boolean isAdjacentOk(TPnodeEquipments tpNodeEquipments, Set<String> visitedNodes,
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
        return !tpNodeEquipments.multiAcDcConverter(adNode);
    }

    private static Set<String> computeEquipments(TPnodeEquipments tpNodeEquipments, List<String> hvdcNode,
        TPnodeEquipments.EquipmentType type) {
        Set<String> listEq = new HashSet<>();

        hvdcNode.forEach(n -> addEquipments(tpNodeEquipments, n, type, listEq));
        return listEq;
    }

    private static void addEquipments(TPnodeEquipments tpNodeEquipments, String node,
        TPnodeEquipments.EquipmentType type, Set<String> listEq) {
        List<TPnodeEquipment> listEqNode = tpNodeEquipments.getNodeEquipments().get(node);
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
