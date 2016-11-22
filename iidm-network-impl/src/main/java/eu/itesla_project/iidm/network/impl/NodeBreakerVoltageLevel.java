/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.commons.collect.Downcast;
import eu.itesla_project.graph.TraverseResult;
import eu.itesla_project.graph.Traverser;
import eu.itesla_project.graph.UndirectedGraph;
import eu.itesla_project.graph.UndirectedGraphImpl;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.VoltageLevel.NodeBreakerView.SwitchAdder;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;
import gnu.trove.list.array.TIntArrayList;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NodeBreakerVoltageLevel extends AbstractVoltageLevel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerVoltageLevel.class);

    private static final Downcast<NodeTerminal, Terminal> TERMINAL_DOWNCAST = new Downcast<>();

    private static final BusChecker BUS_CHECKER = new RteBusChecker();

    private static final BusNamingStrategy NAMING_STRATEGY = new NumberedBusNamingStrategy();

    private final UndirectedGraphImpl<NodeTerminal, SwitchImpl> graph = new UndirectedGraphImpl<>();

    private final Map<String, Integer> switches = new HashMap<>();

    private class StateImpl implements State {

        final CalculatedBusTopology calculatedBusTopology
                = new CalculatedBusTopology();

        final CalculatedBusBreakerTopology calculatedBusBreakerTopology
                = new CalculatedBusBreakerTopology();

        @Override
        public StateImpl copy() {
            return new StateImpl();
        }

    }

    private final StateArray<StateImpl> states;

    private class SwitchAdderImpl extends IdentifiableAdderImpl<SwitchAdderImpl> implements NodeBreakerView.SwitchAdder {

        private Integer node1;

        private Integer node2;

        private SwitchKind kind;

        private boolean open = false;

        private boolean retained = false;

        private SwitchAdderImpl() {
            this(null);
        }

        private SwitchAdderImpl(SwitchKind kind) {
            this.kind = kind;
        }

        @Override
        protected NetworkImpl getNetwork() {
            return NodeBreakerVoltageLevel.this.getNetwork();
        }

        @Override
        protected String getTypeDescription() {
            return "Switch";
        }

        @Override
        public NodeBreakerView.SwitchAdder setNode1(int node1) {
            this.node1 = node1;
            return this;
        }

        @Override
        public NodeBreakerView.SwitchAdder setNode2(int node2) {
            this.node2 = node2;
            return this;
        }

        @Override
        public NodeBreakerView.SwitchAdder setKind(SwitchKind kind) {
            if (kind == null) {
                throw new NullPointerException("kind is null");
            }
            this.kind = kind;
            return this;
        }

        @Override
        public SwitchAdder setKind(String kind) {
            return setKind(SwitchKind.valueOf(kind));
        }

        @Override
        public NodeBreakerView.SwitchAdder setOpen(boolean open) {
            this.open = open;
            return this;
        }

        @Override
        public NodeBreakerView.SwitchAdder setRetained(boolean retained) {
            this.retained = retained;
            return this;
        }

        @Override
        public Switch add() {
            String id = checkAndGetUniqueId();
            if (node1 == null) {
                throw new ValidationException(this, "first connection node is not set");
            }
            if (node2 == null) {
                throw new ValidationException(this, "second connection node is not set");
            }
            if (kind == null) {
                throw new ValidationException(this, "kind is not set");
            }
            SwitchImpl _switch = new SwitchImpl(NodeBreakerVoltageLevel.this, id, getName(), kind, open, retained);
            getNetwork().getObjectStore().checkAndAdd(_switch);
            int e = graph.addEdge(node1, node2, _switch);
            switches.put(id, e);
            invalidateCache();
            getNetwork().getListeners().notifyCreation(_switch);
            return _switch;
        }

    }

    private class InternalConnectionAdderImpl extends IdentifiableAdderImpl<InternalConnectionAdderImpl> implements NodeBreakerView.InternalConnectionAdder {

        private Integer node1;

        private Integer node2;

        private InternalConnectionAdderImpl() {}

        @Override
        protected NetworkImpl getNetwork() {
            return NodeBreakerVoltageLevel.this.getNetwork();
        }

        @Override
        protected String getTypeDescription() {
            return "InternalConnection";
        }

        @Override
        public NodeBreakerView.InternalConnectionAdder setNode1(int node1) {
            this.node1 = node1;
            return this;
        }

        @Override
        public NodeBreakerView.InternalConnectionAdder setNode2(int node2) {
            this.node2 = node2;
            return this;
        }


        @Override
        public void add() {
            if (node1 == null) {
                throw new ValidationException(this, "first connection node is not set");
            }
            if (node2 == null) {
                throw new ValidationException(this, "second connection node is not set");
            }

            int e = graph.addEdge(node1, node2, null);
            invalidateCache();
        }

    }

    /**
     * Cached data for buses
     */
    private static class BusCache {

        private final CalculatedBus[] node2bus;

        private final Map<String, CalculatedBus> id2bus;

        private BusCache(CalculatedBus[] node2bus, Map<String, CalculatedBus> id2bus) {
            this.node2bus = node2bus;
            this.id2bus = id2bus;
        }

        private Collection<CalculatedBus> getBuses() {
            return id2bus.values();
        }

        private CalculatedBus getBus(int node) {
            return node2bus[node];
        }

        private CalculatedBus getBus(String id) {
            return id2bus.get(id);
        }
    }

    /**
     * Bus topology calculated from node breaker topology
     */
    class CalculatedBusTopology {

        protected BusCache busCache;

        protected void updateCache() {
            updateCache(new Predicate<SwitchImpl>() {
                @Override
                public boolean apply(SwitchImpl _switch) {
                    return _switch.isOpen();
                }
            });
        }

        protected BusChecker getBusChecker() {
            return BUS_CHECKER;
        }

        protected void updateCache(final Predicate<SwitchImpl> terminate) {
            if (busCache != null) {
                return;
            }
            LOGGER.trace("Update bus topology of voltage level {}", NodeBreakerVoltageLevel.this.id);
            Map<String, CalculatedBus> id2bus = new LinkedHashMap<>();
            CalculatedBus[] node2bus = new CalculatedBus[graph.getMaxVertex()];
            boolean[] encountered = new boolean[graph.getMaxVertex()];
            Arrays.fill(encountered, false);
            for (int n : graph.getVertices()) {
                if (!encountered[n]) {
                    TerminalExt terminal = graph.getVertexObject(n);
                    final TIntArrayList nodes = new TIntArrayList(1);
                    nodes.add(n);
                    graph.traverse(n, new Traverser<SwitchImpl>() {
                        @Override
                        public TraverseResult traverse(int n1, int e, int n2) {
                            SwitchImpl _switch = graph.getEdgeObject(e);
                            if (_switch != null && terminate.apply(_switch)) {
                                return TraverseResult.TERMINATE;
                            } else {
                                nodes.add(n2);
                                return TraverseResult.CONTINUE;
                            }
                        }
                    }, encountered);

                    // check that the component is a bus
                    String busId = NAMING_STRATEGY.getName(NodeBreakerVoltageLevel.this, nodes);
                    CopyOnWriteArrayList<NodeTerminal> terminals = new CopyOnWriteArrayList<>();
                    for (int i = 0; i < nodes.size(); i++) {
                        int n2 = nodes.getQuick(i);
                        NodeTerminal terminal2 = graph.getVertexObject(n2);
                        if (terminal2 != null) {
                            terminals.add(terminal2);
                        }
                    }
                    if (getBusChecker().isValid(graph, nodes, terminals)) {
                        CalculatedBusImpl bus = new CalculatedBusImpl(busId, NodeBreakerVoltageLevel.this, terminals);
                        id2bus.put(busId, bus);
                        for (int i = 0; i < nodes.size(); i++) {
                            node2bus[nodes.getQuick(i)] = bus;
                        }
                    }

                }
            }
            busCache = new BusCache(node2bus, id2bus);
            LOGGER.trace("Found buses {}", id2bus.values());
        }

        protected void invalidateCache() {
            // detach buses
            if (busCache != null) {
                for (CalculatedBus bus : busCache.id2bus.values()) {
                    bus.invalidate();
                }
                busCache = null;
            }
        }

        Collection<CalculatedBus> getBuses() {
            updateCache();
            return busCache.getBuses();
        }

        CalculatedBus getBus(int node) {
            updateCache();
            return busCache.getBus(node);
        }

        CalculatedBus getBus(String id, boolean throwException) {
            updateCache();
            CalculatedBus bus = busCache.getBus(id);
            if (throwException && bus == null) {
                throw new ITeslaException("Bus " + id + " not found");
            }
            return bus;
        }

        BusExt getConnectableBus(int node) {
            // check id the node is associated to a bus
            BusExt connectableBus = getBus(node);
            if (connectableBus != null) {
                return connectableBus;
            }
            // if not traverse the graph starting from the node (without stopping at open switches) until finding another
            // node associated to a bus
            BusExt[] connectableBus2 = new BusExt[1];
            graph.traverse(node, (v1, e, v2) -> {
                connectableBus2[0] = getBus(v2);
                if (connectableBus2[0] != null) {
                    return TraverseResult.TERMINATE;
                }
                return TraverseResult.CONTINUE;
            });
            // if nothing found, just take the first bus
            if (connectableBus2[0] != null) {
                Iterator<CalculatedBus> it = getBuses().iterator();
                if (!it.hasNext()) {
                    throw new AssertionError("Should not happen");
                }
                return it.next();
            }
            return connectableBus2[0];
        }
    }

    /**
     * Bus breaker topology calculated from node breaker topology
     */
    class CalculatedBusBreakerTopology extends CalculatedBusTopology {

        @Override
        protected void updateCache() {
            updateCache(new Predicate<SwitchImpl>() {
                @Override
                public boolean apply(SwitchImpl _switch) {
                    return _switch.isOpen() || _switch.isRetained();
                }
            });
        }

        protected BusChecker getBusChecker() {
            return new BusChecker() {
                @Override
                public boolean isValid(UndirectedGraph<? extends TerminalExt, SwitchImpl> graph, TIntArrayList nodes, List<NodeTerminal> terminals) {
                    return nodes.size() > 1 || !terminals.isEmpty();
                }
            };
        }

        Bus getBus1(String switchId, boolean throwException) {
            int edge = getEdge(switchId, throwException);
            SwitchImpl _switch = graph.getEdgeObject(edge);
            if (!_switch.isRetained()) {
                if (throwException) {
                    throw new ITeslaException("Switch " + switchId + " not found");
                }
                return null;
            }
            int node1 = graph.getEdgeVertex1(edge);
            return getBus(node1);
        }

        Bus getBus2(String switchId, boolean throwException) {
            int edge = getEdge(switchId, throwException);
            SwitchImpl _switch = graph.getEdgeObject(edge);
            if (!_switch.isRetained()) {
                if (throwException) {
                    throw new ITeslaException("Switch " + switchId + " not found");
                }
                return null;
            }
            int node2 = graph.getEdgeVertex2(edge);
            return getBus(node2);
        }

        Iterable<SwitchImpl> getSwitches() {
            return Iterables.filter(graph.getEdgesObject(), new Predicate<SwitchImpl>() {
                @Override
                public boolean apply(SwitchImpl _switch) {
                    return _switch != null && _switch.isRetained();
                }
            });
        }

        SwitchImpl getSwitch(String switchId, boolean throwException) {
            Integer edge = getEdge(switchId, false);
            if (edge != null) {
                SwitchImpl _switch = graph.getEdgeObject(edge);
                if (_switch.isRetained()) {
                    return _switch;
                }
            }
            if (throwException) {
                throw new ITeslaException("Switch " + switchId + " not found");
            }
            return null;
        }
    }

    private static interface BusChecker {

        boolean isValid(UndirectedGraph<? extends TerminalExt, SwitchImpl> graph, TIntArrayList nodes, List<NodeTerminal> terminals);
    }

    /**
     * RTE bus definition
     */
    private static class RteBusChecker implements BusChecker {

        @Override
        public boolean isValid(UndirectedGraph<? extends TerminalExt, SwitchImpl> graph, TIntArrayList nodes, List<NodeTerminal> terminals) {
            int feederCount = 0;
            int branchCount = 0;
            int busbarSectionCount = 0;
            for (int i = 0; i < nodes.size(); i++) {
                int node = nodes.get(i);
                TerminalExt terminal = graph.getVertexObject(node);
                if (terminal != null) {
                    ConnectableImpl connectable = terminal.getConnectable();
                    switch (connectable.getType()) {
                        case LINE:
                        case TWO_WINDINGS_TRANSFORMER:
                        case THREE_WINDINGS_TRANSFORMER:
                            branchCount++;
                            feederCount++;
                            break;

                        case LOAD:
                        case GENERATOR:
                        case SHUNT_COMPENSATOR:
                        case DANGLING_LINE:
                        case STATIC_VAR_COMPENSATOR:
                        case HVDC_CONVERTER_STATION:
                            feederCount++;
                            break;

                        case BUSBAR_SECTION:
                            busbarSectionCount++;
                            break;

                        default:
                            throw new AssertionError();
                    }
                }
            }
            return (busbarSectionCount >= 1 && feederCount >= 1)
                    || (branchCount >= 1 && feederCount >= 2);
        }
    }

    private interface BusNamingStrategy {

        String getName(VoltageLevel voltageLevel, TIntArrayList nodes);
    }

    private static class RandomBusNamingStrategy implements BusNamingStrategy {

        @Override
        public String getName(VoltageLevel voltageLevel, TIntArrayList nodes) {
            return ObjectStore.getUniqueId();
        }

    }

    private static class NumberedBusNamingStrategy implements BusNamingStrategy {

        private final Map<VoltageLevel, AtomicInteger> counter = new WeakHashMap<>();

        private final Lock lock = new ReentrantLock();

        @Override
        public String getName(VoltageLevel voltageLevel, TIntArrayList nodes) {
            AtomicInteger i;
            lock.lock();
            try {
                i = counter.get(voltageLevel);
                if (i == null) {
                    i = new AtomicInteger();
                    counter.put(voltageLevel, i);
                }
            } finally {
                lock.unlock();
            }
            return voltageLevel.getId() + "_" + i.getAndIncrement();
        }

    }

    private static class SimpleBusNamingStrategy implements BusNamingStrategy {

        @Override
        public String getName(VoltageLevel voltageLevel, TIntArrayList nodes) {
            StringBuilder builder = new StringBuilder(voltageLevel.getId());
            for (int i = 0; i < nodes.size(); i++) {
                int node = nodes.get(i);
                builder.append("_").append(node);
            }
            return builder.toString();
        }

    }

    NodeBreakerVoltageLevel(String id, String name, SubstationImpl substation,
                           float nominalV, float lowVoltageLimit, float highVoltageLimit) {
        super(id, name, substation, nominalV, lowVoltageLimit, highVoltageLimit);
        states = new StateArray<>(substation.getNetwork().getRef(), new StateFactory<StateImpl>() {
            @Override
            public StateImpl newState() {
                return new StateImpl();
            }
        });
    }

    @Override
    public void invalidateCache() {
        states.get().calculatedBusBreakerTopology.invalidateCache();
        states.get().calculatedBusTopology.invalidateCache();
        getNetwork().getConnectedComponentsManager().invalidate();
    }

    private Integer getEdge(String switchId, boolean throwException) {
        Integer edge = switches.get(switchId);
        if (throwException && edge == null) {
            throw new ITeslaException("Switch " + switchId + " not found");
        }
        return edge;
    }

    @Override
    public Iterable<Terminal> getTerminals() {
        return FluentIterable.from(graph.getVerticesObj())
                .filter(Predicates.notNull())
                .transform(TERMINAL_DOWNCAST);
    }

    @Override
    public <C extends Connectable> FluentIterable<C> getConnectables(final Class<C> clazz) {
        return FluentIterable.from(getTerminals())
                .transform(Terminal::getConnectable)
                .filter(clazz);
    }

    @Override
    public <C extends Connectable> int getConnectableCount(final Class<C> clazz) {
        return getConnectables(clazz).size();
    }

    static ITeslaException createNotSupportedNodeBreakerTopologyException() {
        return new ITeslaException("Not supported in a node/breaker topology");
    }

    CalculatedBusBreakerTopology getCalculatedBusBreakerTopology() {
        return states.get().calculatedBusBreakerTopology;
    }

    CalculatedBusTopology getCalculatedBusTopology() {
        return states.get().calculatedBusTopology;
    }

    private final NodeBreakerViewExt nodeBreakerView = new NodeBreakerViewExt() {

        @Override
        public int getNodeCount() {
            return graph.getVertexCount();
        }

        @Override
        public NodeBreakerView setNodeCount(int count) {
            int oldCount = graph.getVertexCount();
            if (count > oldCount) {
                for (int i = oldCount; i < count; i++) {
                    graph.addVertex();
                }
            }
            return this;
        }

        @Override
        public int getNode1(String switchId) {
            int edge = getEdge(switchId, true);
            return graph.getEdgeVertex1(edge);
        }

        @Override
        public int getNode2(String switchId) {
            int edge = getEdge(switchId, true);
            return graph.getEdgeVertex2(edge);
        }

        @Override
        public SwitchAdder newSwitch() {
            return new SwitchAdderImpl();
        }

        @Override
        public InternalConnectionAdder newInternalConnection() {return new InternalConnectionAdderImpl(); }

        @Override
        public SwitchAdder newBreaker() {
            return new SwitchAdderImpl(SwitchKind.BREAKER);
        }

        @Override
        public SwitchAdder newDisconnector() {
            return new SwitchAdderImpl(SwitchKind.DISCONNECTOR);
        }

        @Override
        public SwitchImpl getSwitch(String switchId) {
            Integer edge = getEdge(switchId, false);
            if (edge != null) {
                return graph.getEdgeObject(edge);
            }
            return null;
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Iterables.<Switch>filter(graph.getEdgesObject(), Switch.class); // just to upcast and return an unmodifiable iterable
        }

        @Override
        public int getSwitchCount() {
            return graph.getEdgeCount();
        }

        @Override
        public BusbarSectionAdder newBusbarSection() {
            return new BusbarSectionAdderImpl(NodeBreakerVoltageLevel.this);
        }

        @Override
        public Iterable<BusbarSection> getBusbarSections() {
            return getConnectables(BusbarSection.class);
        }

        @Override
        public int getBusbarSectionCount() {
            return getConnectableCount(BusbarSection.class);
        }

        @Override
        public BusbarSection getBusbarSection(String id) {
            return getNetwork().getObjectStore().get(id, BusbarSection.class);
        }

    };

    @Override
    public NodeBreakerViewExt getNodeBreakerView() {
        return nodeBreakerView;
    }

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Collections.<Bus>unmodifiableCollection(states.get().calculatedBusTopology.getBuses());
        }

        @Override
        public CalculatedBus getBus(String id) {
            return states.get().calculatedBusTopology.getBus(id, false);
        }

    };

    @Override
    public BusViewExt getBusView() {
        return busView;
    }

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Collections.<Bus>unmodifiableCollection(states.get().calculatedBusBreakerTopology.getBuses());
        }

        @Override
        public CalculatedBus getBus(String id) {
            return states.get().calculatedBusBreakerTopology.getBus(id, false);
        }

        @Override
        public BusAdder newBus() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void removeBus(String busId) {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void removeAllBuses() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Iterables.<Switch>filter(states.get().calculatedBusBreakerTopology.getSwitches(), Switch.class); // just to upcast and return an unmodifiable iterable
        }

        @Override
        public void removeSwitch(String switchId) {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void removeAllSwitches() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public Bus getBus1(String switchId) {
            return states.get().calculatedBusBreakerTopology.getBus1(switchId, false);
        }

        @Override
        public Bus getBus2(String switchId) {
            return states.get().calculatedBusBreakerTopology.getBus2(switchId, false);
        }

        @Override
        public Switch getSwitch(String switchId) {
            return states.get().calculatedBusBreakerTopology.getSwitch(switchId, false);
        }

        @Override
        public BusBreakerView.SwitchAdder newSwitch() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

    };

    @Override
    public BusBreakerViewExt getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public TopologyKind getTopologyKind() {
        return TopologyKind.NODE_BREAKER;
    }

    private void checkTerminal(TerminalExt terminal) {
        if (!(terminal instanceof NodeTerminal)) {
            throw new ValidationException(terminal.getConnectable(),
                    "voltage level " + NodeBreakerVoltageLevel.this.id + " has a node/breaker topology"
                            + ", a node connection should be specified instead of a bus connection");
        }
    }

    @Override
    public void attach(TerminalExt terminal, boolean test) {
        checkTerminal(terminal);
        if (test) {
            return;
        }
        int node = ((NodeTerminal) terminal).getNode();
        if (graph.getVertexObject(node) != null) {
            throw new ValidationException(terminal.getConnectable(),
                    "an equipment (" + graph.getVertexObject(node).getConnectable().getId()
                            + ") is already connected to node " + node + " of voltage level "
                            + NodeBreakerVoltageLevel.this.id);
        }

        // create the link terminal <-> voltage level
        terminal.setVoltageLevel(NodeBreakerVoltageLevel.this);

        // create the link terminal <-> graph vertex
        graph.setVertexObject(node, (NodeTerminal) terminal);
    }

    @Override
    public void detach(TerminalExt terminal) {
        assert terminal instanceof NodeTerminal;

        int node = ((NodeTerminal) terminal).getNode();

        assert node >=0 && node < graph.getVertexCount();
        assert graph.getVertexObject(node) == terminal;

        // remove adjacents edges
        final TIntArrayList edgesToRemove = new TIntArrayList();
        graph.traverse(node, new Traverser<SwitchImpl>() {
            @Override
            public TraverseResult traverse(int v1, int e, int v2) {
                edgesToRemove.add(e);
                return graph.getVertexObject(v2) == null ? TraverseResult.CONTINUE : TraverseResult.TERMINATE;
            }
        });
        for (int i = 0; i < edgesToRemove.size(); i++) {
            int e = edgesToRemove.getQuick(i);
            SwitchImpl _switch = graph.getEdgeObject(e);
            switches.remove(_switch.getId());
            graph.removeEdge(e);
        }
        graph.setVertexObject(node, null);

        // remove the link terminal -> voltage level
        terminal.setVoltageLevel(null);
    }

    @Override
    public void clean() {
        // TODO remove unused connection nodes
    }

    private static boolean isBusbarSection(Terminal t) {
        return t != null && t.getConnectable().getType() == ConnectableType.BUSBAR_SECTION;
    }

    private static boolean isOpenedDisconnector(Switch s) {
        return s.getKind() == SwitchKind.DISCONNECTOR && s.isOpen();
    }

    @Override
    public boolean connect(TerminalExt terminal) {
        assert terminal instanceof NodeTerminal;
        int node = ((NodeTerminal) terminal).getNode();
        // find all paths starting from the current terminal to a busbar section that does not contain an open disconnector
        // paths are already sorted
        List<TIntArrayList> paths = graph.findAllPaths(node, NodeBreakerVoltageLevel::isBusbarSection, NodeBreakerVoltageLevel::isOpenedDisconnector);
        boolean connected = false;
        if (paths.size() > 0) {
            // the shorted path is the best, close all opened breakers of the path
            TIntArrayList shortestPath = paths.get(0);
            for (int i = 0; i < shortestPath.size(); i++) {
                int e = shortestPath.get(i);
                SwitchImpl sw = graph.getEdgeObject(e);
                if (sw.getKind() == SwitchKind.BREAKER && sw.isOpen()) {
                    sw.setOpen(false);
                    connected = true;
                }
            }
        }
        return connected;
    }

    @Override
    public boolean disconnect(TerminalExt terminal) {
        assert terminal instanceof NodeTerminal;
        int node = ((NodeTerminal) terminal).getNode();
        // find all paths starting from the current terminal to a busbar section that does not contain an open disconnector
        // (because otherwise there is nothing we can do to connected the terminal using only breakers)
        List<TIntArrayList> paths = graph.findAllPaths(node, NodeBreakerVoltageLevel::isBusbarSection, NodeBreakerVoltageLevel::isOpenedDisconnector);
        if (paths.isEmpty()) {
            return false;
        } else {
            for (TIntArrayList path : paths) {
                boolean pathOpen = false;
                for (int i = 0; i < path.size(); i++) {
                    int e = path.get(i);
                    SwitchImpl sw = graph.getEdgeObject(e);
                    if (sw.getKind() == SwitchKind.BREAKER) {
                        if (!sw.isOpen()) {
                            sw.setOpen(true);
                        }
                        // just one open breaker is enough to disconnect the terminal, so we can stop
                        pathOpen = true;
                        break;
                    }
                }
                if (!pathOpen) {
                    return false;
                }
            }
            return true;
        }
    }

    boolean isConnected(TerminalExt terminal) {
        assert terminal instanceof NodeTerminal;
        int node = ((NodeTerminal) terminal).getNode();
        List<TIntArrayList> paths = graph.findAllPaths(node, NodeBreakerVoltageLevel::isBusbarSection, Switch::isOpen);
        return paths.size() > 0;
    }

    void traverse(NodeTerminal terminal, VoltageLevel.TopologyTraverser traverser) {
        traverse(terminal, traverser, new HashSet<>());
    }

    void traverse(NodeTerminal terminal, VoltageLevel.TopologyTraverser traverser, Set<String> traversedVoltageLevelsIds) {
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(traverser);
        Objects.requireNonNull(traversedVoltageLevelsIds);

        if (traversedVoltageLevelsIds.contains(terminal.getVoltageLevel().getId())) {
            return;
        }
        traversedVoltageLevelsIds.add(terminal.getVoltageLevel().getId());

        if (traverser.traverse(terminal, true)) {
            int node = terminal.getNode();
            List<TerminalExt> nextTerminals = new ArrayList<>();

            addNextTerminals(terminal, nextTerminals);

            graph.traverse(node, (v1, e, v2) -> {
                SwitchImpl aSwitch = graph.getEdgeObject(e);
                NodeTerminal otherTerminal = graph.getVertexObject(v2);
                if (traverser.traverse(aSwitch)) {
                    if (otherTerminal == null) {
                        return TraverseResult.CONTINUE;
                    } else if ((otherTerminal != null && traverser.traverse(otherTerminal, true))) {
                        addNextTerminals(otherTerminal, nextTerminals);
                        addNextTerminals(otherTerminal, nextTerminals);
                        return TraverseResult.CONTINUE;
                    } else {
                        return TraverseResult.TERMINATE;
                    }
                } else {
                    return TraverseResult.TERMINATE;
                }
            });

            for (TerminalExt nextTerminal : nextTerminals) {
                nextTerminal.traverse(traverser, traversedVoltageLevelsIds);
            }
        }
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        states.push(number, new StateFactory<StateImpl>() {
            @Override
            public StateImpl newState() {
                return new StateImpl();
            }
        });
    }

    @Override
    public void reduceStateArraySize(int number) {
        states.pop(number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        states.delete(index);
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, final int sourceIndex) {
        states.allocate(indexes, new StateFactory<StateImpl>() {
            @Override
            public StateImpl newState() {
                return new StateImpl();
            }
        });
    }

    @Override
    public void printTopology() {
        printTopology(System.out, null);
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        out.println("-------------------------------------------------------------");
        out.println("Topology of " + NodeBreakerVoltageLevel.this.id);
        graph.print(out, terminal -> terminal != null ? terminal.getConnectable().toString() : null, null);
    }

    @Override
    public void exportTopology(String filename) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(filename)) {
            exportTopology(outputStream);
        }
    }

    private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

    private static String[] generateColorScale(int n) {
        String[] colors = new String[n];
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            double h = random.nextDouble();
            h += GOLDEN_RATIO_CONJUGATE;
            h %= 1;
            long[] rgb = hsvToRgb(h, 0.5, 0.95);
            String hex = String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]).toUpperCase();
            colors[i] = hex;
        }
        return colors;
    }

    private static long[] hsvToRgb(double h, double s, double v) {
        int h_i = (int) Math.floor(h * 6);
        double f = h * 6 - h_i;
        double p = v * (1 - s);
        double q = v * (1 - f * s);
        double t = v * (1 - (1 - f) * s);
        double r, g, b;
        switch (h_i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
            default:
                throw new AssertionError();
        }
        return new long[] { Math.round(r * 256), Math.round(g * 256), Math.round(b * 256) };
    }

    public void exportTopology(OutputStream os) throws IOException {
        Graph g = new Graph().id("\"" + NodeBreakerVoltageLevel.this.id + "\"");
        Map<Integer, Node> intToNode = new HashMap<>();
        Multimap<String, Integer> busToNodes =  ArrayListMultimap.create();
        for (int n = 0; n < graph.getVertexCount(); n++) {
            Node node = new Node().id(Integer.toString(n));
            intToNode.put(n, node);
            Bus bus = getCalculatedBusBreakerTopology().getBus(n);
            if (bus != null) {
                busToNodes.put(bus.getId(), n);
            } else {
                TerminalExt terminal = graph.getVertexObject(n);
                if (terminal != null) {
                    ConnectableImpl connectable = terminal.getConnectable();
                    String label = n + "\\n" + connectable.getType().toString() + "\\n" + connectable.getId();
                    node.attr("label", label);
                    g.node(node);
                }
            }
        }
        String[] colors = generateColorScale(busToNodes.asMap().keySet().size());
        int i = 0;
        for (String key : busToNodes.asMap().keySet()) {
            Graph newBus = new Graph().id("\"" + key + "\"");
            newBus.attr("label", key);
            for (int nodeInt : busToNodes.get(key)) {
                Node node = intToNode.get(nodeInt);
                TerminalExt terminal = graph.getVertexObject(nodeInt);
                if (terminal != null) {
                    ConnectableImpl connectable = terminal.getConnectable();
                    String label = nodeInt + "\\n" + connectable.getType().toString() + "\\n" + connectable.getId();
                    node.attr("label", label);
                }
                node.attr("style", "filled").attr("color", colors[i]);
                newBus.node(node);
            }
            g.subGraph(newBus);
            i++;
        }

//        writer.append("graph \"").append(NodeBreakerVoltageLevel.this.id).append("\" {\n");
//        for (int n = 0; n < graph.getVertexCount(); n++) {
//            TerminalExt terminal = graph.getVertexObject(n);
//            if (terminal != null) {
//                ConnectableImpl connectable = terminal.getConnectable();
//                String label = n + "\\n" + connectable.getType().toString() + "\\n" + connectable.getId();
//                writer.append("  ").append(Integer.toString(n))
//                        .append(" [label=\"").append(label).append("\"]\n");
//            }
//        }
        boolean drawSwitchId = true;
        for (int e = 0; e < graph.getEdgeCount(); e++) {
            Edge edge = new Edge(intToNode.get(graph.getEdgeVertex1(e)), intToNode.get(graph.getEdgeVertex2(e))).id(Integer.toString(e));

            SwitchImpl _switch = graph.getEdgeObject(e);
            if (_switch != null) {
                if (drawSwitchId) {
                    edge.attr("label", _switch.getKind().toString() + "\n" + _switch.getId()).attr("fontsize", "10");
                }
                edge.attr("style", _switch.isOpen() ? "dotted" : "solid");
            }
            g.edge(edge);
        }
        g.writeTo(os);
//        for (int e = 0; e < graph.getEdgeCount(); e++) {
//            writer.append("  ").append(Integer.toString(graph.getEdgeVertex1(e)))
//                    .append(" -- ").append(Integer.toString(graph.getEdgeVertex2(e)));
//            SwitchImpl _switch = graph.getEdgeObject(e);
//            if (_switch != null) {
//                writer.append(" [");
//                if (drawSwitchId) {
//                    writer.append("label=\"").append(_switch.getId())
//                            .append("\", fontsize=10");
//                }
//                writer.append("style=\"").append(_switch.isOpen() ? "dotted" : "solid").append("\"");
//            }
//            writer.append("]\n");
//        }
//        writer.append("}\n");
    }

}
