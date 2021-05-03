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
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TPnodeEquipments {

    enum EquipmentType {
        TRANSFORMER, AC_DC_CONVERTER, DC_LINE_SEGMENT
    }

    private final Map<String, List<TPnodeEquipment>> nodeEquipments;

    TPnodeEquipments(CgmesModel cgmesModel, Adjacency adjacency) {
        nodeEquipments = new HashMap<>();

        cgmesModel.dcLineSegments().forEach(dcls -> computeDcLineSegment(cgmesModel, adjacency, dcls));

        AcDcConverterNodes acDcConverterNodes = new AcDcConverterNodes(cgmesModel);

        acDcConverterNodes.getConverterNodes().values()
            .forEach(value -> addEquipment(adjacency, value.id, value.acNode,
                value.dcNode, EquipmentType.AC_DC_CONVERTER));

        cgmesModel.groupedTransformerEnds().forEach((t, ends) -> {
            if (ends.size() == 2) {
                computeTwoWindingsTransformer(cgmesModel, adjacency, ends);
            } else if (ends.size() == 3) {
                computeThreeWindingsTransformer(cgmesModel, adjacency, ends);
            }
        });
    }

    private void computeDcLineSegment(CgmesModel cgmesModel, Adjacency adjacency, PropertyBag equipment) {
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 1));
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 2));
        String id = equipment.getId("DCLineSegment");
        addEquipment(adjacency, id, CgmesDcConversion.getDcNode(cgmesModel, t1),
            CgmesDcConversion.getDcNode(cgmesModel, t2), EquipmentType.DC_LINE_SEGMENT);
    }

    private void computeTwoWindingsTransformer(CgmesModel cgmesModel, Adjacency adjacency, PropertyBags ends) {
        PropertyBag end1 = ends.get(0);
        CgmesTerminal t1 = cgmesModel.terminal(end1.getId(CgmesNames.TERMINAL));
        PropertyBag end2 = ends.get(1);
        CgmesTerminal t2 = cgmesModel.terminal(end2.getId(CgmesNames.TERMINAL));

        String id = end1.getId(CgmesNames.POWER_TRANSFORMER);
        List<String> nodes = new ArrayList<>();
        nodes.add(CgmesDcConversion.getAcNode(cgmesModel, t1));
        nodes.add(CgmesDcConversion.getAcNode(cgmesModel, t2));
        if (isValidTransformer(adjacency, nodes)) {
            addTransformer(adjacency, id, nodes, EquipmentType.TRANSFORMER);
        }
    }

    private void computeThreeWindingsTransformer(CgmesModel cgmesModel, Adjacency adjacency, PropertyBags ends) {
        PropertyBag end1 = ends.get(0);
        CgmesTerminal t1 = cgmesModel.terminal(end1.getId(CgmesNames.TERMINAL));
        PropertyBag end2 = ends.get(1);
        CgmesTerminal t2 = cgmesModel.terminal(end2.getId(CgmesNames.TERMINAL));
        PropertyBag end3 = ends.get(2);
        CgmesTerminal t3 = cgmesModel.terminal(end3.getId(CgmesNames.TERMINAL));

        String id = end1.getId(CgmesNames.POWER_TRANSFORMER);
        List<String> nodes = new ArrayList<>();
        nodes.add(CgmesDcConversion.getAcNode(cgmesModel, t1));
        nodes.add(CgmesDcConversion.getAcNode(cgmesModel, t2));
        nodes.add(CgmesDcConversion.getAcNode(cgmesModel, t3));
        if (isValidTransformer(adjacency, nodes)) {
            addTransformer(adjacency, id, nodes, EquipmentType.TRANSFORMER);
        }
    }

    private void addEquipment(Adjacency adjacency, String id, String nodeId1, String nodeId2,
        EquipmentType type) {
        if (!adjacency.get().containsKey(nodeId1)
            || !adjacency.get().containsKey(nodeId2)) {
            return;
        }
        TPnodeEquipment eq = new TPnodeEquipment(type, id);
        nodeEquipments.computeIfAbsent(nodeId1, k -> new ArrayList<>()).add(eq);
        nodeEquipments.computeIfAbsent(nodeId2, k -> new ArrayList<>()).add(eq);
    }

    private void addEquipment(Adjacency adjacency, String id, String acNodeId,
        List<String> dcNodeIds, EquipmentType type) {
        if (!adjacency.get().containsKey(acNodeId)) {
            return;
        }
        if (dcNodeIds.stream().anyMatch(n -> !adjacency.get().containsKey(n))) {
            return;
        }
        TPnodeEquipment eq = new TPnodeEquipment(type, id);
        nodeEquipments.computeIfAbsent(acNodeId, k -> new ArrayList<>()).add(eq);
        dcNodeIds.forEach(n -> nodeEquipments.computeIfAbsent(n, k -> new ArrayList<>()).add(eq));
    }

    private boolean isValidTransformer(Adjacency adjacency, List<String> nodes) {
        return nodes.stream().anyMatch(adjacency::containsAcDcConverter);
    }

    private void addTransformer(Adjacency adjacency, String id, List<String> nodes, EquipmentType type) {
        TPnodeEquipment eq = new TPnodeEquipment(type, id);
        nodes.stream()
            .filter(n -> adjacency.get().containsKey(n))
            .forEach(n -> nodeEquipments.computeIfAbsent(n, k -> new ArrayList<>()).add(eq));
    }

    boolean containsAnyTransformer(String node) {
        List<TPnodeEquipment> listEquipment = nodeEquipments.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .anyMatch(eq -> eq.type == EquipmentType.TRANSFORMER);
    }

    boolean containsAnyAcDcConverter(String node) {
        List<TPnodeEquipment> listEquipment = nodeEquipments.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .anyMatch(eq -> eq.type == EquipmentType.AC_DC_CONVERTER);
    }

    boolean multiAcDcConverter(String node) {
        List<TPnodeEquipment> listEquipment = nodeEquipments.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .filter(eq -> eq.type == EquipmentType.AC_DC_CONVERTER)
            .count() >= 2;
    }

    boolean connectedEquipments(String equipment1, String equipment2, List<String> nodes) {
        return nodes.stream().anyMatch(n -> connectedEquipment(n, equipment1, equipment2));
    }

    private boolean connectedEquipment(String node, String equipment1, String equipment2) {
        List<TPnodeEquipment> listEquipment = nodeEquipments.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .filter(eq -> eq.equipmentId.equals(equipment1) || eq.equipmentId.equals(equipment2))
            .count() == 2;
    }

    Map<String, List<TPnodeEquipment>> getNodeEquipments() {
        return nodeEquipments;
    }

    void debug() {
        LOG.debug("TPnodeEquipments");
        nodeEquipments.forEach(this::debug);
    }

    private void debug(String tpNodeId, List<TPnodeEquipment> listTPnodeEquipment) {
        LOG.debug("NodeId: {}", tpNodeId);
        listTPnodeEquipment.forEach(TPnodeEquipment::debug);
    }

    void debugEq(List<String> lnodes) {
        lnodes.forEach(this::debugEq);
    }

    private void debugEq(String node) {
        LOG.debug("EQ. Node {}", node);
        if (nodeEquipments.containsKey(node)) {
            nodeEquipments.get(node)
                .forEach(eq -> LOG.debug("    {} {}", eq.type, eq.equipmentId));
        }
    }

    void debugDcLs(List<String> lnodes) {
        lnodes.forEach(this::debugDcLs);
    }

    private void debugDcLs(String node) {
        if (nodeEquipments.containsKey(node)) {
            nodeEquipments.get(node).stream()
                .filter(eq -> eq.type == TPnodeEquipments.EquipmentType.DC_LINE_SEGMENT)
                .forEach(eq -> LOG.debug("DcLineSegment {}", eq.equipmentId));
        }
    }

    static class TPnodeEquipment {
        EquipmentType type;
        String equipmentId;

        TPnodeEquipment(EquipmentType type, String equipmentId) {
            this.type = type;
            this.equipmentId = equipmentId;
        }

        void debug() {
            LOG.debug("    {} {}", this.type, this.equipmentId);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(TPnodeEquipments.class);
}
