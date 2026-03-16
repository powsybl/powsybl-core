/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import com.powsybl.iidm.network.Identifiable;

import java.util.UUID;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at rte-france.com>}
 */
public class SimpleCgmesAliasNamingStrategy extends AbstractCgmesAliasNamingStrategy {

    public SimpleCgmesAliasNamingStrategy(UUID uuidNamespace) {
        super(uuidNamespace);
    }

    @Override
    public String getName() {
        return NamingStrategyFactory.CGMES;
    }

    @Override
    public String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        if (identifiable.getAliasFromType(aliasType).isPresent()) {
            return identifiable.getAliasFromType(aliasType).orElseThrow();
        }
        return getCgmesId(getCgmesObjectReferences(identifiable, aliasType));
    }

    @Override
    public String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        if (identifiable.hasProperty(propertyName)) {
            return identifiable.getProperty(propertyName);
        }
        return getCgmesId(getCgmesObjectReferences(identifiable, propertyName));
    }
}
