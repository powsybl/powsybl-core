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
public class GeneratingUnitUpdate {

    GeneratingUnitUpdate(CgmesModel cgmesModel) {
        this.cgmesModel = cgmesModel;
        this.cachedGeneratingUnits = new HashMap<>();
    }

    void cache() {
        cgmesModel.generatingUnits().forEach(gu -> {
            String generatingUnitId = gu.getId("GeneratingUnit");
            if (generatingUnitId != null) {
                double normalPF = gu.asDouble("normalPF");
                cachedGeneratingUnits.put(generatingUnitId, normalPF);
            }
        });
    }

    public OptionalDouble getNormalPF(String generatingUnitId) {
        return cachedGeneratingUnits.containsKey(generatingUnitId) ? OptionalDouble.of(cachedGeneratingUnits.get(generatingUnitId)) : OptionalDouble.empty();
    }

    private final CgmesModel cgmesModel;
    private final Map<String, Double> cachedGeneratingUnits;
}
