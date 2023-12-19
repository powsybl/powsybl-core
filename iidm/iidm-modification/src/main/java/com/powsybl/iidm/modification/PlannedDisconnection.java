/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PlannedDisconnection extends AbstractDisconnection {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedDisconnection.class);

    PlannedDisconnection(String connectableId, boolean openFictitiousSwitches) {
        super(connectableId);

        if (!openFictitiousSwitches) {
            this.openableSwitches = this.openableSwitches.and(SwitchPredicates.IS_NONFICTIONAL);
        }
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        applyModification(network, true, LOG, reporter);
    }
}
