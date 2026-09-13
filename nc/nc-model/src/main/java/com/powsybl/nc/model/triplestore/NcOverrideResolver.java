/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.triplestore;

import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.io.NcOverrideKey;
import com.powsybl.nc.model.io.NcOverridingObjectsFields;
import com.powsybl.nc.model.io.NcOverridingQuery;
import com.powsybl.nc.model.io.NcUtils;
import com.powsybl.triplestore.api.PropertyBag;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class NcOverrideResolver {

    private NcOverrideResolver() {
    }

    static Map<NcOverrideKey, String> resolve(OffsetDateTime timestamp,
                                              NcProfileSelector.Selection selection,
                                              NcQueryExecutor queryExecutor) {
        Map<NcOverrideKey, String> overridingData = new HashMap<>();
        for (NcOverridingQuery query : NcOverridingQuery.values()) {
            Set<String> contexts = selection.contexts(NcKeyword.STEADY_STATE_INSTRUCTION);
            if (contexts.isEmpty()) {
                continue;
            }
            for (PropertyBag propertyBag : queryExecutor.query(query.getRequestName(), contexts)) {
                if (isApplicable(propertyBag, timestamp)) {
                    addValues(overridingData, propertyBag, query);
                }
            }
        }
        return Map.copyOf(overridingData);
    }

    private static void addValues(Map<NcOverrideKey, String> overridingData, PropertyBag propertyBag,
                                  NcOverridingQuery query) {
        for (NcOverridingObjectsFields fields : query.getFields()) {
            String id = propertyBag.getId(fields.getObjectName());
            String value = propertyBag.get(fields.getEffectiveFieldName());
            if (id != null && value != null) {
                overridingData.put(NcOverrideKey.of(id, fields), value);
            }
        }
    }

    private static boolean isApplicable(PropertyBag propertyBag, OffsetDateTime timestamp) {
        return NcUtils.checkProfileKeyword(propertyBag, NcKeyword.STEADY_STATE_INSTRUCTION)
            && NcUtils.checkProfileValidityInterval(propertyBag, timestamp);
    }
}
