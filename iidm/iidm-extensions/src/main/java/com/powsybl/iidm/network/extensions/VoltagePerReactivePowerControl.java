/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface VoltagePerReactivePowerControl extends Extension<StaticVarCompensator> {

    String NAME = "voltagePerReactivePowerControl";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the slope of the StaticVarCompensator, that defines how the reactive power output changes
     * in proportion to the difference between the controlled bus target voltage and the controlled bus voltage.
     * The slope is in kV by MVar.
     */
    double getSlope();

    VoltagePerReactivePowerControl setSlope(double slope);
}
