/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifiant;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierList implements Identifier {
    private final List<Identifier> identifiers;

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public IdentifierList(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public Contingency filterIdentifiable(Network network) {
        return identifiers.stream()
                .map(identifiant -> identifiant.filterIdentifiable(network))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.LIST;
    }

}
