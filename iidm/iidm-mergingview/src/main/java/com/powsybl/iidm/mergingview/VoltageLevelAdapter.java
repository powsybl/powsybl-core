/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class VoltageLevelAdapter extends AbstractVoltageLevelAdapter {

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
            return  getDelegate().getSwitchCount();
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
}
