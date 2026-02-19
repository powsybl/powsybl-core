/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public final class IdentityNamingStrategy implements NamingStrategy {

    @Override
    public String getName() {
        return NamingStrategyFactory.IDENTITY;
    }

    @Override
    public String getIidmId(String type, String id) {
        return id;
    }

    @Override
    public String getIidmName(String type, String name) {
        return name;
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable) {
        return identifiable.getId();
    }

    @Override
    public String getCgmesId(CgmesObjectReference... refs) {
        return CgmesObjectReference.combine(refs);
    }

    @Override
    public String getCgmesId(String identifier) {
        return identifier;
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

    @Override
    public void debug(String baseName, DataSource ds) {
        // do nothing
    }
}
