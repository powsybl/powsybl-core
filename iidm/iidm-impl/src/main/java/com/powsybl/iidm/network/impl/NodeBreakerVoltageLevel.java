/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView.SwitchAdder;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import com.powsybl.math.graph.GraphUtil;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraph;
import com.powsybl.math.graph.UndirectedGraphImpl;
import gnu.trove.list.array.TIntArrayList;
import org.anarres.graphviz.builder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NodeBreakerVoltageLevel extends AbstractVoltageLevel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerVoltageLevel.class);

    private static final boolean DRAW_SWITCH_ID = true;

    private static final BusChecker CALCULATED_BUS_CHECKER = new CalculatedBusChecker();

    private static final BusChecker CALCULATED_BUS_BREAKER_CHECKER = new CalculatedBusBreakerChecker();

    private static final BusNamingStrategy NAMING_STRATEGY = new LowestNodeNumberBusNamingStrategy();

    private final UndirectedGraphImpl<NodeTerminal, SwitchImpl> graph = new UndirectedGraphImpl<>();

    private final Map<String, Integer> switches = new HashMap<>();

    private class VariantImpl implements Variant {

        final CalculatedBusTopology calculatedBusTopology
                = new CalculatedBusTopology();

        final CalculatedBusBreakerTopology calculatedBusBreakerTopology
                = new CalculatedBusBreakerTopology();

        @Override
        public VariantImpl copy() {
            return new VariantImpl();
        }

    }

    private final VariantArray<VariantImpl> variants;

    private final class SwitchAdderImpl extends AbstractIdentifiableAdder<SwitchAdderImpl> implements NodeBreakerView.SwitchAdder {

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
            SwitchImpl aSwitch = new SwitchImpl(NodeBreakerVoltageLevel.this, id, getName(), isFictitious(), kind, open, retained);
            getNetwork().getIndex().checkAndAdd(aSwitch);
            graph.addVertexIfNotPresent(node1);
            graph.addVertexIfNotPresent(node2);
            int e = graph.addEdge(node1, node2, aSwitch);
            switches.put(id, e);
            invalidateCache();
            getNetwork().getListeners().notifyCreation(aSwitch);
            return aSwitch;
        }

    }

    private final class InternalConnectionAdderImpl implements NodeBreakerView.InternalConnectionAdder {

        private Integer node1;

        private Integer node2;

        private InternalConnectionAdderImpl() {
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
                throw new ValidationException(NodeBreakerVoltageLevel.this, "first connection node is not set");
            }
            if (node2 == null) {
                throw new ValidationException(NodeBreakerVoltageLevel.this, "second connection node is not set");
            }
            graph.addVertexIfNotPresent(node1);
            graph.addVertexIfNotPresent(node2);
            graph.addEdge(node1, node2, null);
            invalidateCache();
        }

    }

    /**
     * Cached data for buses
     */
    private static final class BusCache {

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
            updateCache(Switch::isOpen);
        }

        protected BusChecker getBusChecker() {
            return CALCULATED_BUS_CHECKER;
        }

        private void traverse(int n, boolean[] encountered, Predicate<SwitchImpl> terminate, Map<String, CalculatedBus> id2bus, CalculatedBus[] node2bus) {
            if (!encountered[n]) {
                final TIntArrayList nodes = new TIntArrayList(1);
                nodes.add(n);
                graph.traverse(n, (n1, e, n2) -> {
                    SwitchImpl aSwitch = graph.getEdgeObject(e);
                    if (aSwitch != null && terminate.apply(aSwitch)) {
                        return TraverseResult.TERMINATE;
                    }

                    nodes.add(n2);
                    return TraverseResult.CONTINUE;
                }, encountered);

                // check that the component is a bus
                String busId = NAMING_STRATEGY.getId(NodeBreakerVoltageLevel.this, nodes);
                CopyOnWriteArrayList<NodeTerminal> terminals = new CopyOnWriteArrayList<>();
                for (int i = 0; i < nodes.size(); i++) {
                    int n2 = nodes.getQuick(i);
                    NodeTerminal terminal2 = graph.getVertexObject(n2);
                    if (terminal2 != null) {
                        terminals.add(terminal2);
                    }
                }
                if (getBusChecker().isValid(graph, nodes, terminals)) {
                    String busName = NAMING_STRATEGY.getName(NodeBreakerVoltageLevel.this, nodes);
                    CalculatedBusImpl bus = new CalculatedBusImpl(busId, busName, NodeBreakerVoltageLevel.this.fictitious, NodeBreakerVoltageLevel.this, nodes, terminals);
                    id2bus.put(busId, bus);
                    for (int i = 0; i < nodes.size(); i++) {
                        node2bus[nodes.getQuick(i)] = bus;
                    }
                }
            }
        }

        protected void updateCache(final Predicate<SwitchImpl> terminate) {
            if (busCache != null) {
                return;
            }
            LOGGER.trace("Update bus topology of voltage level {}", NodeBreakerVoltageLevel.this.id);
            Map<String, CalculatedBus> id2bus = new LinkedHashMap<>();
            CalculatedBus[] node2bus = new CalculatedBus[graph.getVertexCapacity()];
            boolean[] encountered = new boolean[graph.getVertexCapacity()];
            Arrays.fill(encountered, false);
            for (int e : graph.getEdges()) {
                traverse(graph.getEdgeVertex1(e), encountered, terminate, id2bus, node2bus);
                traverse(graph.getEdgeVertex2(e), encountered, terminate, id2bus, node2bus);
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
                throw new PowsyblException("Bus " + id + " not found");
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
                if (connectableBus2[0] != null) {
                    // traverse does not stop the algorithm when TERMINATE, it only stops searching in a given direction
                    // this condition insures that while checking all the edges (in every direction) of a node, if a bus is found, it will not be lost
                    return TraverseResult.TERMINATE;
                }
                connectableBus2[0] = getBus(v2);
                if (connectableBus2[0] != null) {
                    return TraverseResult.TERMINATE;
                }
                return TraverseResult.CONTINUE;
            });
            // if nothing found, just take the first bus
            if (connectableBus2[0] == null) {
                Collection<CalculatedBus> buses = getBuses();
                if (buses.isEmpty()) { // if the whole voltage level is disconnected, return null
                    return null;
                }
                return buses.iterator().next();
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
            updateCache(sw -> sw.isOpen() || sw.isRetained());
        }

        @Override
        protected BusChecker getBusChecker() {
            return CALCULATED_BUS_BREAKER_CHECKER;
        }

        Bus getBus1(String switchId, boolean throwException) {
            int edge = getEdge(switchId, throwException);
            SwitchImpl aSwitch = graph.getEdgeObject(edge);
            if (!aSwitch.isRetained()) {
                if (throwException) {
                    throw createSwitchNotFoundException(switchId);
                }
                return null;
            }
            int node1 = graph.getEdgeVertex1(edge);
            return getBus(node1);
        }

        Bus getBus2(String switchId, boolean throwException) {
            int edge = getEdge(switchId, throwException);
            SwitchImpl aSwitch = graph.getEdgeObject(edge);
            if (!aSwitch.isRetained()) {
                if (throwException) {
                    throw createSwitchNotFoundException(switchId);
                }
                return null;
            }
            int node2 = graph.getEdgeVertex2(edge);
            return getBus(node2);
        }

        Iterable<SwitchImpl> getSwitches() {
            return Iterables.filter(graph.getEdgesObject(), sw -> sw != null && sw.isRetained());
        }

        Stream<Switch> getSwitchStream() {
            return graph.getEdgeObjectStream().filter(Objects::nonNull).filter(Switch::isRetained).map(Function.identity());
        }

        int getSwitchCount() {
            return (int) graph.getEdgeObjectStream().filter(Objects::nonNull).filter(SwitchImpl::isRetained).count();
        }

        SwitchImpl getSwitch(String switchId, boolean throwException) {
            Integer edge = getEdge(switchId, false);
            if (edge != null) {
                SwitchImpl aSwitch = graph.getEdgeObject(edge);
                if (aSwitch.isRetained()) {
                    return aSwitch;
                }
            }
            if (throwException) {
                throw createSwitchNotFoundException(switchId);
            }
            return null;
        }
    }

    private interface BusChecker {

        boolean isValid(UndirectedGraph<? extends TerminalExt, SwitchImpl> graph, TIntArrayList nodes, List<NodeTerminal> terminals);
    }

    private static class CalculatedBusChecker implements BusChecker {

        @Override
        public boolean isValid(UndirectedGraph<? extends TerminalExt, SwitchImpl> graph, TIntArrayList nodes, List<NodeTerminal> terminals) {
            int feederCount = 0;
            int branchCount = 0;
            int busbarSectionCount = 0;
            for (int i = 0; i < nodes.size(); i++) {
                int node = nodes.get(i);
                TerminalExt terminal = graph.getVertexObject(node);
                if (terminal != null) {
                    AbstractConnectable connectable = terminal.getConnectable();
                    switch (connectable.getType()) {
                        case LINE:
                        case TWO_WINDINGS_TRANSFORMER:
                        case THREE_WINDINGS_TRANSFORMER:
                        case HVDC_CONVERTER_STATION:
                            branchCount++;
                            feederCount++;
                            break;

                        case LOAD:
                        case GENERATOR:
                        case BATTERY:
                        case SHUNT_COMPENSATOR:
                        case DANGLING_LINE:
                        case STATIC_VAR_COMPENSATOR:
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

    private static class CalculatedBusBreakerChecker implements BusChecker {
        @Override
        public boolean isValid(UndirectedGraph<? extends TerminalExt, SwitchImpl> graph, TIntArrayList nodes, List<NodeTerminal> terminals) {
            return !nodes.isEmpty();
        }
    }

    private interface BusNamingStrategy {

        String getId(NodeBreakerVoltageLevel voltageLevel, TIntArrayList nodes);

        String getName(NodeBreakerVoltageLevel voltageLevel, TIntArrayList nodes);
    }

    private static class LowestNodeNumberBusNamingStrategy implements BusNamingStrategy {

        @Override
        public String getId(NodeBreakerVoltageLevel voltageLevel, TIntArrayList nodes) {
            return voltageLevel.getId() + "_" + nodes.min();
        }

        @Override
        public String getName(NodeBreakerVoltageLevel voltageLevel, TIntArrayList nodes) {
            return voltageLevel.name != null ? voltageLevel.name + "_" + nodes.min() : null;
        }
    }

    NodeBreakerVoltageLevel(String id, String name, boolean fictitious, SubstationImpl substation,
                            double nominalV, double lowVoltageLimit, double highVoltageLimit) {
        super(id, name, fictitious, substation, nominalV, lowVoltageLimit, highVoltageLimit);
        variants = new VariantArray<>(substation.getNetwork().getRef(), VariantImpl::new);
    }

    @Override
    public void invalidateCache() {
        variants.get().calculatedBusBreakerTopology.invalidateCache();
        variants.get().calculatedBusTopology.invalidateCache();
        getNetwork().getConnectedComponentsManager().invalidate();
    }

    private Integer getEdge(String switchId, boolean throwException) {
        Integer edge = switches.get(switchId);
        if (throwException && edge == null) {
            throw createSwitchNotFoundException(switchId);
        }
        return edge;
    }

    @Override
    public Iterable<Terminal> getTerminals() {
        return FluentIterable.from(graph.getVerticesObj())
                .filter(Predicates.notNull())
                .transform(Functions.identity());
    }

    @Override
    public Stream<Terminal> getTerminalStream() {
        return graph.getVertexObjectStream().filter(Objects::nonNull).map(Function.identity());
    }

    static PowsyblException createNotSupportedNodeBreakerTopologyException() {
        return new PowsyblException("Not supported in a node/breaker topology");
    }

    private static PowsyblException createSwitchNotFoundException(String switchId) {
        return new PowsyblException("Switch " + switchId + " not found");
    }

    CalculatedBusBreakerTopology getCalculatedBusBreakerTopology() {
        return variants.get().calculatedBusBreakerTopology;
    }

    CalculatedBusTopology getCalculatedBusTopology() {
        return variants.get().calculatedBusTopology;
    }

    private final NodeBreakerViewExt nodeBreakerView = new NodeBreakerViewExt() {
        /**
         * @deprecated Use {@link #getMaximumNodeIndex()} instead.
         */
        @Override
        @Deprecated
        public int getNodeCount() {
            return graph.getVertexCount();
        }

        @Override
        public int getMaximumNodeIndex() {
            return graph.getVertexCapacity() - 1;
        }

        @Override
        public int[] getNodes() {
            return graph.getVertices();
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
        public Terminal getTerminal(int node) {
            return graph.getVertexObject(node);
        }

        @Override
        public Optional<Terminal> getOptionalTerminal(int node) {
            if (graph.vertexExists(node)) {
                return Optional.ofNullable(graph.getVertexObject(node));
            }
            return Optional.empty();
        }

        @Override
        public boolean hasAttachedEquipment(int node) {
            return graph.vertexExists(node);
        }

        @Override
        public Terminal getTerminal1(String switchId) {
            return getTerminal(getNode1(switchId));
        }

        @Override
        public Terminal getTerminal2(String switchId) {
            return getTerminal(getNode2(switchId));
        }

        @Override
        public SwitchAdder newSwitch() {
            return new SwitchAdderImpl();
        }

        @Override
        public InternalConnectionAdder newInternalConnection() {
            return new InternalConnectionAdderImpl();
        }

        @Override
        public int getInternalConnectionCount() {
            return (int) getInternalConnectionStream().count();
        }

        @Override
        public Iterable<InternalConnection> getInternalConnections() {
            return getInternalConnectionStream().collect(Collectors.toList());
        }

        @Override
        public Stream<InternalConnection> getInternalConnectionStream() {
            return Arrays.stream(graph.getEdges())
                    .filter(e -> graph.getEdgeObject(e) == null)
                    .mapToObj(e -> new InternalConnection() {
                        @Override
                        public int getNode1() {
                            return graph.getEdgeVertex1(e);
                        }

                        @Override
                        public int getNode2() {
                            return graph.getEdgeVertex2(e);
                        }
                    });
        }

        @Override
        public void removeInternalConnections(int node1, int node2) {
            int[] internalConnectionsToBeRemoved = Arrays.stream(graph.getEdges())
                    .filter(e -> graph.getEdgeObject(e) == null)
                    .filter(e -> (graph.getEdgeVertex1(e) == node1 && graph.getEdgeVertex2(e) == node2) ||
                            (graph.getEdgeVertex1(e) == node2 && graph.getEdgeVertex2(e) == node1))
                    .toArray();
            if (internalConnectionsToBeRemoved.length == 0) {
                throw new PowsyblException("Internal connection not found between " + node1 + " and " + node2);
            }
            for (int ic : internalConnectionsToBeRemoved) {
                graph.removeEdge(ic);
            }
            clean();
            invalidateCache();
        }

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
            return Iterables.filter(graph.getEdgesObject(), Switch.class); // just to upcast and return an unmodifiable iterable
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return graph.getEdgeObjectStream().map(Function.identity());
        }

        @Override
        public int getSwitchCount() {
            return graph.getEdgeCount();
        }

        @Override
        public void removeSwitch(String switchId) {
            Integer e = switches.remove(switchId);
            if (e == null) {
                throw new PowsyblException("Switch '" + switchId
                        + "' not found in substation voltage level '" + id + "'");
            }
            SwitchImpl aSwitch = graph.removeEdge(e);
            clean();

            getNetwork().getIndex().remove(aSwitch);
            getNetwork().getListeners().notifyRemoval(aSwitch);
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
        public Stream<BusbarSection> getBusbarSectionStream() {
            return getConnectableStream(BusbarSection.class);
        }

        @Override
        public int getBusbarSectionCount() {
            return getConnectableCount(BusbarSection.class);
        }

        @Override
        public BusbarSection getBusbarSection(String id) {
            return getNetwork().getIndex().get(id, BusbarSection.class);
        }

        private com.powsybl.math.graph.Traverser adapt(Traverser t) {
            return (v1, e, v2) -> t.traverse(v1, graph.getEdgeObject(e), v2) ? TraverseResult.CONTINUE : TraverseResult.TERMINATE;
        }

        @Override
        public void traverse(int node, Traverser t) {
            graph.traverse(node, adapt(t));
        }
    };

    @Override
    public NodeBreakerViewExt getNodeBreakerView() {
        return nodeBreakerView;
    }

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Collections.unmodifiableCollection(variants.get().calculatedBusTopology.getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return variants.get().calculatedBusTopology.getBuses().stream().map(Function.identity());
        }

        @Override
        public CalculatedBus getBus(String id) {
            return variants.get().calculatedBusTopology.getBus(id, false);
        }

        @Override
        public Bus getMergedBus(String busBarId) {
            NodeTerminal nt = (NodeTerminal) nodeBreakerView.getBusbarSection(busBarId).getTerminal();
            int node = nt.getNode();
            return variants.get().calculatedBusTopology.getBus(node);
        }
    };

    @Override
    public BusViewExt getBusView() {
        return busView;
    }

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Collections.unmodifiableCollection(variants.get().calculatedBusBreakerTopology.getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return variants.get().calculatedBusBreakerTopology.getBuses().stream().map(Function.identity());
        }

        @Override
        public CalculatedBus getBus(String id) {
            return variants.get().calculatedBusBreakerTopology.getBus(id, false);
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
            return Iterables.filter(variants.get().calculatedBusBreakerTopology.getSwitches(), Switch.class); // just to upcast and return an unmodifiable iterable
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return variants.get().calculatedBusBreakerTopology.getSwitchStream();
        }

        @Override
        public int getSwitchCount() {
            return variants.get().calculatedBusBreakerTopology.getSwitchCount();
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
            return variants.get().calculatedBusBreakerTopology.getBus1(switchId, true);
        }

        @Override
        public Bus getBus2(String switchId) {
            return variants.get().calculatedBusBreakerTopology.getBus2(switchId, true);
        }

        @Override
        public Switch getSwitch(String switchId) {
            return variants.get().calculatedBusBreakerTopology.getSwitch(switchId, true);
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
    public Iterable<Switch> getSwitches() {
        return getNodeBreakerView().getSwitches();
    }

    @Override
    public int getSwitchCount() {
        return getNodeBreakerView().getSwitchCount();
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
        graph.addVertexIfNotPresent(node);
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

        assert node >= 0 && node < graph.getVertexCapacity();
        assert graph.getVertexObject(node) == terminal;

        graph.setVertexObject(node, null);

        // remove the link terminal -> voltage level
        terminal.setVoltageLevel(null);
        clean();
    }

    private void clean() {
        GraphUtil.removeIsolatedVertices(graph);
    }

    private static boolean isBusbarSection(Terminal t) {
        return t != null && t.getConnectable().getType() == ConnectableType.BUSBAR_SECTION;
    }

    private static boolean isOpenedDisconnector(Switch s) {
        return s != null && s.getKind() == SwitchKind.DISCONNECTOR && s.isOpen();
    }

    @Override
    public boolean connect(TerminalExt terminal) {
        assert terminal instanceof NodeTerminal;
        int node = ((NodeTerminal) terminal).getNode();
        // find all paths starting from the current terminal to a busbar section that does not contain an open disconnector
        // paths are already sorted
        List<TIntArrayList> paths = graph.findAllPaths(node, NodeBreakerVoltageLevel::isBusbarSection, NodeBreakerVoltageLevel::isOpenedDisconnector);
        boolean connected = false;
        if (!paths.isEmpty()) {
            // the shorted path is the best, close all opened breakers of the path
            TIntArrayList shortestPath = paths.get(0);
            for (int i = 0; i < shortestPath.size(); i++) {
                int e = shortestPath.get(i);
                SwitchImpl sw = graph.getEdgeObject(e);
                if (sw != null && sw.getKind() == SwitchKind.BREAKER && sw.isOpen()) {
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
        }

        for (TIntArrayList path : paths) {
            boolean pathOpen = false;
            for (int i = 0; i < path.size(); i++) {
                int e = path.get(i);
                SwitchImpl sw = graph.getEdgeObject(e);
                if (sw != null && sw.getKind() == SwitchKind.BREAKER) {
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

    boolean isConnected(TerminalExt terminal) {
        assert terminal instanceof NodeTerminal;

        return terminal.getBusView().getBus() != null;
    }

    void traverse(NodeTerminal terminal, VoltageLevel.TopologyTraverser traverser) {
        traverse(terminal, traverser, new HashSet<>());
    }

    void traverse(NodeTerminal terminal, VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(traverser);
        Objects.requireNonNull(traversedTerminals);

        if (traversedTerminals.contains(terminal)) {
            return;
        }

        if (traverser.traverse(terminal, true)) {
            traversedTerminals.add(terminal);

            int node = terminal.getNode();
            List<TerminalExt> nextTerminals = new ArrayList<>();

            addNextTerminals(terminal, nextTerminals);

            graph.traverse(node, (v1, e, v2) -> {
                SwitchImpl aSwitch = graph.getEdgeObject(e);
                NodeTerminal otherTerminal = graph.getVertexObject(v2);
                if (aSwitch == null // internal connection case
                        || traverser.traverse(aSwitch)) {
                    if (otherTerminal == null) {
                        return TraverseResult.CONTINUE;
                    } else if (traverser.traverse(otherTerminal, true)) {
                        traversedTerminals.add(otherTerminal);

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
                nextTerminal.traverse(traverser, traversedTerminals);
            }
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        variants.push(number, VariantImpl::new);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        variants.pop(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        variants.delete(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        variants.allocate(indexes, VariantImpl::new);
    }

    @Override
    protected void removeTopology() {
        removeAllEdges();
    }

    private void removeAllEdges() {
        for (SwitchImpl s : graph.getEdgesObject()) {
            if (s != null) {
                getNetwork().getIndex().remove(s);
                getNetwork().getListeners().notifyRemoval(s);
            }
        }
        graph.removeAllEdges();
        switches.clear();
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
    public void exportTopology(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            exportTopology(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportTopology(Writer writer) {
        exportTopology(writer, new SecureRandom());
    }

    @Override
    public void exportTopology(Writer writer, Random random) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(random);

        GraphVizScope scope = new GraphVizScope.Impl();
        GraphVizGraph gvGraph = new GraphVizGraph();

        exportNodes(random, gvGraph, scope);
        exportEdges(gvGraph, scope);

        try {
            gvGraph.writeTo(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void exportNodes(Random random, GraphVizGraph gvGraph, GraphVizScope scope) {
        // create bus color scale
        Map<String, String> busColor = new HashMap<>();
        List<CalculatedBus> buses = new ArrayList<>(getCalculatedBusBreakerTopology().getBuses());
        String[] colors = Colors.generateColorScale(buses.size(), random);
        for (int i = 0; i < buses.size(); i++) {
            CalculatedBus bus = buses.get(i);
            busColor.put(bus.getId(), colors[i]);
        }

        for (int n = 0; n < graph.getVertexCount(); n++) {
            Bus bus = getCalculatedBusBreakerTopology().getBus(n);
            String label = "" + n;
            TerminalExt terminal = graph.getVertexObject(n);
            if (terminal != null) {
                AbstractConnectable connectable = terminal.getConnectable();
                label += System.lineSeparator() + connectable.getType().toString() + System.lineSeparator() + connectable.getId();
            }
            GraphVizNode gvNode = gvGraph.node(scope, n)
                    .label(label)
                    .shape("ellipse");
            if (bus != null) {
                gvNode.style("filled")
                        .attr(GraphVizAttribute.fillcolor, busColor.get(bus.getId()));
                gvGraph.cluster(scope, bus).add(gvNode)
                        .attr(GraphVizAttribute.pencolor, "transparent");
            }
        }
    }

    private void exportEdges(GraphVizGraph gvGraph, GraphVizScope scope) {
        for (int e = 0; e < graph.getEdgeCount(); e++) {
            GraphVizEdge edge = gvGraph.edge(scope, graph.getEdgeVertex1(e), graph.getEdgeVertex2(e));
            SwitchImpl aSwitch = graph.getEdgeObject(e);
            if (aSwitch != null) {
                if (DRAW_SWITCH_ID) {
                    edge.label(aSwitch.getKind().toString() + System.lineSeparator() + aSwitch.getId())
                            .attr(GraphVizAttribute.fontsize, "10");
                }
                edge.style(aSwitch.isOpen() ? "dotted" : "solid");
            }
        }
    }
}
