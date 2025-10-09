/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class IdentifierNetworkPredicate implements NetworkPredicate {

    private final Set<String> ids = new LinkedHashSet<>();

    public static IdentifierNetworkPredicate of(String... ids) {
        Objects.requireNonNull(ids);
        return new IdentifierNetworkPredicate(Arrays.asList(ids));
    }

    public IdentifierNetworkPredicate(Collection<String> ids) {
        Objects.requireNonNull(ids);

        this.ids.addAll(ids);
    }

    /**
     * Keep this substation if the IDs list contains the ID of this substation or one of its voltage levels.
     * @param substation The substation to test
     * @return true if the IDs list contains the ID of this substation or one of its voltage levels, false otherwise
     */
    @Override
    public boolean test(Substation substation) {
        Objects.requireNonNull(substation);
        if (ids.contains(substation.getId())) {
            return true;
        }

        return substation.getVoltageLevelStream()
                .map(VoltageLevel::getId)
                .anyMatch(ids::contains);
    }

    /**
     * Keep this voltage level if the IDs list contains the ID of this voltage level.
     * @param voltageLevel The voltage level to test
     * @return true if the IDs list contains the ID of this voltage level, false otherwise
     */
    @Override
    public boolean test(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        return ids.contains(voltageLevel.getId()) || voltageLevel.getSubstation().map(sub -> ids.contains(sub.getId())).orElse(false);
    }
}
