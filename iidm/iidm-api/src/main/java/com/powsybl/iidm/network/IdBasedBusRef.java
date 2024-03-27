/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;

import java.util.Objects;
import java.util.Optional;

/**
 * There would be three types of id:
 * 1. id of equipment:
 * 2. id of a configured bus itself:
 * 3. id of branch, in this case, side is required
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdBasedBusRef extends AbstractBusRef {

    private final String id;
    private final TwoSides side;

    public IdBasedBusRef(String id) {
        this.id = Objects.requireNonNull(id);
        this.side = null;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IdBasedBusRef(@JsonProperty("id") String id, @JsonProperty("side") TwoSides side) {
        this.id = Objects.requireNonNull(id);
        this.side = side;
    }

    @Override
    protected Optional<Bus> resolveByLevel(Network network, TopologyLevel level) {
        final Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            return Optional.empty();
        }

        if (side == null) {
            if (identifiable instanceof Bus bus) {
                if (level == TopologyLevel.BUS_BRANCH) {
                    return bus.getConnectedTerminalStream().map(t -> t.getBusView().getBus()).filter(Objects::nonNull).findFirst();
                } else {
                    return Optional.of(bus);
                }
            } else if (identifiable instanceof Injection) {
                final Injection<?> injection = (Injection<?>) identifiable;
                return chooseBusByLevel(injection.getTerminal(), level);
            } else {
                throw new PowsyblException(id + " is not a bus or injection.");
            }
        } else {
            if (identifiable instanceof Branch) {
                Branch<?> branch = (Branch<?>) identifiable;
                Terminal terminal = branch.getTerminal(side);
                return chooseBusByLevel(terminal, level);
            } else {
                throw new PowsyblException(id + " is not a branch.");
            }
        }
    }

    public String getId() {
        return id;
    }

    public TwoSides getSide() {
        return side;
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

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        return getSide() != null ? getSide().equals(that.getSide()) : that.getSide() == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, side);
    }
}
