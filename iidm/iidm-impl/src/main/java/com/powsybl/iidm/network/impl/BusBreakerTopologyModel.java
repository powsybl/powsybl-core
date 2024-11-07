/**
 * Copyright (c) 2016-2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraphImpl;
import com.powsybl.math.graph.UndirectedGraphListener;
import org.anarres.graphviz.builder.GraphVizAttribute;
import org.anarres.graphviz.builder.GraphVizEdge;
import org.anarres.graphviz.builder.GraphVizGraph;
import org.anarres.graphviz.builder.GraphVizScope;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BusBreakerTopologyModel extends AbstractTopologyModel {

    private static final boolean DRAW_SWITCH_ID = true;

    private final class SwitchAdderImpl extends AbstractIdentifiableAdder<SwitchAdderImpl> implements VoltageLevel.BusBreakerView.SwitchAdder {

        private String busId1;

        private String busId2;

        private boolean open = false;

        private SwitchAdderImpl() {
        }

        @Override
        protected NetworkImpl getNetwork() {
            return BusBreakerTopologyModel.this.getNetwork();
        }

        @Override
        protected String getTypeDescription() {
            return "Switch";
        }

        @Override
        public VoltageLevel.BusBreakerView.SwitchAdder setBus1(String bus1) {
            this.busId1 = bus1;
            return this;
        }

        @Override
        public VoltageLevel.BusBreakerView.SwitchAdder setBus2(String bus2) {
            this.busId2 = bus2;
            return this;
        }

        @Override
        public VoltageLevel.BusBreakerView.SwitchAdder setOpen(boolean open) {
            this.open = open;
            return this;
        }

        @Override
        public Switch add() {
            String id = checkAndGetUniqueId();
            if (busId1 == null) {
                throw new ValidationException(this, "first connection bus is not set");
            }
            if (busId2 == null) {
                throw new ValidationException(this, "second connection bus is not set");
            }
            if (busId1.equals(busId2)) {
                throw new ValidationException(this, "same bus at both ends");
            }

            SwitchImpl aSwitch = new SwitchImpl(voltageLevel, id, getName(), isFictitious(), SwitchKind.BREAKER, open, true);
            addSwitch(aSwitch, busId1, busId2);
            getNetwork().getListeners().notifyCreation(aSwitch);
            return aSwitch;
        }

    }

    private final UndirectedGraphImpl<ConfiguredBus, SwitchImpl> graph = new UndirectedGraphImpl<>(NODE_INDEX_LIMIT);

    /* buses indexed by vertex number */
    private final Map<String, Integer> buses = new HashMap<>();

    /* switches indexed by edge number */
    private final Map<String, Integer> switches = new HashMap<>();

    private Integer getVertex(String busId, boolean throwException) {
        Objects.requireNonNull(busId, "bus id is null");
        Integer v = buses.get(busId);
        if (throwException && v == null) {
            throw new PowsyblException("Bus " + busId
                    + " not found in voltage level "
                    + voltageLevel.getId());
        }
        return v;
    }

    ConfiguredBus getBus(String busId, boolean throwException) {
        Integer v = getVertex(busId, throwException);
        if (v != null) {
            ConfiguredBus bus = graph.getVertexObject(v);
            if (!bus.getId().equals(busId)) {
                throw new IllegalStateException("Invalid bus id (expected: " + busId + ", actual: " + bus.getId() + ")");
            }
            return bus;
        }
        return null;
    }

    private Integer getEdge(String switchId, boolean throwException) {
        Objects.requireNonNull(switchId, "switch id is null");
        Integer e = switches.get(switchId);
        if (throwException && e == null) {
            throw new PowsyblException("Switch " + switchId
                    + " not found in voltage level"
                    + voltageLevel.getId());
        }
        return e;
    }

    private SwitchImpl getSwitch(String switchId, boolean throwException) {
        Integer e = getEdge(switchId, throwException);
        if (e != null) {
            SwitchImpl aSwitch = graph.getEdgeObject(e);
            if (!aSwitch.getId().equals(switchId)) {
                throw new IllegalStateException("Invalid switch id (expected: " + switchId + ", actual: " + aSwitch.getId() + ")");
            }
            return aSwitch;
        }
        return null;
    }

    /**
     * Bus only topology cache
     */
    private static final class BusCache {

        /* merged bus by id */
        private final Map<String, MergedBus> mergedBus;

        /* bus to merged bus mapping */
        private final Map<ConfiguredBus, MergedBus> mapping;

        private BusCache(Map<String, MergedBus> mergedBus, Map<ConfiguredBus, MergedBus> mapping) {
            this.mergedBus = mergedBus;
            this.mapping = mapping;
        }

        private Collection<MergedBus> getMergedBuses() {
            return mergedBus.values();
        }

        private MergedBus getMergedBus(String id) {
            return mergedBus.get(id);
        }

        private MergedBus getMergedBus(ConfiguredBus cfgBus) {
            return mapping.get(cfgBus);
        }

    }

    /**
     * Bus only topology calculated from bus/breaker topology
     */
    class CalculatedBusTopology {

        protected boolean isBusValid(Set<ConfiguredBus> busSet) {
            int feederCount = 0;
            for (TerminalExt terminal : FluentIterable.from(busSet).transformAndConcat(ConfiguredBus::getConnectedTerminals)) {
                AbstractConnectable connectable = terminal.getConnectable();
                switch (connectable.getType()) {
                    case LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, HVDC_CONVERTER_STATION,
                         DANGLING_LINE, LOAD, GENERATOR, BATTERY, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR -> feederCount++;
                    case GROUND -> {
                        // Do nothing
                    }
                    default -> throw new IllegalStateException();
                }
            }
            return Networks.isBusValid(feederCount);
        }

        private MergedBus createMergedBus(int busNum, Set<ConfiguredBus> busSet) {
            String suffix = "_" + busNum;
            String mergedBusId = Identifiables.getUniqueId(voltageLevel.getId() + suffix, getNetwork().getIndex()::contains);
            String mergedBusName = voltageLevel.getOptionalName().map(name -> name + suffix).orElse(null);
            return new MergedBus(mergedBusId, mergedBusName, voltageLevel.isFictitious(), busSet);
        }

        private void updateCache() {
            if (variants.get().cache != null) {
                return;
            }

            Map<String, MergedBus> mergedBuses = new LinkedHashMap<>();

            // mapping between configured buses and merged buses
            Map<ConfiguredBus, MergedBus> mapping = new IdentityHashMap<>();

            boolean[] encountered = new boolean[graph.getVertexCapacity()];
            Arrays.fill(encountered, false);
            int busNum = 0;
            for (int v : graph.getVertices()) {
                if (!encountered[v]) {
                    final Set<ConfiguredBus> busSet = new LinkedHashSet<>(1);
                    busSet.add(graph.getVertexObject(v));
                    graph.traverse(v, TraversalType.DEPTH_FIRST, (v1, e, v2) -> {
                        SwitchImpl aSwitch = graph.getEdgeObject(e);
                        if (aSwitch.isOpen()) {
                            return TraverseResult.TERMINATE_PATH;
                        } else {
                            busSet.add(graph.getVertexObject(v2));
                            return TraverseResult.CONTINUE;
                        }
                    }, encountered);
                    if (isBusValid(busSet)) {
                        MergedBus mergedBus = createMergedBus(busNum++, busSet);
                        mergedBuses.put(mergedBus.getId(), mergedBus);
                        busSet.forEach(bus -> mapping.put(bus, mergedBus));
                    }
                }
            }

            variants.get().cache = new BusCache(mergedBuses, mapping);
        }

        private void invalidateCache() {
            // detach buses
            if (variants.get().cache != null) {
                for (MergedBus bus : variants.get().cache.getMergedBuses()) {
                    bus.invalidate();
                }
                variants.get().cache = null;
            }
        }

        private Collection<MergedBus> getMergedBuses() {
            updateCache();
            return variants.get().cache.getMergedBuses();
        }

        private MergedBus getMergedBus(String mergedBusId, boolean throwException) {
            updateCache();
            MergedBus bus = variants.get().cache.getMergedBus(mergedBusId);
            if (throwException && bus == null) {
                throw new PowsyblException("Bus " + mergedBusId
                        + " not found in voltage level "
                        + voltageLevel.getId());
            }
            return bus;
        }

        MergedBus getMergedBus(ConfiguredBus bus) {
            Objects.requireNonNull(bus, "bus is null");
            updateCache();
            return variants.get().cache.getMergedBus(bus);
        }

    }

    final CalculatedBusTopology calculatedBusTopology
            = new CalculatedBusTopology();

    private static final class VariantImpl implements Variant {

        private BusCache cache;

        private VariantImpl() {
        }

        @Override
        public VariantImpl copy() {
            return new VariantImpl();
        }
    }

    protected final VariantArray<VariantImpl> variants;

    BusBreakerTopologyModel(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
        // the ref object of the variant array is the same as the current object
        variants = new VariantArray<>(voltageLevel.getNetworkRef(), VariantImpl::new);
        // invalidate topology and connected components
        graph.addListener(new UndirectedGraphListener<>() {
            @Override
            public void vertexAdded(int v) {
                invalidateCache();
            }

            @Override
            public void vertexObjectSet(int v, ConfiguredBus obj) {
                invalidateCache();
            }

            @Override
            public void vertexRemoved(int v, ConfiguredBus obj) {
                invalidateCache();
            }

            @Override
            public void allVerticesRemoved() {
                invalidateCache();
            }

            @Override
            public void edgeAdded(int e, SwitchImpl obj) {
                invalidateCache();
            }

            @Override
            public void edgeBeforeRemoval(int e, SwitchImpl obj) {
                // Nothing to do, notifications are handled properly in removeSwitch
            }

            @Override
            public void edgeRemoved(int e, SwitchImpl obj) {
                invalidateCache();
            }

            @Override
            public void allEdgesBeforeRemoval(Collection<SwitchImpl> obj) {
                // Nothing to do, notifications are handled properly in removeAllSwitches
            }

            @Override
            public void allEdgesRemoved(Collection<SwitchImpl> obj) {
                invalidateCache();
            }
        });
    }

    @Override
    public void invalidateCache(boolean exceptBusBreakerView) {
        calculatedBusTopology.invalidateCache();
        getNetwork().getBusView().invalidateCache();
        getNetwork().getBusBreakerView().invalidateCache();
        getNetwork().getConnectedComponentsManager().invalidate();
        getNetwork().getSynchronousComponentsManager().invalidate();
    }

    @Override
    public Iterable<Terminal> getTerminals() {
        return FluentIterable.from(graph.getVerticesObj())
                .transformAndConcat(ConfiguredBus::getTerminals)
                .transform(Terminal.class::cast);
    }

    @Override
    public Stream<Terminal> getTerminalStream() {
        return graph.getVertexObjectStream().flatMap(bus -> bus.getTerminals().stream());
    }

    static PowsyblException createNotSupportedBusBreakerTopologyException() {
        return new PowsyblException("Not supported in a bus breaker topology");
    }

    private final VoltageLevelExt.NodeBreakerViewExt nodeBreakerView = new VoltageLevelExt.NodeBreakerViewExt() {
        @Override
        public double getFictitiousP0(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public VoltageLevel.NodeBreakerView setFictitiousP0(int node, double p0) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public double getFictitiousQ0(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public VoltageLevel.NodeBreakerView setFictitiousQ0(int node, double q0) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int getMaximumNodeIndex() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int[] getNodes() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int getNode1(String switchId) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int getNode2(String switchId) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Terminal getTerminal(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Stream<Switch> getSwitchStream(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public List<Switch> getSwitches(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public IntStream getNodeInternalConnectedToStream(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public List<Integer> getNodesInternalConnectedTo(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Optional<Terminal> getOptionalTerminal(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public boolean hasAttachedEquipment(int node) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Terminal getTerminal1(String switchId) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Terminal getTerminal2(String switchId) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public SwitchAdder newSwitch() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public InternalConnectionAdder newInternalConnection() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int getInternalConnectionCount() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Iterable<InternalConnection> getInternalConnections() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Stream<InternalConnection> getInternalConnectionStream() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public void removeInternalConnections(int node1, int node2) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public SwitchAdder newBreaker() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public SwitchAdder newDisconnector() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Switch getSwitch(String switchId) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int getSwitchCount() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public void removeSwitch(String switchId) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public BusbarSectionAdder newBusbarSection() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Iterable<BusbarSection> getBusbarSections() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public Stream<BusbarSection> getBusbarSectionStream() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int getBusbarSectionCount() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public BusbarSection getBusbarSection(String id) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public void traverse(int node, TopologyTraverser traverser) {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public void traverse(int[] node, TopologyTraverser traverser) {
            throw createNotSupportedBusBreakerTopologyException();
        }
    };

    @Override
    public VoltageLevelExt.NodeBreakerViewExt getNodeBreakerView() {
        return nodeBreakerView;
    }

    private final VoltageLevelExt.BusBreakerViewExt busBreakerView = new VoltageLevelExt.BusBreakerViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Iterables.unmodifiableIterable(Iterables.transform(graph.getVerticesObj(), Functions.identity()));
        }

        @Override
        public Stream<Bus> getBusStream() {
            return graph.getVertexObjectStream().map(Function.identity());
        }

        @Override
        public int getBusCount() {
            return graph.getVertexCount();
        }

        @Override
        public ConfiguredBus getBus(String id) {
            return BusBreakerTopologyModel.this.getBus(id, false);
        }

        @Override
        public BusAdder newBus() {
            return new BusAdderImpl(voltageLevel);
        }

        @Override
        public void removeBus(String busId) {
            BusBreakerTopologyModel.this.removeBus(busId);
        }

        @Override
        public void removeAllBuses() {
            BusBreakerTopologyModel.this.removeAllBuses();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Iterables.unmodifiableIterable(Iterables.transform(graph.getEdgesObject(), Functions.identity()));
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
            BusBreakerTopologyModel.this.removeSwitch(switchId);
        }

        @Override
        public void removeAllSwitches() {
            BusBreakerTopologyModel.this.removeAllSwitches();
        }

        @Override
        public ConfiguredBus getBus1(String switchId) {
            int e = getEdge(switchId, true);
            int v1 = graph.getEdgeVertex1(e);
            return graph.getVertexObject(v1);
        }

        @Override
        public ConfiguredBus getBus2(String switchId) {
            int e = getEdge(switchId, true);
            int v2 = graph.getEdgeVertex2(e);
            return graph.getVertexObject(v2);
        }

        @Override
        public Collection<Bus> getBusesFromBusViewBusId(String mergedBusId) {
            return getBusStreamFromBusViewBusId(mergedBusId).collect(Collectors.toSet());
        }

        @Override
        public Stream<Bus> getBusStreamFromBusViewBusId(String mergedBusId) {
            MergedBus bus = (MergedBus) busView.getBus(mergedBusId);
            Objects.requireNonNull(bus, "bus is null");
            calculatedBusTopology.updateCache();
            return variants.get().cache.mapping.entrySet().stream().filter(e -> e.getValue() == bus).map(e -> (Bus) e.getKey()).distinct();
        }

        @Override
        public SwitchImpl getSwitch(String switchId) {
            return BusBreakerTopologyModel.this.getSwitch(switchId, false);
        }

        @Override
        public VoltageLevel.BusBreakerView.SwitchAdder newSwitch() {
            return new SwitchAdderImpl();
        }

        private com.powsybl.math.graph.Traverser adapt(TopologyTraverser t) {
            return (vertex1, e, vertex2) -> t.traverse(graph.getVertexObject(vertex1), graph.getEdgeObject(e), graph.getVertexObject(vertex2));
        }

        @Override
        public void traverse(Bus bus, TopologyTraverser traverser) {
            graph.traverse(getVertex(bus.getId(), true), TraversalType.DEPTH_FIRST, adapt(traverser));
        }
    };

    @Override
    public VoltageLevelExt.BusBreakerViewExt getBusBreakerView() {
        return busBreakerView;
    }

    private final VoltageLevelExt.BusViewExt busView = new VoltageLevelExt.BusViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Collections.unmodifiableCollection(calculatedBusTopology.getMergedBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return calculatedBusTopology.getMergedBuses().stream().map(Function.identity());
        }

        @Override
        public MergedBus getBus(String id) {
            return calculatedBusTopology.getMergedBus(id, false);
        }

        @Override
        public Bus getMergedBus(String configuredBusId) {
            ConfiguredBus b = (ConfiguredBus) busBreakerView.getBus(configuredBusId);
            return calculatedBusTopology.getMergedBus(b);
        }
    };

    @Override
    public VoltageLevelExt.BusViewExt getBusView() {
        return busView;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return getBusBreakerView().getSwitches();
    }

    @Override
    public int getSwitchCount() {
        return getBusBreakerView().getSwitchCount();
    }

    @Override
    public TopologyKind getTopologyKind() {
        return TopologyKind.BUS_BREAKER;
    }

    void addBus(ConfiguredBus bus) {
        getNetwork().getIndex().checkAndAdd(bus);
        int v = graph.addVertex();
        graph.setVertexObject(v, bus);
        buses.put(bus.getId(), v);
    }

    private void removeBus(String busId) {
        ConfiguredBus bus = getBus(busId, true);
        if (bus.getTerminalCount() > 0) {
            throw new ValidationException(voltageLevel, "Cannot remove bus "
                    + bus.getId() + " because of connectable equipments");
        }
        // TODO improve check efficency
        for (Map.Entry<String, Integer> entry : switches.entrySet()) {
            String switchId = entry.getKey();
            int e = entry.getValue();
            int v1 = graph.getEdgeVertex1(e);
            int v2 = graph.getEdgeVertex2(e);
            ConfiguredBus b1 = graph.getVertexObject(v1);
            ConfiguredBus b2 = graph.getVertexObject(v2);
            if (bus == b1 || bus == b2) {
                throw new PowsyblException("Cannot remove bus '" + bus.getId()
                        + "' because switch '" + switchId + "' is connected to it");
            }
        }

        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(bus);

        network.getIndex().remove(bus);
        int v = buses.remove(bus.getId());
        graph.removeVertex(v);

        network.getListeners().notifyAfterRemoval(busId);
    }

    private void removeAllBuses() {
        if (graph.getEdgeCount() > 0) {
            throw new ValidationException(voltageLevel, "Cannot remove all buses because there is still some switches");
        }
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            if (bus.getTerminalCount() > 0) {
                throw new ValidationException(voltageLevel, "Cannot remove bus "
                        + bus.getId() + " because of connected equipments");
            }
        }
        NetworkImpl network = getNetwork();
        List<String> removedBusesIds = new ArrayList<>(graph.getVertexCount());
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            removedBusesIds.add(bus.getId());
            network.getListeners().notifyBeforeRemoval(bus);

            network.getIndex().remove(bus);
        }
        graph.removeAllVertices();
        buses.clear();

        removedBusesIds.forEach(id -> network.getListeners().notifyAfterRemoval(id));
    }

    private void addSwitch(SwitchImpl aSwitch, String busId1, String busId2) {
        int v1 = getVertex(busId1, true);
        int v2 = getVertex(busId2, true);
        getNetwork().getIndex().checkAndAdd(aSwitch);
        int e = graph.addEdge(v1, v2, aSwitch);
        switches.put(aSwitch.getId(), e);
    }

    private void removeSwitch(String switchId) {
        Integer e = switches.get(switchId);
        if (e == null) {
            throw new PowsyblException("Switch '" + switchId
                    + "' not found in voltage level '" + voltageLevel.getId() + "'");
        }
        NetworkImpl network = getNetwork();
        SwitchImpl aSwitch = graph.getEdgeObject(e);
        network.getListeners().notifyBeforeRemoval(aSwitch);

        switches.remove(switchId);
        graph.removeEdge(e);
        network.getIndex().remove(aSwitch);

        network.getListeners().notifyAfterRemoval(switchId);
    }

    private void removeAllSwitches() {
        NetworkImpl network = getNetwork();

        List<String> removedSwitchesIds = new ArrayList<>(graph.getEdgeCount());
        for (SwitchImpl s : graph.getEdgesObject()) {
            removedSwitchesIds.add(s.getId());
            network.getListeners().notifyBeforeRemoval(s);

            network.getIndex().remove(s);
        }
        graph.removeAllEdges();
        switches.clear();

        for (String removedSwitchId : removedSwitchesIds) {
            network.getListeners().notifyAfterRemoval(removedSwitchId);
        }
    }

    private void checkTerminal(TerminalExt terminal) {
        if (!(terminal instanceof BusTerminal)) {
            throw new ValidationException(terminal.getConnectable(),
                    "voltage level " + voltageLevel.getId() + " has a bus/breaker topology"
                            + ", a bus connection should be specified instead of a node connection");
        }

        // check connectable buses exist
        String connectableBusId = ((BusTerminal) terminal).getConnectableBusId();
        if (connectableBusId != null) {
            getBus(connectableBusId, true);
        }
    }

    @Override
    public void attach(final TerminalExt terminal, boolean test) {
        checkTerminal(terminal);
        if (test) {
            return;
        }
        // create the link terminal -> voltage level
        terminal.setVoltageLevel(voltageLevel);

        // create the link bus -> terminal
        String connectableBusId = ((BusTerminal) terminal).getConnectableBusId();

        final ConfiguredBus connectableBus = getBus(connectableBusId, true);

        getNetwork().getVariantManager().forEachVariant(() -> {
            connectableBus.addTerminal((BusTerminal) terminal);

            // invalidate connected components
            invalidateCache();
        });
    }

    @Override
    public void detach(final TerminalExt terminal) {
        if (!(terminal instanceof BusTerminal)) {
            throw new IllegalArgumentException("Incorrect terminal type");
        }

        // remove the link bus -> terminal
        String connectableBusId = ((BusTerminal) terminal).getConnectableBusId();

        final ConfiguredBus connectableBus = getBus(connectableBusId, true);

        getNetwork().getVariantManager().forEachVariant(() -> {
            connectableBus.removeTerminal((BusTerminal) terminal);
            ((BusTerminal) terminal).setConnectableBusId(null);

            invalidateCache();
        });
        // remove the link terminal -> voltage level
        terminal.setVoltageLevel(null);
    }

    boolean connect(TerminalExt terminal) {
        if (!(terminal instanceof BusTerminal)) {
            throw new IllegalStateException("Given TerminalExt not supported: " + terminal.getClass().getName());
        }

        // already connected?
        if (terminal.isConnected()) {
            return false;
        }

        ((BusTerminal) terminal).setConnected(true);

        // invalidate connected components
        invalidateCache();

        return true;
    }

    @Override
    public boolean connect(TerminalExt terminal, Predicate<? super SwitchImpl> isTypeSwitchToOperate) {
        return connect(terminal);
    }

    boolean disconnect(TerminalExt terminal) {
        if (!(terminal instanceof BusTerminal)) {
            throw new IllegalStateException("Given TerminalExt not supported: " + terminal.getClass().getName());
        }
        // already disconnected?
        if (!terminal.isConnected()) {
            return false;
        }

        ((BusTerminal) terminal).setConnected(false);

        // invalidate connected components
        invalidateCache();

        return true;
    }

    @Override
    public boolean disconnect(TerminalExt terminal, Predicate<? super SwitchImpl> isSwitchOpenable) {
        return disconnect(terminal);
    }

    void traverse(BusTerminal terminal, Terminal.TopologyTraverser traverser, TraversalType traversalType) {
        traverse(terminal, traverser, new HashSet<>(), traversalType);
    }

    /**
     * Traverse from given bus terminal using the given topology traverser, using the fact that the terminals in the
     * given set have already been traversed.
     * @return false if the traverser has to stop, meaning that a {@link TraverseResult#TERMINATE_TRAVERSER}
     * has been returned from the traverser, true otherwise
     */
    boolean traverse(BusTerminal terminal, Terminal.TopologyTraverser traverser, Set<Terminal> visitedTerminals, TraversalType traversalType) {
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(traverser);
        Objects.requireNonNull(visitedTerminals);

        // check if we are allowed to traverse the terminal itself
        TraverseResult termTraverseResult = getTraverserResult(visitedTerminals, terminal, traverser);
        if (termTraverseResult == TraverseResult.TERMINATE_TRAVERSER) {
            return false;
        } else if (termTraverseResult == TraverseResult.CONTINUE) {
            List<TerminalExt> nextTerminals = new ArrayList<>();
            addNextTerminals(terminal, nextTerminals);

            // then check we can traverse terminals connected to same bus
            int v = getVertex(terminal.getConnectableBusId(), true);
            ConfiguredBus bus = graph.getVertexObject(v);
            for (BusTerminal t : bus.getTerminals()) {
                TraverseResult tTraverseResult = getTraverserResult(visitedTerminals, t, traverser);
                if (tTraverseResult == TraverseResult.TERMINATE_TRAVERSER) {
                    return false;
                } else if (tTraverseResult == TraverseResult.CONTINUE) {
                    addNextTerminals(t, nextTerminals);
                }
            }

            // then go through other buses of the voltage level
            boolean traversalTerminated = traverseOtherBuses(v, nextTerminals, traverser, visitedTerminals, traversalType);
            if (traversalTerminated) {
                return false;
            }

            for (TerminalExt t : nextTerminals) {
                if (!t.traverse(traverser, visitedTerminals, traversalType)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean traverseOtherBuses(int v, List<TerminalExt> nextTerminals,
                                       Terminal.TopologyTraverser traverser, Set<Terminal> visitedTerminals, TraversalType traversalType) {
        return !graph.traverse(v, traversalType, (v1, e, v2) -> {
            SwitchImpl aSwitch = graph.getEdgeObject(e);
            List<BusTerminal> otherBusTerminals = graph.getVertexObject(v2).getTerminals();
            TraverseResult switchTraverseResult = traverser.traverse(aSwitch);
            if (switchTraverseResult == TraverseResult.CONTINUE && !otherBusTerminals.isEmpty()) {
                BusTerminal otherTerminal = otherBusTerminals.get(0);
                TraverseResult otherTermTraverseResult = getTraverserResult(visitedTerminals, otherTerminal, traverser);
                if (otherTermTraverseResult == TraverseResult.CONTINUE) {
                    addNextTerminals(otherTerminal, nextTerminals);
                }
                return otherTermTraverseResult;
            }
            return switchTraverseResult;
        });
    }

    private static TraverseResult getTraverserResult(Set<Terminal> visitedTerminals, BusTerminal terminal, Terminal.TopologyTraverser traverser) {
        return visitedTerminals.add(terminal) ? traverser.traverse(terminal, terminal.isConnected()) : TraverseResult.TERMINATE_PATH;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        variants.push(number, () -> variants.copy(sourceIndex));
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
        variants.allocate(indexes, () -> variants.copy(sourceIndex));
    }

    @Override
    protected void removeTopology() {
        removeAllSwitches();
        removeAllBuses();
    }

    @Override
    public void printTopology() {
        printTopology(System.out, null);
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        out.println("-------------------------------------------------------------");
        out.println("Topology of " + voltageLevel.getId());

        Function<ConfiguredBus, String> vertexToString = bus -> {
            StringBuilder builder = new StringBuilder();
            builder.append(bus.getId())
                    .append(" [");
            for (Iterator<TerminalExt> it = bus.getConnectedTerminals().iterator(); it.hasNext(); ) {
                TerminalExt terminal = it.next();
                builder.append(dict != null ? dict.getShortId(terminal.getConnectable().getId()) : terminal.getConnectable().getId());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        };

        Function<SwitchImpl, String> edgeToString = aSwitch -> {
            StringBuilder builder = new StringBuilder();
            builder.append("id=").append(aSwitch.getId())
                    .append(" status=").append(aSwitch.isOpen() ? "open" : "closed");
            return builder.toString();
        };

        graph.print(out, vertexToString, edgeToString);
    }

    @Override
    public void exportTopology(Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            exportTopology(writer);
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

        String[] colors = Colors.generateColorScale(graph.getVertexCount(), random);
        int i = 0;
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            gvGraph.node(scope, bus.getId())
                    .label("BUS" + System.lineSeparator() + bus.getId())
                    .shape("ellipse")
                    .style("filled")
                    .attr(GraphVizAttribute.fillcolor, colors[i]);
            for (TerminalExt terminal : bus.getTerminals()) {
                AbstractConnectable connectable = terminal.getConnectable();
                String label = connectable.getType().toString()
                    + System.lineSeparator() + connectable.getId()
                    + connectable.getOptionalName().map(name -> System.lineSeparator() + name).orElse("");
                gvGraph.node(scope, connectable.getId())
                        .label(label)
                        .shape("ellipse")
                        .style("filled")
                        .attr(GraphVizAttribute.fillcolor, colors[i]);
            }
            i++;
        }

        for (ConfiguredBus bus : graph.getVerticesObj()) {
            for (TerminalExt terminal : bus.getTerminals()) {
                AbstractConnectable connectable = terminal.getConnectable();
                gvGraph.edge(scope, bus.getId(), connectable.getId())
                        .style(terminal.isConnected() ? "solid" : "dotted");
            }
        }
        for (int e = 0; e < graph.getEdgeCount(); e++) {
            int v1 = graph.getEdgeVertex1(e);
            int v2 = graph.getEdgeVertex2(e);
            SwitchImpl sw = graph.getEdgeObject(e);
            ConfiguredBus bus1 = graph.getVertexObject(v1);
            ConfiguredBus bus2 = graph.getVertexObject(v2);
            // Assign an id to the edge to allow parallel edges (multigraph)
            GraphVizEdge edge = gvGraph.edge(scope, bus1.getId(), bus2.getId(), sw.getId())
                    .style(sw.isOpen() ? "dotted" : "solid");
            if (DRAW_SWITCH_ID) {
                String label = sw.getKind().toString()
                    + System.lineSeparator() + sw.getId()
                    + sw.getOptionalName().map(n -> System.lineSeparator() + n).orElse("");
                edge.label(label)
                        .attr(GraphVizAttribute.fontsize, "10");
            }
        }

        try {
            gvGraph.writeTo(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
