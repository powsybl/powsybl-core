/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import java.util.UUID;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at rte-france.com>}
 */
public class FixedCgmesAliasNamingStrategy extends AbstractCgmesAliasNamingStrategy {

    public FixedCgmesAliasNamingStrategy(UUID uuidNamespace) {
        super(uuidNamespace);
    }

    @Override
    public String getName() {
        return NamingStrategyFactory.CGMES_FIX_ALL_INVALID_IDS;
    }

}
