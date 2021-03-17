/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;

import java.util.Objects;
import java.util.Optional;

/**
 * There would be two types of id:
 * 1. id of equipment:
 * 2. id of bus itself:
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class IdBasedBusRef implements BusRef {

    private final String id;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IdBasedBusRef(@JsonProperty("id") String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public Optional<Bus> resolve(Network network) {
        final Bus bus = network.getBusView().getBus(id);
        if (bus != null) {
            return Optional.of(bus);
        }
        final Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            return Optional.empty();
        }
        if (identifiable instanceof Injection) {
            final Injection injection = (Injection) identifiable;
            return Optional.of(injection.getTerminal().getBusView().getBus());
        } else {
            throw new PowsyblException(id + " is not a bus or injection.");
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdBasedBusRef)) {
            return false;
        }

        IdBasedBusRef that = (IdBasedBusRef) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
