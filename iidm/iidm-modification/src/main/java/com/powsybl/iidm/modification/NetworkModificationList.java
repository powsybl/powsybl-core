/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkModificationList extends AbstractNetworkModification {

    private final List<NetworkModification> modificationList;

    public NetworkModificationList(List<NetworkModification> modificationList) {
        this.modificationList = Objects.requireNonNull(modificationList);
    }

    public NetworkModificationList(NetworkModification... modificationList) {
        this(Arrays.asList(modificationList));
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        modificationList.forEach(modification -> modification.apply(network, namingStrategy, throwException, computationManager, reportNode));
    }

    public boolean fullDryRun(Network network) {
        return fullDryRun(network, ReportNode.NO_OP);
    }

    public boolean fullDryRun(Network network, ReportNode reportNode) {
        return fullDryRun(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault(), reportNode);
    }

    public boolean fullDryRun(Network network, NamingStrategy namingStrategy,
                          ComputationManager computationManager, ReportNode reportNode) {
        String templateKey = "networkModificationsDryRun";
        String messageTemplate = "Dry-run: Checking if network modifications can be applied on network ${networkNameOrId}";
        ReportNode dryRunReportNode = reportNode.newReportNode()
                .withMessageTemplate(templateKey, messageTemplate)
                .withUntypedValue("networkNameOrId", network.getNameOrId())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
        try {
            //TODO The following copy performs an XML export/import. It will be more performant to change it to the BIN format.
            Network dryRunNetwork = NetworkSerDe.copy(network);
            dryRunNetwork.setName(network.getNameOrId() + "_Dry-run");
            apply(dryRunNetwork, namingStrategy, true, computationManager, dryRunReportNode);
        } catch (PowsyblException powsyblException) {
            dryRunReportNode.newReportNode()
                    .withMessageTemplate("networkModificationsDryRun-failure",
                            "Dry-run failed. Error message is: ${dryRunError}")
                    .withUntypedValue("dryRunError", powsyblException.getMessage())
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
            return false;
        }
        dryRunReportNode.newReportNode()
                .withMessageTemplate("networkModificationsDryRun-success",
                        "DRY-RUN: Network modifications can successfully be applied on network ${networkNameOrId}")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
        return true;
    }
}
