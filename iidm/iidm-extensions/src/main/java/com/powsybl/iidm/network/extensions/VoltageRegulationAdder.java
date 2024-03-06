/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.ReactiveLimitsHolder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface VoltageRegulationAdder<T extends Injection<T> & ReactiveLimitsHolder> extends ExtensionAdder<T, VoltageRegulation<T>> {

    @Override
    default Class<VoltageRegulation> getExtensionClass() {
        return VoltageRegulation.class;
    }

    VoltageRegulationAdder<T> withVoltageRegulatorOn(boolean voltageRegulatorOn);

    VoltageRegulationAdder<T> withTargetV(double targetV);

    VoltageRegulationAdder<T> withRegulatingTerminal(Terminal terminal);
}
