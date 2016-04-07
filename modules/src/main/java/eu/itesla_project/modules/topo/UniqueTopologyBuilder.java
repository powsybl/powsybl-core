/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Networks;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UniqueTopologyBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueTopologyBuilder.class);

    private static class StackedConnectivity {

        private final Multimap<PossibleTopology.Equipment, PossibleTopology.Equipment> connections = HashMultimap.create();

        private StackedConnectivity(TopologyHistory topoHisto) {
            for (final TopologyChoice topoChoice : topoHisto.getTopologyChoices()) {
                for (PossibleTopology possibleTopo : topoChoice.getPossibleTopologies()) {
                    for (PossibleTopology.Substation substation : possibleTopo.getMetaSubstation().getSubstations()) {
                        for (PossibleTopology.Bus bus : substation.getBuses()) {
                            for (PossibleTopology.Equipment eq1 : bus.getEquipments()) {
                                for (PossibleTopology.Equipment eq2 : bus.getEquipments()) {
                                    if (eq1 != eq2) {
                                        connections.put(eq1, eq2);
                                        connections.put(eq2, eq1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private boolean areSometimesConnected(PossibleTopology.Bus bus1, PossibleTopology.Bus bus2) {
            for (PossibleTopology.Equipment eq1 : bus1.getEquipments()) {
                for (PossibleTopology.Equipment eq2 : bus2.getEquipments()) {
                    if (connections.containsEntry(eq1, eq2)) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    private final TopologyHistory topoHisto;

    private final ShortIdDictionary dict;

    public UniqueTopologyBuilder(TopologyHistory topoHisto, ShortIdDictionary dict) {
        this.topoHisto = Objects.requireNonNull(topoHisto);
        this.dict = dict;
    }

    public UniqueTopologyBuilder(TopologyHistory topoHisto) {
        this(topoHisto, null);
    }

    private void createBuses(Map<String, UniqueTopology> uniqueTopos) {
        class GraphContext {
            DirectedGraph<PossibleTopology.Equipment, Object> graph = new SimpleDirectedGraph<>(Object.class);
            Map<Object, AtomicInteger> edgeCount = new HashMap<>();
        }

        for (final TopologyChoice topoChoice : topoHisto.getTopologyChoices()) {
            Map<String, GraphContext> graphs = new HashMap<>();
            for (PossibleTopology possibleTopo : topoChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopo.getMetaSubstation().getSubstations()) {
                    if (!substation.isFictive()) {
                        GraphContext graphContext = graphs.get(substation.getId());
                        if (graphContext == null) {
                            graphContext = new GraphContext();
                            graphs.put(substation.getId(), graphContext);
                        }
                        for (PossibleTopology.Bus bus : substation.getBuses()) {
                            for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                                if (!graphContext.graph.containsVertex(eq)) {
                                    graphContext.graph.addVertex(eq);
                                }
                            }
                        }
                        for (PossibleTopology.Bus bus : substation.getBuses()) {
                            for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                                for (PossibleTopology.Equipment eq2 : bus.getEquipments()) {
                                    if (eq != eq2) {
                                        Object e = graphContext.graph.getEdge(eq, eq2);
                                        if (e == null) {
                                            e = new Object();
                                            graphContext.edgeCount.put(e, new AtomicInteger());
                                            graphContext.graph.addEdge(eq, eq2, e);
                                        }
                                        graphContext.edgeCount.get(e).incrementAndGet();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, GraphContext> entry : graphs.entrySet()) {
                String substationId = entry.getKey();
                final GraphContext graphContext = entry.getValue();
                for (Object e : new ArrayList<>(graphContext.graph.edgeSet())) {
                    if (graphContext.edgeCount.get(e).get() != topoChoice.getPossibleTopologies().size()) {
                        graphContext.graph.removeEdge(e);
                        graphContext.edgeCount.remove(e);
                    }
                }
                List<Set<PossibleTopology.Equipment>> connectedSets = new ConnectivityInspector<>(graphContext.graph).connectedSets();
                UniqueTopology uniqueTopo = new UniqueTopology(substationId);
                for (Set<PossibleTopology.Equipment> connectedSet : connectedSets) {
                    uniqueTopo.getBuses().add(new PossibleTopology.Bus(new ArrayList<>(connectedSet)));
                }
                uniqueTopos.put(substationId, uniqueTopo);
            }
        }
    }

    private void createSwitches(Map<String, UniqueTopology> uniqueTopos) {

        StackedConnectivity stackedConnectivity = new StackedConnectivity(topoHisto);

        for (Map.Entry<String, UniqueTopology> entry : uniqueTopos.entrySet()) {

            String substationId = entry.getKey();
            UniqueTopology uniqueTopo = entry.getValue();

            UndirectedGraph<PossibleTopology.Bus, Object> graph = new SimpleGraph<>(Object.class);
            for (PossibleTopology.Bus bus : uniqueTopo.getBuses()) {
                graph.addVertex(bus);
            }
            // on connecte tout le monde
            for (PossibleTopology.Bus bus1 : uniqueTopo.getBuses()) {
                for (PossibleTopology.Bus bus2 : uniqueTopo.getBuses()) {
                    if (bus1 != bus2 && !graph.containsEdge(bus1, bus2)) {
                        graph.addEdge(bus1, bus2);
                    }
                }
            }

            for (Object e : new ArrayList<>(graph.edgeSet())) {
                PossibleTopology.Bus bus1 = graph.getEdgeSource(e);
                PossibleTopology.Bus bus2 = graph.getEdgeTarget(e);
                // does this edge connect 2 buses directly connected in one
                // of the topology?
                boolean sometimesConnected = stackedConnectivity.areSometimesConnected(bus1, bus2);

                // does this edge close a cycle?
                boolean closeCycle = false;
                List<List<PossibleTopology.Bus>> cycles = new PatonCycleBase<>(graph).findCycleBase();
                for (List<PossibleTopology.Bus> cycle : cycles) {
                    if (cycle.contains(bus1) && cycle.contains(bus2)) {
                        closeCycle = true;
                        break;
                    }
                }

                if (!sometimesConnected && closeCycle) {
                    graph.removeEdge(e);
                }
            }

            int i = 0;
            for (Object e : graph.edgeSet()) {
                PossibleTopology.Bus bus1 = graph.getEdgeSource(e);
                PossibleTopology.Bus bus2 = graph.getEdgeTarget(e);
                uniqueTopo.getSwitches().add(new UniqueTopology.Switch(substationId + "_" + i++, bus1, bus2));
            }
        }
    }

    public Map<String, UniqueTopology> build() {

        Map<String, UniqueTopology> uniqueTopos = new HashMap<>();
        createBuses(uniqueTopos);
        createSwitches(uniqueTopos);

        // link history possible topologies and the corresponding unique topology
        for (final TopologyChoice topoChoice : topoHisto.getTopologyChoices()) {
            for (PossibleTopology possibleTopo : topoChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopo.getMetaSubstation().getSubstations()) {
                    if (!substation.isFictive()) {
                        for (UniqueTopology.Switch s : uniqueTopos.get(substation.getId()).getSwitches()) {
                            PossibleTopology.Bus bus1 = s.getBus1();
                            PossibleTopology.Bus bus2 = s.getBus2();
                            PossibleTopology.Equipment eq1 = bus1.getEquipments().iterator().next();
                            PossibleTopology.Equipment eq2 = bus2.getEquipments().iterator().next();
                            boolean open = true;
                            for (PossibleTopology.Bus bus : substation.getBuses()) {
                                if (bus.getEquipments().contains(eq1) && bus.getEquipments().contains(eq2)) {
                                    open = false;
                                }
                            }
                            substation.getSwitches().put(s.getId(), open);
                        }
                    }
                }
            }
        }

        NumberingContext context = new NumberingContext();
        for (UniqueTopology uniqueTopo : uniqueTopos.values()) {
            uniqueTopo.number(context);
        }

        return uniqueTopos;
    }

    /**
     * Remove equipments in reference network that are not in the unique topo
     */
    public static void cleanNetwork(Network network, Map<String, UniqueTopology> uniqueTopos) {
        // substations
        List<String> substationsRemoved = new ArrayList<>();
        for (VoltageLevel vl : Lists.newArrayList(network.getVoltageLevels())) {
            if (!uniqueTopos.containsKey(vl.getId())) {
                substationsRemoved.add(vl.getId());
                throw new RuntimeException("TODO voltage level removal");
            }
        }
        if (substationsRemoved.size() > 0) {
            LOGGER.debug("{} substations removed from reference network because not in the unique topo", substationsRemoved.size());
            LOGGER.trace("Detailed list of removed substations: {}", substationsRemoved);
        }
        List<String> equipmentsRemoved = new ArrayList<>();
        List<String> fictiveEquipmentsRemoved = new ArrayList<>();
        // branches
        List<TwoTerminalsConnectable> branches = new ArrayList<>();
        for (Line l : network.getLines()) {
            branches.add(l);
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            branches.add(twt);
        }
        for (TwoTerminalsConnectable branch : branches) {
            UniqueTopology topo1 = uniqueTopos.get(branch.getTerminal1().getVoltageLevel().getId());
            UniqueTopology topo2 = uniqueTopos.get(branch.getTerminal2().getVoltageLevel().getId());
            if (!topo1.containsEquipment(branch.getId()) && !topo2.containsEquipment(branch.getId())) {
                if (branch.getId().contains(TopologyHistory.FICTIVE_PATTERN)) {
                    fictiveEquipmentsRemoved.add(branch.getId());
                } else {
                    equipmentsRemoved.add(branch.getId());
                }
                branch.remove();
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            throw new RuntimeException("TODO support 3 windings tfo");
        }

        // injections
        List<SingleTerminalConnectable> injections = new ArrayList<>();
        for (Generator g : network.getGenerators()) {
            injections.add(g);
        }
        for (Load l : network.getLoads()) {
            injections.add(l);
        }
        for (ShuntCompensator sc : network.getShunts()) {
            injections.add(sc);
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            injections.add(dl);
        }
        for (SingleTerminalConnectable injection : injections) {
            UniqueTopology topo = uniqueTopos.get(injection.getTerminal().getVoltageLevel().getId());
            if (!topo.containsEquipment(injection.getId())) {
                if (injection.getId().contains(TopologyHistory.FICTIVE_PATTERN)) {
                    fictiveEquipmentsRemoved.add(injection.getId());
                } else {
                    equipmentsRemoved.add(injection.getId());
                }
                injection.remove();
            }
        }
        if (equipmentsRemoved.size() > 0) {
            LOGGER.debug("{} equipments removed from reference network because not in the unique topo", equipmentsRemoved.size());
            LOGGER.trace("Detailed list of equipments removed from reference network: {}", equipmentsRemoved);
        }
        if (fictiveEquipmentsRemoved.size() > 0) {
            LOGGER.debug("{} fictive equipments removed from reference network because not in the unique topo", fictiveEquipmentsRemoved.size());
        }
    }

    public void build(Network network) {

        // build a unique topology for each of the substation reflecting history
        Map<String, UniqueTopology> uniqueTopos = build();

        // remove from the reference network equipments not in the history, i.e always disconnected in the history
        cleanNetwork(network, uniqueTopos);

        // apply unique topology to the network
        for (VoltageLevel vl : network.getVoltageLevels()) {
            UniqueTopology uniqueTopo = uniqueTopos.get(vl.getId());
//                uniqueTopo.print(System.out);
            uniqueTopo.apply(network);
        }

        Networks.printBalanceSummary("unique topo", network, LOGGER);

        new UniqueTopologyChecker(network, topoHisto, uniqueTopos, dict).check();
    }

}
