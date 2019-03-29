/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Terminal;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableBusbarSection extends AbstractImmutableIdentifiable<BusbarSection> implements BusbarSection {

    private final ImmutableCacheIndex cache;

    ImmutableBusbarSection(BusbarSection identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public double getV() {
        return identifiable.getV();
    }

    @Override
    public double getAngle() {
        return identifiable.getAngle();
    }

    @Override
    public Terminal getTerminal() {
        return cache.getTerminal(identifiable.getTerminal());
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.BUSBAR_SECTION;
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(cache::getTerminal).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
