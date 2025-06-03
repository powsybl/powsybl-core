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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.identifiers.NetworkElementIdentifier.IdentifierType.ID_WITH_WILDCARDS;

/**
 *
 * <p>Identifier that finds a network element that have some unknown characters.</p>
 * <p>The unknown characters should be replaced in the identifier by a wildcard character. There can be a maximum of 5 wildcards in the identifier.</p>
 * <p>By default, the wildcard character is '?'. But you can change it if needed / wanted by using
 * the {@link #IdWithWildcardsNetworkElementIdentifier(String, String, String)} constructor.</p>
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IdWithWildcardsNetworkElementIdentifier implements NetworkElementIdentifier {
    public static final int ALLOWED_WILDCARDS_NUMBER = 5;
    public static final String DEFAULT_WILDCARD_CHARACTER = "?";

    private final String originalIdentifier;
    private String identifierPattern;
    private final String wildcardCharacter;
    private final String contingencyId;

    public IdWithWildcardsNetworkElementIdentifier(String identifier) {
        this(identifier, DEFAULT_WILDCARD_CHARACTER, null);
    }

    public IdWithWildcardsNetworkElementIdentifier(String identifier, String contingencyId) {
        this(identifier, DEFAULT_WILDCARD_CHARACTER, contingencyId);
    }

    public IdWithWildcardsNetworkElementIdentifier(String identifier, String wildcardCharacter, String contingencyId) {
        this.originalIdentifier = Objects.requireNonNull(identifier);
        this.contingencyId = contingencyId;
        if (wildcardCharacter.codePointCount(0, wildcardCharacter.length()) != 1) { // Count code points to accept supplementary UTF-16 characters
            throw new IllegalArgumentException("Wildcard character must be a single character");
        }
        this.wildcardCharacter = wildcardCharacter;
        initialize();
    }

    private void initialize() {
        int separatorNumber = StringUtils.countMatches(originalIdentifier, wildcardCharacter);
        if (separatorNumber > ALLOWED_WILDCARDS_NUMBER) {
            throw new PowsyblException("There can be a maximum of " + ALLOWED_WILDCARDS_NUMBER + " wildcards ('" + wildcardCharacter + "')");
        }
        if (separatorNumber == 0) {
            throw new PowsyblException("There is no wildcard in your identifier, please use IdBasedNetworkElementIdentifier instead");
        }
        // Escape all non wildcard characters
        String[] chunks = originalIdentifier.split(Pattern.quote(wildcardCharacter));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.length; i++) {
            if (i != 0) {
                sb.append('.');
            }
            sb.append(Pattern.quote(chunks[i]));
        }
        if (originalIdentifier.endsWith(wildcardCharacter)) {
            sb.append('.');
        }
        identifierPattern = sb.toString();
    }

    @Override
    public Set<Identifiable> filterIdentifiable(Network network) {
        return network.getIdentifiables()
            .stream()
            .filter(identifiable -> identifiable.getId().matches(identifierPattern))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> getNotFoundElements(Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(identifierPattern);
        return identifiable == null ? Collections.singleton(identifierPattern) : Collections.emptySet();
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
        return originalIdentifier;
    }

    public String getWildcardCharacter() {
        return wildcardCharacter;
    }
}
