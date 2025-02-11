/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.identifiers.NetworkElementIdentifier.IdentifierType.ID_WITH_WILDCARDS;

/**
 *
 * <p>Identifier that finds a network element that have some unknown characters.</p>
 * <p>The unknown characters should be replaced in the identifier by the wildcard character '?'.</p>
 * <p>There can be a maximum of 5 wildcards in the identifier and the only special characters allowed for the identifier are '_', '-' and '.'.</p>
 * <p>For example, the identifier "GEN_?" allows to find "GEN_1".</p>
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 *
 */
public class IdWithWildcardsNetworkElementIdentifier implements NetworkElementIdentifier {
    private String identifier;
    public static final char WILDCARD = '?';
    public static final int ALLOWED_WILDCARDS_NUMBER = 5;
    private final String contingencyId;

    public IdWithWildcardsNetworkElementIdentifier(String identifier) {
        this(identifier, null);
    }

    public IdWithWildcardsNetworkElementIdentifier(String identifier, String contingencyId) {
        this.identifier = Objects.requireNonNull(identifier);
        this.contingencyId = contingencyId;
        initialize();
    }

    private void initialize() {
        String allowedCharactersRegex = "^[A-Za-z0-9_? .-]*$";

        if (!identifier.matches(allowedCharactersRegex)) {
            throw new PowsyblException("Only characters allowed for this identifier are letters, numbers, '_', '-', '.' and the wildcard character '?'");
        }
        int separatorNumber = StringUtils.countMatches(identifier, WILDCARD);
        if (separatorNumber > ALLOWED_WILDCARDS_NUMBER) {
            throw new PowsyblException("There can be a maximum of " + ALLOWED_WILDCARDS_NUMBER + " wildcards ('?')");
        }
        if (separatorNumber == 0) {
            throw new PowsyblException("There is no wildcard in your identifier, please use IdBasedNetworkElementIdentifier instead");
        }
        identifier = identifier.replace(".", "\\.").replace(WILDCARD, '.');
    }

    @Override
    public Set<Identifiable> filterIdentifiable(Network network) {
        return network.getIdentifiables()
            .stream()
            .filter(identifiable -> identifiable.getId().matches(identifier))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> getNotFoundElements(Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(identifier);
        return identifiable == null ? Collections.singleton(identifier) : Collections.emptySet();
    }

    @Override
    public IdentifierType getType() {
        return ID_WITH_WILDCARDS;
    }

    @Override
    public Optional<String> getContingencyId() {
        return Optional.ofNullable(contingencyId);
    }

    public String getIdentifier() {
        return identifier;
    }
}
