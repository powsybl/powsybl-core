/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import com.google.common.collect.*;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.impl.util.RefObj;
import eu.itesla_project.iidm.network.impl.util.RefChain;
import com.google.common.base.Function;
import eu.itesla_project.graph.GraphUtil;
import eu.itesla_project.graph.GraphUtil.ConnectedComponentsComputationResult;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable.Side;
import gnu.trove.list.array.TIntArrayList;
import java.util.*;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NetworkImpl extends IdentifiableImpl<Network> implements Network, MultiStateObject, Stateful {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkImpl.class);

    private static final Function<VoltageLevel, Iterable<Bus>> toBusBreakerViewBuses = new Function<VoltageLevel, Iterable<Bus>>() {
        @Override
        public Iterable<Bus> apply(VoltageLevel vl) {
            return vl.getBusBreakerView().getBuses();
        }
    };

    private static final Function<VoltageLevel, Iterable<Switch>> toBusBreakerViewSwitches = new Function<VoltageLevel, Iterable<Switch>>() {
        @Override
        public Iterable<Switch> apply(VoltageLevel vl) {
            return vl.getBusBreakerView().getSwitches();
        }
    };

    private static final Function<VoltageLevel, Iterable<Bus>> toBusViewBuses = new Function<VoltageLevel, Iterable<Bus>>() {
        @Override
        public Iterable<Bus> apply(VoltageLevel vl) {
            return vl.getBusView().getBuses();
        }
    };

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
                                 .transformAndConcat(toBusBreakerViewBuses);
        }

        @Override
        public Iterable<Switch> getSwitchs() {
            return FluentIterable.from(getVoltageLevels())
                                 .transformAndConcat(toBusBreakerViewSwitches);
        }

    }

    private final BusBreakerViewImpl busBreakerView = new BusBreakerViewImpl();

    class BusViewImpl implements BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                                 .transformAndConcat(toBusViewBuses);
        }

        @Override
        public Collection<ConnectedComponent> getConnectedComponents() {
            return Collections.unmodifiableList(states.get().connectedComponentsManager.getConnectedComponents());
        }

    }

    private final BusViewImpl busView = new BusViewImpl();

    NetworkImpl(String id, String name, String sourceFormat) {
        super(id, name);
        Objects.requireNonNull(sourceFormat, "source format is null");
        this.sourceFormat = sourceFormat;
        stateManager = new StateManagerImpl(objectStore);
        states = new StateArray<>(ref, new StateFactory<StateImpl>() {
            @Override
            public StateImpl newState() {
                return new StateImpl();
            }
        });
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
        return FluentIterable.from(getSubstations()).transform(s -> s.getCountry()).toSet();
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
    public int getVoltageLevelCount() {
        return objectStore.getAll(BusBreakerVoltageLevel.class).size()
                + objectStore.getAll(NodeBreakerVoltageLevel.class).size() ;
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
    public int getLoadCount() {
        return objectStore.getAll(LoadImpl.class).size();
    }

    @Override
    public LoadImpl getLoad(String id) {
        return objectStore.get(id, LoadImpl.class);
    }

    @Override
    public Iterable<ShuntCompensator> getShunts() {
        return Collections.unmodifiableCollection(objectStore.getAll(ShuntCompensatorImpl.class));
    }

    @Override
    public int getShuntCount() {
        return objectStore.getAll(ShuntCompensatorImpl.class).size();
    }

    @Override
    public ShuntCompensatorImpl getShunt(String id) {
        return objectStore.get(id, ShuntCompensatorImpl.class);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Collections.unmodifiableCollection(objectStore.getAll(DanglingLineImpl.class));
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
    public HvdcConverterStationImpl<?> getHvdcConverterStation(String id) {
        HvdcConverterStationImpl<?> converterStation = objectStore.get(id, LccConverterStationImpl.class);
        if (converterStation == null) {
            converterStation = objectStore.get(id, VscConverterStationImpl.class);
        }
        return converterStation;
    }

    @Override
    public int getHvdcConverterStationCount() {
        return objectStore.getAll(LccConverterStationImpl.class).size() + objectStore.getAll(VscConverterStationImpl.class).size();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return Iterables.concat(objectStore.getAll(LccConverterStationImpl.class), objectStore.getAll(VscConverterStationImpl.class));
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

    class ConnectedComponentsManager {

        private List<ConnectedComponent> connectedComponents;

        void invalidate() {
            connectedComponents = null;
        }

        void update() {
            if (connectedComponents != null) {
                return;
            }

            long startTime = System.currentTimeMillis();

            // reset
            for (Bus b : getBusBreakerView().getBuses()) {
                ((BusExt) b).setConnectedComponentNumber(-1);
            }

            int num = 0;
            Map<String, Integer> id2num = new HashMap<>();
            List<BusExt> num2bus = new ArrayList<>();
            for (Bus bus : getBusView().getBuses()) {
                num2bus.add((BusExt) bus);
                id2num.put(bus.getId(), num);
                num++;
            }
            TIntArrayList[] adjacencyList = new TIntArrayList[num];
            for (int i = 0; i < adjacencyList.length; i++) {
                adjacencyList[i] = new TIntArrayList(3);
            }
            for (LineImpl line : Sets.union(objectStore.getAll(LineImpl.class), objectStore.getAll(TieLineImpl.class))) {
                BusExt bus1 = line.getTerminal1().getBusView().getBus();
                BusExt bus2 = line.getTerminal2().getBusView().getBus();
                if (bus1 != null && bus2 != null) {
                    int busNum1 = id2num.get(bus1.getId());
                    int busNum2 = id2num.get(bus2.getId());
                    adjacencyList[busNum1].add(busNum2);
                    adjacencyList[busNum2].add(busNum1);
                }
            }
            for (TwoWindingsTransformerImpl transfo : objectStore.getAll(TwoWindingsTransformerImpl.class)) {
                BusExt bus1 = transfo.getTerminal1().getBusView().getBus();
                BusExt bus2 = transfo.getTerminal2().getBusView().getBus();
                if (bus1 != null && bus2 != null) {
                    int busNum1 = id2num.get(bus1.getId());
                    int busNum2 = id2num.get(bus2.getId());
                    adjacencyList[busNum1].add(busNum2);
                    adjacencyList[busNum2].add(busNum1);
                }
            }
            for (ThreeWindingsTransformerImpl transfo : objectStore.getAll(ThreeWindingsTransformerImpl.class)) {
                BusExt bus1 = transfo.getLeg1().getTerminal().getBusView().getBus();
                BusExt bus2 = transfo.getLeg2().getTerminal().getBusView().getBus();
                BusExt bus3 = transfo.getLeg3().getTerminal().getBusView().getBus();
                if (bus1 != null && bus2 != null) {
                    int busNum1 = id2num.get(bus1.getId());
                    int busNum2 = id2num.get(bus2.getId());
                    adjacencyList[busNum1].add(busNum2);
                    adjacencyList[busNum2].add(busNum1);
                }
                if (bus1 != null && bus3 != null) {
                    int busNum1 = id2num.get(bus1.getId());
                    int busNum3 = id2num.get(bus3.getId());
                    adjacencyList[busNum1].add(busNum3);
                    adjacencyList[busNum3].add(busNum1);
                }
                if (bus2 != null && bus3 != null) {
                    int busNum2 = id2num.get(bus2.getId());
                    int busNum3 = id2num.get(bus3.getId());
                    adjacencyList[busNum2].add(busNum3);
                    adjacencyList[busNum3].add(busNum2);
                }
            }

            ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);

            connectedComponents = new ArrayList<>(result.getComponentSize().length);
            for (int i = 0; i < result.getComponentSize().length; i++) {
                connectedComponents.add(new ConnectedComponentImpl(i, result.getComponentSize()[i], ref));
            }

            for (int i = 0; i < result.getComponentNumber().length; i++) {
                BusExt bus = num2bus.get(i);
                bus.setConnectedComponentNumber(result.getComponentNumber()[i]);
            }

            LOGGER.debug("Connected components computed in {} ms", (System.currentTimeMillis()-startTime));
        }

        List<ConnectedComponent> getConnectedComponents() {
            update();
            return connectedComponents;
        }

        ConnectedComponent getConnectedComponent(int num) {
            // update() must not be put here, but explicitly called each time before because update may
            // trigger a new cc computation and so on a change in the value of the num cc already passed
            // (and outdated consequently) in parameter of this method
            return num != -1 ? connectedComponents.get(num) : null;
        }

    }

    private class StateImpl implements State {

        private final ConnectedComponentsManager connectedComponentsManager
                = new ConnectedComponentsManager();

        @Override
        public StateImpl copy() {
            return new StateImpl();
        }

    }

    private final StateArray<StateImpl> states;

    ConnectedComponentsManager getConnectedComponentsManager() {
        return states.get().connectedComponentsManager;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, final int sourceIndex) {
        states.push(number, new StateFactory<StateImpl>() {
            @Override
            public StateImpl newState() {
                return states.copy(sourceIndex);
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
                return states.copy(sourceIndex);
            }
        });
    }

    @Override
    protected String getTypeDescription() {
        return "Network";
    }

    private void setId(String id) {
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
            throw new RuntimeException("Merging of multi-states network is not supported");
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
            if (objs.size() > 0) {
                throw new RuntimeException("The following object(s) of type "
                        + clazz.getSimpleName() + " exist(s) in both networks: "
                        + objs);
            }
        }

        class LineMerge {
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

            class HalfLineMerge {
                String id;
                String name;
                float r;
                float x;
                float g1;
                float g2;
                float b1;
                float b2;
                float xnodeP;
                float xnodeQ;
            }

            final HalfLineMerge half1 = new HalfLineMerge();
            final HalfLineMerge half2 = new HalfLineMerge();

            CurrentLimits limits1;
            CurrentLimits limits2;
            float p1;
            float q1;
            float p2;
            float q2;

            Country country1;
            Country country2;
        }

        // try to find dangling lines couples
        Map<String, DanglingLine> dl1byXnodeCode = new HashMap<>();
        for (DanglingLine dl1 : getDanglingLines()) {
            if (dl1.getUcteXnodeCode() != null) {
                dl1byXnodeCode.put(dl1.getUcteXnodeCode(), dl1);
            }
        }
        List<LineMerge> lines = new ArrayList<>();
        for (DanglingLine dl2 : Lists.newArrayList(other.getDanglingLines())) {
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
                    throw new RuntimeException("Dangling line couple " + dl1.getId()
                            + " have inconsistent Xnodes (" + dl1.getUcteXnodeCode()
                            + "!=" + dl2.getUcteXnodeCode() + ")");
                }
            }
            if (dl1 != null) {
                LineMerge l = new LineMerge();
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

        // do not forget to remove the other network from its store!!!
        otherNetwork.objectStore.remove(otherNetwork);

        // merge the stores
        objectStore.merge(otherNetwork.objectStore);

        // fix network back reference of the other network objects
        otherNetwork.ref.setRef(ref);

        Multimap<Boundary, LineMerge> mergedLineByBoundary = HashMultimap.create();
        for (LineMerge lm:  lines) {
            LOGGER.debug("Replacing dangling line couple '{}' (xnode={}, country1={}, country2={}) by a line",
                    lm.id, lm.xnode, lm.country1, lm.country2);
            TieLineAdderImpl la = newTieLine()
                    .setId(lm.id)
                    .setVoltageLevel1(lm.voltageLevel1)
                    .setVoltageLevel2(lm.voltageLevel2)
                    .line1().setId(lm.half1.id)
                            .setName(lm.half1.name)
                            .setR(lm.half1.r)
                            .setX(lm.half1.x)
                            .setG1(lm.half1.g1)
                            .setG2(lm.half1.g2)
                            .setB1(lm.half1.b1)
                            .setB2(lm.half1.b2)
                            .setXnodeP(lm.half1.xnodeP)
                            .setXnodeQ(lm.half1.xnodeQ)
                    .line2().setId(lm.half2.id)
                            .setName(lm.half2.name)
                            .setR(lm.half2.r)
                            .setX(lm.half2.x)
                            .setG1(lm.half2.g1)
                            .setG2(lm.half2.g2)
                            .setB1(lm.half2.b1)
                            .setB2(lm.half2.b2)
                            .setXnodeP(lm.half2.xnodeP)
                            .setXnodeQ(lm.half2.xnodeQ)
                    .setUcteXnodeCode(lm.xnode);
            if (lm.bus1 != null) {
                la.setBus1(lm.bus1);
            }
            la.setConnectableBus1(lm.connectableBus1);
            if (lm.bus2 != null) {
                la.setBus2(lm.bus2);
            }
            la.setConnectableBus2(lm.connectableBus2);
            if (lm.node1 != null) {
                la.setNode1(lm.node1);
            }
            if (lm.node2 != null) {
                la.setNode2(lm.node2);
            }
            TieLineImpl l = la.add();
            l.setCurrentLimits(Side.ONE, (CurrentLimitsImpl) lm.limits1);
            l.setCurrentLimits(Side.TWO, (CurrentLimitsImpl) lm.limits2);
            l.getTerminal1().setP(lm.p1).setQ(lm.q1);
            l.getTerminal2().setP(lm.p2).setQ(lm.q2);

            mergedLineByBoundary.put(new Boundary(lm.country1, lm.country2), lm);
        }

        if (lines.size() > 0) {
            LOGGER.info("{} dangling line couples have been replaced by a line: {}", lines.size(),
                    mergedLineByBoundary.asMap().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().size()).collect(Collectors.toList()));
        }

        // update the source format
        if (!sourceFormat.equals(otherNetwork.sourceFormat)) {
            sourceFormat = "hybrid";
        }

        // change the network id
        setId(getId() + " + " + otherNetwork.getId());

        LOGGER.info("Merging of {} done in {} ms", id, (System.currentTimeMillis() - start));
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
