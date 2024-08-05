/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
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

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        if (network.getBattery(id) == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "BatteryTripping",
                "Battery '" + id + "' not found");
        }
        return dryRunConclusive;
    }
}
