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
 * <p>They are 2 possible modes:</p>
 * <ul>
 *     <li>The legacy mode, where:
 *     <ul>
 *         <li>the wildcard character is '?'</li>
 *         <li>the only special characters allowed for the identifier are ' ', '_', '-' and '.'</li>
 *     </ul>
 *     </li>
 *     <li>An extended mode, where:
 *     <ul>
 *         <li>the wildcard character is '@'</li>
 *         <li>all the printable US ASCII characters (the ones in [0x20 ; 0x7f]) are allowed for the identifier</li>
 *     </ul>
 *     </li>
 * <p>The used mode is determined by the presence of the '@' character. If it is present in the identifier, the extended mode will be used.
 * Else it will be the legacy mode </p>
 * </br>
 * <p>Here are some examples:</p>
 * <ul>
 *     <li>The identifier "GEN_?" allows to find "GEN_1" (legacy mode).</li>
 *     <li>The identifier "GEN_0/?+" fails (legacy mode does not support '/' and '+' characters).</li>
 *     <li>The identifier "GEN_?/@+" allows to find "GEN_?/1+", but not "GEN_0/1+" (extended mode: the '?' is considered as a normal character).</li>
 * </ul>
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IdWithWildcardsNetworkElementIdentifier implements NetworkElementIdentifier {
    public static final char LEGACY_MODE_WILDCARD = '?';
    public static final String LEGACY_MODE_ALLOWED_CHARACTERS = "^[A-Za-z0-9_? .-]*$";
    public static final String LEGACY_MODE_ERROR_MESSAGE = "Only characters allowed for this identifier are letters, numbers, '_', '-', '.', spaces and the wildcard character '?'";

    public static final char EXTENDED_MODE_WILDCARD = '@';
    public static final String EXTENDED_MODE_ALLOWED_CHARACTERS = "^[\\p{Print}|@]*$"; // All printable ASCII chars (in [0x20 ; 0x7e]) + @
    public static final String EXTENDED_MODE_ERROR_MESSAGE = "Only ASCII printable characters + wildcard '@' are allowed for this identifier.";

    private final String originalIdentifier;
    private String identifierPattern;
    public static final int ALLOWED_WILDCARDS_NUMBER = 5;
    private final String contingencyId;

    public IdWithWildcardsNetworkElementIdentifier(String identifier) {
        this(identifier, null);
    }

    public IdWithWildcardsNetworkElementIdentifier(String identifier, String contingencyId) {
        this.originalIdentifier = Objects.requireNonNull(identifier);
        this.contingencyId = contingencyId;
        initialize();
    }

    private void initialize() {
        String allowedCharacters = LEGACY_MODE_ALLOWED_CHARACTERS;
        char wildcard = LEGACY_MODE_WILDCARD;
        String errorMessage = LEGACY_MODE_ERROR_MESSAGE;

        // If the extended-mode wildcard is present, switch to extended mode
        if (originalIdentifier.indexOf(EXTENDED_MODE_WILDCARD) != -1) {
            allowedCharacters = EXTENDED_MODE_ALLOWED_CHARACTERS;
            wildcard = EXTENDED_MODE_WILDCARD;
            errorMessage = EXTENDED_MODE_ERROR_MESSAGE;
        }

        if (!originalIdentifier.matches(allowedCharacters)) {
            throw new PowsyblException(errorMessage);
        }
        int separatorNumber = StringUtils.countMatches(originalIdentifier, wildcard);
        if (separatorNumber > ALLOWED_WILDCARDS_NUMBER) {
            throw new PowsyblException("There can be a maximum of " + ALLOWED_WILDCARDS_NUMBER + " wildcards ('" + wildcard + "')");
        }
        if (separatorNumber == 0) {
            throw new PowsyblException("There is no wildcard in your identifier, please use IdBasedNetworkElementIdentifier instead");
        }
        // Escape all non wildcard sequence characters
        String[] chunks = originalIdentifier.split(Pattern.quote("" + wildcard));
        StringBuilder sb = new StringBuilder();
        for (String chunk : chunks) {
            sb.append(Pattern.quote(chunk)).append('.');
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
}
