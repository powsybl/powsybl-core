/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class IdentifierActionList extends ActionList {

    private final Map<ActionBuilder<?>, NetworkElementIdentifier> elementIdentifierMap;

    public IdentifierActionList(List<Action> actions, Map<ActionBuilder<?>, NetworkElementIdentifier> elementIdentifierMap) {
        super(actions);
        this.elementIdentifierMap = elementIdentifierMap;
    }

    public List<Action> getActions(Network network) {
        return Stream.concat(elementIdentifierMap.entrySet().stream().map(entry -> {
            Set<Identifiable<?>> identifiables = entry.getValue().filterIdentifiable(network);
            if (identifiables.size() != 1) {
                throw new PowsyblException("for identifier in action builder more than one or none network element was found");
            }
            return entry.getKey().withNetworkElementId(identifiables.iterator().next().getId()).build();
        }), actions.stream()).toList();
    }

    public Map<ActionBuilder<?>, NetworkElementIdentifier> getElementIdentifierMap() {
        return elementIdentifierMap;
    }
}
