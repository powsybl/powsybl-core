/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModel;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class OperationalLimitUpdate {

    OperationalLimitUpdate(CgmesModel cgmesModel) {
        this.cgmesModel = cgmesModel;
        this.cachedOperationalLimits = new HashMap<>();
    }

    void cache() {
        cgmesModel.operationalLimits().forEach(ol -> {
            String operationalLimitId = ol.getId("OperationalLimit");
            if (operationalLimitId != null) {
                double value = ol.asDouble("value");
                cachedOperationalLimits.put(operationalLimitId, value);
            }
        });
    }

    public OptionalDouble getValue(String operationalLimitId) {
        return cachedOperationalLimits.containsKey(operationalLimitId) ? OptionalDouble.of(cachedOperationalLimits.get(operationalLimitId)) : OptionalDouble.empty();
    }

    private final CgmesModel cgmesModel;
    private final Map<String, Double> cachedOperationalLimits;
}
