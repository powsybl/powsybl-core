/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Network;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class NetworkBusViewAdapter implements Network.BusView {

    private final MergingViewIndex index;

    public NetworkBusViewAdapter(final MergingViewIndex index) {
        this.index = index;
    }

    @Override
    public Iterable<Bus> getBuses() {
        return Collections.unmodifiableSet(getBusStream().collect(Collectors.toSet()));
    }

    @Override
    public Stream<Bus> getBusStream() {
        return index.getNetworkStream()
                .map(Network::getBusView)
                .map(Network.BusView::getBusStream)
                .flatMap(stream -> stream)
                .map(index::getBus);
    }

    @Override
    public Collection<Component> getConnectedComponents() {
        return index.getNetworkStream()
                .map(Network::getBusView)
                .map(Network.BusView::getConnectedComponents)
                .flatMap(Collection::stream)
                .map(index::getComponent)
                .collect(Collectors.toSet());
    }

    @Override
    public Bus getBus(final String id) {
        return index.getNetworkStream()
                .map(Network::getBusView)
                .map(b -> b.getBus(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
