/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractNetworkModification implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkModification.class);

    @Override
    public void apply(Network network) {
        apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), ReportNode.NO_OP);
    }

    @Override
    public boolean apply(Network network, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), ReportNode.NO_OP, dryRun);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network, new DefaultNamingStrategy(), false, computationManager, ReportNode.NO_OP);
    }

    @Override
    public boolean apply(Network network, ComputationManager computationManager, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, computationManager, ReportNode.NO_OP, dryRun);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), false, computationManager, reportNode);
    }

    @Override
    public boolean apply(Network network, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, computationManager, reportNode, dryRun);
    }

    @Override
    public void apply(Network network, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public boolean apply(Network network, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public void apply(Network network, boolean throwException, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), throwException, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public boolean apply(Network network, boolean throwException, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), throwException, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), throwException, computationManager, reportNode);
    }

    @Override
    public boolean apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), throwException, computationManager, reportNode, dryRun);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy) {
        apply(network, namingStrategy, false, LocalComputationManager.getDefault(), ReportNode.NO_OP);
    }

    @Override
    public boolean apply(Network network, NamingStrategy namingStrategy, boolean dryRun) {
        return apply(network, namingStrategy, false, LocalComputationManager.getDefault(), ReportNode.NO_OP, dryRun);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager) {
        apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP);
    }

    @Override
    public boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, boolean dryRun) {
        return apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP, dryRun);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        apply(network, namingStrategy, false, computationManager, reportNode);
    }

    @Override
    public boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        return apply(network, namingStrategy, false, computationManager, reportNode, dryRun);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode) {
        apply(network, namingStrategy, false, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public boolean apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode, boolean dryRun) {
        return apply(network, namingStrategy, false, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode) {
        apply(network, namingStrategy, throwException, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode, boolean dryRun) {
        return apply(network, namingStrategy, throwException, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        if (dryRun) {
            ReportNode dryRunReportNode = reportOnDryRunStart(network, reportNode);
            try {
                //TODO The following copy performs an XML export/import. It will be more performant to change it to the BIN format.
                Network dryRunNetwork = NetworkSerDe.copy(network);
                dryRunNetwork.setName(network.getNameOrId() + "_Dry-run");
                apply(dryRunNetwork, namingStrategy, true, computationManager, dryRunReportNode);
            } catch (PowsyblException powsyblException) {
                reportOnInconclusiveDryRun(dryRunReportNode, powsyblException.getMessage());
                return false;
            }
            dryRunReportNode.newReportNode()
                .withMessageTemplate("networkModificationDryRun-success",
                    "DRY-RUN: Network modifications can successfully be applied on network '${networkNameOrId}'")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
        } else {
            apply(network, namingStrategy, throwException, computationManager, reportNode);
        }
        return true;
    }

    /**
     * Utility during apply functions, logs or throw the message.
     *
     * @param throwException if true will throw {@link com.powsybl.commons.PowsyblException} with the given message
     */
    protected void logOrThrow(boolean throwException, String message) {
        if (throwException) {
            throw new PowsyblException(message);
        } else {
            LOGGER.warn("Error while applying modification : {}", message);
        }
    }

    /**
     * Returns the name of the network modification. That name corresponds to the type of network modification
     * @return the name of the network modification
     */
    public abstract String getName();

    protected ReportNode reportOnDryRunStart(Network network, ReportNode reportNode) {
        String templateKey = "networkModificationDryRun";
        String messageTemplate = "Dry-run: Checking if network modification ${networkModification} can be applied on network '${networkNameOrId}'";
        return reportNode.newReportNode()
            .withMessageTemplate(templateKey, messageTemplate)
            .withUntypedValue("networkModification", getName())
            .withUntypedValue("networkNameOrId", network.getNameOrId())
            .withSeverity(TypedValue.INFO_SEVERITY)
            .add();
    }

    protected void reportOnInconclusiveDryRun(ReportNode reportNode, String cause) {
        reportNode.newReportNode()
            .withMessageTemplate("networkModificationDryRun-failure",
                "Dry-run failed for ${networkModification}. The issue is: ${dryRunError}")
            .withUntypedValue("dryRunError", cause)
            .withUntypedValue("networkModification", getName())
            .add();
    }
}
