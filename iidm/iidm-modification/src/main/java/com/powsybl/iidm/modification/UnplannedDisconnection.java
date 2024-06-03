/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.SwitchPredicates;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class UnplannedDisconnection extends AbstractDisconnection {

    UnplannedDisconnection(String connectableId, boolean openFictitiousSwitches, ThreeSides side) {
        super(connectableId, openFictitiousSwitches ?
            SwitchPredicates.IS_OPEN.negate().and(SwitchPredicates.IS_BREAKER) :
            SwitchPredicates.IS_OPEN.negate().and(SwitchPredicates.IS_BREAKER).and(SwitchPredicates.IS_NONFICTIONAL), side);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        applyModification(network, false, reportNode);
    }
}
