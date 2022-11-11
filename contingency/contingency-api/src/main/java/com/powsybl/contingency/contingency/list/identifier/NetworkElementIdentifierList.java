/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifier;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class NetworkElementIdentifierList implements NetworkElementIdentifier {
    private final List<NetworkElementIdentifier> networkElementIdentifiers;

    public List<NetworkElementIdentifier> getIdentifiers() {
        return networkElementIdentifiers;
    }

    public NetworkElementIdentifierList(List<NetworkElementIdentifier> networkElementIdentifiers) {
        this.networkElementIdentifiers = Objects.requireNonNull(networkElementIdentifiers);
    }

    @Override
    public Optional<Identifiable> filterIdentifiable(Network network) {
        return networkElementIdentifiers.stream()
                .map(identifiant -> identifiant.filterIdentifiable(network))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(null);
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.LIST;
    }

}
