/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class StaticVarCompensatorTripping extends AbstractInjectionTripping {

    public StaticVarCompensatorTripping(String id) {
        super(id);
    }

    @Override
    protected StaticVarCompensator getInjection(Network network) {
        StaticVarCompensator injection = network.getStaticVarCompensator(id);
        if (injection == null) {
            throw new PowsyblException("StaticVarCompensator '" + id + "' not found");
        }

        return injection;
    }

    @Override
    public String getName() {
        return "StaticVarCompensatorTripping";
    }
}
