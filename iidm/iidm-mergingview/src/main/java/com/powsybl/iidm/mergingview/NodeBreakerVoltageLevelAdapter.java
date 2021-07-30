/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
class NodeBreakerVoltageLevelAdapter extends AbstractVoltageLevelAdapter {

    class NodeBreakerViewAdapter extends AbstractAdapter<NodeBreakerView> implements NodeBreakerView {

        final class SwitchAdderAdapter extends AbstractIdentifiableAdderAdapter<SwitchAdder> implements SwitchAdder {

            SwitchAdderAdapter(final SwitchAdder delegate, final MergingViewIndex index) {
                super(delegate, index);
            }

            @Override
            public Switch add() {
                checkAndSetUniqueId();
                return getIndex().getSwitch(getDelegate().add());
            }

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

        final class InternalConnectionAdderAdapter implements InternalConnectionAdder {

            private final InternalConnectionAdder delegate;

            InternalConnectionAdderAdapter(final InternalConnectionAdder delegate) {
                this.delegate = Objects.requireNonNull(delegate);
            }

            @Override
            public InternalConnectionAdder setNode1(int node1) {
                delegate.setNode1(node1);
                return this;
            }

            @Override
            public InternalConnectionAdder setNode2(int node2) {
                delegate.setNode2(node2);
                return this;
            }

            @Override
            public void add() {
                delegate.add();
            }
        }

