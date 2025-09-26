/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.DcBus;
import com.powsybl.iidm.network.Network;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractComponent implements Component {

    private final int num;

    private final int size;

    protected AbstractComponent(int num, int size) {
        this.num = num;
        this.size = size;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Iterable<Bus> getBuses() {
        return StreamSupport.stream(getNetwork().getBusView().getBuses().spliterator(), false)
                            .filter(getBusPredicate())
                            .toList();
    }

    @Override
    public Stream<Bus> getBusStream() {
        return getNetwork().getBusView().getBusStream().filter(getBusPredicate());
    }

    @Override
    public Iterable<DcBus> getDcBuses() {
        return StreamSupport.stream(getNetwork().getDcBuses().spliterator(), false)
                            .filter(getDcBusPredicate())
                            .toList();
    }

    @Override
    public Stream<DcBus> getDcBusStream() {
        return getNetwork().getDcBusStream().filter(getDcBusPredicate());
    }

    protected abstract Network getNetwork();

    protected abstract Predicate<Bus> getBusPredicate();

    protected abstract Predicate<DcBus> getDcBusPredicate();
}
