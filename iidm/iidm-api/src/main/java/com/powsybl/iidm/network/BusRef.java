/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

@JsonSubTypes({
        @JsonSubTypes.Type(value = BranchBasedBusRef.class),
        @JsonSubTypes.Type(value = IdBasedBusRef.class),
        @JsonSubTypes.Type(value = InjectionBasedBusRef.class),
        @JsonSubTypes.Type(value = NodeNumberBasedBusRef.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public interface BusRef {

    /**
     * @return an empty if not found or underlying implements not supported
     */
    Optional<Bus> resolve(Network network);
}
