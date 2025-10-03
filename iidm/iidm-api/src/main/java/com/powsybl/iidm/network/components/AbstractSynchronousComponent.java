/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DcBus;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractSynchronousComponent extends AbstractComponent {

    protected AbstractSynchronousComponent(int num, int size) {
        super(num, size);
    }

    @Override
    public Iterable<DcBus> getDcBuses() {
        return Collections.emptyList();
    }

    @Override
    public Stream<DcBus> getDcBusStream() {
        return Stream.empty();
    }

    @Override
    protected Predicate<Bus> getBusPredicate() {
        return bus -> bus.getSynchronousComponent() == AbstractSynchronousComponent.this;
    }

    @Override
    protected Predicate<DcBus> getDcBusPredicate() {
        return dcBus -> false;
    }
}