        NodeBreakerViewAdapter(NodeBreakerView delegate, MergingViewIndex index) {
            super(delegate, index);
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
        public SwitchAdder newSwitch() {
            return new SwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
        }

        @Override
        public InternalConnectionAdder newInternalConnection() {
            return new InternalConnectionAdderAdapter(getDelegate().newInternalConnection());
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
        public void removeInternalConnections(int node1, int node2) {
            // TODO(mathbagu)
            throw MergingView.createNotImplementedException();
        }

        @Override
        public SwitchAdder newBreaker() {
            return new SwitchAdderAdapter(getDelegate().newBreaker(), getIndex());
        }

        @Override
        public SwitchAdder newDisconnector() {
            return new SwitchAdderAdapter(getDelegate().newDisconnector(), getIndex());
        }

        @Override
        public int getNode1(String switchId) {
            return getDelegate().getNode1(switchId);
        }

        @Override
        public int getNode2(String switchId) {
            return getDelegate().getNode2(switchId);
        }

        @Override
        public Terminal getTerminal(int node) {
            return getIndex().getTerminal(getDelegate().getTerminal(node));
        }

        @Override
        public Stream<Switch> getSwitchStream(int node) {
            return getDelegate().getSwitchStream(node);
        }

        @Override
        public Iterable<Switch> getSwitches(int node) {
            return getDelegate().getSwitches(node);
        }

        @Override
        public Stream<InternalConnection> getInternalConnectionStream(int node) {
            return getDelegate().getInternalConnectionStream(node);
        }

        @Override
        public Iterable<InternalConnection> getInternalConnections(int node) {
            return getDelegate().getInternalConnections(node);
        }

        @Override
        public Optional<Terminal> getOptionalTerminal(int node) {
            return Optional.ofNullable(getTerminal(node));
        }

        @Override
        public boolean hasAttachedEquipment(int node) {
            return getDelegate().hasAttachedEquipment(node);
        }

        @Override
        public Terminal getTerminal1(String switchId) {
            return getIndex().getTerminal(getDelegate().getTerminal1(switchId));
        }

        @Override
        public Terminal getTerminal2(String switchId) {
            return getIndex().getTerminal(getDelegate().getTerminal2(switchId));
        }

        @Override
        public Switch getSwitch(String switchId) {
            return getIndex().getSwitch(getDelegate().getSwitch(switchId));
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Iterables.transform(getDelegate().getSwitches(),
                    getIndex()::getSwitch);
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getDelegate().getSwitchStream()
                    .map(getIndex()::getSwitch);
        }

        @Override
        public int getSwitchCount() {
            return getDelegate().getSwitchCount();
        }

        @Override
        public void removeSwitch(String switchId) {
            // TODO(mathbagu)
            throw MergingView.createNotImplementedException();
        }

        @Override
        public BusbarSectionAdder newBusbarSection() {
            return new BusbarSectionAdderAdapter(getDelegate().newBusbarSection(), getIndex());
        }

        @Override
        public Iterable<BusbarSection> getBusbarSections() {
            return Iterables.transform(getDelegate().getBusbarSections(),
                    getIndex()::getBusbarSection);
        }

        @Override
        public Stream<BusbarSection> getBusbarSectionStream() {
            return getDelegate().getBusbarSectionStream()
                    .map(getIndex()::getBusbarSection);
        }

        @Override
        public int getBusbarSectionCount() {
            return getDelegate().getBusbarSectionCount();
        }

        @Override
        public BusbarSection getBusbarSection(String id) {
            return getIndex().getBusbarSection(getDelegate().getBusbarSection(id));
        }

        @Override
        public void traverse(int node, Traverser traverser) {
            // TODO(mathbagu)
            throw MergingView.createNotImplementedException();
        }
    }

    static class BusBreakerViewAdapter extends BusCache<BusBreakerView> implements BusBreakerViewExt {

        BusBreakerViewAdapter(BusBreakerView delegate, MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Iterable<Bus> getBuses() {
            return Iterables.transform(getDelegate().getBuses(), this::getBus);
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getDelegate().getBusStream().map(this::getBus);
        }

        @Override
        public Bus getBus(String id) {
            return getBus(getDelegate().getBus(id));
        }

        @Override
        public BusAdder newBus() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void removeBus(String busId) {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void removeAllBuses() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Iterables.transform(getDelegate().getSwitches(),
                    getIndex()::getSwitch);
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getDelegate().getSwitchStream()
                    .map(getIndex()::getSwitch);
        }

        @Override
        public int getSwitchCount() {
            return getDelegate().getSwitchCount();
        }

        @Override
        public void removeSwitch(String switchId) {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void removeAllSwitches() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public Bus getBus1(String switchId) {
            return getBus(getDelegate().getBus1(switchId));
        }

        @Override
        public Bus getBus2(String switchId) {
            return getBus(getDelegate().getBus2(switchId));
        }

        @Override
        public Switch getSwitch(String switchId) {
            return getIndex().getSwitch(getDelegate().getSwitch(switchId));
        }

        @Override
        public SwitchAdder newSwitch() {
            throw createNotSupportedNodeBreakerTopologyException();
        }

        private PowsyblException createNotSupportedNodeBreakerTopologyException() {
            return new PowsyblException("Not supported in a node/breaker topology");
        }
    }

    static class BusViewAdapter extends BusCache<BusView> implements BusViewExt {

        BusViewAdapter(BusView delegate, MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Iterable<Bus> getBuses() {
            return Iterables.transform(getDelegate().getBuses(), this::getBus);
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getDelegate().getBusStream().map(this::getBus);
        }

        @Override
        public Bus getBus(String id) {
            return getBus(getDelegate().getBus(id));
        }

        @Override
        public Bus getMergedBus(String configuredBusId) {
            return getBus(getDelegate().getMergedBus(configuredBusId));
        }
    }

    private final NodeBreakerViewAdapter nodeBreakerView;

    private final BusBreakerViewAdapter busBreakerView;

    private final BusViewAdapter busView;

    NodeBreakerVoltageLevelAdapter(final VoltageLevel delegate, final MergingViewIndex index) {
        super(delegate, index);
        nodeBreakerView = new NodeBreakerViewAdapter(delegate.getNodeBreakerView(), index);
        busBreakerView = new BusBreakerViewAdapter(delegate.getBusBreakerView(), index);
        busView = new BusViewAdapter(delegate.getBusView(), index);
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerViewExt getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusViewExt getBusView() {
        return busView;
    }

    @Override
    public void invalidateCache() {
        busBreakerView.invalidateCache();
        busView.invalidateCache();
    }
}
