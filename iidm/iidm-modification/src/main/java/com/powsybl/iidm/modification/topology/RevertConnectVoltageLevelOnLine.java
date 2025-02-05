/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * This method reverses the action done in the ConnectVoltageLevelOnLine class :
 * it replaces 2 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the voltage level in common (switching voltage level), if it contains no equipments anymore, except bus or bus bar section
 * <p>
 * Before modification:
 * <pre>
 *     VL1 ----------- switching voltage level ----------- VL2
 *           (line1)                             (line2)</pre>
 * After modification:
 * <pre>
 *     VL1 ------------------------- VL2
 *                  (line)</pre>
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class RevertConnectVoltageLevelOnLine extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(RevertConnectVoltageLevelOnLine.class);

    private final String line1Id;
    private final String line2Id;

    private final String lineId;
    private final String lineName;

    /**
     * Constructor.
     * <p>
     * NB: This constructor is package-private, Please use {@link RevertConnectVoltageLevelOnLineBuilder} instead.
     */
    RevertConnectVoltageLevelOnLine(String line1Id, String line2Id, String lineId, String lineName) {
        this.line1Id = Objects.requireNonNull(line1Id);
        this.line2Id = Objects.requireNonNull(line2Id);
        this.lineId = Objects.requireNonNull(lineId);
        this.lineName = lineName;
    }

    @Override
    public String getName() {
        return "RevertConnectVoltageLevelOnLine";
    }

    private static Line checkAndGetLine(Network network, String lineId, ReportNode reportNode, boolean throwException) {
        Line line = network.getLine(lineId);
        if (line == null) {
            notFoundLineReport(reportNode, lineId);
            ModificationLogs.logOrThrow(throwException, String.format("Line %s is not found", lineId));
        }

        return line;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Line line1 = checkAndGetLine(network, line1Id, reportNode, throwException);
        Line line2 = checkAndGetLine(network, line2Id, reportNode, throwException);
        if (line1 == null || line2 == null) {
            return;
        }

        // Check and find the voltage level in common
        Set<String> vlIds = new HashSet<>();
        String line1VlId1 = line1.getTerminal1().getVoltageLevel().getId();
        String line1VlId2 = line1.getTerminal2().getVoltageLevel().getId();
        String line2VlId1 = line2.getTerminal1().getVoltageLevel().getId();
        String line2VlId2 = line2.getTerminal2().getVoltageLevel().getId();
        String commonVlId = "";

        vlIds.add(line1VlId1);
        if (!vlIds.add(line1VlId2)) {
            commonVlId = line1VlId2;
        }
        if (!vlIds.add(line2VlId1)) {
            commonVlId = line2VlId1;
        }
        if (!vlIds.add(line2VlId2)) {
            commonVlId = line2VlId2;
        }

        if (vlIds.size() != 3) {
            noVoltageLevelInCommonReport(reportNode, line1Id, line2Id);
            ModificationLogs.logOrThrow(throwException, String.format("Lines %s and %s should have one and only one voltage level in common at their extremities", line1Id, line2Id));
            return;
        }

        VoltageLevel commonVl = network.getVoltageLevel(commonVlId);
        TwoSides line1Side1 = line1VlId1.equals(commonVlId) ? TwoSides.TWO : TwoSides.ONE;
        TwoSides line1Side2 = line1Side1 == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
        TwoSides line2Side2 = line2VlId1.equals(commonVlId) ? TwoSides.TWO : TwoSides.ONE;
        TwoSides line2Side1 = line2Side2 == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;

        // Set parameters of the new line replacing the two existing lines
        LineAdder lineAdder = createLineAdder(lineId, lineName, line1Side1 == TwoSides.TWO ? line1VlId2 : line1VlId1,
                line2Side2 == TwoSides.TWO ? line2VlId2 : line2VlId1, network, line1, line2);

        attachLine(line1.getTerminal(line1Side1), lineAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(line2.getTerminal(line2Side2), lineAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // get line1 limits
        LoadingLimitsBags limitsLine1Side1 = new LoadingLimitsBags(() -> line1.getActivePowerLimits(line1Side1), () -> line1.getApparentPowerLimits(line1Side1), () -> line1.getCurrentLimits(line1Side1));
        LoadingLimitsBags limitsLine1Side2 = new LoadingLimitsBags(() -> line1.getActivePowerLimits(line1Side2), () -> line1.getApparentPowerLimits(line1Side2), () -> line1.getCurrentLimits(line1Side2));

        // get line2 limits
        LoadingLimitsBags limitsLine2Side1 = new LoadingLimitsBags(() -> line2.getActivePowerLimits(line2Side1), () -> line2.getApparentPowerLimits(line2Side1), () -> line2.getCurrentLimits(line2Side1));
        LoadingLimitsBags limitsLine2Side2 = new LoadingLimitsBags(() -> line2.getActivePowerLimits(line2Side2), () -> line2.getApparentPowerLimits(line2Side2), () -> line2.getCurrentLimits(line2Side2));

        // Remove the two existing lines
        line1.remove();
        removedLineReport(reportNode, line1Id);
        LOG.info("Line {} removed", line1Id);

        line2.remove();
        removedLineReport(reportNode, line2Id);
        LOG.info("Line {} removed", line2Id);

        // Create the new line
        Line line = lineAdder.add();
        LoadingLimitsBags limitsSide1 = mergeLimits(line1Id, limitsLine1Side1, limitsLine1Side2, reportNode);
        LoadingLimitsBags limitsSide2 = mergeLimits(line2Id, limitsLine2Side2, limitsLine2Side1, reportNode);

        addLoadingLimits(line, limitsSide1, TwoSides.ONE);
        addLoadingLimits(line, limitsSide2, TwoSides.TWO);
        createdLineReport(reportNode, lineId);
        LOG.info("New line {} created, replacing lines {} and {}", lineId, line1Id, line2Id);

        // remove voltage level and substation in common, if necessary
        removeVoltageLevelAndSubstation(commonVl, reportNode);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Line line1 = network.getLine(line1Id);
        Line line2 = network.getLine(line2Id);
        if (line1 == null || line2 == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else {
            Set<String> vlIds = new HashSet<>();
            vlIds.add(line1.getTerminal1().getVoltageLevel().getId());
            vlIds.add(line1.getTerminal2().getVoltageLevel().getId());
            vlIds.add(line2.getTerminal1().getVoltageLevel().getId());
            vlIds.add(line2.getTerminal2().getVoltageLevel().getId());
            impact = vlIds.size() == 3 ? NetworkModificationImpact.HAS_IMPACT_ON_NETWORK : NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    public String getLine1Id() {
        return line1Id;
    }

    public String getLine2Id() {
        return line2Id;
    }

    public String getLineId() {
        return lineId;
    }

    public String getLineName() {
        return lineName;
    }
}
