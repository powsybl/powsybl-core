/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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

    String getGeographicalTag(String geo);

    String getIidmId(String type, String id);

    String getCgmesId(Identifiable<?> identifiable);

    default String getCgmesId(Identifiable<?> identifiable, String subObject) {
        return identifiable.getId() + "_" + subObject;
    }

    default String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        return identifiable.getAliasFromType(aliasType).orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
    }

    default String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        return identifiable.getProperty(propertyName);
    }

    default String getCgmesId(String identifier) {
        return identifier;
    }

    String getName(String type, String name);

    void readIdMapping(Identifiable<?> identifiable, String type);

    void debugIdMapping(String baseName, DataSource ds);

    // FIXME(Luma) This will end up being a way to obtain a unique id for a given identifiable and a list of (enumerated) subojects
    //  Different naming strategies may choose to:
    //  - return random uuids (not stable, previously were persisted in an external file) or
    //  - stable name-based uids combining identifiable id with prefixes and/or suffixes based on the list of enumerated subobjects
    String getUniqueId(String name);

    final class Identity implements NamingStrategy {

        @Override
        public String getName() {
            return NamingStrategyFactory.IDENTITY;
        }

        @Override
        public String getGeographicalTag(String geo) {
            return geo;
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
        public String getName(String type, String name) {
            return name;
        }

        @Override
        public void readIdMapping(Identifiable<?> identifiable, String type) {
            // do nothing
        }

        @Override
        public void debugIdMapping(String baseName, DataSource ds) {
            // do nothing
        }

        @Override
        public String getUniqueId(String name) {
            return name;
        }
    }
}
