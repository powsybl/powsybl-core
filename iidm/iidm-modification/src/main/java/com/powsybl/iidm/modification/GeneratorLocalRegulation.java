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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.iidm.modification.util.ModificationReports.generatorLocalRegulationReport;

/**
 * <p>Network modification to force generator to regulate locally instead of remotely.</p>
 * <p>Generator's RegulatingTerminal is set to the generator's own Terminal.</p>
 * <p>TargetV engineering unit value is adapted but keeps the same per unit value.</p>
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class GeneratorLocalRegulation extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorLocalRegulation.class);

    @Override
    public String getName() {
        return "GeneratorLocalRegulation";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        network.getGeneratorStream().forEach(g -> forceLocalRegulation(g, reportNode));
    }

    private void forceLocalRegulation(Generator g, ReportNode reportNode) {
        if (g.getRegulatingTerminal() != g.getTerminal()) {
            // Calculate the (new) local targetV which should be the same value in per unit as the (old) remote targetV
            double remoteTargetV = g.getTargetV();
            double remoteNominalV = g.getRegulatingTerminal().getVoltageLevel().getNominalV();
            double localNominalV = g.getTerminal().getVoltageLevel().getNominalV();
            double localTargetV = localNominalV * remoteTargetV / remoteNominalV;

            // Change the regulation (local instead of remote)
            g.setRegulatingTerminal(g.getTerminal());
            g.setTargetV(localTargetV);

            // Notify the change
            LOG.info("Changed regulation for generator: {} to local instead of remote", g.getId());
            generatorLocalRegulationReport(reportNode, g.getId());
        }
    }
}
