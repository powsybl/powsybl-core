/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface VoltageRegulationAdder extends ExtensionAdder<Battery, VoltageRegulation> {

    @Override
    default Class<VoltageRegulation> getExtensionClass() {
        return VoltageRegulation.class;
    }

    VoltageRegulationAdder withVoltageRegulatorOn(boolean voltageRegulatorOn);

    VoltageRegulationAdder withTargetV(double targetV);

    VoltageRegulationAdder withRegulatingTerminal(Terminal terminal);
}
