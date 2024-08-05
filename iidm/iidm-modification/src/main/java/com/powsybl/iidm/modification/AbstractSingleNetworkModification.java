/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract non-sealed class AbstractSingleNetworkModification extends AbstractNetworkModification {

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                         ReportNode reportNode, boolean dryRun) {
        if (dryRun) {
            ReportNode reportNode1 = reportOnDryRunStart(network, reportNode);
            try {
                doApply(network, namingStrategy, true, computationManager, reportNode1, true);
            } catch (Exception e) {
                reportOnInconclusiveDryRun(reportNode1, e.getMessage());
                return false;
            }
        } else {
            doApply(network, namingStrategy, throwException, computationManager, reportNode, false);
        }
        return true;
    }

    public abstract void doApply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                                 ReportNode reportNode, boolean dryRun);
}
