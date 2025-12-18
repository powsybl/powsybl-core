/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Identifiable;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface NamingStrategy {

    String getName();

    String getIidmId(String type, String id);

    String getIidmName(String type, String name);

    String getCgmesId(Identifiable<?> identifiable);

    default String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        return identifiable.getAliasFromType(aliasType).orElseGet(() -> getCgmesId(getCgmesObjectReferences(identifiable, aliasType)));
    }

    default String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        return identifiable.getProperty(propertyName);
    }

    default String getCgmesId(String identifier) {
        return identifier;
    }

    void debug(String baseName, DataSource ds);

    String getCgmesId(CgmesObjectReference... refs);

    default CgmesObjectReference[] getCgmesObjectReferences(Identifiable<?> identifiable, String aliasOrProperty) {
        return switch (aliasOrProperty) {
            case ALIAS_PHASE_TAP_CHANGER1 -> new CgmesObjectReference[] {refTyped(identifiable), PHASE_TAP_CHANGER, ref(1)};
            case ALIAS_PHASE_TAP_CHANGER2 -> new CgmesObjectReference[] {refTyped(identifiable), PHASE_TAP_CHANGER, ref(2)};
            case ALIAS_PHASE_TAP_CHANGER3 -> new CgmesObjectReference[] {refTyped(identifiable), PHASE_TAP_CHANGER, ref(3)};
            case ALIAS_RATIO_TAP_CHANGER1 -> new CgmesObjectReference[] {refTyped(identifiable), RATIO_TAP_CHANGER, ref(1)};
            case ALIAS_RATIO_TAP_CHANGER2 -> new CgmesObjectReference[] {refTyped(identifiable), RATIO_TAP_CHANGER, ref(2)};
            case ALIAS_RATIO_TAP_CHANGER3 -> new CgmesObjectReference[] {refTyped(identifiable), RATIO_TAP_CHANGER, ref(3)};
            case ALIAS_TRANSFORMER_END1 -> new CgmesObjectReference[] {ref(identifiable), combo(TRANSFORMER_END, ref(1))};
            case ALIAS_TRANSFORMER_END2 -> new CgmesObjectReference[] {ref(identifiable), combo(TRANSFORMER_END, ref(2))};
            case ALIAS_TRANSFORMER_END3 -> new CgmesObjectReference[] {ref(identifiable), combo(TRANSFORMER_END, ref(3))};
            default -> throw new IllegalStateException("Unexpected value: " + aliasOrProperty);
        };
    }

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
