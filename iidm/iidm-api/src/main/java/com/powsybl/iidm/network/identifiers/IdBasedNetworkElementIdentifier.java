/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdBasedNetworkElementIdentifier implements NetworkElementIdentifier {

    private final String identifier;
    private final String contingencyId;

    public IdBasedNetworkElementIdentifier(String identifier) {
        this(identifier, null);
    }

    public IdBasedNetworkElementIdentifier(String identifier, String contingencyId) {
        this.identifier = Objects.requireNonNull(identifier);
        this.contingencyId = contingencyId;
    }

    @Override
    public Set<Identifiable> filterIdentifiable(Network network) {
        Identifiable identifiable = network.getIdentifiable(identifier);
        return identifiable == null ? Collections.emptySet() : Collections.singleton(identifiable);
    }

    @Override
    public Set<String> getNotFoundElements(Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(identifier);
        return identifiable == null ? Collections.singleton(identifier) : Collections.emptySet();
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.ID_BASED;
    }

    @Override
    public Optional<String> getContingencyId() {
        return Optional.ofNullable(contingencyId);
    }
}
