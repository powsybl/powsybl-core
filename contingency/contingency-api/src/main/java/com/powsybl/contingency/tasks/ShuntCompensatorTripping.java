/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ShuntCompensatorTripping extends AbstractInjectionTripping {

    public ShuntCompensatorTripping(String id) {
        super(id);
    }

    @Override
    protected Injection getInjection(Network network) {
        Injection injection = network.getShuntCompensator(id);
        if (injection == null) {
            throw new PowsyblException("ShuntCompensator '" + id + "' not found");
        }

        return injection;
    }
}
