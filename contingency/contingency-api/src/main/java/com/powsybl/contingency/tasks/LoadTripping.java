/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

/**
 * @author Hadrien Godard <hadrien.godard at artelys.com>
 */
public class LoadTripping extends AbstractInjectionTripping {

    public LoadTripping(String id) {
        super(id);
    }

    @Override
    protected Injection getInjection(Network network) {
        Injection injection = network.getLoad(id);
        if (injection == null) {
            throw new PowsyblException("Load '" + id + "' not found");
        }

        return injection;
    }
}
