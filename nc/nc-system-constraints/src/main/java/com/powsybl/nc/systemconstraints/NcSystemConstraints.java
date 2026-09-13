/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import com.powsybl.nc.model.NcModelExtension;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcSystemConstraints implements NcModelExtension {
    public static final String NAME = "system-constraints";

    private final Set<NcVoltageAngleLimit> voltageAngleLimits;
    private final Set<NcPowerTransferCorridor> powerTransferCorridors;

    NcSystemConstraints(Set<NcVoltageAngleLimit> voltageAngleLimits, Set<NcPowerTransferCorridor> powerTransferCorridors) {
        this.voltageAngleLimits = Collections.unmodifiableSet(new LinkedHashSet<>(voltageAngleLimits));
        this.powerTransferCorridors = Collections.unmodifiableSet(new LinkedHashSet<>(powerTransferCorridors));
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Set<NcVoltageAngleLimit> getVoltageAngleLimits() {
        return voltageAngleLimits;
    }

    public Set<NcPowerTransferCorridor> getPowerTransferCorridors() {
        return powerTransferCorridors;
    }
}
