/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Identifiable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Identifiables {

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
}
