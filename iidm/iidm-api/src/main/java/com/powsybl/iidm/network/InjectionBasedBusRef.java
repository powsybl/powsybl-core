/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class InjectionBasedBusRef implements BusRef {

    private final String injectionId;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public InjectionBasedBusRef(@JsonProperty("injectionId") String injectionId) {
        this.injectionId = Objects.requireNonNull(injectionId);
    }

    @Override
    public Optional<Bus> resolve(Network network) {
        Identifiable identifiable = network.getIdentifiable(injectionId);
        if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            return Optional.ofNullable(injection.getTerminal().getBusView().getBus());
        }
        return Optional.empty();
    }

    public String getInjectionId() {
        return injectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InjectionBasedBusRef)) {
            return false;
        }

        InjectionBasedBusRef that = (InjectionBasedBusRef) o;

        return getInjectionId().equals(that.getInjectionId());
    }

    @Override
    public int hashCode() {
        return getInjectionId().hashCode();
    }
}
