/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.HVDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesDc {
    private final CgmesModel cgmesModel;

    public CgmesDc(CgmesModel cgmes) {
        this.cgmesModel = cgmes;

        computeDcIslandsNodes();

        Map<String, AcDcConverterNodes> acDcConverterNodes = computeAcDcConverterNodes();
        printAcDcConverterNodes(acDcConverterNodes);
    }

    private void computeDcIslandsNodes() {
        Map<String, List<String>> adjacencies = computeDcAdjacencies();

        Set<List<String>> dcIslandsNodes = new HashSet<>();
        Set<String> visitedDcTopologicalNodes = new HashSet<>();
        adjacencies.keySet().forEach(dcTopologicalNodeId -> {
            if (visitedDcTopologicalNodes.contains(dcTopologicalNodeId)) {
                return;
            }
            System.err.printf("JAM nodo adyacente %s %n", dcTopologicalNodeId);
            visitedDcTopologicalNodes.add(dcTopologicalNodeId);
            List<String> adjacentDcTopologicalNodes = calculateAdjacentDcTopologicalNodes(dcTopologicalNodeId,
                adjacencies, visitedDcTopologicalNodes);
            dcIslandsNodes.add(adjacentDcTopologicalNodes);
        });

        printDcIslandsNodes(dcIslandsNodes);
    }

    private Map<String, List<String>> computeDcAdjacencies() {
        Map<String, List<String>> dcAdjacencies = new HashMap<>();
        cgmesModel.dcLineSegments().forEach(dcls -> considerAdjacency(dcls, dcAdjacencies));
        return dcAdjacencies;
    }

    // cgmesModel.acDcConverters().forEach(c -> printConverter(c));

    private List<String> calculateAdjacentDcTopologicalNodes(String dcTopologicalNodeId,
        Map<String, List<String>> adjacencies, Set<String> visitedDcTopologicalNodes) {

        List<String> adjacentDcTopologicalNodes = new ArrayList<>();
        adjacentDcTopologicalNodes.add(dcTopologicalNodeId);
        if (adjacencies.containsKey(dcTopologicalNodeId)) {
            int k = 0;
            while (k < adjacentDcTopologicalNodes.size()) {
                String dcTopologicalNode = adjacentDcTopologicalNodes.get(k);
                adjacencies.get(dcTopologicalNode).forEach(adjacentDcTopologicalNode -> {
                    if (visitedDcTopologicalNodes.contains(adjacentDcTopologicalNode)) {
                        return;
                    }
                    adjacentDcTopologicalNodes.add(adjacentDcTopologicalNode);
                    visitedDcTopologicalNodes.add(adjacentDcTopologicalNode);
                });
                k++;
            }
        }
        return adjacentDcTopologicalNodes;
    }

    private void printConverter(PropertyBag acDcConverter) {
        String id = acDcConverter.getId("ACDCConverter");
        String terminalId = acDcConverter.getId("Terminal");
        String name = acDcConverter.get("name");
        System.err.printf("AcDcConverter Id %s name %s Tid %s %n", id, name, terminalId);
    }

    private void printDcIslandsNodes(Set<List<String>> dcIslandsNodes) {
        dcIslandsNodes.forEach(i -> System.err.printf("Island %s %n", i.toString()));
    }

    private void considerAdjacency(PropertyBag equipment, Map<String, List<String>> dcAdjacencies) {
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 1));
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(equipment.getId(CgmesNames.DC_TERMINAL + 2));
        if (!t1.connected() || !t2.connected()) {
            return;
        }
        addDcAdjacency(t1.dcTopologicalNode(), t2.dcTopologicalNode(), dcAdjacencies);
    }

    private void addDcAdjacency(String nodeId1, String nodeId2, Map<String, List<String>> dcAdjacencies) {
        dcAdjacencies.computeIfAbsent(nodeId1, k -> new ArrayList<>()).add(nodeId2);
        dcAdjacencies.computeIfAbsent(nodeId2, k -> new ArrayList<>()).add(nodeId1);
        System.err.printf("Ad %s %s %n", nodeId1, nodeId2);
    }

    private Map<String, AcDcConverterNodes> computeAcDcConverterNodes() {
        Map<String, AcDcConverterNodes> acDcConverterNodes = new HashMap<>();

        cgmesModel.acDcConverters().forEach(c -> addAcDcConverter(c, acDcConverterNodes));
        cgmesModel.dcTerminals().forEach(t -> addDcTerminalToAcDcConverter(t, acDcConverterNodes));
        return acDcConverterNodes;
    }

    private void addAcDcConverter(PropertyBag c, Map<String, AcDcConverterNodes> acDcConverterNodes) {
        String id = c.getId("ACDCConverter");
        String terminalId = c.getId("Terminal");
        String acTopologicalNode = terminalId;

        acDcConverterNodes.computeIfAbsent(id, k -> new AcDcConverterNodes(id, acTopologicalNode));
    }

    private void addDcTerminalToAcDcConverter(PropertyBag t, Map<String, AcDcConverterNodes> acDcConverterNodes) {
        CgmesDcTerminal dcTerminal = new CgmesDcTerminal(t);
        if (dcTerminal.dcConductingEquipmentType().equals("CsConverter") ||
            dcTerminal.dcConductingEquipmentType().equals("VsConverter")) {

            AcDcConverterNodes acDcConverter = acDcConverterNodes.get(dcTerminal.dcConductingEquipment());
            if (acDcConverter != null) {
                acDcConverter.addDcTopologicalNode(dcTerminal.dcTopologicalNode());
            }
        }
    }

    private void printAcDcConverterNodes(Map<String, AcDcConverterNodes> acDcConverterNode) {
        System.err.printf("================ MAP ============= %n");
        acDcConverterNode.entrySet().forEach(entry -> {
            System.err.println(entry.getKey() + ", " + entry.getValue().id + ", "
                + entry.getValue().acTopologicalNode + ", "
                + entry.getValue().dcTopologicalNode.toString());
        });
    }

    static class AcDcConverterNodes {
        String id;
        String acTopologicalNode;
        List<String> dcTopologicalNode;

        AcDcConverterNodes(String id, String acTopologicalNode) {
            this.id = id;
            this.acTopologicalNode = acTopologicalNode;
            this.dcTopologicalNode = new ArrayList<>();
        }

        void addDcTopologicalNode(String dcTopologicalNode) {
            this.dcTopologicalNode.add(dcTopologicalNode);
        }
    }
}
