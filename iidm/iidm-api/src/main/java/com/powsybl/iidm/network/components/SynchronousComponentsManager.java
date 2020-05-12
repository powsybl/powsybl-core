/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class SynchronousComponentsManager extends AbstractComponentsManager<SynchronousComponent> {

    public SynchronousComponentsManager(Network network) {
        super(network);
    }

    protected SynchronousComponent createComponent(int num, int size) {
        return new SynchronousComponent(network, num, size);
    }

    @Override
    protected String getComponentLabel() {
        return "Synchronous";
    }

    @Override
    protected void setComponentNumber(Bus bus, int num) {
        Objects.requireNonNull(bus);
        bus.setSynchronousComponentNumber(num);
    }
}
