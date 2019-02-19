/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableComponent implements Component {

    private final ImmutableCacheIndex cache;

    private Component component;

    ImmutableComponent(Component component, ImmutableCacheIndex cache) {
        this.component = Objects.requireNonNull(component);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public int getNum() {
        return component.getNum();
    }

    @Override
    public int getSize() {
        return component.getSize();
    }

    @Override
    public Iterable<Bus> getBuses() {
        return Iterables.transform(component.getBuses(), cache::getBus);
    }

    @Override
    public Stream<Bus> getBusStream() {
        return component.getBusStream().map(cache::getBus);
    }
}
