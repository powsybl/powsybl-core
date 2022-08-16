/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
class BusBreakerVoltageLevelAdapter extends AbstractVoltageLevelAdapter {

    class BusBreakerViewAdapter extends AbstractAdapter<BusBreakerView> implements BusBreakerViewExt {

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

        BusBreakerViewAdapter(BusBreakerView delegate, MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Iterable<Bus> getBuses() {
            return Iterables.transform(getDelegate().getBuses(), this::getBus);
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getDelegate().getBusStream()
                    .map(this::getBus);
        }

        @Override
        public Bus getBus(String id) {
            return getBus(getDelegate().getBus(id));
        }

        @Override
        public BusAdder newBus() {
            return new BusAdderAdapter(getDelegate().newBus(), getIndex());
        }

        @Override
        public void removeBus(String busId) {
            // TODO(mathbagu)
            throw MergingView.createNotImplementedException();
        }

        @Override
        public void removeAllBuses() {
            // TODO(mathbagu)
            throw MergingView.createNotImplementedException();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Iterables.transform(getDelegate().getSwitches(), getIndex()::getSwitch);
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
        public void removeAllSwitches() {
            // TODO(mathbagu)
            throw MergingView.createNotImplementedException();
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
        public Collection<Bus> getBusesFromMergedBusId(String mergedBusId) {
            return getBusStreamFromMergedBusId(mergedBusId).collect(Collectors.toSet());
        }

        @Override
        public Stream<Bus> getBusStreamFromMergedBusId(String mergedBusId) {
            return getDelegate().getBusStreamFromMergedBusId(mergedBusId).map(this::getBus);
        }

        @Override
        public Switch getSwitch(String switchId) {
            return getIndex().getSwitch(getDelegate().getSwitch(switchId));
        }

        @Override
        public SwitchAdder newSwitch() {
            return new SwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
        }

        @Override
        public Bus getBus(Bus bus) {
            return getIndex().getBus(bus);
        }

        @Override
        public void traverse(Bus bus, TopologyTraverser traverser) {
            // TODO
            throw MergingView.createNotImplementedException();
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

    private final BusBreakerViewAdapter busBreakerView;

    private final BusViewAdapter busView;

    BusBreakerVoltageLevelAdapter(final VoltageLevel delegate, final MergingViewIndex index) {
        super(delegate, index);
        busBreakerView = new BusBreakerViewAdapter(delegate.getBusBreakerView(), index);
        busView = new BusViewAdapter(delegate.getBusView(), index);
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        // The delegate is a BusBreakerVoltageLevel, so we can return its Node/Breaker view that throws exception for all the methods.
        return getDelegate().getNodeBreakerView();
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
        busView.invalidateCache();
    }
}
