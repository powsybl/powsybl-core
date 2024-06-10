/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DanglingLineTripping extends AbstractInjectionTripping {

    public DanglingLineTripping(String id) {
        super(id);
    }

    @Override
    protected DanglingLine getInjection(Network network) {
        DanglingLine injection = network.getDanglingLine(id);
        if (injection == null) {
            throw new PowsyblException("Dangling line '" + id + "' not found");
        }

        return injection;
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        if (network.getDanglingLine(id) == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "DanglingLineTripping",
                "DanglingLine '" + id + "' not found");
        }
        return dryRunConclusive;
    }
}
