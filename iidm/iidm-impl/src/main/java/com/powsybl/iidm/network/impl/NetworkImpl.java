/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.math.graph.GraphUtil;
import com.powsybl.math.graph.GraphUtil.ConnectedComponentsComputationResult;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.impl.util.RefChain;
import com.powsybl.iidm.network.impl.util.RefObj;
import gnu.trove.list.array.TIntArrayList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NetworkImpl extends AbstractIdentifiable<Network> implements Network, MultiStateObject, Stateful {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkImpl.class);

    private final RefChain<NetworkImpl> ref = new RefChain<>(new RefObj<>(this));

    private DateTime caseDate = new DateTime(); // default is the time at which the network has been created

    private int forecastDistance = 0;

    private String sourceFormat;

    private final ObjectStore objectStore = new ObjectStore();

    private final StateManagerImpl stateManager;

    private final NetworkListenerList listeners = new NetworkListenerList();

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                                 .transformAndConcat(vl -> vl.getBusBreakerView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getBusStream());
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return FluentIterable.from(getVoltageLevels())
                                 .transformAndConcat(vl -> vl.getBusBreakerView().getSwitches());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getSwitchStream());
        }

        @Override
        public int getSwitchCount() {
            return getVoltageLevelStream().mapToInt(vl -> vl.getBusBreakerView().getSwitchCount()).sum();
        }

    }

    private final BusBreakerViewImpl busBreakerView = new BusBreakerViewImpl();

    class BusViewImpl implements BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                                 .transformAndConcat(vl -> vl.getBusView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream());
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return Collections.unmodifiableList(states.get().connectedComponentsManager.getConnectedComponents());
        }

    }

    private final BusViewImpl busView = new BusViewImpl();

    NetworkImpl(String id, String name, String sourceFormat) {
        super(id, name);
        Objects.requireNonNull(sourceFormat, "source format is null");
        this.sourceFormat = sourceFormat;
        stateManager = new StateManagerImpl(objectStore);
        states = new StateArray<>(ref, StateImpl::new);
        // add the network the object list as it is a stateful object
        // and it needs to be notified when and extension or a reduction of
        // the state array is requested
        objectStore.checkAndAdd(this);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    public DateTime getCaseDate() {
        return caseDate;
    }

    @Override
    public NetworkImpl setCaseDate(DateTime caseDate) {
        ValidationUtil.checkCaseDate(this, caseDate);
        this.caseDate = caseDate;
        return this;
    }

    @Override
    public int getForecastDistance() {
        return forecastDistance;
    }

    @Override
    public NetworkImpl setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        this.forecastDistance = forecastDistance;
        return this;
    }

    @Override
    public String getSourceFormat() {
        return sourceFormat;
    }

    RefChain<NetworkImpl> getRef() {
        return ref;
    }

    NetworkListenerList getListeners() {
        return listeners;
    }

    public ObjectStore getObjectStore() {
        return objectStore;
    }

    @Override
    public StateManagerImpl getStateManager() {
        return stateManager;
    }

    @Override
    public int getStateIndex() {
        return stateManager.getStateContext().getStateIndex();
    }

    @Override
    public Set<Country> getCountries() {
        return getSubstationStream().map(Substation::getCountry).collect(Collectors.toCollection(() -> EnumSet.noneOf(Country.class)));
    }

    @Override
    public int getCountryCount() {
        return getCountries().size();
    }

    @Override
    public SubstationAdder newSubstation() {
        return new SubstationAdderImpl(ref);
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return Collections.unmodifiableCollection(objectStore.getAll(SubstationImpl.class));
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return objectStore.getAll(SubstationImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getSubstationCount() {
        return objectStore.getAll(SubstationImpl.class).size();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return Substations.filter(getSubstations(), country, tsoId, geographicalTags);
    }

    @Override
    public SubstationImpl getSubstation(String id) {
        return objectStore.get(id, SubstationImpl.class);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.concat(objectStore.getAll(BusBreakerVoltageLevel.class),
                                objectStore.getAll(NodeBreakerVoltageLevel.class));
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return Stream.concat(objectStore.getAll(BusBreakerVoltageLevel.class).stream(),
                objectStore.getAll(NodeBreakerVoltageLevel.class).stream());
    }

    @Override
    public int getVoltageLevelCount() {
        return objectStore.getAll(BusBreakerVoltageLevel.class).size()
                + objectStore.getAll(NodeBreakerVoltageLevel.class).size();
    }

    @Override
    public VoltageLevelExt getVoltageLevel(String id) {
        return objectStore.get(id, VoltageLevelExt.class);
    }

    @Override
    public LineAdderImpl newLine() {
        return new LineAdderImpl(this);
    }

    @Override
    public Iterable<Line> getLines() {
        return Iterables.concat(objectStore.getAll(LineImpl.class), objectStore.getAll(TieLineImpl.class));
    }

    @Override
    public Branch getBranch(String branchId) {
        Objects.requireNonNull(branchId);
        Branch branch = getLine(branchId);
        if (branch == null) {
            branch = getTwoWindingsTransformer(branchId);
        }
        return branch;
    }

    @Override
    public Iterable<Branch> getBranches() {
        return Iterables.concat(getLines(), getTwoWindingsTransformers());
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return Stream.concat(getLineStream(), getTwoWindingsTransformerStream());
    }

    @Override
    public int getBranchCount() {
        return getLineCount() + getTwoWindingsTransformerCount();
    }

    @Override
    public Stream<Line> getLineStream() {
        return Stream.concat(objectStore.getAll(LineImpl.class).stream(), objectStore.getAll(TieLineImpl.class).stream());
    }

    @Override
    public int getLineCount() {
        return objectStore.getAll(LineImpl.class).size() + objectStore.getAll(TieLineImpl.class).size();
    }

    @Override
    public LineImpl getLine(String id) {
        LineImpl line = objectStore.get(id, LineImpl.class);
        if (line == null) {
            line = objectStore.get(id, TieLineImpl.class);
        }
        return line;
    }

    @Override
    public TieLineAdderImpl newTieLine() {
        return new TieLineAdderImpl(this);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Collections.unmodifiableCollection(objectStore.getAll(TwoWindingsTransformerImpl.class));
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return objectStore.getAll(TwoWindingsTransformerImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return objectStore.getAll(TwoWindingsTransformerImpl.class).size();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return objectStore.get(id, TwoWindingsTransformerImpl.class);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Collections.unmodifiableCollection(objectStore.getAll(ThreeWindingsTransformerImpl.class));
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return objectStore.getAll(ThreeWindingsTransformerImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return objectStore.getAll(ThreeWindingsTransformerImpl.class).size();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return objectStore.get(id, ThreeWindingsTransformerImpl.class);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Collections.unmodifiableCollection(objectStore.getAll(GeneratorImpl.class));
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return objectStore.getAll(GeneratorImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getGeneratorCount() {
        return objectStore.getAll(GeneratorImpl.class).size();
    }

    @Override
    public GeneratorImpl getGenerator(String id) {
        return objectStore.get(id, GeneratorImpl.class);
    }

    @Override
    public Iterable<Load> getLoads() {
        return Collections.unmodifiableCollection(objectStore.getAll(LoadImpl.class));
    }

    @Override
    public Stream<Load> getLoadStream() {
        return objectStore.getAll(LoadImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getLoadCount() {
        return objectStore.getAll(LoadImpl.class).size();
    }

    @Override
    public LoadImpl getLoad(String id) {
        return objectStore.get(id, LoadImpl.class);
    }

    /**
     * @deprecated Use {@link #getShuntCompensators)} instead.
     */
    @Override
    @Deprecated
    public Iterable<ShuntCompensator> getShunts() {
        return getShuntCompensators();
    }

    /**
     * @deprecated Use {@link #getShuntCompensatorStream()} instead.
     */
    @Override
    @Deprecated
    public Stream<ShuntCompensator> getShuntStream() {
        return getShuntCompensatorStream();
    }

    /**
     * @deprecated Use {@link #getShuntCompensatorCount()} instead.
     */
    @Override
    @Deprecated
    public int getShuntCount() {
        return getShuntCompensatorCount();
    }

    /**
     * @deprecated Use {@link #getShuntCompensator(String)} instead.
     */
    @Override
    @Deprecated
    public ShuntCompensatorImpl getShunt(String id) {
        return getShuntCompensator(id);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Collections.unmodifiableCollection(objectStore.getAll(ShuntCompensatorImpl.class));
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return objectStore.getAll(ShuntCompensatorImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getShuntCompensatorCount() {
        return objectStore.getAll(ShuntCompensatorImpl.class).size();
    }

    @Override
    public ShuntCompensatorImpl getShuntCompensator(String id) {
        return objectStore.get(id, ShuntCompensatorImpl.class);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Collections.unmodifiableCollection(objectStore.getAll(DanglingLineImpl.class));
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return objectStore.getAll(DanglingLineImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getDanglingLineCount() {
        return objectStore.getAll(DanglingLineImpl.class).size();
    }

    @Override
    public DanglingLineImpl getDanglingLine(String id) {
        return objectStore.get(id, DanglingLineImpl.class);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Collections.unmodifiableCollection(objectStore.getAll(StaticVarCompensatorImpl.class));
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return objectStore.getAll(StaticVarCompensatorImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return objectStore.getAll(StaticVarCompensatorImpl.class).size();
    }

    @Override
    public StaticVarCompensatorImpl getStaticVarCompensator(String id) {
        return objectStore.get(id, StaticVarCompensatorImpl.class);
    }

    @Override
    public Switch getSwitch(String id) {
        return objectStore.get(id, SwitchImpl.class);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Collections.unmodifiableCollection(objectStore.getAll(SwitchImpl.class));
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return objectStore.getAll(SwitchImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getSwitchCount() {
        return objectStore.getAll(SwitchImpl.class).size();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return objectStore.get(id, BusbarSectionImpl.class);
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return Collections.unmodifiableCollection(objectStore.getAll(BusbarSectionImpl.class));
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return objectStore.getAll(BusbarSectionImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getBusbarSectionCount() {
        return objectStore.getAll(BusbarSectionImpl.class).size();
    }

    @Override
    public AbstractHvdcConverterStation<?> getHvdcConverterStation(String id) {
        AbstractHvdcConverterStation<?> converterStation = getLccConverterStation(id);
        if (converterStation == null) {
            converterStation = getVscConverterStation(id);
        }
        return converterStation;
    }

    @Override
    public int getHvdcConverterStationCount() {
        return getLccConverterStationCount() + getVscConverterStationCount();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return Iterables.concat(getLccConverterStations(), getVscConverterStations());
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return Stream.concat(getLccConverterStationStream(), getVscConverterStationStream());
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Collections.unmodifiableCollection(objectStore.getAll(LccConverterStationImpl.class));
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return objectStore.getAll(LccConverterStationImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getLccConverterStationCount() {
        return objectStore.getAll(LccConverterStationImpl.class).size();
    }

    @Override
    public LccConverterStationImpl getLccConverterStation(String id) {
        return objectStore.get(id, LccConverterStationImpl.class);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Collections.unmodifiableCollection(objectStore.getAll(VscConverterStationImpl.class));
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return objectStore.getAll(VscConverterStationImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getVscConverterStationCount() {
        return objectStore.getAll(VscConverterStationImpl.class).size();
    }

    @Override
    public VscConverterStationImpl getVscConverterStation(String id) {
        return objectStore.get(id, VscConverterStationImpl.class);
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return objectStore.get(id, HvdcLineImpl.class);
    }

    @Override
    public int getHvdcLineCount() {
        return objectStore.getAll(HvdcLineImpl.class).size();
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return Collections.unmodifiableCollection(objectStore.getAll(HvdcLineImpl.class));
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return objectStore.getAll(HvdcLineImpl.class).stream().map(Function.identity());
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        return new HvdcLineAdderImpl(ref);
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        return objectStore.get(id, Identifiable.class);
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return objectStore.getAll();
    }

    @Override
    public BusBreakerViewImpl getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusViewImpl getBusView() {
        return busView;
    }

    private abstract static class AbstractComponentsManager<C extends Component> {

        protected final NetworkImpl network;

        private AbstractComponentsManager(NetworkImpl network) {
            this.network = Objects.requireNonNull(network);
        }

        private List<C> components;

        void invalidate() {
            components = null;
        }

        protected void addToAdjacencyList(Bus bus1, Bus bus2, Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
            if (bus1 != null && bus2 != null) {
                int busNum1 = id2num.get(bus1.getId());
                int busNum2 = id2num.get(bus2.getId());
                adjacencyList[busNum1].add(busNum2);
                adjacencyList[busNum2].add(busNum1);
            }
        }

        protected void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
            for (LineImpl line : Sets.union(network.objectStore.getAll(LineImpl.class), network.objectStore.getAll(TieLineImpl.class))) {
                BusExt bus1 = line.getTerminal1().getBusView().getBus();
                BusExt bus2 = line.getTerminal2().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
            }
            for (TwoWindingsTransformerImpl transfo : network.objectStore.getAll(TwoWindingsTransformerImpl.class)) {
                BusExt bus1 = transfo.getTerminal1().getBusView().getBus();
                BusExt bus2 = transfo.getTerminal2().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
            }
            for (ThreeWindingsTransformerImpl transfo : network.objectStore.getAll(ThreeWindingsTransformerImpl.class)) {
                BusExt bus1 = transfo.getLeg1().getTerminal().getBusView().getBus();
                BusExt bus2 = transfo.getLeg2().getTerminal().getBusView().getBus();
                BusExt bus3 = transfo.getLeg3().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
                addToAdjacencyList(bus1, bus3, id2num, adjacencyList);
                addToAdjacencyList(bus2, bus3, id2num, adjacencyList);
            }
        }

        protected abstract C createComponent(int num, int size);

        protected abstract String getComponentLabel();

        protected abstract void setComponentNumber(BusExt bus, int num);

        void update() {
            if (components != null) {
                return;
            }

            long startTime = System.currentTimeMillis();

            // reset
            for (Bus b : network.getBusBreakerView().getBuses()) {
                setComponentNumber((BusExt) b, -1);
            }

            int num = 0;
            Map<String, Integer> id2num = new HashMap<>();
            List<BusExt> num2bus = new ArrayList<>();
            for (Bus bus : network.getBusView().getBuses()) {
                num2bus.add((BusExt) bus);
                id2num.put(bus.getId(), num);
                num++;
            }
            TIntArrayList[] adjacencyList = new TIntArrayList[num];
            for (int i = 0; i < adjacencyList.length; i++) {
                adjacencyList[i] = new TIntArrayList(3);
            }
            fillAdjacencyList(id2num, adjacencyList);

            ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);

            components = new ArrayList<>(result.getComponentSize().length);
            for (int i = 0; i < result.getComponentSize().length; i++) {
                components.add(createComponent(i, result.getComponentSize()[i]));
            }

            for (int i = 0; i < result.getComponentNumber().length; i++) {
                BusExt bus = num2bus.get(i);
                setComponentNumber(bus, result.getComponentNumber()[i]);
            }

            LOGGER.debug("{} components computed in {} ms", getComponentLabel(), System.currentTimeMillis() - startTime);
        }

        List<C> getConnectedComponents() {
            update();
            return components;
        }

        C getComponent(int num) {
            // update() must not be put here, but explicitly called each time before because update may
            // trigger a new component computation and so on a change in the value of the num component already passed
            // (and outdated consequently) in parameter of this method
            return num != -1 ? components.get(num) : null;
        }

    }

    static final class ConnectedComponentsManager extends AbstractComponentsManager<ConnectedComponentImpl> {

        private ConnectedComponentsManager(NetworkImpl network) {
            super(network);
        }

        @Override
        protected void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
            super.fillAdjacencyList(id2num, adjacencyList);
            for (HvdcLineImpl line : network.objectStore.getAll(HvdcLineImpl.class)) {
                BusExt bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
                BusExt bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
            }
        }

        @Override
        protected String getComponentLabel() {
            return "Connected";
        }

        @Override
        protected void setComponentNumber(BusExt bus, int num) {
            Objects.requireNonNull(bus);
            bus.setConnectedComponentNumber(num);
        }

        protected ConnectedComponentImpl createComponent(int num, int size) {
            return new ConnectedComponentImpl(num, size, network.ref);
        }
    }

    static final class SynchronousComponentsManager extends AbstractComponentsManager<ComponentImpl> {

        private SynchronousComponentsManager(NetworkImpl network) {
            super(network);
        }

        protected ComponentImpl createComponent(int num, int size) {
            return new ComponentImpl(num, size, network.ref);
        }

        @Override
        protected String getComponentLabel() {
            return "Synchronous";
        }

        @Override
        protected void setComponentNumber(BusExt bus, int num) {
            Objects.requireNonNull(bus);
            bus.setSynchronousComponentNumber(num);
        }
    }

    private class StateImpl implements State {

        private final ConnectedComponentsManager connectedComponentsManager
                = new ConnectedComponentsManager(NetworkImpl.this);

        private final SynchronousComponentsManager synchronousComponentsManager
                = new SynchronousComponentsManager(NetworkImpl.this);

        @Override
        public StateImpl copy() {
            return new StateImpl();
        }

    }

    private final StateArray<StateImpl> states;

    ConnectedComponentsManager getConnectedComponentsManager() {
        return states.get().connectedComponentsManager;
    }

    SynchronousComponentsManager getSynchronousComponentsManager() {
        return states.get().synchronousComponentsManager;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, final int sourceIndex) {
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
    protected String getTypeDescription() {
        return "Network";
    }

    private void setIdEraseName(String id) {
        objectStore.remove(this);
        this.id = id;
        name = null; // reset the name
        objectStore.checkAndAdd(this);
    }

    @Override
    public void merge(Network other) {
        NetworkImpl otherNetwork = (NetworkImpl) other;

        // this check must not be done on the number of state but on the size
        // of the internal state array because the network can have only
        // one state but an internal array with a size greater that one and
        // some re-usable states
        if (stateManager.getStateArraySize() != 1 || otherNetwork.stateManager.getStateArraySize() != 1) {
            throw new PowsyblException("Merging of multi-states network is not supported");
        }

        long start = System.currentTimeMillis();

        // check mergeability
        Multimap<Class<? extends Identifiable>, String> intersection = objectStore.intersection(otherNetwork.objectStore);
        for (Map.Entry<Class<? extends Identifiable>, Collection<String>> entry : intersection.asMap().entrySet()) {
            Class<? extends Identifiable> clazz = entry.getKey();
            if (clazz == DanglingLineImpl.class) { // fine for dangling lines
                continue;
            }
            Collection<String> objs = entry.getValue();
            if (!objs.isEmpty()) {
                throw new PowsyblException("The following object(s) of type "
                        + clazz.getSimpleName() + " exist(s) in both networks: "
                        + objs);
            }
        }

        // try to find dangling lines couples
        Map<String, DanglingLine> dl1byXnodeCode = new HashMap<>();
        for (DanglingLine dl1 : getDanglingLines()) {
            if (dl1.getUcteXnodeCode() != null) {
                dl1byXnodeCode.put(dl1.getUcteXnodeCode(), dl1);
            }
        }
        List<MergedLine> lines = new ArrayList<>();
        for (DanglingLine dl2 : Lists.newArrayList(other.getDanglingLines())) {
            DanglingLine dl1 = getDanglingLineByTheOther(dl2, dl1byXnodeCode);
            mergeDanglingLines(lines, dl1, dl2);
        }

        // do not forget to remove the other network from its store!!!
        otherNetwork.objectStore.remove(otherNetwork);

        // merge the stores
        objectStore.merge(otherNetwork.objectStore);

        // fix network back reference of the other network objects
        otherNetwork.ref.setRef(ref);

        Multimap<Boundary, MergedLine> mergedLineByBoundary = HashMultimap.create();
        replaceDanglingLineByLine(lines, mergedLineByBoundary);

        if (!lines.isEmpty()) {
            LOGGER.info("{} dangling line couples have been replaced by a line: {}", lines.size(),
                    mergedLineByBoundary.asMap().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().size()).collect(Collectors.toList()));
        }

        // update the source format
        if (!sourceFormat.equals(otherNetwork.sourceFormat)) {
            sourceFormat = "hybrid";
        }

        // change the network id
        setIdEraseName(getId() + " + " + otherNetwork.getId());

        LOGGER.info("Merging of {} done in {} ms", id, System.currentTimeMillis() - start);
    }

    private DanglingLine getDanglingLineByTheOther(DanglingLine dl2, Map<String, DanglingLine> dl1byXnodeCode) {
        DanglingLine dl1 = getDanglingLine(dl2.getId());
        if (dl1 == null) {
            // mapping by ucte xnode code
            if (dl2.getUcteXnodeCode() != null) {
                dl1 = dl1byXnodeCode.get(dl2.getUcteXnodeCode());
            }
        } else {
            // mapping by id
            if (dl1.getUcteXnodeCode() != null && dl2.getUcteXnodeCode() != null
                    && !dl1.getUcteXnodeCode().equals(dl2.getUcteXnodeCode())) {
                throw new PowsyblException("Dangling line couple " + dl1.getId()
                        + " have inconsistent Xnodes (" + dl1.getUcteXnodeCode()
                        + "!=" + dl2.getUcteXnodeCode() + ")");
            }
        }
        return dl1;
    }

    private void mergeDanglingLines(List<MergedLine> lines, DanglingLine dl1, DanglingLine dl2) {
        if (dl1 != null) {
            MergedLine l = new MergedLine();
            l.id = dl1.getId().compareTo(dl2.getId()) < 0 ? dl1.getId() + " + " + dl2.getId() : dl2.getId() + " + " + dl1.getId();
            Terminal t1 = dl1.getTerminal();
            Terminal t2 = dl2.getTerminal();
            VoltageLevel vl1 = t1.getVoltageLevel();
            VoltageLevel vl2 = t2.getVoltageLevel();
            l.voltageLevel1 = vl1.getId();
            l.voltageLevel2 = vl2.getId();
            l.xnode = dl1.getUcteXnodeCode();
            l.half1.id = dl1.getId();
            l.half1.name = dl1.getName();
            l.half1.r = dl1.getR();
            l.half1.x = dl1.getX();
            l.half1.g1 = dl1.getG();
            l.half1.g2 = 0;
            l.half1.b1 = dl1.getB();
            l.half1.b2 = 0;
            l.half1.xnodeP = dl1.getP0();
            l.half1.xnodeQ = dl1.getQ0();
            l.half2.id = dl2.getId();
            l.half2.name = dl2.getName();
            l.half2.r = dl2.getR();
            l.half2.x = dl2.getX();
            l.half2.g1 = dl2.getG();
            l.half2.g2 = 0;
            l.half2.b1 = dl2.getB();
            l.half2.b2 = 0;
            l.half2.xnodeP = dl2.getP0();
            l.half2.xnodeQ = dl2.getQ0();
            l.limits1 = dl1.getCurrentLimits();
            l.limits2 = dl2.getCurrentLimits();
            if (t1.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
                Bus b1 = t1.getBusBreakerView().getBus();
                if (b1 != null) {
                    l.bus1 = b1.getId();
                }
                l.connectableBus1 = t1.getBusBreakerView().getConnectableBus().getId();
            } else {
                l.node1 = t1.getNodeBreakerView().getNode();
            }
            if (t2.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
                Bus b2 = t2.getBusBreakerView().getBus();
                if (b2 != null) {
                    l.bus2 = b2.getId();
                }
                l.connectableBus2 = t2.getBusBreakerView().getConnectableBus().getId();
            } else {
                l.node2 = t2.getNodeBreakerView().getNode();
            }
            l.p1 = t1.getP();
            l.q1 = t1.getQ();
            l.p2 = t2.getP();
            l.q2 = t2.getQ();
            l.country1 = vl1.getSubstation().getCountry();
            l.country2 = vl2.getSubstation().getCountry();
            lines.add(l);

            // remove the 2 dangling lines
            dl1.remove();
            dl2.remove();
        }
    }

    private void replaceDanglingLineByLine(List<MergedLine> lines, Multimap<Boundary, MergedLine> mergedLineByBoundary) {
        for (MergedLine mergedLine : lines) {
            LOGGER.debug("Replacing dangling line couple '{}' (xnode={}, country1={}, country2={}) by a line",
                    mergedLine.id, mergedLine.xnode, mergedLine.country1, mergedLine.country2);
            TieLineAdderImpl la = newTieLine()
                    .setId(mergedLine.id)
                    .setVoltageLevel1(mergedLine.voltageLevel1)
                    .setVoltageLevel2(mergedLine.voltageLevel2)
                    .line1().setId(mergedLine.half1.id)
                            .setName(mergedLine.half1.name)
                            .setR(mergedLine.half1.r)
                            .setX(mergedLine.half1.x)
                            .setG1(mergedLine.half1.g1)
                            .setG2(mergedLine.half1.g2)
                            .setB1(mergedLine.half1.b1)
                            .setB2(mergedLine.half1.b2)
                            .setXnodeP(mergedLine.half1.xnodeP)
                            .setXnodeQ(mergedLine.half1.xnodeQ)
                    .line2().setId(mergedLine.half2.id)
                            .setName(mergedLine.half2.name)
                            .setR(mergedLine.half2.r)
                            .setX(mergedLine.half2.x)
                            .setG1(mergedLine.half2.g1)
                            .setG2(mergedLine.half2.g2)
                            .setB1(mergedLine.half2.b1)
                            .setB2(mergedLine.half2.b2)
                            .setXnodeP(mergedLine.half2.xnodeP)
                            .setXnodeQ(mergedLine.half2.xnodeQ)
                    .setUcteXnodeCode(mergedLine.xnode);
            if (mergedLine.bus1 != null) {
                la.setBus1(mergedLine.bus1);
            }
            la.setConnectableBus1(mergedLine.connectableBus1);
            if (mergedLine.bus2 != null) {
                la.setBus2(mergedLine.bus2);
            }
            la.setConnectableBus2(mergedLine.connectableBus2);
            if (mergedLine.node1 != null) {
                la.setNode1(mergedLine.node1);
            }
            if (mergedLine.node2 != null) {
                la.setNode2(mergedLine.node2);
            }
            TieLineImpl l = la.add();
            l.setCurrentLimits(Side.ONE, (CurrentLimitsImpl) mergedLine.limits1);
            l.setCurrentLimits(Side.TWO, (CurrentLimitsImpl) mergedLine.limits2);
            l.getTerminal1().setP(mergedLine.p1).setQ(mergedLine.q1);
            l.getTerminal2().setP(mergedLine.p2).setQ(mergedLine.q2);

            mergedLineByBoundary.put(new Boundary(mergedLine.country1, mergedLine.country2), mergedLine);
        }
    }

    class MergedLine {
        String id;
        String voltageLevel1;
        String voltageLevel2;
        String xnode;
        String bus1;
        String bus2;
        String connectableBus1;
        String connectableBus2;
        Integer node1;
        Integer node2;

        class HalfMergedLine {
            String id;
            String name;
            double r;
            double x;
            double g1;
            double g2;
            double b1;
            double b2;
            double xnodeP;
            double xnodeQ;
        }

        final HalfMergedLine half1 = new HalfMergedLine();
        final HalfMergedLine half2 = new HalfMergedLine();

        CurrentLimits limits1;
        CurrentLimits limits2;
        double p1;
        double q1;
        double p2;
        double q2;

        Country country1;
        Country country2;
    }

    @Override
    public void merge(Network... others) {
        for (Network other : others) {
            merge(other);
        }
    }

    @Override
    public void addListener(NetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }
}
