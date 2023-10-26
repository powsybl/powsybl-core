/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at rte-france.com>}
 */
public class FixedCgmesAliasNamingStrategy extends AbstractCgmesAliasNamingStrategy {

    public FixedCgmesAliasNamingStrategy() {
        super();
    }

    public FixedCgmesAliasNamingStrategy(Map<String, String> idByUuid) {
        super(idByUuid);
    }

    @Override
    public String getName() {
        return NamingStrategyFactory.CGMES_FIX_ALL_INVALID_IDS;
    }

}
