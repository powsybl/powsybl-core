/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation.mode.getter;

import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.regulation.RegulationMode;

import java.util.Set;

import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageSourceConverterRegulationModeGetter implements RegulationModeGetter {
    @Override
    public boolean supports(Class<?> holderClass) {
        return holderClass == VoltageSourceConverter.class;
    }

    @Override
    public Set<RegulationMode> getAllowedRegulationModes(boolean isRemoteRegulating) {
        return isRemoteRegulating ? Set.of(VOLTAGE, REACTIVE_POWER) : Set.of(VOLTAGE);
    }
}
