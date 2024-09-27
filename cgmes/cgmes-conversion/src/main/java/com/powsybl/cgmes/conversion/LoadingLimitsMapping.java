/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class LoadingLimitsMapping {

    private final Context context;
    private final Map<OperationalLimitsGroup, CurrentLimitsAdder> currentLimitsAdders;
    private final Map<OperationalLimitsGroup, ActivePowerLimitsAdder> activePowerLimitsAdders;
    private final Map<OperationalLimitsGroup, ApparentPowerLimitsAdder> apparentPowerLimitsAdders;

    LoadingLimitsMapping(Context context) {
        this.context = Objects.requireNonNull(context);
        this.currentLimitsAdders = new HashMap<>();
        this.activePowerLimitsAdders = new HashMap<>();
        this.apparentPowerLimitsAdders = new HashMap<>();
    }

    /**
     * Get or create the limit adder for the given limits group and limit subclass.
     * Different CGMES OperationalLimit can be mapped to the same IIDM LoadingLimit,
     * hence the need to retrieve the limit adder if it has already been created before.
     * @param limitsGroup The OperationalLimitsGroup to which the LoadingLimitsAdder shall add the limits.
     * @param limitSubClass The operational limit subclass indicating which LoadingLimitsAdder subclass shall be used.
     * @return The LoadingLimitsAdder for the given limits group and limit subclass.
     */
    public LoadingLimitsAdder getLoadingLimitsAdder(OperationalLimitsGroup limitsGroup, String limitSubClass) {
        return switch (limitSubClass) {
            case CgmesNames.CURRENT_LIMIT -> currentLimitsAdders.computeIfAbsent(limitsGroup, s -> limitsGroup.newCurrentLimits());
            case CgmesNames.ACTIVE_POWER_LIMIT -> activePowerLimitsAdders.computeIfAbsent(limitsGroup, s -> limitsGroup.newActivePowerLimits());
            case CgmesNames.APPARENT_POWER_LIMIT -> apparentPowerLimitsAdders.computeIfAbsent(limitsGroup, s -> limitsGroup.newApparentPowerLimits());
            default -> throw new IllegalArgumentException();
        };
    }

    /**
     * Execute the add method for all the LoadingLimitsAdder stored in this mapping class.
     * This method shall be called after all the CGMES OperationalLimit have been converted.
     */
    void addAll() {
        for (Map<OperationalLimitsGroup, ? extends LoadingLimitsAdder> adder : List.of(currentLimitsAdders, activePowerLimitsAdders, apparentPowerLimitsAdders)) {
            for (Map.Entry<OperationalLimitsGroup, ? extends LoadingLimitsAdder> entry : adder.entrySet()) {
                if (!Double.isNaN(entry.getValue().getPermanentLimit()) || entry.getValue().hasTemporaryLimits()) {
                    entry.getValue()
                            .fixLimits(context.config().getMissingPermanentLimitPercentage(), context::fixed)
                            .add();
                }
            }
            adder.clear();
        }
    }
}
