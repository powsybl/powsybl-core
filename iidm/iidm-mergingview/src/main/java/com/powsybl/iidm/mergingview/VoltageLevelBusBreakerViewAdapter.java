/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.BusBreakerView;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelBusBreakerViewAdapter extends AbstractAdapter<VoltageLevel.BusBreakerView> implements VoltageLevel.BusBreakerView {

    VoltageLevelBusBreakerViewAdapter(final BusBreakerView delegate, final MergingViewIndex index) {
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
        return getDelegate().getBusStream()
                .map(getIndex()::getBus);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Collections.unmodifiableSet(getSwitchStream().collect(Collectors.toSet()));
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return getDelegate().getSwitchStream()
                .map(getIndex()::getSwitch);
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
        return new VoltageLevelBusBreakerViewSwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
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
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void removeAllBuses() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void removeSwitch(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void removeAllSwitches() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
