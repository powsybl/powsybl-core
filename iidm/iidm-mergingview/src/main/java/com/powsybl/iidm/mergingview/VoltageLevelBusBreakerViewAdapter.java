/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.BusBreakerView;

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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public Iterable<Bus> getBuses() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Bus> getBusStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Bus getBus(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void removeBus(final String busId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void removeAllBuses() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getSwitchCount() {
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

    @Override
    public Bus getBus1(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Bus getBus2(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Switch getSwitch(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder newSwitch() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
