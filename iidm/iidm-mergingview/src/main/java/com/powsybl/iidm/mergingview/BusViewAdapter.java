/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Collection;
import java.util.stream.Stream;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Network;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusViewAdapter implements Network.BusView {

    private final MergingViewIndex index;

    public BusViewAdapter(final MergingViewIndex index) {
        this.index = index;
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
    public Collection<Component> getConnectedComponents() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
