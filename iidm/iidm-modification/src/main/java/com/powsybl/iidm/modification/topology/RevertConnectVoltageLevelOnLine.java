/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.ModificationReports.createdLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.noVoltageLevelInCommonReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.removedLineReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.LoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.attachLine;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createLineAdder;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.mergeLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removeVoltageLevelAndSubstation;

/**
 * This method reverses the action done in the ConnectVoltageLevelOnLine class :
 * it replaces 2 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the voltage level in common (switching voltage level), if it contains no equipments anymore, except bus or bus bar section
 *
 *    VL1 ----------- switching voltage level ----------- VL2         =========>        VL1 ------------------------- VL2
 *          (line1)                             (line2)                                              (line)
 *
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class RevertConnectVoltageLevelOnLine extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(RevertConnectVoltageLevelOnLine.class);

    private String line1Id;
    private String line2Id;

    private String lineId;
    private String lineName;

    /**
     * Constructor.
     *
     * NB: This constructor is package-private, Please use {@link RevertConnectVoltageLevelOnLineBuilder} instead.
     */
    RevertConnectVoltageLevelOnLine(String line1Id, String line2Id, String lineId, String lineName) {
        this.line1Id = Objects.requireNonNull(line1Id);
        this.line2Id = Objects.requireNonNull(line2Id);
        this.lineId = Objects.requireNonNull(lineId);
        this.lineName = lineName;
    }

    public RevertConnectVoltageLevelOnLine setLine1Id(String line1Id) {
        this.line1Id = Objects.requireNonNull(line1Id);
        return this;
    }

    public RevertConnectVoltageLevelOnLine setLine2Id(String line2Id) {
        this.line2Id = Objects.requireNonNull(line2Id);
        return this;
    }

    public RevertConnectVoltageLevelOnLine setLineId(String lineId) {
        this.lineId = Objects.requireNonNull(lineId);
        return this;
    }

    public RevertConnectVoltageLevelOnLine setLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }

    private Line checkAndGetLine(Network network, String lineId, Reporter reporter, boolean throwException) {
        Line line = network.getLine(lineId);
        if (line == null) {
            notFoundLineReport(reporter, lineId);
            LOG.error("Line {} is not found", lineId);
            if (throwException) {
                throw new PowsyblException(String.format("Line %s is not found", lineId));
            }
        }
        return line;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        Line line1 = checkAndGetLine(network, line1Id, reporter, throwException);
        Line line2 = checkAndGetLine(network, line2Id, reporter, throwException);
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
            noVoltageLevelInCommonReport(reporter, line1Id, line2Id);
            LOG.error("Lines {} and {} should have one and only one voltage level in common at their extremities", line1Id, line2Id);
            if (throwException) {
                throw new PowsyblException(String.format("Lines %s and %s should have one and only one voltage level in common at their extremities", line1Id, line2Id));
            } else {
                return;
            }
        }

        VoltageLevel commonVl = network.getVoltageLevel(commonVlId);
        Branch.Side line1Side1 = line1VlId1.equals(commonVlId) ? Branch.Side.TWO : Branch.Side.ONE;
        Branch.Side line1Side2 = line1Side1 == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        Branch.Side line2Side2 = line2VlId1.equals(commonVlId) ? Branch.Side.TWO : Branch.Side.ONE;
        Branch.Side line2Side1 = line2Side2 == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;

        // Set parameters of the new line replacing the two existing lines
        LineAdder lineAdder = createLineAdder(lineId, lineName, line1Side1 == Branch.Side.TWO ? line1VlId2 : line1VlId1,
                line2Side2 == Branch.Side.TWO ? line2VlId2 : line2VlId1, network, line1, line2);

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
        removedLineReport(reporter, line1Id);

        line2.remove();
        removedLineReport(reporter, line2Id);

        // Create the new line
        Line line = lineAdder.add();
        LoadingLimitsBags limitsSide1 = mergeLimits(line1Id, limitsLine1Side1, limitsLine1Side2, reporter);
        LoadingLimitsBags limitsSide2 = mergeLimits(line2Id, limitsLine2Side2, limitsLine2Side1, reporter);

        addLoadingLimits(line, limitsSide1, Branch.Side.ONE);
        addLoadingLimits(line, limitsSide2, Branch.Side.TWO);
        createdLineReport(reporter, lineId);
        LOG.info("New line {} created, replacing lines {} and {}", lineId, line1Id, line2Id);

        // remove voltage level and substation in common, if necessary
        removeVoltageLevelAndSubstation(commonVl, reporter);
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
