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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.identifiers.NetworkElementIdentifier.IdentifierType.ELEMENT_WITH_UNKNOWN_CHARACTER;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ElementWithUnknownCharacterIdentifier implements NetworkElementIdentifier {
    private String identifier;
    public static final String SEPARATOR = "?";
    public static final int SEPARATOR_NUMBER_ALLOWED = 5;
    private final String contingencyId;

    public ElementWithUnknownCharacterIdentifier(String identifier) {
        this(identifier, null);
    }

    public ElementWithUnknownCharacterIdentifier(String identifier, String contingencyId) {
        this.identifier = Objects.requireNonNull(identifier);
        this.contingencyId = contingencyId;
        initialize();
    }

    private void initialize() {
        String allowedCharactersRegex = "^[A-Za-z0-9_?.-]*$";

        if (!identifier.matches(allowedCharactersRegex)) {
            throw new PowsyblException("Only characters allowed for this identifier are letters, numbers, \'_\', \'?\', \'.\' and \'-\'");
        }
        int separatorNumber = StringUtils.countMatches(identifier, "?");
        if (separatorNumber > SEPARATOR_NUMBER_ALLOWED) {
            throw new PowsyblException("there can be maximum " + SEPARATOR_NUMBER_ALLOWED + " \'?\'");
        }
        identifier = identifier.replace(".", "\\.").replace(SEPARATOR, ".");
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
        return network.getIdentifiables()
            .stream()
            .map(Identifiable::getId)
            .filter(id -> !identifier.matches(id))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public IdentifierType getType() {
        return ELEMENT_WITH_UNKNOWN_CHARACTER;
    }

    @Override
    public Optional<String> getContingencyId() {
        return Optional.ofNullable(contingencyId);
    }

    public String getIdentifier() {
        return identifier;
    }
}
