/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class Adjacency {

    enum AdjacentType {
        DC_LINE_SEGMENT, AC_DC_CONVERTER, AC_TRANSFORMER
    }

    private final Map<String, List<Adjacent>> adjacency;

    Adjacency(CgmesModel cgmesModel, AcDcConverterNodes acDcConverterNodes) {
        adjacency = new HashMap<>();
        cgmesModel.dcLineSegments().forEach(dcls -> computeDcLineSegmentAdjacency(cgmesModel, dcls));

        acDcConverterNodes.getConverterNodes()
            .forEach((key, value) -> computeAcDcConverterAdjacency(value.acNode,
                value.dcNode));

        cgmesModel.groupedTransformerEnds().forEach((t, ends) -> {
            if (ends.size() == 2) {
                computeTwoWindingsTransformerAdjacency(cgmesModel, ends);
            } else if (ends.size() == 3) {
                computeThreeWindingsTransformerAdjacency(cgmesModel, ends);
            } else {
                throw new PowsyblException(String.format("Unexpected TransformerEnds: ends %d", ends.size()));
            }
        });
    }

    private void computeDcLineSegmentAdjacency(CgmesModel cgmesModel, PropertyBag equipment) {
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 1));
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 2));

        addAdjacency(CgmesDcConversion.getDcNode(cgmesModel, t1), CgmesDcConversion.getDcNode(cgmesModel, t2),
            AdjacentType.DC_LINE_SEGMENT);
    }

    private void computeAcDcConverterAdjacency(String acNode, List<String> dcNodes) {
        dcNodes.forEach(
            dcNode -> addAdjacency(acNode, dcNode, AdjacentType.AC_DC_CONVERTER));
        for (int k = 0; k < dcNodes.size() - 1; k++) {
            String dcNode = dcNodes.get(k);
            for (int l = k + 1; l < dcNodes.size(); l++) {
                addAdjacency(dcNode, dcNodes.get(l), AdjacentType.AC_DC_CONVERTER);
            }
        }
    }

    private void computeTwoWindingsTransformerAdjacency(CgmesModel cgmesModel, PropertyBags ends) {
        PropertyBag end1 = ends.get(0);
        CgmesTerminal t1 = cgmesModel.terminal(end1.getId(CgmesNames.TERMINAL));
        PropertyBag end2 = ends.get(1);
        CgmesTerminal t2 = cgmesModel.terminal(end2.getId(CgmesNames.TERMINAL));

        addTransformerAdjacency(Arrays.asList(CgmesDcConversion.getAcNode(cgmesModel, t1), CgmesDcConversion.getAcNode(cgmesModel, t2)));
    }

    private void computeThreeWindingsTransformerAdjacency(CgmesModel cgmesModel, PropertyBags ends) {
        PropertyBag end1 = ends.get(0);
        CgmesTerminal t1 = cgmesModel.terminal(end1.getId(CgmesNames.TERMINAL));
        PropertyBag end2 = ends.get(1);
        CgmesTerminal t2 = cgmesModel.terminal(end2.getId(CgmesNames.TERMINAL));
        PropertyBag end3 = ends.get(2);
        CgmesTerminal t3 = cgmesModel.terminal(end3.getId(CgmesNames.TERMINAL));

        addTransformerAdjacency(Arrays.asList(CgmesDcConversion.getAcNode(cgmesModel, t1),
            CgmesDcConversion.getAcNode(cgmesModel, t2), CgmesDcConversion.getAcNode(cgmesModel, t3)));
    }

    private void addTransformerAdjacency(List<String> nodes) {
        if (nodes.stream().anyMatch(this::containsAcDcConverter)) {
            for (int k = 0; k < nodes.size() - 1; k++) {
                String node = nodes.get(k);
                for (int l = k + 1; l < nodes.size(); l++) {
                    addAdjacency(node, nodes.get(l), AdjacentType.AC_TRANSFORMER);
                }
            }
        }
    }

    private void addAdjacency(String nodeId1, String nodeId2, AdjacentType type) {
        Adjacent ad1 = new Adjacent(type, nodeId1);
        Adjacent ad2 = new Adjacent(type, nodeId2);
        adjacency.computeIfAbsent(nodeId1, k -> new ArrayList<>()).add(ad2);
        adjacency.computeIfAbsent(nodeId2, k -> new ArrayList<>()).add(ad1);
    }

    boolean containsAcDcConverter(String nodeId) {
        if (adjacency.containsKey(nodeId)) {
            return adjacency.get(nodeId).stream().anyMatch(ad -> isAcDcConverter(ad.type));
        }
        return false;
    }

    static boolean isDcLineSegment(AdjacentType type) {
        return type == AdjacentType.DC_LINE_SEGMENT;
    }

    static boolean isAcDcConverter(AdjacentType type) {
        return type == AdjacentType.AC_DC_CONVERTER;
    }

    Map<String, List<Adjacent>> get() {
        return adjacency;
    }

    boolean isEmpty() {
        return adjacency.isEmpty();
    }

    boolean areAdjacentsByAcDcConverter(String node1, String node2) {
        if (adjacency.containsKey(node1)) {
            return adjacency.get(node1).stream()
                .anyMatch(ad -> ad.type == AdjacentType.AC_DC_CONVERTER && ad.node.equals(node2));
        }
        return false;
    }

    static class Adjacent {
        AdjacentType type;
        String node;

        Adjacent(AdjacentType type, String node) {
            Objects.requireNonNull(type);
            Objects.requireNonNull(node);
            this.type = type;
            this.node = node;
        }
    }
}
