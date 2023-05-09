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
import com.powsybl.entsoe.util.MergedXnodeAdder;
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
import static com.powsybl.iidm.network.util.TieLineUtil.mergeIdenticalAliases;
import static com.powsybl.iidm.network.util.TieLineUtil.mergeProperties;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ReplaceTieLinesByLines extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceTieLinesByLines.class);

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        for (TieLine tl : network.getTieLines()) {
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
            if (!dl1.getExtensions().isEmpty()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Extension [{}] of dangling line {} will be lost",
                            dl1.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(",")),
                            dl1Id);
                }
                // TODO add reporter
            }
            if (!dl2.getExtensions().isEmpty()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Extension [{}] of dangling line {} will be lost",
                            dl2.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(",")),
                            dl2Id);
                }
                // TODO add reporter
            }
            if (!tl.getExtensions().isEmpty()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Extension [{}] of tie line {} will be lost",
                            tl.getExtensions().stream().map(Extension::getName).collect(Collectors.joining(",")),
                            tl.getId());
                }
                // TODO add reporter
            }
            TopologyModificationUtils.LoadingLimitsBags limits1 = new TopologyModificationUtils.LoadingLimitsBags(dl1::getActivePowerLimits,
                    dl1::getApparentPowerLimits, dl1::getCurrentLimits);
            TopologyModificationUtils.LoadingLimitsBags limits2 = new TopologyModificationUtils.LoadingLimitsBags(dl2::getActivePowerLimits,
                    dl2::getApparentPowerLimits, dl2::getCurrentLimits);
            String xNode = tl.getUcteXnodeCode();
            double p1 = dl1.getTerminal().getP();
            double xNodeP1 = dl1.getBoundary().getP();
            double q1 = dl1.getTerminal().getQ();
            double xNodeQ1 = dl1.getBoundary().getQ();
            double p2 = dl2.getTerminal().getP();
            double xNodeP2 = dl2.getBoundary().getP();
            double q2 = dl2.getTerminal().getQ();
            double xNodeQ2 = dl2.getBoundary().getQ();
            Properties properties = new Properties();
            tl.getPropertyNames().forEach(pn -> properties.put(pn, tl.getProperty(pn)));
            mergeProperties(dl1, dl2, properties); // TODO add reporter in method (mergeProperties(dl1, dl2, properties, reporter)
            Map<String, String> aliases = new HashMap<>();
            tl.getAliases().forEach(alias -> aliases.put(alias, tl.getAliasType(alias).orElse("")));
            mergeIdenticalAliases(dl1, dl2, aliases); // TODO add reporter in method (mergeIdenticalAliases(dl1, dl2, aliases, reporter)
            tl.remove();
            dl1.remove();
            dl2.remove();
            Line line = adder.add();
            properties.forEach((pn, pv) -> line.setProperty((String) pn, (String) pv));
            aliases.forEach((alias, type) -> {
                if ("".equals(type)) {
                    line.addAlias(alias);
                } else {
                    line.addAlias(alias, type);
                }
            });
            line.getTerminal1().setP(p1).setQ(q1);
            line.getTerminal2().setP(p2).setQ(q2);
            addLoadingLimits(line, limits1, Branch.Side.ONE);
            addLoadingLimits(line, limits2, Branch.Side.TWO);
            line.newExtension(MergedXnodeAdder.class)
                    .withLine1Name(dl1Id)
                    .withLine2Name(dl2Id)
                    .withXnodeP1(xNodeP1)
                    .withXnodeP2(xNodeP2)
                    .withXnodeQ1(xNodeQ1)
                    .withXnodeQ2(xNodeQ2)
                    .withCode(xNode)
                    .add();
            LOG.info("Removed tie line {} and associated dangling lines {} and {} at X-node {}. Created line {}", line.getId(), dl1Id, dl2Id, xNode, line.getId());
            // TODO add reporter
        }
    }
}
