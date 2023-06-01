/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
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
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ReplaceTieLinesByLines extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceTieLinesByLines.class);

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        for (TieLine tl : network.getTieLineStream().collect(Collectors.toList())) {
            BoundaryLine bl1 = tl.getBoundaryLine1();
            BoundaryLine bl2 = tl.getBoundaryLine2();
            String bl1Id = bl1.getId();
            String bl2Id = bl2.getId();
            LineAdder adder = network.newLine()
                    .setId(tl.getId())
                    .setName(tl.getOptionalName().orElse(null))
                    .setR(tl.getR())
                    .setX(tl.getX())
                    .setB1(tl.getB1())
                    .setB2(tl.getB2())
                    .setG1(tl.getG1())
                    .setG2(tl.getG2())
                    .setVoltageLevel1(bl1.getTerminal().getVoltageLevel().getId())
                    .setVoltageLevel2(bl2.getTerminal().getVoltageLevel().getId());
            if (bl1.getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                adder.setNode1(bl1.getTerminal().getNodeBreakerView().getNode());
            } else {
                adder.setConnectableBus1(bl1.getTerminal().getBusBreakerView().getConnectableBus().getId())
                        .setBus1(Optional.ofNullable(bl1.getTerminal().getBusBreakerView().getBus())
                                .map(Identifiable::getId)
                                .orElse(null));
            }
            if (bl2.getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                adder.setNode2(bl2.getTerminal().getNodeBreakerView().getNode());
            } else {
                adder.setConnectableBus2(bl2.getTerminal().getBusBreakerView().getConnectableBus().getId())
                        .setBus2(Optional.ofNullable(bl2.getTerminal().getBusBreakerView().getBus())
                                .map(Identifiable::getId)
                                .orElse(null));
            }
            warningAboutExtensions(bl1, bl2, tl, reporter);
            TopologyModificationUtils.LoadingLimitsBags limits1 = new TopologyModificationUtils.LoadingLimitsBags(bl1::getActivePowerLimits,
                    bl1::getApparentPowerLimits, bl1::getCurrentLimits);
            TopologyModificationUtils.LoadingLimitsBags limits2 = new TopologyModificationUtils.LoadingLimitsBags(bl2::getActivePowerLimits,
                    bl2::getApparentPowerLimits, bl2::getCurrentLimits);
            String xNode = tl.getUcteXnodeCode();
            double p1 = bl1.getTerminal().getP();
            double q1 = bl1.getTerminal().getQ();
            double p2 = bl2.getTerminal().getP();
            double q2 = bl2.getTerminal().getQ();
            Properties properties = new Properties();
            tl.getPropertyNames().forEach(pn -> properties.put(pn, tl.getProperty(pn)));
            mergeProperties(bl1, bl2, properties, reporter);
            Map<String, String> aliases = new HashMap<>();
            tl.getAliases().forEach(alias -> aliases.put(alias, tl.getAliasType(alias).orElse("")));
            mergeDifferentAliases(bl1, bl2, aliases, reporter);
            tl.remove();
            bl1.remove();
            bl2.remove();
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
            addLoadingLimits(line, limits1, Branch.Side.ONE);
            addLoadingLimits(line, limits2, Branch.Side.TWO);
            // Add previous boundary lines ID and Xnode
            line.addAlias(bl1Id, "boundaryLine1Id");
            line.addAlias(bl2Id, "boundaryLine2Id");
            if (xNode != null) {
                line.addAlias(xNode, "xNode");
            }
            LOG.info("Removed tie line {} and associated boundary lines {} and {} at X-node {}. Created line {}", line.getId(), bl1Id, bl2Id, xNode, line.getId());
            removedTieLineAndAssociatedBoundaryLines(reporter, line.getId(), bl1Id, bl2Id, xNode);
            createdLineReport(reporter, line.getId());
        }
    }

    private static void warningAboutExtensions(BoundaryLine bl1, BoundaryLine bl2, TieLine tl, Reporter reporter) {
        String bl1Id = bl1.getId();
        String bl2Id = bl2.getId();
        if (!bl1.getExtensions().isEmpty()) {
            String extensions = bl1.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(","));
            LOG.warn("Extension [{}] of boundary line {} will be lost", extensions, bl1Id);
            lostBoundaryLineExtensions(reporter, extensions, bl1Id);
        }
        if (!bl2.getExtensions().isEmpty()) {
            String extensions = bl2.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(","));
            LOG.warn("Extension [{}] of boundary line {} will be lost", extensions, bl2Id);
            lostBoundaryLineExtensions(reporter, extensions, bl2Id);
        }
        if (!tl.getExtensions().isEmpty()) {
            String extensions = tl.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(","));
            LOG.warn("Extension [{}] of tie line {} will be lost", extensions, tl.getId());
            lostTieLineExtensions(reporter, extensions, tl.getId());
        }
    }
}
