/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifier;

import com.google.common.collect.ImmutableSet;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdBasedNetworkElementIdentifier implements NetworkElementIdentifier {

    private final Set<String> identifiers;
    private final String contingencyId;

    public IdBasedNetworkElementIdentifier(Set<String> identifiers, String contingencyId) {
        this.identifiers = ImmutableSet.copyOf(identifiers);
        this.contingencyId = Objects.requireNonNull(contingencyId);
    }

    @Override
    public Set<Identifiable<?>> filterIdentifiable(Network network) {
        return identifiers.stream()
                .map(network::getIdentifiable)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getIdentifiers() {
        return ImmutableSet.copyOf(identifiers);
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.ID_BASED;
    }

    @Override
    public String getContingencyName() {
        return contingencyId;
    }
}
