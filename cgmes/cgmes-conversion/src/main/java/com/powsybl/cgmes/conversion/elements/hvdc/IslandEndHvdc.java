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
        HVDC_T0_C1_LS1, HVDC_T1_C1_LS1, HVDC_TN_CN_LSN, HVDC_T1_C1_LS2, HVDC_T2_C2_LS1,
    }

    private final List<HvdcEnd> hvdc;

    IslandEndHvdc() {
        hvdc = new ArrayList<>();
    }

    void add(Adjacency adjacency, TPnodeEquipments tpNodeEquipments, List<String> islandNodesEnd) {
        if (islandNodesEnd.isEmpty()) {
            return;
        }
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

        if (!visitedTopologicalNodes.isEmpty()) {
            return;
        }
        // IslandsEnds without transformers
        // Take a non-visited node with acDcConverters
        k = 0;
        while (k < islandNodesEnd.size()) {
            String topologicalNodeEnd = islandNodesEnd.get(k);
            if (!visitedTopologicalNodes.contains(topologicalNodeEnd)
                && tpNodeEquipments.containsAnyAcDcConverter(topologicalNodeEnd)) {
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

    private static List<String> computeHvdcNodes(Adjacency adjacency, TPnodeEquipments tpNodeEquipments,
        Set<String> visitedTopologicalNodes,
        String topologicalNodeEnd, List<String> islandNodesEnd) {
        List<String> listTp = new ArrayList<>();

        listTp.add(topologicalNodeEnd);
        visitedTopologicalNodes.add(topologicalNodeEnd);

        int k = 0;
        while (k < listTp.size()) {
            String topologicalNode = listTp.get(k);
            if (adjacency.get().containsKey(topologicalNode)) {
                adjacency.get().get(topologicalNode).forEach(adjacent -> {
                    if (isAdjacentOk(tpNodeEquipments, visitedTopologicalNodes, islandNodesEnd,
                        adjacent.type, adjacent.topologicalNode)) {
                        listTp.add(adjacent.topologicalNode);
                        visitedTopologicalNodes.add(adjacent.topologicalNode);
                    }
                });
            }
            k++;
        }
        return listTp;
    }

    private static boolean isAdjacentOk(TPnodeEquipments tpNodeEquipments, Set<String> visitedTopologicalNodes,
        List<String> islandNodesEnd, AdjacentType adType, String adTopologicalNode) {
        if (Adjacency.isDcLineSegment(adType)) {
            return false;
        }
        if (!islandNodesEnd.contains(adTopologicalNode)) {
            return false;
        }
        if (visitedTopologicalNodes.contains(adTopologicalNode)) {
            return false;
        }
        return !tpNodeEquipments.multiAcDcConverter(adTopologicalNode);
    }

    private static Set<String> computeEquipments(TPnodeEquipments tpNodeEquipments, List<String> hvdcNode,
        TPnodeEquipments.EquipmentType type) {
        Set<String> listEq = new HashSet<>();

        hvdcNode.forEach(n -> addEquipments(tpNodeEquipments, n, type, listEq));
        return listEq;
    }

    private static void addEquipments(TPnodeEquipments tpNodeEquipments, String topologicalNode,
        TPnodeEquipments.EquipmentType type, Set<String> listEq) {
        List<TPnodeEquipment> listEqNode = tpNodeEquipments.getNodeEquipments().get(topologicalNode);
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
        if (hvdcEnd1.transformersEnd.size() != hvdcEnd2.transformersEnd.size()) {
            return false;
        }
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
        final List<String> topologicalNodesEnd;
        final Set<String> transformersEnd;
        final Set<String> acDcConvertersEnd;
        final Set<String> dcLineSegmentsEnd;

        HvdcEnd(List<String> topologicalNodesEnd, Set<String> transformersEnd, Set<String> acDcConvertersEnd, Set<String> dcLineSegmentsEnd) {
            Objects.requireNonNull(topologicalNodesEnd);
            Objects.requireNonNull(transformersEnd);
            Objects.requireNonNull(acDcConvertersEnd);
            Objects.requireNonNull(dcLineSegmentsEnd);
            this.topologicalNodesEnd = topologicalNodesEnd;
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
            LOG.debug("    topologicalNodesEnd: {}", this.topologicalNodesEnd);
            LOG.debug("    transformersEnd: {}", this.transformersEnd);
            LOG.debug("    acDcConvertersEnd: {}", this.acDcConvertersEnd);
            LOG.debug("    dcLineSegmentsEnd: {}", this.dcLineSegmentsEnd);
            LOG.debug("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IslandEndHvdc.class);
}
