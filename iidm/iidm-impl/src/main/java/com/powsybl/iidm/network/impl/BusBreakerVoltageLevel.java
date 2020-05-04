/**
 * Copyright (c) 2016-2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraphImpl;
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
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusBreakerVoltageLevel extends AbstractVoltageLevel {

    private static final boolean DRAW_SWITCH_ID = false;

    private final class SwitchAdderImpl extends AbstractIdentifiableAdder<SwitchAdderImpl> implements BusBreakerView.SwitchAdder {

        private String busId1;

        private String busId2;

        private boolean open = false;

        private SwitchAdderImpl() {
        }

        @Override
        protected NetworkImpl getNetwork() {
            return BusBreakerVoltageLevel.this.getNetwork();
        }

        @Override
        protected String getTypeDescription() {
            return "Switch";
        }

        @Override
        public BusBreakerView.SwitchAdder setBus1(String bus1) {
            this.busId1 = bus1;
            return this;
        }

        @Override
        public BusBreakerView.SwitchAdder setBus2(String bus2) {
            this.busId2 = bus2;
            return this;
        }

        @Override
        public BusBreakerView.SwitchAdder setOpen(boolean open) {
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

            SwitchImpl aSwitch = new SwitchImpl(BusBreakerVoltageLevel.this, id, getName(), isFictitious(), SwitchKind.BREAKER, open, true);
            addSwitch(aSwitch, busId1, busId2);
            getNetwork().getListeners().notifyCreation(aSwitch);
            return aSwitch;
        }

    }

    private final UndirectedGraphImpl<ConfiguredBus, SwitchImpl> graph = new UndirectedGraphImpl<>();

    /* buses indexed by vertex number */
    private final Map<String, Integer> buses = new HashMap<>();

    /* switches indexed by edge number */
    private final Map<String, Integer> switches = new HashMap<>();

    private Integer getVertex(String busId, boolean throwException) {
        Objects.requireNonNull(busId, "bus id is null");
        Integer v = buses.get(busId);
        if (throwException && v == null) {
            throw new PowsyblException("Bus " + busId
                    + " not found in substation voltage level "
                    + BusBreakerVoltageLevel.this.id);
        }
        return v;
    }

    ConfiguredBus getBus(String busId, boolean throwException) {
        Integer v = getVertex(busId, throwException);
        if (v != null) {
            ConfiguredBus bus = graph.getVertexObject(v);
            if (!bus.getId().equals(busId)) {
                throw new AssertionError("Invalid bus id (expected: " + busId + ", actual: " + bus.getId() + ")");
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
                    + " not found in substation voltage level"
                    + BusBreakerVoltageLevel.this.id);
        }
        return e;
    }

    private SwitchImpl getSwitch(String switchId, boolean throwException) {
        Integer e = getEdge(switchId, throwException);
        if (e != null) {
            SwitchImpl aSwitch = graph.getEdgeObject(e);
            if (!aSwitch.getId().equals(switchId)) {
                throw new AssertionError("Invalid switch id (expected: " + switchId + ", actual: " + aSwitch.getId() + ")");
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
            int branchCount = 0;
            for (TerminalExt terminal : FluentIterable.from(busSet).transformAndConcat(ConfiguredBus::getConnectedTerminals)) {
                AbstractConnectable connectable = terminal.getConnectable();
                switch (connectable.getType()) {
                    case LINE:
                    case TWO_WINDINGS_TRANSFORMER:
                    case THREE_WINDINGS_TRANSFORMER:
                    case HVDC_CONVERTER_STATION:
                        branchCount++;
                        feederCount++;
                        break;

                    case DANGLING_LINE:
                    case LOAD:
                    case GENERATOR:
                    case BATTERY:
                    case SHUNT_COMPENSATOR:
                    case STATIC_VAR_COMPENSATOR:
                        feederCount++;
                        break;

                    case BUSBAR_SECTION: // must not happend in a bus/breaker topology
                    default:
                        throw new AssertionError();
                }
            }
            return Networks.isBusValid(branchCount);
        }

        private MergedBus createMergedBus(int busNum, Set<ConfiguredBus> busSet) {
            String suffix = "_" + busNum;
            String mergedBusId = BusBreakerVoltageLevel.this.id + suffix;
            String mergedBusName = BusBreakerVoltageLevel.this.name != null ? BusBreakerVoltageLevel.this.name + suffix : null;
            return new MergedBus(mergedBusId, mergedBusName, BusBreakerVoltageLevel.this.fictitious, busSet);
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
                    graph.traverse(v, (v1, e, v2) -> {
                        SwitchImpl aSwitch = graph.getEdgeObject(e);
                        if (aSwitch.isOpen()) {
                            return TraverseResult.TERMINATE;
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
                        + " not found in substation voltage level "
                        + BusBreakerVoltageLevel.this.id);
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

    BusBreakerVoltageLevel(String id, String name, boolean fictitious, SubstationImpl substation,
                           double nominalV, double lowVoltageLimit, double highVoltageLimit) {
        super(id, name, fictitious, substation, nominalV, lowVoltageLimit, highVoltageLimit);
        variants = new VariantArray<>(substation.getNetwork().getRef(), VariantImpl::new);
        // invalidate topology and connected components
        graph.addListener(this::invalidateCache);
    }

    @Override
    public void invalidateCache() {
        calculatedBusTopology.invalidateCache();
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

    private final NodeBreakerViewExt nodeBreakerView = new NodeBreakerViewExt() {
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
        public void traverse(int node, Traverser traverser) {
            throw createNotSupportedBusBreakerTopologyException();
        }
    };

    @Override
    public NodeBreakerViewExt getNodeBreakerView() {
        return nodeBreakerView;
    }

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public Iterable<Bus> getBuses() {
            return Iterables.unmodifiableIterable(Iterables.transform(graph.getVerticesObj(), Functions.identity()));
        }

        @Override
        public Stream<Bus> getBusStream() {
            return graph.getVertexObjectStream().map(Function.identity());
        }

        @Override
        public ConfiguredBus getBus(String id) {
            return BusBreakerVoltageLevel.this.getBus(id, false);
        }

        @Override
        public BusAdder newBus() {
            return new BusAdderImpl(BusBreakerVoltageLevel.this);
        }

        @Override
        public void removeBus(String busId) {
            BusBreakerVoltageLevel.this.removeBus(busId);
        }

        @Override
        public void removeAllBuses() {
            BusBreakerVoltageLevel.this.removeAllBuses();
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
            BusBreakerVoltageLevel.this.removeSwitch(switchId);
        }

        @Override
        public void removeAllSwitches() {
            BusBreakerVoltageLevel.this.removeAllSwitches();
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
        public SwitchImpl getSwitch(String switchId) {
            return BusBreakerVoltageLevel.this.getSwitch(switchId, false);
        }

        @Override
        public BusBreakerView.SwitchAdder newSwitch() {
            return new SwitchAdderImpl();
        }

    };

    @Override
    public BusBreakerViewExt getBusBreakerView() {
        return busBreakerView;
    }

    private final BusViewExt busView = new BusViewExt() {

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
    public BusViewExt getBusView() {
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
            throw new ValidationException(this, "Cannot remove bus "
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
        getNetwork().getIndex().remove(bus);
        getNetwork().getListeners().notifyRemoval(bus);
        int v = buses.remove(bus.getId());
        graph.removeVertex(v);
    }

    private void removeAllBuses() {
        if (graph.getEdgeCount() > 0) {
            throw new ValidationException(this, "Cannot remove all buses because there is still some switches");
        }
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            if (bus.getTerminalCount() > 0) {
                throw new ValidationException(this, "Cannot remove bus "
                        + bus.getId() + " because of connected equipments");
            }
        }
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            getNetwork().getIndex().remove(bus);
            getNetwork().getListeners().notifyRemoval(bus);
        }
        graph.removeAllVertices();
        buses.clear();
    }

    private void addSwitch(SwitchImpl aSwitch, String busId1, String busId2) {
        int v1 = getVertex(busId1, true);
        int v2 = getVertex(busId2, true);
        getNetwork().getIndex().checkAndAdd(aSwitch);
        int e = graph.addEdge(v1, v2, aSwitch);
        switches.put(aSwitch.getId(), e);
    }

    private void removeSwitch(String switchId) {
        Integer e = switches.remove(switchId);
        if (e == null) {
            throw new PowsyblException("Switch '" + switchId
                    + "' not found in substation voltage level '" + id + "'");
        }
        SwitchImpl aSwitch = graph.removeEdge(e);
        getNetwork().getIndex().remove(aSwitch);
        getNetwork().getListeners().notifyRemoval(aSwitch);
    }

    private void removeAllSwitches() {
        for (SwitchImpl s : graph.getEdgesObject()) {
            getNetwork().getIndex().remove(s);
            getNetwork().getListeners().notifyRemoval(s);
        }
        graph.removeAllEdges();
        switches.clear();
    }

    private void checkTerminal(TerminalExt terminal) {
        if (!(terminal instanceof BusTerminal)) {
            throw new ValidationException(terminal.getConnectable(),
                    "voltage level " + BusBreakerVoltageLevel.this.id + " has a bus/breaker topology"
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
        terminal.setVoltageLevel(this);

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
        assert terminal instanceof BusTerminal;

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

    @Override
    public boolean connect(TerminalExt terminal) {
        assert terminal instanceof BusTerminal;

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
    public boolean disconnect(TerminalExt terminal) {
        assert terminal instanceof BusTerminal;

        // already connected?
        if (!terminal.isConnected()) {
            return false;
        }

        ((BusTerminal) terminal).setConnected(false);

        // invalidate connected components
        invalidateCache();

        return true;
    }

    void traverse(BusTerminal terminal, VoltageLevel.TopologyTraverser traverser) {
        traverse(terminal, traverser, new HashSet<>());
    }

    void traverse(BusTerminal terminal, VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(traverser);
        Objects.requireNonNull(traversedTerminals);

        if (traversedTerminals.contains(terminal)) {
            return;
        }

        List<TerminalExt> nextTerminals = new ArrayList<>();

        // check if we are allowed to traverse the terminal itself
        if (traverser.traverse(terminal, terminal.isConnected())) {
            traversedTerminals.add(terminal);

            addNextTerminals(terminal, nextTerminals);

            // then check we can traverse terminal connected to same bus
            int v = getVertex(terminal.getConnectableBusId(), true);
            ConfiguredBus bus = graph.getVertexObject(v);
            bus.getTerminals().stream()
                    .filter(t -> t != terminal)
                    .filter(t -> traverser.traverse(t, t.isConnected()))
                    .forEach(t -> addNextTerminals(t, nextTerminals));

            // then go through other buses of the substation
            graph.traverse(v, (v1, e, v2) -> {
                SwitchImpl aSwitch = graph.getEdgeObject(e);
                ConfiguredBus otherBus = graph.getVertexObject(v2);
                if (traverser.traverse(aSwitch)) {
                    if (otherBus.getTerminalCount() == 0) {
                        return TraverseResult.CONTINUE;
                    }

                    BusTerminal otherTerminal = otherBus.getTerminals().get(0);
                    if (traverser.traverse(otherTerminal, otherTerminal.isConnected())) {
                        traversedTerminals.add(otherTerminal);

                        addNextTerminals(otherTerminal, nextTerminals);
                        return TraverseResult.CONTINUE;
                    }
                }
                return TraverseResult.TERMINATE;
            });

            nextTerminals.forEach(t -> t.traverse(traverser, traversedTerminals));
        }
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
        out.println("Topology of " + BusBreakerVoltageLevel.this.id);

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
                gvGraph.node(scope, connectable.getId())
                        .label(connectable.getType().toString() + System.lineSeparator() + connectable.getId())
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
            GraphVizEdge edge = gvGraph.edge(scope, bus1.getId(), bus2.getId())
                    .style(sw.isOpen() ? "solid" : "dotted");
            if (DRAW_SWITCH_ID) {
                edge.label(sw.getId())
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
