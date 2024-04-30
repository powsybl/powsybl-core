/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class NamingStrategyFactory {

    public static final String IDENTITY = "identity";
    public static final String CGMES = "cgmes"; // This simple naming strategy will fix only ids for IIDM identifiables
    public static final String CGMES_FIX_ALL_INVALID_IDS = "cgmes-fix-all-invalid-ids";

    public static final List<String> LIST = List.of(IDENTITY, CGMES, CGMES_FIX_ALL_INVALID_IDS);

    public static NamingStrategy create(String impl, UUID uuidNamespace) {
        Objects.requireNonNull(impl);
        return switch (impl) {
            case IDENTITY -> new NamingStrategy.Identity();
            case CGMES -> new SimpleCgmesAliasNamingStrategy(uuidNamespace);
            case CGMES_FIX_ALL_INVALID_IDS -> new FixedCgmesAliasNamingStrategy(uuidNamespace);
            default -> throw new PowsyblException("Unknown naming strategy: " + impl);
        };
    }

    private NamingStrategyFactory() {
    }
}
