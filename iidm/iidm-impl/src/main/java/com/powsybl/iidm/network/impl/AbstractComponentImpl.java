/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractComponentImpl implements Component {

    private final int num;

    private final int size;

    private final Ref<NetworkImpl> networkRef;

    AbstractComponentImpl(int num, int size, Ref<NetworkImpl> networkRef) {
        this.num = num;
        this.size = size;
        this.networkRef = Objects.requireNonNull(networkRef);
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
        return StreamSupport.stream(networkRef.get().getBusView().getBuses().spliterator(), false)
                            .filter(getBusPredicate())
                            .collect(Collectors.toList());
    }

    @Override
    public Stream<Bus> getBusStream() {
        return networkRef.get().getBusView().getBusStream().filter(getBusPredicate());
    }

    protected abstract Predicate<Bus> getBusPredicate();
}
