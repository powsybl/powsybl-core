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
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class RegulatingControlUpdate {

    RegulatingControlUpdate(CgmesModel cgmesModel) {
        this.cgmesModel = cgmesModel;
        this.cachedRegulatingControls = new HashMap<>();
    }

    void cache() {
        cgmesModel.regulatingControls().forEach(rc -> {
            String rcId = rc.getId("RegulatingControl");
            if (rcId != null) {
                Boolean enabled = rc.asBoolean("enabled").orElse(null);
                double targetValue = rc.asDouble("targetValue");
                double targetDeadband = rc.asDouble("targetDeadband");
                cachedRegulatingControls.put(rcId, new RegulatingControl(enabled, targetValue, targetDeadband));
            }
        });
    }

    public Optional<RegulatingControl> getRegulatingControl(String regulatingControlId) {
        return Optional.ofNullable(cachedRegulatingControls.get(regulatingControlId));
    }

    public static final class RegulatingControl {
        final Boolean enabled;
        final double targetValue;
        final double targetDeadband;

        private RegulatingControl(Boolean enabled, double targetValue, double targetDeadband) {
            this.enabled = enabled;
            this.targetValue = targetValue;
            this.targetDeadband = targetDeadband;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public double getTargetValue() {
            return targetValue;
        }

        public double getTargetDeadband() {
            return targetDeadband;
        }
    }

    private final CgmesModel cgmesModel;
    private final Map<String, RegulatingControl> cachedRegulatingControls;

}
