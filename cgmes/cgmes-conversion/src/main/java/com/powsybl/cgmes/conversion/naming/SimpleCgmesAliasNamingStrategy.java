/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
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
        // We assume all identifiers stored in aliases came from original CGMES models
        // and we do not try to fix them
        if (identifiable instanceof DanglingLine dl) {
            return identifiable.getAliasFromType(aliasType).or(() -> dl.getTieLine().flatMap(tl -> tl.getAliasFromType(aliasType))).orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
        }
        return identifiable.getAliasFromType(aliasType)
                .orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
    }

    @Override
    public String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        // We only try to fix subRegionId and regionId identifiers stored in properties
        // Any other identifer stored in a property comes from original CGMES data
        // and is assumed to be correct
        if (propertyName.endsWith("egionId")) {
            return super.getCgmesIdFromProperty(identifiable, propertyName);
        } else {
            return identifiable.getProperty(propertyName);
        }
    }
}
