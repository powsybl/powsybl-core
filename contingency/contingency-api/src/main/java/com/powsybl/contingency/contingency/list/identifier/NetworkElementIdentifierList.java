/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.contingency.list.identifier;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class NetworkElementIdentifierList implements NetworkElementIdentifier {
    private final List<NetworkElementIdentifier> networkElementIdentifiers;
    private final String contingencyId;

    public List<NetworkElementIdentifier> getNetworkElementIdentifiers() {
        return networkElementIdentifiers;
    }

    public NetworkElementIdentifierList(List<NetworkElementIdentifier> networkElementIdentifiers) {
        this(networkElementIdentifiers, null);
    }

    public NetworkElementIdentifierList(List<NetworkElementIdentifier> networkElementIdentifiers, String contingencyId) {
        this.networkElementIdentifiers = ImmutableList.copyOf(networkElementIdentifiers);
        this.contingencyId = contingencyId;
    }

    @Override
    public Set<Identifiable<?>> filterIdentifiable(Network network) {
        Set<Identifiable<?>> identifiables = new LinkedHashSet<>();
        networkElementIdentifiers.forEach(identifiant -> identifiables.addAll(identifiant.filterIdentifiable(network)));
        return identifiables;
    }

    @Override
    public Set<String> getNotFoundElements(Network network) {
        Set<String> notFoundElements = new LinkedHashSet<>();
        networkElementIdentifiers.forEach(identifiant -> notFoundElements.addAll(identifiant.getNotFoundElements(network)));
        return notFoundElements;
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.LIST;
    }

    @Override
    public Optional<String> getContingencyId() {
        return Optional.ofNullable(contingencyId);
    }
}
