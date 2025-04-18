/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.modification.util.ModificationReports;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.network.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractNetworkModification implements NetworkModification {

    protected static final NetworkModificationImpact DEFAULT_IMPACT = NetworkModificationImpact.HAS_IMPACT_ON_NETWORK;
    protected static final double EPSILON = 1e-10;

    protected NetworkModificationImpact impact;

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
            ReportNode dryRunReportNode = ModificationReports.reportOnDryRunStart(reportNode, network.getNameOrId(), getName());
            try {
                //TODO The following copy performs a JSON export/import. It will be more performant to change it to the BIN format.
                Network dryRunNetwork = NetworkSerDe.copy(network, TreeDataFormat.JSON);
                dryRunNetwork.setName(network.getNameOrId() + "_Dry-run");
                apply(dryRunNetwork, namingStrategy, true, computationManager, dryRunReportNode);
            } catch (PowsyblException powsyblException) {
                ModificationReports.reportOnInconclusiveDryRun(dryRunReportNode, powsyblException.getMessage(), getName(), network.getNameOrId());
                return false;
            }
            ModificationReports.dryRunReportNode(dryRunReportNode, getName(), network.getNameOrId());
        } else {
            apply(network, namingStrategy, throwException, computationManager, reportNode);
        }
        return true;
    }

    /**
     * Returns the name of the network modification. That name corresponds to the type of network modification
     * @return the name of the network modification
     */
    public abstract String getName();

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        return DEFAULT_IMPACT;
    }

    protected static boolean checkVoltageLevel(Identifiable<?> identifiable) {
        VoltageLevel vl;
        if (identifiable instanceof Bus bus) {
            vl = bus.getVoltageLevel();
        } else if (identifiable instanceof BusbarSection bbs) {
            vl = bbs.getTerminal().getVoltageLevel();
        } else {
            return false;
        }
        return vl != null;
    }

    protected boolean areValuesEqual(Double newValue, double currentValue, boolean isRelativeValue) {
        return newValue == null || Math.abs(newValue - (isRelativeValue ? 0 : currentValue)) < EPSILON;
    }

    protected boolean areValuesEqual(Integer newValue, int currentValue, boolean isRelativeValue) {
        return newValue == null || Math.abs(newValue - (isRelativeValue ? 0 : currentValue)) < 1;
    }

    protected boolean isValueOutsideRange(int newValue, int minValue, int maxValue) {
        return newValue < minValue || newValue > maxValue;
    }
}
