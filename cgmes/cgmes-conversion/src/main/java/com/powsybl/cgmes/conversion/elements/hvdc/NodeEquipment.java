/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.*;
import java.util.stream.Collectors;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class NodeEquipment {

    enum EquipmentType {
        TRANSFORMER, AC_DC_CONVERTER, DC_LINE_SEGMENT
    }

    private final Map<String, List<EquipmentReference>> nodeEquipment;

    NodeEquipment(CgmesModel cgmesModel, Context context, AcDcConverterNodes acDcConverterNodes, Adjacency adjacency) {
        nodeEquipment = new HashMap<>();

        cgmesModel.dcLineSegments().forEach(dcls -> computeDcLineSegment(cgmesModel, adjacency, dcls));

        acDcConverterNodes.getConverterNodes().values()
            .forEach(value -> addEquipment(adjacency, value.id, value.acNode,
                value.dcNode, EquipmentType.AC_DC_CONVERTER));

        cgmesModel.transformers().stream()
                .map(t -> context.transformerEnds(t.getId("PowerTransformer")))
                .forEach(ends -> {
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
        EquipmentReference eq = new EquipmentReference(type, id);
        nodeEquipment.computeIfAbsent(nodeId1, k -> new ArrayList<>()).add(eq);
        nodeEquipment.computeIfAbsent(nodeId2, k -> new ArrayList<>()).add(eq);
    }

    private void addEquipment(Adjacency adjacency, String id, String acNodeId,
        List<String> dcNodeIds, EquipmentType type) {
        if (!adjacency.get().containsKey(acNodeId)) {
            return;
        }
        if (dcNodeIds.stream().anyMatch(n -> !adjacency.get().containsKey(n))) {
            return;
        }
        EquipmentReference eq = new EquipmentReference(type, id);
        nodeEquipment.computeIfAbsent(acNodeId, k -> new ArrayList<>()).add(eq);
        dcNodeIds.forEach(n -> nodeEquipment.computeIfAbsent(n, k -> new ArrayList<>()).add(eq));
    }

    private boolean isValidTransformer(Adjacency adjacency, List<String> nodes) {
        return nodes.stream().anyMatch(adjacency::containsAcDcConverter);
    }

    private void addTransformer(Adjacency adjacency, String id, List<String> nodes, EquipmentType type) {
        EquipmentReference eq = new EquipmentReference(type, id);
        nodes.stream()
            .filter(n -> adjacency.get().containsKey(n))
            .forEach(n -> nodeEquipment.computeIfAbsent(n, k -> new ArrayList<>()).add(eq));
    }

    boolean containsAnyTransformer(String node) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .anyMatch(eq -> eq.type == EquipmentType.TRANSFORMER);
    }

    boolean containsAnyAcDcConverter(String node) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .anyMatch(eq -> eq.type == EquipmentType.AC_DC_CONVERTER);
    }

    boolean containsAnyNotUsedAcDcConverter(String node, Set<String> usedAcDcConverters) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
                .anyMatch(eq -> eq.type == EquipmentType.AC_DC_CONVERTER && !usedAcDcConverters.contains(eq.equipmentId));
    }

    boolean multiAcDcConverter(String node) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return false;
        }
        // Only one equipment with same Id
        return listEquipment.stream().filter(eq -> eq.type == EquipmentType.AC_DC_CONVERTER).map(eq -> eq.equipmentId)
            .collect(Collectors.toSet()).size() >= 2;
    }

    boolean containsAcDcConverter(String node, String acDcConverter) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return false;
        }
        return listEquipment.stream()
            .anyMatch(eq -> eq.equipmentId.equals(acDcConverter) && eq.type == EquipmentType.AC_DC_CONVERTER);
    }

    int acDcConvertersConnectedTo(String node) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return 0;
        }
        return listEquipment.stream().filter(eq -> eq.type == EquipmentType.AC_DC_CONVERTER).map(eq -> eq.equipmentId)
        .collect(Collectors.toSet()).size();
    }

    boolean existDcLineSegmentBetweenBothNodes(String node1, String node2) {
        List<EquipmentReference> listEquipment1 = nodeEquipment.get(node1);
        List<EquipmentReference> listEquipment2 = nodeEquipment.get(node2);
        if (listEquipment1 == null || listEquipment2 == null) {
            return false;
        }
        return listEquipment1.stream().anyMatch(eq -> eq.type == EquipmentType.DC_LINE_SEGMENT && listEquipment2.contains(eq));
    }

    boolean connectedEquipment(String equipment1, String equipment2, List<String> nodes) {
        return nodes.stream().anyMatch(n -> connectedEquipment(n, equipment1, equipment2));
    }

    private boolean connectedEquipment(String node, String equipment1, String equipment2) {
        List<EquipmentReference> listEquipment = nodeEquipment.get(node);
        if (listEquipment == null) {
            return false;
        }
        // Only one equipment with same Id
        return listEquipment.stream()
            .filter(eq -> eq.equipmentId.equals(equipment1) || eq.equipmentId.equals(equipment2))
            .collect(Collectors.toSet()).size() == 2;
    }

    Map<String, List<EquipmentReference>> getNodeEquipment() {
        return nodeEquipment;
    }

    static class EquipmentReference {
        EquipmentType type;
        String equipmentId;

        EquipmentReference(EquipmentType type, String equipmentId) {
            this.type = type;
            this.equipmentId = equipmentId;
        }
    }
}
