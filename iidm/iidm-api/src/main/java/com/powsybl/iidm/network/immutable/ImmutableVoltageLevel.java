/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ImmutableVoltageLevel extends AbstractImmutableIdentifiable<VoltageLevel> implements VoltageLevel {

    private final ImmutableCacheIndex cache;

    ImmutableVoltageLevel(VoltageLevel identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public Substation getSubstation() {
        return cache.getSubstation(identifiable.getSubstation());
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
        return (T) cache.getConnectable(identifiable.getConnectable(id, aClass));
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        return Iterables.transform(identifiable.getConnectables(clazz), c -> (T) cache.getConnectable(c));
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return identifiable.getConnectableStream(clazz).map(c -> (T) cache.getConnectable(c));
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return identifiable.getConnectableCount(clazz);
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return Iterables.transform(identifiable.getConnectables(), cache::getConnectable);
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return identifiable.getConnectableStream().map(cache::getConnectable);
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
        return Iterables.transform(identifiable.getGenerators(), cache::getGenerator);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(cache::getGenerator);
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
        return Iterables.transform(identifiable.getLoads(), cache::getLoad);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(cache::getLoad);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(identifiable.getSwitches(), cache::getSwitch);
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
        return Iterables.transform(identifiable.getShuntCompensators(), cache::getShuntCompensator);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(cache::getShuntCompensator);
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
        return Iterables.transform(identifiable.getDanglingLines(), cache::getDanglingLine);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(cache::getDanglingLine);
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
        return Iterables.transform(identifiable.getStaticVarCompensators(), cache::getStaticVarCompensator);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(cache::getStaticVarCompensator);
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
        return Iterables.transform(identifiable.getVscConverterStations(), cache::getVscConverterStation);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(cache::getVscConverterStation);
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
        return Iterables.transform(identifiable.getLccConverterStations(), cache::getLccConverterStation);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(cache::getLccConverterStation);
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
            public int getInternalConnectionCount() {
                return nbv.getInternalConnectionCount();
            }

            @Override
            public Iterable<InternalConnection> getInternalConnections() {
                return nbv.getInternalConnections();
            }

            @Override
            public Stream<InternalConnection> getInternalConnectionStream() {
                return nbv.getInternalConnectionStream();
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
                return cache.getTerminal(nbv.getTerminal(node));
            }

            @Override
            public Terminal getTerminal1(String switchId) {
                return cache.getTerminal(nbv.getTerminal1(switchId));
            }

            @Override
            public Terminal getTerminal2(String switchId) {
                return cache.getTerminal(nbv.getTerminal2(switchId));
            }

            @Override
            public Switch getSwitch(String switchId) {
                return cache.getSwitch(nbv.getSwitch(switchId));
            }

            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(nbv.getSwitches(), cache::getSwitch);
            }

            @Override
            public Stream<Switch> getSwitchStream() {
                return nbv.getSwitchStream().map(cache::getSwitch);
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
                return Iterables.transform(bbv.getBuses(), cache::getBus);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return bbv.getBusStream().map(cache::getBus);
            }

            @Override
            public Bus getBus(String id) {
                return cache.getBus(bbv.getBus(id));
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
                return Iterables.transform(bbv.getSwitches(), cache::getSwitch);
            }

            @Override
            public Stream<Switch> getSwitchStream() {
                return bbv.getSwitchStream().map(cache::getSwitch);
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
                return cache.getBus(bbv.getBus1(switchId));
            }

            @Override
            public Bus getBus2(String switchId) {
                return cache.getBus(bbv.getBus2(switchId));
            }

            @Override
            public Switch getSwitch(String switchId) {
                return cache.getSwitch(bbv.getSwitch(switchId));
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
                return Iterables.transform(busView.getBuses(), cache::getBus);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return busView.getBusStream().map(cache::getBus);
            }

            @Override
            public Bus getBus(String id) {
                return cache.getBus(busView.getBus(id));
            }

            @Override
            public Bus getMergedBus(String configuredBusId) {
                return cache.getBus(busView.getMergedBus(configuredBusId));
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
    public void exportTopology(Path file) throws IOException {
        identifiable.exportTopology(file);
    }

    @Override
    public void exportTopology(Writer writer, Random random) throws IOException {
        identifiable.exportTopology(writer, random);
    }

    @Override
    public void exportTopology(Writer writer) throws IOException {
        identifiable.exportTopology(writer);
    }

    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }
}
