/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class Adjacency {

    enum AdjacentType {
        DC_LINE_SEGMENT, AC_DC_CONVERTER,
    }

    Map<String, List<Adjacent>> adjacency;

    Adjacency(CgmesModel cgmesModel) {
        adjacency = new HashMap<>();
        cgmesModel.dcLineSegments().forEach(dcls -> computeDcLineSegmentAdjacency(cgmesModel, dcls));

        AcDcConverterNodes acDcConverterNodes = new AcDcConverterNodes(cgmesModel);
        acDcConverterNodes.converterNodes.entrySet()
            .forEach(entry -> computeAcDcConverterAdjacency(entry.getValue().acTopologicalNode,
                entry.getValue().dcTopologicalNode));
    }

    private void computeDcLineSegmentAdjacency(CgmesModel cgmesModel, PropertyBag equipment) {
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 1));
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 2));
        if (!t1.connected() || !t2.connected()) {
            return;
        }
        addAdjacency(t1.dcTopologicalNode(), t2.dcTopologicalNode(), AdjacentType.DC_LINE_SEGMENT);
    }

    private void computeAcDcConverterAdjacency(String acTopologicalNode, List<String> dcTopologicalNodes) {
        dcTopologicalNodes.forEach(
            dcTopologicalNode -> addAdjacency(acTopologicalNode, dcTopologicalNode, AdjacentType.AC_DC_CONVERTER));
        for (int k = 0; k < dcTopologicalNodes.size() - 1; k++) {
            String dcTopologicalNode = dcTopologicalNodes.get(k);
            for (int l = k + 1; l < dcTopologicalNodes.size(); l++) {
                addAdjacency(dcTopologicalNode, dcTopologicalNodes.get(l), AdjacentType.AC_DC_CONVERTER);
            }
        }
    }

    private void addAdjacency(String topologicalNodeId1, String topologicalNodeId2, AdjacentType type) {
        Adjacent ad1 = new Adjacent(type, topologicalNodeId1);
        Adjacent ad2 = new Adjacent(type, topologicalNodeId2);
        adjacency.computeIfAbsent(topologicalNodeId1, k -> new ArrayList<>()).add(ad2);
        adjacency.computeIfAbsent(topologicalNodeId2, k -> new ArrayList<>()).add(ad1);
    }

    static boolean isDcLineSegment(AdjacentType type) {
        return type == AdjacentType.DC_LINE_SEGMENT;
    }

    static boolean isAcDcConverter(AdjacentType type) {
        return type == AdjacentType.AC_DC_CONVERTER;
    }

    void print() {
        LOG.info("Adjacency");
        adjacency.entrySet().forEach(k -> print(k.getKey(), k.getValue()));
    }

    private void print(String topologicalNodeId, List<Adjacent> adjacent) {
        LOG.info("TopologicalNodeId {}", topologicalNodeId);
        adjacent.forEach(ad -> ad.print());
    }

    static class Adjacent {
        AdjacentType type;
        String topologicalNode;

        Adjacent(AdjacentType type, String topologicalNode) {
            this.type = type;
            this.topologicalNode = topologicalNode;
        }

        void print() {
            LOG.info("    {}  {}", this.type, this.topologicalNode);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Adjacency.class);
}
