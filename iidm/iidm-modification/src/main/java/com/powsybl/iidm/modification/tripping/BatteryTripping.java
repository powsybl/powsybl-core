/**
 * Copyright (c) 2022, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BatteryTripping extends AbstractInjectionTripping {

    public BatteryTripping(String id) {
        super(id);
    }

    @Override
    protected Battery getInjection(Network network) {
        Battery injection = network.getBattery(id);
        if (injection == null) {
            throw new PowsyblException("Battery '" + id + "' not found");
        }

        return injection;
    }
}
