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

import java.util.Optional;
import java.util.Set;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public interface NetworkElementIdentifier {
    Set<Identifiable> filterIdentifiable(Network network);

    Set<String> getNotFoundElements(Network network);

    enum IdentifierType {
        ID_BASED,
        VOLTAGE_LEVELS_AND_ORDER,
        LIST,
        ID_WITH_WILDCARDS,
        SUBSTATION_OR_VOLTAGE_LEVEL_EQUIPMENTS,
    }

    IdentifierType getType();

    Optional<String> getContingencyId();

    /**
     * Determine if one contingency per element found or a contingency with all the elements found should be created
     * @return {@code true} if one contingency per element found should be created, {@code false} if a contingency with
     * all the elements found should be created
     */
    default boolean isMonoElementContingencies() {
        return false;
    }
}
