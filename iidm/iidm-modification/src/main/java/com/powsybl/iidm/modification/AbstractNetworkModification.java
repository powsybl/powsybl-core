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
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract sealed class AbstractNetworkModification implements NetworkModification
        permits AbstractSingleNetworkModification, NetworkModificationList {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkModification.class);

    @Override
    public final boolean apply(Network network) {
        return apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), ReportNode.NO_OP, false);
    }

    @Override
    public final boolean apply(Network network, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), ReportNode.NO_OP, dryRun);
    }

    @Override
    public final boolean apply(Network network, ComputationManager computationManager) {
        return apply(network, new DefaultNamingStrategy(), false, computationManager, ReportNode.NO_OP, false);
    }

    @Override
    public final boolean apply(Network network, ComputationManager computationManager, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, computationManager, ReportNode.NO_OP, dryRun);
    }

    @Override
    public final boolean apply(Network network, ComputationManager computationManager, ReportNode reportNode) {
        return apply(network, new DefaultNamingStrategy(), false, computationManager, reportNode, false);
    }

    @Override
    public final boolean apply(Network network, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, computationManager, reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, ReportNode reportNode) {
        return apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), reportNode, false);
    }

    @Override
    public final boolean apply(Network network, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, boolean throwException, ReportNode reportNode) {
        return apply(network, new DefaultNamingStrategy(), throwException, LocalComputationManager.getDefault(), reportNode, false);
    }

    @Override
    public final boolean apply(Network network, boolean throwException, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), throwException, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        return apply(network, new DefaultNamingStrategy(), throwException, computationManager, reportNode, false);
    }

    @Override
    public final boolean apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        return apply(network, new DefaultNamingStrategy(), throwException, computationManager, reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy) {
        return apply(network, namingStrategy, false, LocalComputationManager.getDefault(), ReportNode.NO_OP, false);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, boolean dryRun) {
        return apply(network, namingStrategy, false, LocalComputationManager.getDefault(), ReportNode.NO_OP, dryRun);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager) {
        return apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP, false);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, boolean dryRun) {
        return apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP, dryRun);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        return apply(network, namingStrategy, false, computationManager, reportNode, false);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        return apply(network, namingStrategy, false, computationManager, reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode) {
        return apply(network, namingStrategy, false, LocalComputationManager.getDefault(), reportNode, false);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode, boolean dryRun) {
        return apply(network, namingStrategy, false, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode) {
        return apply(network, namingStrategy, throwException, LocalComputationManager.getDefault(), reportNode, false);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode, boolean dryRun) {
        return apply(network, namingStrategy, throwException, LocalComputationManager.getDefault(), reportNode, dryRun);
    }

    @Override
    public final boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                         ReportNode reportNode) {
        return apply(network, namingStrategy, throwException, computationManager, reportNode, false);
    }

    public abstract String getName();

    @Override
    public boolean hasImpactOnNetwork() {
        return true;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return false;
    }

    protected ReportNode reportOnDryRunStart(Network network, ReportNode reportNode) {
        String templateKey = "networkModificationDryRun";
        String messageTemplate = "Dry-run: Checking if network modification '${networkModification}' can be applied on network '${networkNameOrId}'";
        return reportNode.newReportNode()
                .withMessageTemplate(templateKey, messageTemplate)
                .withUntypedValue("networkModification", getName())
                .withUntypedValue("networkNameOrId", network.getNameOrId())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    protected static void reportOnInconclusiveDryRun(ReportNode reportNode, String networkModification, String cause) {
        reportNode.newReportNode()
            .withMessageTemplate("networkModificationsDryRun-failure",
                "Dry-run failed for ${networkModification}. The issue is: ${dryRunError}")
            .withUntypedValue("dryRunError", cause)
            .withUntypedValue("networkModification", networkModification)
            .add();
    }

    protected static boolean checkVoltageLevel(Identifiable<?> identifiable, ReportNode reportNode, String networkModification, boolean dryRunConclusive) {
        boolean localDryRunConclusive = dryRunConclusive;
        VoltageLevel vl = null;
        if (identifiable instanceof Bus bus) {
            vl = bus.getVoltageLevel();
        } else if (identifiable instanceof BusbarSection bbs) {
            vl = bbs.getTerminal().getVoltageLevel();
        } else {
            localDryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                networkModification,
                "Unexpected type of identifiable " + identifiable.getId() + ": " + identifiable.getType());
        }
        if (vl == null) {
            localDryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                networkModification,
                "Voltage level is null");
        }
        return localDryRunConclusive;
    }

    protected static boolean checkLine(Network network, String lineId, ReportNode reportNode, String networkModification, boolean dryRunConclusive) {
        boolean localDryRunConclusive = dryRunConclusive;
        Line line = network.getLine(lineId);
        if (line == null) {
            localDryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                networkModification,
                String.format("Line %s is not found", lineId));
        }
        return localDryRunConclusive;
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
}
