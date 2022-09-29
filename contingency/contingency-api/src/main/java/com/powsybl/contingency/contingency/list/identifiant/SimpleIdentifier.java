/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifiant;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class SimpleIdentifier implements Identifier {

    private final String identifier;

    public SimpleIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Contingency filterIdentifiable(Network network) {
        Identifiable identifiable = network.getIdentifiable(identifier);
        if (identifiable == null) {
            return null;
        }
        return new Contingency(identifiable.getId(), Contingency.getContingencyElement(identifiable));
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.SIMPLE;
    }
}
