/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifier;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdBasedNetworkElementIdentifier implements NetworkElementIdentifier {

    private final String identifier;

    public IdBasedNetworkElementIdentifier(String identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    @Override
    public Optional<Identifiable> filterIdentifiable(Network network) {
        return Optional.ofNullable(network.getIdentifiable(identifier));
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.ID_BASED;
    }
}
