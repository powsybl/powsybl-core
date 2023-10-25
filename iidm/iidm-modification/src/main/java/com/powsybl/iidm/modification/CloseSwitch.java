/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class CloseSwitch extends AbstractNetworkModification {
    private final String switchId;

    CloseSwitch(String switchId) {
        this.switchId = Objects.requireNonNull(switchId);
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        Switch sw = network.getSwitch(switchId);
        if (sw == null) {
            throw new PowsyblException("Switch '" + switchId + "' not found");
        }
        sw.setOpen(false);
    }
}
