/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class VoltageLevelAdapter extends AbstractIdentifiableAdapter<VoltageLevel> implements VoltageLevel {

    static class BusBreakerViewAdapter extends AbstractAdapter<VoltageLevel.BusBreakerView> implements VoltageLevel.BusBreakerView {

        static class SwitchAdderAdapter extends AbstractIdentifiableAdderAdapter<VoltageLevel.BusBreakerView.SwitchAdder> implements VoltageLevel.BusBreakerView.SwitchAdder {

            SwitchAdderAdapter(final VoltageLevel.BusBreakerView.SwitchAdder delegate, final MergingViewIndex index) {
                super(delegate, index);
            }

            @Override
            public Switch add() {
                checkAndSetUniqueId();
                return getIndex().getSwitch(getDelegate().add());
            }

            // -------------------------------
            // Simple delegated methods ------
            // -------------------------------
            @Override
            public VoltageLevel.BusBreakerView.SwitchAdder setBus1(String bus1) {
                getDelegate().setBus1(bus1);
                return this;
            }

            @Override
            public VoltageLevel.BusBreakerView.SwitchAdder setBus2(String bus2) {
                getDelegate().setBus2(bus2);
                return this;
            }

            @Override
            public VoltageLevel.BusBreakerView.SwitchAdder setOpen(final boolean open) {
                getDelegate().setOpen(open);
                return this;
            }

            @Override
            public VoltageLevel.BusBreakerView.SwitchAdder setFictitious(final boolean fictitious) {
                getDelegate().setFictitious(fictitious);
                return this;
            }
        }

        BusBreakerViewAdapter(final BusBreakerView delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public BusAdder newBus() {
            return new BusAdderAdapter(getDelegate().newBus(), getIndex());
        }

        @Override
        public Bus getBus(final String id) {
            return getIndex().getBus(getDelegate().getBus(id));
        }

        @Override
        public Iterable<Bus> getBuses() {
            return Collections.unmodifiableSet(getBusStream().collect(Collectors.toSet()));
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getDelegate().getBusStream().map(getIndex()::getBus);
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Collections.unmodifiableSet(getSwitchStream().collect(Collectors.toSet()));
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getDelegate().getSwitchStream().map(getIndex()::getSwitch);
        }

        @Override
        public Bus getBus1(final String switchId) {
            return getIndex().getBus(getDelegate().getBus1(switchId));
        }

        @Override
        public Bus getBus2(final String switchId) {
            return getIndex().getBus(getDelegate().getBus2(switchId));
        }

        @Override
        public Switch getSwitch(final String switchId) {
            return getIndex().getSwitch(getDelegate().getSwitch(switchId));
        }

        @Override
        public VoltageLevel.BusBreakerView.SwitchAdder newSwitch() {
            return new SwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
        }

        // -------------------------------
        // Simple delegated methods ------
        // -------------------------------
        @Override
        public int getSwitchCount() {
            return getDelegate().getSwitchCount();
        }

        // -------------------------------
        // Not implemented methods -------
        // -------------------------------
        @Override
        public void removeBus(final String busId) {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public void removeAllBuses() {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public void removeSwitch(final String switchId) {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public void removeAllSwitches() {
            throw MergingView.createNotImplementedException();
        }
    }

    private BusBreakerViewAdapter busBreakerView;

    static class NodeBreakerViewAdapter extends AbstractAdapter<VoltageLevel.NodeBreakerView> implements VoltageLevel.NodeBreakerView {

        static class SwitchAdderAdapter extends AbstractIdentifiableAdderAdapter<VoltageLevel.NodeBreakerView.SwitchAdder> implements VoltageLevel.NodeBreakerView.SwitchAdder {

            SwitchAdderAdapter(final VoltageLevel.NodeBreakerView.SwitchAdder delegate, final MergingViewIndex index) {
                super(delegate, index);
            }

            @Override
            public Switch add() {
                checkAndSetUniqueId();
                return getIndex().getSwitch(getDelegate().add());
            }

            // -------------------------------
            // Simple delegated methods ------
            // -------------------------------
            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setNode1(final int node1) {
                getDelegate().setNode1(node1);
                return this;
            }

            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setNode2(final int node2) {
                getDelegate().setNode2(node2);
                return this;
            }

            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setKind(final SwitchKind kind) {
                getDelegate().setKind(kind);
                return this;
            }

            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setKind(final String kind) {
                getDelegate().setKind(kind);
                return this;
            }

            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setOpen(final boolean open) {
                getDelegate().setOpen(open);
                return this;
            }

            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setRetained(final boolean retained) {
                getDelegate().setRetained(retained);
                return this;
            }

            @Override
            public VoltageLevel.NodeBreakerView.SwitchAdder setFictitious(final boolean fictitious) {
                getDelegate().setFictitious(fictitious);
                return this;
            }
        }

        NodeBreakerViewAdapter(final NodeBreakerView delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Terminal getTerminal(final int node) {
            return getIndex().getTerminal(getDelegate().getTerminal(node));
        }

        @Override
        public Optional<Terminal> getOptionalTerminal(final int node) {
            return getDelegate().getOptionalTerminal(node).map(t -> getIndex().getTerminal(t));
        }

        @Override
        public boolean hasAttachedEquipment(final int node) {
            return getDelegate().hasAttachedEquipment(node);
        }

        @Override
        public Terminal getTerminal1(final String switchId) {
            return getIndex().getTerminal(getDelegate().getTerminal1(switchId));
        }

        @Override
        public Terminal getTerminal2(final String switchId) {
            return getIndex().getTerminal(getDelegate().getTerminal2(switchId));
        }

        @Override
        public Switch getSwitch(final String switchId) {
            return getIndex().getSwitch(getDelegate().getSwitch(switchId));
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return getSwitchStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getDelegate().getSwitchStream().map(getIndex()::getSwitch);
        }

        @Override
        public Iterable<BusbarSection> getBusbarSections() {
            return getBusbarSectionStream().collect(Collectors.toList());
        }

        @Override
        public Stream<BusbarSection> getBusbarSectionStream() {
            return getDelegate().getBusbarSectionStream().map(getIndex()::getBusbarSection);
        }

        @Override
        public BusbarSection getBusbarSection(final String id) {
            return getIndex().getBusbarSection(getDelegate().getBusbarSection(id));
        }

        @Override
        public VoltageLevel.NodeBreakerView.SwitchAdder newSwitch() {
            return new SwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
        }

        @Override
        public VoltageLevel.NodeBreakerView.SwitchAdder newBreaker() {
            return new SwitchAdderAdapter(getDelegate().newBreaker(), getIndex());
        }

        @Override
        public VoltageLevel.NodeBreakerView.SwitchAdder newDisconnector() {
            return new SwitchAdderAdapter(getDelegate().newDisconnector(), getIndex());
        }

        // -------------------------------
        // Simple delegated methods ------
        // -------------------------------

        /**
         * @deprecated Use {@link #getMaximumNodeIndex()} instead.
         */
        @Override
        @Deprecated
        public int getNodeCount() {
            return getDelegate().getNodeCount();
        }

        @Override
        public int getMaximumNodeIndex() {
            return getDelegate().getMaximumNodeIndex();
        }

        @Override
        public int[] getNodes() {
            return getDelegate().getNodes();
        }

        @Override
        public int getInternalConnectionCount() {
            return getDelegate().getInternalConnectionCount();
        }

        @Override
        public Iterable<InternalConnection> getInternalConnections() {
            return getDelegate().getInternalConnections();
        }

        @Override
        public Stream<InternalConnection> getInternalConnectionStream() {
            return getDelegate().getInternalConnectionStream();
        }

        @Override
        public int getNode1(final String switchId) {
            return getDelegate().getNode1(switchId);
        }

        @Override
        public int getNode2(final String switchId) {
            return getDelegate().getNode2(switchId);
        }

        @Override
        public int getSwitchCount() {
            return getDelegate().getSwitchCount();
        }

        @Override
        public int getBusbarSectionCount() {
            return getDelegate().getBusbarSectionCount();
        }

        @Override
        public InternalConnectionAdder newInternalConnection() {
            return getDelegate().newInternalConnection();
        }

        @Override
        public BusbarSectionAdder newBusbarSection() {
            return new BusbarSectionAdderAdapter(getDelegate().newBusbarSection(), getIndex());
        }

        // ------------------------------
        // Not implemented methods ------
        // ------------------------------
        @Override
        public void removeSwitch(final String switchId) {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public void traverse(final int node, final Traverser traverser) {
            throw MergingView.createNotImplementedException();
        }
    }

    private NodeBreakerViewAdapter nodeBreakerView;

    static class BusViewAdapter extends AbstractAdapter<VoltageLevel.BusView> implements VoltageLevel.BusView {

        BusViewAdapter(final BusView delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        // -------------------------------
        // Not implemented methods -------
        // -------------------------------
        @Override
        public Iterable<Bus> getBuses() {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public Stream<Bus> getBusStream() {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public Bus getBus(final String id) {
            throw MergingView.createNotImplementedException();
        }

        @Override
        public Bus getMergedBus(final String configuredBusId) {
            throw MergingView.createNotImplementedException();
        }
    }

    private BusViewAdapter busView;

    VoltageLevelAdapter(final VoltageLevel delegate, final MergingViewIndex index) {
        super(delegate, index);
        busBreakerView = new BusBreakerViewAdapter(getDelegate().getBusBreakerView(), getIndex());
        nodeBreakerView = new NodeBreakerViewAdapter(getDelegate().getNodeBreakerView(), getIndex());
        busView = new BusViewAdapter(getDelegate().getBusView(), getIndex());
    }

    @Override
    public VoltageLevel.BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public VoltageLevel.NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public VoltageLevel.BusView getBusView() {
        return busView;
    }

    @Override
    public Substation getSubstation() {
        return getIndex().getSubstation(getDelegate().getSubstation());
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        return new VscConverterStationAdderAdapter(getDelegate().newVscConverterStation(), getIndex());
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(getDelegate().getVscConverterStations(),
                getIndex()::getVscConverterStation);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getDelegate().getVscConverterStationStream().map(getIndex()::getVscConverterStation);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return Iterables.transform(getDelegate().getBatteries(),
                getIndex()::getBattery);
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getDelegate().getBatteryStream().map(getIndex()::getBattery);
    }

    @Override
    public GeneratorAdder newGenerator() {
        return new GeneratorAdderAdapter(getDelegate().newGenerator(), getIndex());
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(getDelegate().getGenerators(),
                getIndex()::getGenerator);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getDelegate().getGeneratorStream().map(getIndex()::getGenerator);
    }

    @Override
    public LoadAdder newLoad() {
        return new LoadAdderAdapter(getDelegate().newLoad(), getIndex());
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(getDelegate().getLoads(),
                getIndex()::getLoad);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getDelegate().getLoadStream().map(getIndex()::getLoad);
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        return new ShuntCompensatorAdderAdapter(getDelegate().newShuntCompensator(), getIndex());
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(getDelegate().getShuntCompensators(),
                getIndex()::getShuntCompensator);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getDelegate().getShuntCompensatorStream().map(getIndex()::getShuntCompensator);
    }

    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        return new StaticVarCompensatorAdderAdapter(getDelegate().newStaticVarCompensator(), getIndex());
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(getDelegate().getStaticVarCompensators(),
                getIndex()::getStaticVarCompensator);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getDelegate().getStaticVarCompensatorStream().map(getIndex()::getStaticVarCompensator);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(getDelegate().getSwitches(),
                getIndex()::getSwitch);
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        return new LccConverterStationAdderAdapter(getDelegate().newLccConverterStation(), getIndex());
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getDelegate().getLccConverterStationStream().map(getIndex()::getLccConverterStation);
    }

    public DanglingLineAdder newDanglingLine() {
        return new DanglingLineAdderAdapter(getDelegate().newDanglingLine(), getIndex());
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getDelegate().getDanglingLineStream()
                .filter(dl -> !getIndex().isMerged(dl))
                .map(getIndex()::getDanglingLine);
    }

    @Override
    public int getDanglingLineCount() {
        return (int) getDanglingLineStream().count();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public <T extends Connectable> T getConnectable(final String id, final Class<T> aClass) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(final Class<T> clazz) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(final Class<T> clazz) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <T extends Connectable> int getConnectableCount(final Class<T> clazz) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public int getConnectableCount() {
        throw MergingView.createNotImplementedException();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getSwitchCount() {
        return getDelegate().getSwitchCount();
    }

    @Override
    public int getGeneratorCount() {
        return getDelegate().getGeneratorCount();
    }

    @Override
    public int getBatteryCount() {
        return getDelegate().getBatteryCount();
    }

    @Override
    public int getLoadCount() {
        return getDelegate().getLoadCount();
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return getDelegate().getStaticVarCompensatorCount();
    }

    @Override
    public int getVscConverterStationCount() {
        return getDelegate().getVscConverterStationCount();
    }

    @Override
    public ContainerType getContainerType() {
        return getDelegate().getContainerType();
    }

    @Override
    public double getNominalV() {
        return getDelegate().getNominalV();
    }

    @Override
    public VoltageLevel setNominalV(final double nominalV) {
        getDelegate().setNominalV(nominalV);
        return this;
    }

    @Override
    public double getLowVoltageLimit() {
        return getDelegate().getLowVoltageLimit();
    }

    @Override
    public VoltageLevel setLowVoltageLimit(final double lowVoltageLimit) {
        getDelegate().setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public double getHighVoltageLimit() {
        return getDelegate().getHighVoltageLimit();
    }

    @Override
    public VoltageLevel setHighVoltageLimit(final double highVoltageLimit) {
        getDelegate().setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public int getShuntCompensatorCount() {
        return getDelegate().getShuntCompensatorCount();
    }

    @Override
    public int getLccConverterStationCount() {
        return getDelegate().getLccConverterStationCount();
    }

    @Override
    public void visitEquipments(final TopologyVisitor visitor) {
        getDelegate().visitEquipments(new TopologyVisitorAdapter(visitor, getIndex()));
    }

    @Override
    public TopologyKind getTopologyKind() {
        return getDelegate().getTopologyKind();
    }

    @Override
    public void printTopology() {
        getDelegate().printTopology();
    }

    @Override
    public void printTopology(final PrintStream out, final ShortIdDictionary dict) {
        getDelegate().printTopology(out, dict);
    }

    @Override
    public void exportTopology(final Path file) throws IOException {
        getDelegate().exportTopology(file);
    }

    @Override
    public void exportTopology(final Writer writer, final Random random) throws IOException {
        getDelegate().exportTopology(writer, random);
    }

    @Override
    public void exportTopology(final Writer writer) throws IOException {
        getDelegate().exportTopology(writer);
    }

    @Override
    public BatteryAdder newBattery() {
        return new BatteryAdderAdapter(getDelegate().newBattery(), getIndex());
    }
}
