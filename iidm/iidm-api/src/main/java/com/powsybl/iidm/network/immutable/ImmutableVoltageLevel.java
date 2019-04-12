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
import java.util.Random;
import java.util.stream.Stream;

/**
 * An immutable {@link VoltageLevel}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
final class ImmutableVoltageLevel extends AbstractImmutableIdentifiable<VoltageLevel> implements VoltageLevel {

    ImmutableVoltageLevel(VoltageLevel identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    /**
     * {@inheritDoc}
     * @return Returns {@link ImmutableSubstation}
     */
    @Override
    public Substation getSubstation() {
        return cache.getSubstation(identifiable.getSubstation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNominalV() {
        return identifiable.getNominalV();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public VoltageLevel setNominalV(double nominalV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLowVoltageLimit() {
        return identifiable.getLowVoltageLimit();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHighVoltageLimit() {
        return identifiable.getHighVoltageLimit();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * Connectables are wrapped in immutable.
     */
    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> aClass) {
        return (T) cache.getConnectable(identifiable.getConnectable(id, aClass));
    }

    /**
     * {@inheritDoc}
     * Connectables are wrapped in immutable.
     */
    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        return Iterables.transform(identifiable.getConnectables(clazz), c -> (T) cache.getConnectable(c));
    }

    /**
     * {@inheritDoc}
     * Connectables are wrapped in immutable.
     */
    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return identifiable.getConnectableStream(clazz).map(c -> (T) cache.getConnectable(c));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return identifiable.getConnectableCount(clazz);
    }

    /**
     * {@inheritDoc}
     * Connectables are wrapped in immutable.
     */
    @Override
    public Iterable<Connectable> getConnectables() {
        return Iterables.transform(identifiable.getConnectables(), cache::getConnectable);
    }

    /**
     * {@inheritDoc}
     * Connectables are wrapped in immutable.
     */
    @Override
    public Stream<Connectable> getConnectableStream() {
        return identifiable.getConnectableStream().map(cache::getConnectable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getConnectableCount() {
        return identifiable.getConnectableCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public GeneratorAdder newGenerator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * Generators are wrapped in {@link ImmutableGenerator}.
     */
    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), cache::getGenerator);
    }

    /**
     * {@inheritDoc}
     * Generators are wrapped in {@link ImmutableGenerator}.
     */
    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(cache::getGenerator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGeneratorCount() {
        return identifiable.getGeneratorCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public BatteryAdder newBattery() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * Batteries are wrapped in {@link ImmutableBattery}.
     */
    @Override
    public Iterable<Battery> getBatteries() {
        return Iterables.transform(identifiable.getBatteries(), cache::getBattery);
    }

    /**
     * {@inheritDoc}
     * Batteries are wrapped in {@link ImmutableBattery}.
     */
    @Override
    public Stream<Battery> getBatteryStream() {
        return identifiable.getBatteryStream().map(cache::getBattery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatteryCount() {
        return identifiable.getBatteryCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public LoadAdder newLoad() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * Loads are wrapped in {@link ImmutableLoad}.
     */
    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), cache::getLoad);
    }

    /**
     * {@inheritDoc}
     * Loads are wrapped in {@link ImmutableLoad}.
     */
    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(cache::getLoad);
    }

    /**
     * {@inheritDoc}
     * Switches are wrapped in {@link ImmutableSwitch}.
     */
    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(identifiable.getSwitches(), cache::getSwitch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSwitchCount() {
        return identifiable.getSwitchCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLoadCount() {
        return identifiable.getLoadCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * ShuntCompensators are wrapped in {@link ImmutableShuntCompensator}.
     */
    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), cache::getShuntCompensator);
    }

    /**
     * {@inheritDoc}
     * ShuntCompensators are wrapped in {@link ImmutableShuntCompensator}.
     */
    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(cache::getShuntCompensator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getShuntCompensatorCount() {
        return identifiable.getShuntCompensatorCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public DanglingLineAdder newDanglingLine() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * DanglingLines are wrapped in {@link ImmutableDanglingLine}.
     */
    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), cache::getDanglingLine);
    }

    /**
     * {@inheritDoc}
     * DanglingLines are wrapped in {@link ImmutableDanglingLine}.
     */
    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(cache::getDanglingLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDanglingLineCount() {
        return identifiable.getDanglingLineCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * StaticVarCompensators are wrapped in {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), cache::getStaticVarCompensator);
    }

    /**
     * {@inheritDoc}
     * StaticVarCompensators are wrapped in {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(cache::getStaticVarCompensator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStaticVarCompensatorCount() {
        return identifiable.getStaticVarCompensatorCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public VscConverterStationAdder newVscConverterStation() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in immutable converter statsion.
     */
    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), cache::getVscConverterStation);
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in immutable converter statsion.
     */
    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(cache::getVscConverterStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVscConverterStationCount() {
        return identifiable.getVscConverterStationCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public LccConverterStationAdder newLccConverterStation() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in immutable converter statsion.
     */
    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), cache::getLccConverterStation);
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in immutable converter statsion.
     */
    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(cache::getLccConverterStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLccConverterStationCount() {
        return identifiable.getLccConverterStationCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        identifiable.visitEquipments(visitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopologyKind getTopologyKind() {
        return identifiable.getTopologyKind();
    }

    /**
     * {@inheritDoc}
     * @return an immutable {@link com.powsybl.iidm.network.VoltageLevel.NodeBreakerView}
     */
    @Override
    public NodeBreakerView getNodeBreakerView() {
        return new NodeBreakerView() {

            private final NodeBreakerView nbv = identifiable.getNodeBreakerView();

            /**
             * {@inheritDoc}
             */
            @Override
            public int getNodeCount() {
                return nbv.getNodeCount();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int[] getNodes() {
                return nbv.getNodes();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public NodeBreakerView setNodeCount(int count) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public SwitchAdder newSwitch() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public InternalConnectionAdder newInternalConnection() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             */
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

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public SwitchAdder newBreaker() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public SwitchAdder newDisconnector() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getNode1(String switchId) {
                return nbv.getNode1(switchId);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getNode2(String switchId) {
                return nbv.getNode2(switchId);
            }

            /**
             * {@inheritDoc}
             * @return Returns an {@link ImmutableTerminal}
             */
            @Override
            public Terminal getTerminal(int node) {
                return cache.getTerminal(nbv.getTerminal(node));
            }

            /**
             * {@inheritDoc}
             * @return Returns an {@link ImmutableTerminal}
             */
            @Override
            public Terminal getTerminal1(String switchId) {
                return cache.getTerminal(nbv.getTerminal1(switchId));
            }

            /**
             * {@inheritDoc}
             * @return Returns an {@link ImmutableTerminal}
             */
            @Override
            public Terminal getTerminal2(String switchId) {
                return cache.getTerminal(nbv.getTerminal2(switchId));
            }

            /**
             * {@inheritDoc}
             * Switch is wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Switch getSwitch(String switchId) {
                return cache.getSwitch(nbv.getSwitch(switchId));
            }

            /**
             * {@inheritDoc}
             * Switches are wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(nbv.getSwitches(), cache::getSwitch);
            }

            /**
             * {@inheritDoc}
             * Switches are wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Stream<Switch> getSwitchStream() {
                return nbv.getSwitchStream().map(cache::getSwitch);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getSwitchCount() {
                return nbv.getSwitchCount();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public void removeSwitch(String switchId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public BusbarSectionAdder newBusbarSection() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public Iterable<BusbarSection> getBusbarSections() {
                return Iterables.transform(nbv.getBusbarSections(), cache::getBusbarSection);
            }

            @Override
            public Stream<BusbarSection> getBusbarSectionStream() {
                return nbv.getBusbarSectionStream().map(cache::getBusbarSection);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getBusbarSectionCount() {
                return nbv.getBusbarSectionCount();
            }

            @Override
            public BusbarSection getBusbarSection(String id) {
                return cache.getBusbarSection(nbv.getBusbarSection(id));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void traverse(int node, Traverser traverser) {
                nbv.traverse(node, traverser);
            }
        };
    }

    /**
     * {@inheritDoc}
     * @return an immutable {@link com.powsybl.iidm.network.VoltageLevel.BusBreakerView}
     */
    @Override
    public BusBreakerView getBusBreakerView() {
        return new BusBreakerView() {

            private final BusBreakerView bbv = identifiable.getBusBreakerView();

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(bbv.getBuses(), cache::getBus);
            }

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Stream<Bus> getBusStream() {
                return bbv.getBusStream().map(cache::getBus);
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getBus(String id) {
                return cache.getBus(bbv.getBus(id));
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public BusAdder newBus() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public void removeBus(String busId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public void removeAllBuses() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             * Switches are wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(bbv.getSwitches(), cache::getSwitch);
            }

            /**
             * {@inheritDoc}
             * Switches are wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Stream<Switch> getSwitchStream() {
                return bbv.getSwitchStream().map(cache::getSwitch);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getSwitchCount() {
                return bbv.getSwitchCount();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public void removeSwitch(String switchId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public void removeAllSwitches() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getBus1(String switchId) {
                return cache.getBus(bbv.getBus1(switchId));
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getBus2(String switchId) {
                return cache.getBus(bbv.getBus2(switchId));
            }

            /**
             * {@inheritDoc}
             * Switch is wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Switch getSwitch(String switchId) {
                return cache.getSwitch(bbv.getSwitch(switchId));
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public SwitchAdder newSwitch() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    /**
     * {@inheritDoc}
     * @return an immutable {@link com.powsybl.iidm.network.VoltageLevel.BusView}
     */
    @Override
    public BusView getBusView() {
        return new BusView() {

            final BusView busView = identifiable.getBusView();

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(busView.getBuses(), cache::getBus);
            }

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Stream<Bus> getBusStream() {
                return busView.getBusStream().map(cache::getBus);
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getBus(String id) {
                return cache.getBus(busView.getBus(id));
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getMergedBus(String configuredBusId) {
                return cache.getBus(busView.getMergedBus(configuredBusId));
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printTopology() {
        identifiable.printTopology();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        identifiable.printTopology(out, dict);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportTopology(Path file) throws IOException {
        identifiable.exportTopology(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportTopology(Writer writer, Random random) throws IOException {
        identifiable.exportTopology(writer, random);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportTopology(Writer writer) throws IOException {
        identifiable.exportTopology(writer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }
}
