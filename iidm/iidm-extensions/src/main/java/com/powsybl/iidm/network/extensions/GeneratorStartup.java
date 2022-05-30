/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface GeneratorStartup extends Extension<Generator> {

    @Override
    default String getName() {
        return "startup";
    }

    float getPredefinedActivePowerSetpoint();

    GeneratorStartup setPredefinedActivePowerSetpoint(float predefinedActivePowerSetpoint);

    float getStartUpCost();

    GeneratorStartup setStartUpCost(float startUpCost);

    float getMarginalCost();

    GeneratorStartup setMarginalCost(float marginalCost);

    float getPlannedOutageRate();

    GeneratorStartup setPlannedOutageRate(float plannedOutageRate);

    float getForcedOutageRate();

    GeneratorStartup setForcedOutageRate(float forcedOutageRate);
}
