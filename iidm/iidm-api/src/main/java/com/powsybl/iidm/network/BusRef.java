/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

@JsonSubTypes({
    @JsonSubTypes.Type(value = IdBasedBusRef.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public interface BusRef {

    /**
     * @return an empty if not found
     * @throws com.powsybl.commons.PowsyblException if underlying implementation not supported
     * @throws IllegalArgumentException if try to resolve by {@link com.powsybl.iidm.network.TopologyLevel#NODE_BREAKER}
     */
    Optional<Bus> resolve(Network network, TopologyLevel level);
}
