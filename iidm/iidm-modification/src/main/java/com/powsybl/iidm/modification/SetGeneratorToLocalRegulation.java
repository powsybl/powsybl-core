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

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.generatorLocalRegulationReport;

/**
 * <p>Network modification to set a generator regulation to local instead of remote.</p>
 * <ul>
 *     <li>Generator's RegulatingTerminal is set to the generator's own Terminal.</li>
 *     <li>TargetV engineering unit value is adapted but the per unit value remains the same.</li>
 * </ul>
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class SetGeneratorToLocalRegulation extends AbstractNetworkModification {

    private final String generatorId;
    private static final Logger LOG = LoggerFactory.getLogger(SetGeneratorToLocalRegulation.class);

    public SetGeneratorToLocalRegulation(String generatorId) {
        this.generatorId = Objects.requireNonNull(generatorId);
    }

    @Override
    public String getName() {
        return "SetGeneratorToLocalRegulation";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Generator generator = network.getGenerator(generatorId);
        if (generator != null
                && generator.getTerminal() != null
                && generator.getRegulatingTerminal() != null
                && generator.getTerminal() != generator.getRegulatingTerminal()) {
            setLocalRegulation(generator, reportNode);
        }
    }

    /**
     * Change the regulatingTerminal and the targetV of a generator to make it regulate locally.
     * @param generator The Generator that should regulate locally.
     * @param reportNode The ReportNode for functional logs.
     */
    private void setLocalRegulation(Generator generator, ReportNode reportNode) {
        // Calculate the (new) local targetV which should be the same value in per unit as the (old) remote targetV
        double remoteTargetV = generator.getTargetV();
        double remoteNominalV = generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
        double localNominalV = generator.getTerminal().getVoltageLevel().getNominalV();
        double localTargetV = localNominalV * remoteTargetV / remoteNominalV;

        // Change the regulation (local instead of remote)
        generator.setRegulatingTerminal(generator.getTerminal());
        generator.setTargetV(localTargetV);

        // Notify the change
        LOG.info("Changed regulation for generator: {} to local instead of remote", generator.getId());
        generatorLocalRegulationReport(reportNode, generator.getId());
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        Generator generator = network.getGenerator(generatorId);
        if (generator == null || generator.getTerminal() == null || generator.getRegulatingTerminal() == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (generator.getTerminal() == generator.getRegulatingTerminal()) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        } else {
            impact = DEFAULT_IMPACT;
        }
        return impact;
    }
}
