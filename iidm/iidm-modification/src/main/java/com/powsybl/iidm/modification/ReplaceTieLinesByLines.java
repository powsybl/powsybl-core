/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.modification.topology.TopologyModificationUtils;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static com.powsybl.iidm.network.util.TieLineUtil.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ReplaceTieLinesByLines extends AbstractSingleNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceTieLinesByLines.class);

    @Override
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                        ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        // Note: local dryRun is not possible (see isLocalDryRunPossible())
        for (TieLine tl : network.getTieLineStream().toList()) {
            DanglingLine dl1 = tl.getDanglingLine1();
            DanglingLine dl2 = tl.getDanglingLine2();
            String dl1Id = dl1.getId();
            String dl2Id = dl2.getId();
            LineAdder adder = network.newLine()
                    .setId(tl.getId())
                    .setName(tl.getOptionalName().orElse(null))
                    .setR(tl.getR())
                    .setX(tl.getX())
                    .setB1(tl.getB1())
                    .setB2(tl.getB2())
                    .setG1(tl.getG1())
                    .setG2(tl.getG2())
                    .setVoltageLevel1(dl1.getTerminal().getVoltageLevel().getId())
                    .setVoltageLevel2(dl2.getTerminal().getVoltageLevel().getId());
            if (dl1.getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                adder.setNode1(dl1.getTerminal().getNodeBreakerView().getNode());
            } else {
                adder.setConnectableBus1(dl1.getTerminal().getBusBreakerView().getConnectableBus().getId())
                        .setBus1(Optional.ofNullable(dl1.getTerminal().getBusBreakerView().getBus())
                                .map(Identifiable::getId)
                                .orElse(null));
            }
            if (dl2.getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                adder.setNode2(dl2.getTerminal().getNodeBreakerView().getNode());
            } else {
                adder.setConnectableBus2(dl2.getTerminal().getBusBreakerView().getConnectableBus().getId())
                        .setBus2(Optional.ofNullable(dl2.getTerminal().getBusBreakerView().getBus())
                                .map(Identifiable::getId)
                                .orElse(null));
            }
            warningAboutExtensions(dl1, dl2, tl, reportNode);
            TopologyModificationUtils.LoadingLimitsBags limits1 = new TopologyModificationUtils.LoadingLimitsBags(dl1::getActivePowerLimits,
                    dl1::getApparentPowerLimits, dl1::getCurrentLimits);
            TopologyModificationUtils.LoadingLimitsBags limits2 = new TopologyModificationUtils.LoadingLimitsBags(dl2::getActivePowerLimits,
                    dl2::getApparentPowerLimits, dl2::getCurrentLimits);
            String pairingKey = tl.getPairingKey();
            double p1 = dl1.getTerminal().getP();
            double q1 = dl1.getTerminal().getQ();
            double p2 = dl2.getTerminal().getP();
            double q2 = dl2.getTerminal().getQ();
            Properties properties = new Properties();
            tl.getPropertyNames().forEach(pn -> properties.put(pn, tl.getProperty(pn)));
            mergeProperties(dl1, dl2, properties, reportNode);
            Map<String, String> aliases = new HashMap<>();
            tl.getAliases().forEach(alias -> aliases.put(alias, tl.getAliasType(alias).orElse("")));
            mergeDifferentAliases(dl1, dl2, aliases, reportNode);
            tl.remove();
            dl1.remove();
            dl2.remove();
            Line line = adder.add();
            properties.forEach((pn, pv) -> line.setProperty((String) pn, (String) pv));
            aliases.forEach((alias, type) -> {
                if (type.isEmpty()) {
                    line.addAlias(alias);
                } else {
                    line.addAlias(alias, type);
                }
            });
            line.getTerminal1().setP(p1).setQ(q1);
            line.getTerminal2().setP(p2).setQ(q2);
            addLoadingLimits(line, limits1, TwoSides.ONE);
            addLoadingLimits(line, limits2, TwoSides.TWO);
            // Add previous dangling lines ID and pairing key
            line.addAlias(dl1Id, "danglingLine1Id");
            line.addAlias(dl2Id, "danglingLine2Id");
            if (pairingKey != null) {
                line.addAlias(pairingKey, "pairingKey");
            }
            LOG.info("Removed tie line {} and associated dangling lines {} and {} with pairing key {}. Created line {}", line.getId(), dl1Id, dl2Id, pairingKey, line.getId());
            removedTieLineAndAssociatedDanglingLines(reportNode, line.getId(), dl1Id, dl2Id, pairingKey);
            createdLineReport(reportNode, line.getId());
        }
    }

    @Override
    public String getName() {
        return "ReplaceTieLinesByLines";
    }

    private static void warningAboutExtensions(DanglingLine dl1, DanglingLine dl2, TieLine tl, ReportNode reportNode) {
        String dl1Id = dl1.getId();
        String dl2Id = dl2.getId();
        if (!dl1.getExtensions().isEmpty()) {
            String extensions = dl1.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(","));
            LOG.warn("Extension [{}] of dangling line {} will be lost", extensions, dl1Id);
            lostDanglingLineExtensions(reportNode, extensions, dl1Id);
        }
        if (!dl2.getExtensions().isEmpty()) {
            String extensions = dl2.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(","));
            LOG.warn("Extension [{}] of dangling line {} will be lost", extensions, dl2Id);
            lostDanglingLineExtensions(reportNode, extensions, dl2Id);
        }
        if (!tl.getExtensions().isEmpty()) {
            String extensions = tl.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(","));
            LOG.warn("Extension [{}] of tie line {} will be lost", extensions, tl.getId());
            lostTieLineExtensions(reportNode, extensions, tl.getId());
        }
    }
}
