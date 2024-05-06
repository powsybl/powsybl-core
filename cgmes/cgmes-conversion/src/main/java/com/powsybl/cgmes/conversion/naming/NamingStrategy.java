/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface NamingStrategy {

    String getName();

    String getIidmId(String type, String id);

    String getIidmName(String type, String name);

    String getCgmesId(Identifiable<?> identifiable);

    default String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        return identifiable.getAliasFromType(aliasType).orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
    }

    default String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        return identifiable.getProperty(propertyName);
    }

    default String getCgmesId(String identifier) {
        return identifier;
    }

    void debug(String baseName, DataSource ds);

    String getCgmesId(CgmesObjectReference... refs);

    final class Identity implements NamingStrategy {

        @Override
        public String getName() {
            return NamingStrategyFactory.IDENTITY;
        }

        @Override
        public String getIidmId(String type, String id) {
            return id;
        }

        @Override
        public String getCgmesId(Identifiable<?> identifiable) {
            return identifiable.getId();
        }

        @Override
        public String getIidmName(String type, String name) {
            return name;
        }

        @Override
        public void debug(String baseName, DataSource ds) {
            // do nothing
        }

        @Override
        public String getCgmesId(CgmesObjectReference... refs) {
            return CgmesObjectReference.combine(refs);
        }
    }
}
