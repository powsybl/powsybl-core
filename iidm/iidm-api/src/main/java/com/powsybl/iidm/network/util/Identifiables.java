/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class Identifiables {

    private static final Logger LOGGER = LoggerFactory.getLogger(Identifiables.class);

    private Identifiables() {
    }

    public static <T extends Identifiable> Collection<T> sort(Iterable<T> identifiables) {
        Map<String, T> sortedIdentifiables = new TreeMap<>();
        for (T identifiable : identifiables) {
            sortedIdentifiables.put(identifiable.getId(), identifiable);
        }
        return sortedIdentifiables.values();
    }

    public static String getNullableId(Identifiable identifiable) {
        return identifiable == null ? null : identifiable.getId();
    }

    public static String getUniqueId(String baseId, Predicate<String> containsId) {
        String checkedBaseId;
        if (baseId != null && baseId.length() > 0) {
            if (!containsId.test(baseId)) {
                return baseId;
            }
            checkedBaseId = baseId;
        } else {
            checkedBaseId = "autoid";
        }
        String uniqueId;
        int i = 0;
        do {
            uniqueId = checkedBaseId + '#' + i++;
        } while (i < Integer.MAX_VALUE && containsId.test(uniqueId));
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Object '{}' is not unique, rename to '{}'", baseId, uniqueId);
        }
        return uniqueId;
    }
}
