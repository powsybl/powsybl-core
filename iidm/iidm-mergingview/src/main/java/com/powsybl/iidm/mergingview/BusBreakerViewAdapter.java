/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.stream.Stream;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusBreakerViewAdapter implements Network.BusBreakerView {

    private final MergingViewIndex index;

    public BusBreakerViewAdapter(final MergingViewIndex index) {
        this.index = index;
    }

    @Override
    public Bus getBus(final String id) {
        return index.getBuses().stream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
}
