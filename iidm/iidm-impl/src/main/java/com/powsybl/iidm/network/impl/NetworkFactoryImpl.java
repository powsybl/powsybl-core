/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkFactoryImpl implements NetworkFactory {

    @Override
    public Network createNetwork(String id, String sourceFormat) {
        return new NetworkImpl(id, id, sourceFormat);
    }

    @Override
    public Network createNetwork(String id, Network... networks) {
        return NetworkImpl.create(id, id, networks);
    }

    @Override
    public Network createNetwork(Network... networks) {
        String id = Arrays.stream(Objects.requireNonNull(networks))
                .map(Network::getId)
                .collect(Collectors.joining("+"));
        return createNetwork(id, networks);
    }
}
