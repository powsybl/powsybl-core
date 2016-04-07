/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopologyHistoryConnectedComponentsAnalyser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyHistoryConnectedComponentsAnalyser.class);

    static class Vertex {
        final String equipmentId;
        final String substationId;
        final String topoId;
        Vertex(String equipmentId, String substationId, String topoId) {
            this.equipmentId = equipmentId;
            this.substationId = substationId;
            this.topoId = topoId;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Vertex) {
                return ((Vertex) o).equipmentId.equals(equipmentId)
                        && ((Vertex) o).substationId.equals(substationId)
                        && ((Vertex) o).topoId.equals(topoId);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(equipmentId, substationId, topoId);
        }
    }

    private final TopologyHistory topologyHistory;

    public TopologyHistoryConnectedComponentsAnalyser(TopologyHistory topologyHistory) {
        this.topologyHistory = Objects.requireNonNull(topologyHistory);
    }

    private UndirectedGraph<Vertex, Object> createGraph() {
        UndirectedGraph<Vertex, Object> graph = new Pseudograph<>(Object.class);

        for (TopologyChoice topologyChoice : topologyHistory.getTopologyChoices()) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        for (PossibleTopology.Equipment equipment : bus.getEquipments()) {
                            graph.addVertex(new Vertex(equipment.getId(), substation.getId(), possibleTopology.getTopoHash()));
                        }
                    }
                }
            }
        }

        class TopoBranchSide {

            private final String topoSetId;
            private final String substationId;
            private final String topoId;

            TopoBranchSide(String topoSetId, String substationId, String topoId) {
                this.topoSetId = topoSetId;
                this.substationId = substationId;
                this.topoId = topoId;
            }

            @Override
            public String toString() {
                return "(" + topoSetId + ", " + substationId + ", " + topoId + ")";
            }
        }

        // list branches and their 2 connection buses
        Map<String, Set<TopoBranchSide>> branches = new HashMap<>();
        for (TopologyChoice topologyChoice : topologyHistory.getTopologyChoices()) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        // connect equipments of the bus all together
                        if (bus.getEquipments().size() > 0) {
                            Iterator<PossibleTopology.Equipment> it = bus.getEquipments().iterator();
                            PossibleTopology.Equipment first = it.next();
                            while (it.hasNext()) {
                                PossibleTopology.Equipment next = it.next();
                                graph.addEdge(new Vertex(first.getId(), substation.getId(), possibleTopology.getTopoHash()),
                                        new Vertex(next.getId(), substation.getId(), possibleTopology.getTopoHash()));
                            }
                        }

                        for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                            if (eq.isBranch(false)) {
                                if (!branches.containsKey(eq.getId())) {
                                    branches.put(eq.getId(), new HashSet<>());
                                }
                                branches.get(eq.getId()).add(new TopoBranchSide(topologyChoice.getClusterId(), substation.getId(), possibleTopology.getTopoHash()));
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Set<TopoBranchSide>> entry : branches.entrySet()) {
            String branchId = entry.getKey();
            Set<TopoBranchSide> sides = entry.getValue();

            Set<String> substationIds = sides.stream().map(s -> s.substationId).collect(Collectors.toSet());
            if (substationIds.size() != 1 && substationIds.size() != 2) {
                throw new RuntimeException("History does not contain the 1 or 2 sides of " + branchId + ": (" + substationIds + ")");
            }
            Iterator<String> it = substationIds.iterator();
            String substationId1 = it.next();
            String substationId2;
            if (substationIds.size() == 1) {
                substationId2 = substationId1;
            } else {
                substationId2 = it.next();
            }

            // 2 cases
            // - branch is always connected to substation of a same topo set => create an edge for all the topo
            // - branch is always connected to substation of a different topo set => create an edge for all the combination of topo
            Set<String> topoSetIds = sides.stream().map(s -> s.topoSetId).collect(Collectors.toSet());
            Set<String> topoIds = sides.stream().map(s -> s.topoId).collect(Collectors.toSet());
            if (topoSetIds.size() == 1) {
                for (String topoId : topoIds) {
                    Vertex v1 = new Vertex(branchId, substationId1, topoId);
                    Vertex v2 = new Vertex(branchId, substationId2, topoId);
                    if (graph.containsVertex(v1) && graph.containsVertex(v2)) {
                        graph.addEdge(v1, v2);
                    }
                }
            } else if (topoSetIds.size() == 2) {
                for (String topoId1 : topoIds) {
                    for (String topoId2 : topoIds) {
                        if (topoId1 != topoId2) {
                            Vertex v1 = new Vertex(branchId, substationId1, topoId1);
                            Vertex v2 = new Vertex(branchId, substationId2, topoId2);
                            if (graph.containsVertex(v1) && graph.containsVertex(v2)) {
                                graph.addEdge(v1, v2);
                            }
                        }
                    }
                }
            } else {
                throw new RuntimeException("Inconsistent history: a branch between more that 2 topo sets");
            }
        }

        return graph;
    }

    public Set<String> analyse() {
        UndirectedGraph<Vertex, Object> graph = createGraph();

        // sort by ascending size
        return new ConnectivityInspector<>(graph).connectedSets().stream()
                .sorted(new Comparator<Set<Vertex>>() {
                    @Override
                    public int compare(Set<Vertex> o1, Set<Vertex> o2) {
                        return o2.size() - o1.size();
                    }
                })
                .map(s -> s.stream().map(v -> v.equipmentId).collect(Collectors.<String>toSet()))
                .collect(Collectors.toList())
                .get(0);
    }
}
