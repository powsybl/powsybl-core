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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraphImpl;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusBreakerVoltageLevel extends AbstractVoltageLevel {

    private final class SwitchAdderImpl extends AbstractIdentifiableAdder<SwitchAdderImpl> implements BusBreakerView.SwitchAdder {

        private String busId1;

        private String busId2;

        private boolean open = false;

        private boolean fictitious = false;

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
        public BusBreakerView.SwitchAdder setFictitious(boolean fictitious) {
            this.fictitious = fictitious;
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

            SwitchImpl aSwitch = new SwitchImpl(BusBreakerVoltageLevel.this, id, getName(), SwitchKind.BREAKER, open, true, fictitious);
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

        private void updateCache() {
            if (states.get().cache != null) {
                return;
            }

            Map<String, MergedBus> mergedBuses = new LinkedHashMap<>();

            // mapping between configured buses and merged buses
            Map<ConfiguredBus, MergedBus> mapping = new IdentityHashMap<>();

            boolean[] encountered = new boolean[graph.getMaxVertex()];
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
                        String mergedBusId = BusBreakerVoltageLevel.this.id + "_" + busNum++;
                        MergedBus mergedBus = new MergedBus(mergedBusId, busSet);
                        mergedBuses.put(mergedBus.getId(), mergedBus);
                        busSet.forEach(bus -> mapping.put(bus, mergedBus));
                    }
                }
            }

            states.get().cache = new BusCache(mergedBuses, mapping);
        }

        private void invalidateCache() {
            // detach buses
            if (states.get().cache != null) {
                for (MergedBus bus : states.get().cache.getMergedBuses()) {
                    bus.invalidate();
                }
                states.get().cache = null;
            }
        }

        private Collection<MergedBus> getMergedBuses() {
            updateCache();
            return states.get().cache.getMergedBuses();
        }

        private MergedBus getMergedBus(String mergedBusId, boolean throwException) {
            updateCache();
            MergedBus bus = states.get().cache.getMergedBus(mergedBusId);
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
            return states.get().cache.getMergedBus(bus);
        }

    }

    final CalculatedBusTopology calculatedBusTopology
            = new CalculatedBusTopology();

    private static final class StateImpl implements State {

        private BusCache cache;

        private StateImpl() {
        }

        @Override
        public StateImpl copy() {
            return new StateImpl();
        }
    }

    protected final StateArray<StateImpl> states;

    BusBreakerVoltageLevel(String id, String name, SubstationImpl substation,
                           double nominalV, double lowVoltageLimit, double highVoltageLimit) {
        super(id, name, substation, nominalV, lowVoltageLimit, highVoltageLimit);
        states = new StateArray<>(substation.getNetwork().getRef(), StateImpl::new);
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
        public int getNodeCount() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public int[] getNodes() {
            throw createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public NodeBreakerView setNodeCount(int count) {
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
        getNetwork().getObjectStore().checkAndAdd(bus);
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
        getNetwork().getObjectStore().remove(bus);
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
            getNetwork().getObjectStore().remove(bus);
        }
        graph.removeAllVertices();
        buses.clear();
    }

    private void addSwitch(SwitchImpl aSwitch, String busId1, String busId2) {
        int v1 = getVertex(busId1, true);
        int v2 = getVertex(busId2, true);
        getNetwork().getObjectStore().checkAndAdd(aSwitch);
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
        getNetwork().getObjectStore().remove(aSwitch);
    }

    private void removeAllSwitches() {
        for (SwitchImpl s : graph.getEdgesObject()) {
            getNetwork().getObjectStore().remove(s);
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

        getNetwork().getStateManager().forEachState(() -> {
            connectableBus.addTerminal((BusTerminal) terminal);

            // invalidate connected components
            invalidateCache();
        });
    }

    @Override
    public void detach(final TerminalExt terminal) {
        assert terminal instanceof BusTerminal;

        // remove the link terminal -> voltage level
        terminal.setVoltageLevel(null);

        // remove the link bus -> terminal
        String connectableBusId = ((BusTerminal) terminal).getConnectableBusId();

        final ConfiguredBus connectableBus = getBus(connectableBusId, true);

        getNetwork().getStateManager().forEachState(() -> {
            connectableBus.removeTerminal((BusTerminal) terminal);
            ((BusTerminal) terminal).setConnectableBusId(null);

            invalidateCache();
        });
    }

    @Override
    public void clean() {
        // nothing to do
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
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        states.push(number, () -> states.copy(sourceIndex));
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
        states.allocate(indexes, () -> states.copy(sourceIndex));
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
    public void exportTopology(String filename) throws IOException {
        try (OutputStream writer = new FileOutputStream(filename)) {
            exportTopology(writer);
        }
    }

    @Override
    public void exportTopology(OutputStream outputStream) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.append("graph \"").append(BusBreakerVoltageLevel.this.id).append("\" {\n");
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            String label = "BUS\\n" + bus.getId();
            writer.append("  ").append(bus.getId())
                        .append(" [label=\"").append(label).append("\"]\n");
            for (TerminalExt terminal : bus.getTerminals()) {
                AbstractConnectable connectable = terminal.getConnectable();
                label = connectable.getType().toString() + "\\n" + connectable.getId();
                writer.append("  ").append(connectable.getId())
                        .append(" [label=\"").append(label).append("\"]\n");
            }
        }
        for (ConfiguredBus bus : graph.getVerticesObj()) {
            for (TerminalExt terminal : bus.getTerminals()) {
                AbstractConnectable connectable = terminal.getConnectable();
                writer.append("  ").append(bus.getId())
                    .append(" -- ").append(connectable.getId())
                    .append(" [").append("style=\"").append(terminal.isConnected() ? "solid" : "dotted").append("\"")
                    .append("]\n");
            }
        }
        boolean drawSwitchId = false;
        for (int e = 0; e < graph.getEdgeCount(); e++) {
            int v1 = graph.getEdgeVertex1(e);
            int v2 = graph.getEdgeVertex2(e);
            SwitchImpl sw = graph.getEdgeObject(e);
            ConfiguredBus bus1 = graph.getVertexObject(v1);
            ConfiguredBus bus2 = graph.getVertexObject(v2);
            writer.append("  ").append(bus1.getId())
                    .append(" -- ").append(bus2.getId())
                    .append(" [");
            if (drawSwitchId) {
                writer.append("label=\"").append(sw.getId())
                        .append("\", fontsize=10");
            }
            writer.append("style=\"").append(sw.isOpen() ? "dotted" : "solid").append("\"");
            writer.append("]\n");
        }
        writer.append("}\n");
    }

}
