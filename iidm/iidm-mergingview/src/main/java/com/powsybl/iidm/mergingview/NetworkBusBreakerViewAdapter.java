/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class NetworkBusBreakerViewAdapter implements Network.BusBreakerView {

    private final MergingViewIndex index;

    NetworkBusBreakerViewAdapter(final MergingViewIndex index) {
        this.index = index;
    }

    @Override
    public Bus getBus(final String id) {
        return index.getNetworkStream()
                    .map(Network::getBusBreakerView)
                    .map(bb -> bb.getBus(id))
                    .filter(Objects::nonNull)
                    .map(index::getBus)
                    .findFirst()
                    .orElse(null);
    }

    @Override
    public Iterable<Bus> getBuses() {
        return Collections.unmodifiableList(getBusStream().collect(Collectors.toList()));
    }

    @Override
    public Stream<Bus> getBusStream() {
        return index.getNetworkStream()
                .map(Network::getBusBreakerView)
                .map(Network.BusBreakerView::getBusStream)
                .flatMap(stream -> stream)
                .map(index::getBus);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Collections.unmodifiableList(getSwitchStream().collect(Collectors.toList()));
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return index.getNetworkStream()
                .map(Network::getBusBreakerView)
                .map(Network.BusBreakerView::getSwitchStream)
                .flatMap(stream -> stream)
                .map(index::getSwitch);
    }

    @Override
    public int getSwitchCount() {
        return (int) getSwitchStream().count();
    }
}
