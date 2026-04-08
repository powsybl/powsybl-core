/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface VoltageRegulation extends Extension<Battery> {

    String NAME = "voltageRegulation";

    @Override
    default String getName() {
        return NAME;
    }

    boolean isVoltageRegulatorOn();

    void setVoltageRegulatorOn(boolean voltageRegulationOn);

    double getTargetV();

    void setTargetV(double targetV);

    Terminal getRegulatingTerminal();

    void setRegulatingTerminal(Terminal terminal);
}
