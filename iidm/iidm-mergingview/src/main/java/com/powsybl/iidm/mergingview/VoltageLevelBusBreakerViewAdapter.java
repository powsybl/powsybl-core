/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.stream.Stream;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevel.BusBreakerView;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelBusBreakerViewAdapter extends AbstractAdapter<VoltageLevel.BusBreakerView> implements VoltageLevel.BusBreakerView {

    protected VoltageLevelBusBreakerViewAdapter(final BusBreakerView delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public BusAdderAdapter newBus() {
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
    public BusAdapter getBus(final String id) {
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
    public BusAdapter getBus1(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusAdapter getBus2(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public SwitchAdapter getSwitch(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder newSwitch() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
