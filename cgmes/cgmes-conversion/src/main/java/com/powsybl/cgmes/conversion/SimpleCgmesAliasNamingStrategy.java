/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;

import java.util.Map;

/**
 * @author Luma Zamarreño <zamarrenolm at rte-france.com>
 */
public class SimpleCgmesAliasNamingStrategy extends AbstractCgmesAliasNamingStrategy {

    public SimpleCgmesAliasNamingStrategy() {
        super();
    }

    public SimpleCgmesAliasNamingStrategy(Map<String, String> idByUuid) {
        super(idByUuid);
    }

    @Override
    public String getName() {
        return NamingStrategyFactory.CGMES;
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable, String subObject) {
        // ConnectivityNodes built as identifiers plus a suffix _CN must be fixed also in base naming strategy
        boolean mustBeFixed = "CN".equals(subObject);
        if (!mustBeFixed) {
            return identifiable.getId() + "_" + subObject;
        }
        return super.getCgmesId(identifiable, subObject);
    }

    @Override
    public String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        // We assume all identifiers stored in aliases came from original CGMES models
        // and we do not try to fix them
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
