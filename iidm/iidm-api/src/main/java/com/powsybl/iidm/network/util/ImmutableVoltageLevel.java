/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ImmutableVoltageLevel extends AbstractImmutableIdentifiable<VoltageLevel> implements VoltageLevel {

    private static final Map<VoltageLevel, ImmutableVoltageLevel> CACHE = new HashMap<>();

    private ImmutableVoltageLevel(VoltageLevel identifiable) {
        super(identifiable);
    }

    static ImmutableVoltageLevel ofNullable(VoltageLevel identifiable) {
        return identifiable == null ? null : CACHE.computeIfAbsent(identifiable, k -> new ImmutableVoltageLevel(identifiable));
    }

    @Override
    public Substation getSubstation() {
        return ImmutableSubstation.ofNullable(identifiable.getSubstation());
    }

    @Override
    public double getNominalV() {
        return identifiable.getNominalV();
    }

    @Override
    public VoltageLevel setNominalV(double nominalV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getLowVoltageLimit() {
        return identifiable.getLowVoltageLimit();
    }

    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getHighVoltageLimit() {
        return identifiable.getHighVoltageLimit();
    }

    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> aClass) {
        return (T) ImmutableFactory.ofNullableConnectable(identifiable.getConnectable(id, aClass));
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        return Iterables.transform(identifiable.getConnectables(clazz), c -> (T) ImmutableFactory.ofNullableConnectable(c));
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return identifiable.getConnectableStream(clazz).map(c -> (T) ImmutableFactory.ofNullableConnectable(c));
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return identifiable.getConnectableCount(clazz);
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return Iterables.transform(identifiable.getConnectables(), ImmutableFactory::ofNullableConnectable);
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return identifiable.getConnectableStream().map(ImmutableFactory::ofNullableConnectable);
    }

    @Override
    public int getConnectableCount() {
        return identifiable.getConnectableCount();
    }

    @Override
    public GeneratorAdder newGenerator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), ImmutableGenerator::ofNullable);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(ImmutableGenerator::ofNullable);
    }

    @Override
    public int getGeneratorCount() {
        return identifiable.getGeneratorCount();
    }

    @Override
    public LoadAdder newLoad() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), ImmutableLoad::ofNullable);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(ImmutableLoad::ofNullable);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(identifiable.getSwitches(), ImmutableSwitch::ofNullable);
    }

    @Override
    public int getSwitchCount() {
        return identifiable.getSwitchCount();
    }

    @Override
    public int getLoadCount() {
        return identifiable.getLoadCount();
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), ImmutableShuntCompensator::ofNullable);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(ImmutableShuntCompensator::ofNullable);
    }

    @Override
    public int getShuntCompensatorCount() {
        return identifiable.getShuntCompensatorCount();
    }

    @Override
    public DanglingLineAdder newDanglingLine() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), ImmutableDanglingLine::ofNullable);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(ImmutableDanglingLine::ofNullable);
    }

    @Override
    public int getDanglingLineCount() {
        return identifiable.getDanglingLineCount();
    }

    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), ImmutableStaticVarCompensator::ofNullable);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(ImmutableStaticVarCompensator::ofNullable);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return identifiable.getStaticVarCompensatorCount();
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), ImmutableVscConverterStation::ofNullable);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(ImmutableVscConverterStation::ofNullable);
    }

    @Override
    public int getVscConverterStationCount() {
        return identifiable.getVscConverterStationCount();
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), ImmutableLccConverterStation::ofNullable);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(ImmutableLccConverterStation::ofNullable);
    }

    @Override
    public int getLccConverterStationCount() {
        return identifiable.getLccConverterStationCount();
    }

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        identifiable.visitEquipments(visitor);
    }

    @Override
    public TopologyKind getTopologyKind() {
        return identifiable.getTopologyKind();
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return new NodeBreakerView() {

            private final NodeBreakerView nbv = identifiable.getNodeBreakerView();

            @Override
            public int getNodeCount() {
                return nbv.getNodeCount();
            }

            @Override
            public int[] getNodes() {
                return nbv.getNodes();
            }

            @Override
            public NodeBreakerView setNodeCount(int count) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public SwitchAdder newSwitch() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public InternalConnectionAdder newInternalConnection() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public SwitchAdder newBreaker() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public SwitchAdder newDisconnector() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public int getNode1(String switchId) {
                return nbv.getNode1(switchId);
            }

            @Override
            public int getNode2(String switchId) {
                return nbv.getNode2(switchId);
            }

            @Override
            public Terminal getTerminal(int node) {
                return ImmutableTerminal.ofNullable(nbv.getTerminal(node));
            }

            @Override
            public Terminal getTerminal1(String switchId) {
                return ImmutableTerminal.ofNullable(nbv.getTerminal1(switchId));
            }

            @Override
            public Terminal getTerminal2(String switchId) {
                return ImmutableTerminal.ofNullable(nbv.getTerminal2(switchId));
            }

            @Override
            public Switch getSwitch(String switchId) {
                return ImmutableSwitch.ofNullable(nbv.getSwitch(switchId));
            }

            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(nbv.getSwitches(), ImmutableSwitch::ofNullable);
            }

            @Override
            public Stream<Switch> getSwitchStream() {
                return nbv.getSwitchStream().map(ImmutableSwitch::ofNullable);
            }

            @Override
            public int getSwitchCount() {
                return nbv.getSwitchCount();
            }

            @Override
            public void removeSwitch(String switchId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public BusbarSectionAdder newBusbarSection() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public Iterable<BusbarSection> getBusbarSections() {
                return nbv.getBusbarSections();
            }

            @Override
            public Stream<BusbarSection> getBusbarSectionStream() {
                return nbv.getBusbarSectionStream();
            }

            @Override
            public int getBusbarSectionCount() {
                return nbv.getBusbarSectionCount();
            }

            @Override
            public BusbarSection getBusbarSection(String id) {
                return nbv.getBusbarSection(id);
            }

            @Override
            public void traverse(int node, Traverser traverser) {
                nbv.traverse(node, traverser);
            }
        };
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return new BusBreakerView() {

            private final BusBreakerView bbv = identifiable.getBusBreakerView();

            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(bbv.getBuses(), ImmutableBus::ofNullable);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return bbv.getBusStream().map(ImmutableBus::ofNullable);
            }

            @Override
            public Bus getBus(String id) {
                return ImmutableBus.ofNullable(bbv.getBus(id));
            }

            @Override
            public BusAdder newBus() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public void removeBus(String busId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public void removeAllBuses() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(bbv.getSwitches(), ImmutableSwitch::ofNullable);
            }

            @Override
            public Stream<Switch> getSwitchStream() {
                return bbv.getSwitchStream().map(ImmutableSwitch::ofNullable);
            }

            @Override
            public int getSwitchCount() {
                return bbv.getSwitchCount();
            }

            @Override
            public void removeSwitch(String switchId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public void removeAllSwitches() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public Bus getBus1(String switchId) {
                return ImmutableBus.ofNullable(bbv.getBus1(switchId));
            }

            @Override
            public Bus getBus2(String switchId) {
                return ImmutableBus.ofNullable(bbv.getBus2(switchId));
            }

            @Override
            public Switch getSwitch(String switchId) {
                return ImmutableSwitch.ofNullable(bbv.getSwitch(switchId));
            }

            @Override
            public SwitchAdder newSwitch() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    @Override
    public BusView getBusView() {
        return new BusView() {

            final BusView busView = identifiable.getBusView();
            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(busView.getBuses(), ImmutableBus::ofNullable);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return busView.getBusStream().map(ImmutableBus::ofNullable);
            }

            @Override
            public Bus getBus(String id) {
                return ImmutableBus.ofNullable(busView.getBus(id));
            }

            @Override
            public Bus getMergedBus(String configuredBusId) {
                return ImmutableBus.ofNullable(busView.getMergedBus(configuredBusId));
            }
        };
    }

    @Override
    public void printTopology() {
        identifiable.printTopology();
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        identifiable.printTopology(out, dict);
    }

    @Override
    public void exportTopology(String filename) throws IOException {
        identifiable.exportTopology(filename);
    }

    @Override
    public void exportTopology(OutputStream outputStream) throws IOException {
        identifiable.exportTopology(outputStream);
    }

    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }
}
